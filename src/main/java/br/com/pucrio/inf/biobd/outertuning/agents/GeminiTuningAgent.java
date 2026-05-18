package br.com.pucrio.inf.biobd.outertuning.agents;

import br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Heuristic;
import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.ActionSF;
import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.ConnectionSGBD;
import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.Schema;
import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.SQL;
import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.Table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Standalone agent that collects workload data and sends it to the Gemini API
 * for database tuning suggestions. Wired via CoordenatorAgent — does not
 * depend on or modify OuterTuningAgent.
 *
 * Author: Lucas Ferraz Massena
 */
public class GeminiTuningAgent {

    public static final String HEURISTIC_NAME = "GeminiTuning";
    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private final Configuration config;

    public GeminiTuningAgent(Configuration config) {
        this.config = config;
    }

    /** Returns the Heuristic descriptor for this agent (shown in the UI). */
    public static Heuristic getHeuristic() {
        Heuristic h = new Heuristic();
        h.setName(HEURISTIC_NAME);
        h.setVersion("1");
        h.setStrategy("Gemini Tuning Suggestion");
        h.setAuthor("Google/Lucas Ferraz Massena");
        return h;
    }

    /**
     * Analyzes the captured workload and schema with Gemini and returns
     * a list of ActionSF tuning suggestions.
     */
    public List<ActionSF> analyze(CopyOnWriteArrayList<SQL> sqlList, Schema schema) {
        List<ActionSF> actions = new ArrayList<>();

        String apiKey = getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("[GeminiTuning] API key not configured. " +
                "Set GEMINI_API_KEY env var or geminiApiKey in outertuning.properties.");
            return actions;
        }
        if (sqlList.isEmpty()) return actions;

        try {
            String prompt = buildPrompt(sqlList, schema);
            String response = callGemini(prompt, apiKey);
            if (response != null) {
                actions = parseResponse(response, sqlList);
                System.out.println("[GeminiTuning] Received " + actions.size() + " suggestion(s).");
            }
        } catch (Exception e) {
            System.out.println("[GeminiTuning] Error: " + e.getMessage());
        }
        return actions;
    }

    private String getApiKey() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.isEmpty()) {
            key = config.getProperty("geminiApiKey");
        }
        return key;
    }

    private String buildPrompt(CopyOnWriteArrayList<SQL> sqls, Schema schema) {
        StringBuilder sb = new StringBuilder();
        String sgbd = config.getProperty("sgbd");
        boolean isPostgres = "postgresql".equalsIgnoreCase(sgbd);
        sb.append("You are a ").append(isPostgres ? "PostgreSQL" : "MySQL")
          .append(" database tuning expert. Analyze the workload and schema ")
          .append("below and suggest specific tuning actions.\n\n");

        sb.append("=== DATABASE SCHEMA ===\n");
        for (Table t : schema.tables) {
            sb.append("Table ").append(t.getName())
              .append(" (").append(t.getNumberRows()).append(" rows): ")
              .append(t.getFieldsString()).append("\n");
        }

        if (isPostgres) {
            sb.append("\n=== POSTGRESQL INDEX GUIDELINES ===\n");
            sb.append("The cost estimator uses HypoPG (hypothetical indexes).\n");
            sb.append("1. Suggest an index for EVERY SQL in the workload, regardless of table size or\n");
            sb.append("   whether a WHERE clause is present. If an index genuinely cannot help (e.g.\n");
            sb.append("   full-table aggregate on a tiny table), still emit a JSON entry but set\n");
            sb.append("   expectedBonus to 0 and explain why in the justification field.\n");
            sb.append("2. Prefer covering indexes: use the INCLUDE clause to add SELECT-list columns\n");
            sb.append("   so the planner can do an Index-Only Scan and avoid heap fetches.\n");
            sb.append("   Example: CREATE INDEX idx ON orders (o_w_id, o_d_id) INCLUDE (o_carrier_id, o_entry_d)\n");
            sb.append("3. Do NOT suggest a duplicate of an existing index — check the schema carefully.\n");
            sb.append("4. Do NOT end the DDL command with a semicolon.\n\n");
        }

        sb.append("=== CAPTURED SQL WORKLOAD ===\n");
        int limit = Math.min(sqls.size(), 10);
        for (int i = 0; i < limit; i++) {
            SQL sql = sqls.get(i);
            sb.append("\nSQL #").append(sql.getId()).append(":\n");
            sb.append("  Query: ").append(sql.getSql()).append("\n");
            sb.append("  Executions: ").append(sql.getCaptureCount()).append("\n");
            if (sql.getLastPlan() != null) {
                sb.append("  Cost: ").append(sql.getLastPlan().getCost()).append("\n");
                sb.append("  Duration: ").append(sql.getLastPlan().getDuration()).append("s\n");
                if (sql.getLastPlan().getPlan() != null && !sql.getLastPlan().getPlan().isEmpty()) {
                    sb.append("  Execution Plan: ").append(sql.getLastPlan().getPlan()).append("\n");
                }
            }
            if (!sql.getTablesQuery().isEmpty()) {
                sb.append("  Tables used: ");
                for (Table t : sql.getTablesQuery()) sb.append(t.getName()).append(" ");
                sb.append("\n");
            }
        }

        sb.append("\n=== TUNING REQUEST ===\n");
        sb.append("For each SQL above suggest ONE index tuning action following the guidelines. ");
        if (isPostgres) {
            sb.append("Apply the PostgreSQL Index Guidelines above. ");
        }
        sb.append("Only suggest CREATE INDEX commands — do NOT suggest materialized views.\n");
        sb.append("Respond ONLY with a JSON array. Each element must have:\n");
        sb.append("  sqlId (int), actionType (\"INDEX\"),\n");
        sb.append("  command (SQL DDL string), justification (string), expectedBonus (float 0-100)\n");
        sb.append("Example: [{\"sqlId\":1,\"actionType\":\"INDEX\",");
        sb.append("\"command\":\"CREATE INDEX idx ON orders(customer_id)\",");
        sb.append("\"justification\":\"Avoids full scan\",\"expectedBonus\":40.0}]\n");
        sb.append("Respond with the JSON array only. No markdown, no extra text.");

        return sb.toString();
    }

    private String callGemini(String prompt, String apiKey) throws Exception {
        TrustManager[] trustAll = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            public void checkClientTrusted(X509Certificate[] c, String a) {}
            public void checkServerTrusted(X509Certificate[] c, String a) {}
        }};
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAll, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((h, s) -> true);

        URL url = new URL(GEMINI_URL + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);

        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        JsonArray parts = new JsonArray();
        parts.add(part);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        JsonObject body = new JsonObject();
        body.add("contents", contents);

        byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(input);
        }

        int code = conn.getResponseCode();
        InputStream stream = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) result.append(line);
        }

        if (code != 200) {
            System.out.println("[GeminiTuning] API error " + code + ": " + result);
            return null;
        }
        return result.toString();
    }

    private List<ActionSF> parseResponse(String response, CopyOnWriteArrayList<SQL> sqls) {
        List<ActionSF> actions = new ArrayList<>();
        try {
            JsonObject resp = new JsonParser().parse(response).getAsJsonObject();
            String text = resp.getAsJsonArray("candidates").get(0)
                .getAsJsonObject().getAsJsonObject("content")
                .getAsJsonArray("parts").get(0)
                .getAsJsonObject().get("text").getAsString().trim();

            if (text.contains("[")) {
                text = text.substring(text.indexOf("["));
                int end = text.lastIndexOf("]");
                if (end >= 0) text = text.substring(0, end + 1);
            }

            JsonArray suggestions = new JsonParser().parse(text).getAsJsonArray();
            for (int i = 0; i < suggestions.size(); i++) {
                JsonObject s = suggestions.get(i).getAsJsonObject();
                int sqlId       = s.has("sqlId")         ? s.get("sqlId").getAsInt()           : 0;
                String type     = s.has("actionType")    ? s.get("actionType").getAsString()    : "INDEX";
                String cmd      = s.has("command")       ? s.get("command").getAsString()       : "";
                String just     = s.has("justification") ? s.get("justification").getAsString() : "";
                float bonus     = s.has("expectedBonus") ? s.get("expectedBonus").getAsFloat()  : 0f;

                if ("MATERIALIZED_VIEW".equals(type)) {
                    System.out.println("[GeminiTuning] Skipping MATERIALIZED_VIEW suggestion for SQL#" + sqlId);
                    continue;
                }

                ActionSF action = new ActionSF();
                action.setId("GEMINI_" + sqlId + "_" + i);
                action.setName("GeminiSuggestion_SQL" + sqlId + "_" + type + "_" + i);
                action.setType("Index");
                action.setCommand(cmd);
                action.setJustify(just);
                action.setHeuristic(HEURISTIC_NAME);
                action.setCreationCost(0f);
                action.setStatus("candidate");

                SQL linkedSql = null;
                for (SQL sql : sqls) {
                    if (sql.getId() == sqlId) {
                        linkedSql = sql;
                        action.addSql(sql);
                        sql.addAction(action);
                        break;
                    }
                }

                if (linkedSql != null && !cmd.isEmpty()
                        && cmd.trim().toUpperCase().startsWith("CREATE INDEX")) {
                    // getCost() returns long (truncates decimals). Parse the float cost
                // directly from the plan text to preserve precision for low-cost queries.
                float originalCost = 0f;
                if (linkedSql.getLastPlan() != null) {
                    String planText = linkedSql.getLastPlan().getPlan();
                    if (planText != null && !planText.isEmpty()) {
                        int dotDot = planText.indexOf("..");
                        if (dotDot >= 0) {
                            try {
                                int end = planText.indexOf(" ", dotDot + 2);
                                originalCost = end > 0
                                        ? Float.parseFloat(planText.substring(dotDot + 2, end))
                                        : (float) linkedSql.getLastPlan().getCost();
                            } catch (NumberFormatException ignored) {
                                originalCost = (float) linkedSql.getLastPlan().getCost();
                            }
                        }
                    }
                }
                    action.setOriginalCost(originalCost);
                    float[] estimate = estimateCostWithIndex(cmd, linkedSql.getSql());
                    float costWithIndex  = estimate[0];
                    float creationCost   = estimate[1];
                    if (costWithIndex >= 0) {
                        float execucoes = linkedSql.getCaptureCount();
                        float bonusAcumulado = (originalCost - costWithIndex) * execucoes;
                        action.setCost(costWithIndex);
                        action.setCreationCost(creationCost);
                        action.setBonus(bonusAcumulado);
                        System.out.println("[GeminiTuning] SQL#" + sqlId
                                + " custo original=" + originalCost
                                + " custo com indice=" + costWithIndex
                                + " execucoes=" + (int) execucoes
                                + " criacao=" + creationCost + "ms"
                                + " bonus acumulado=" + bonusAcumulado);
                    } else {
                        action.setCost(0f);
                        action.setCreationCost(0f);
                        action.setBonus(bonus);
                    }
                } else {
                    action.setCost(0f);
                    action.setCreationCost(0f);
                    action.setBonus(bonus);
                }

                actions.add(action);
            }
        } catch (Exception e) {
            System.out.println("[GeminiTuning] Parse error: " + e.getMessage());
        }
        return actions;
    }

    /**
     * Returns float[]{queryCost, creationCostMs}, or {-1, -1} on failure.
     * Routes to MySQL (real index) or PostgreSQL (HypoPG) based on config.
     */
    private float[] estimateCostWithIndex(String indexCommand, String querySql) {
        String sgbd = config.getProperty("sgbd");
        if ("postgresql".equalsIgnoreCase(sgbd)) {
            return estimateCostWithHypoPG(indexCommand, querySql);
        }
        return estimateCostWithMysqlIndex(indexCommand, querySql);
    }

    /**
     * MySQL strategy: creates a real index, runs EXPLAIN FORMAT=JSON, then drops it.
     * CreationCostMs reflects the actual time to build the index.
     */
    private float[] estimateCostWithMysqlIndex(String indexCommand, String querySql) {
        String indexName = extractIndexName(indexCommand);
        String tableName = extractTableName(indexCommand);
        if (indexName == null || tableName == null) return new float[]{-1f, -1f};

        ConnectionSGBD conn = new ConnectionSGBD();
        try {
            Statement stmt = conn.getStatement();

            try { stmt.execute("DROP INDEX " + indexName + " ON " + tableName); }
            catch (Exception ignored) {}

            long t0 = System.currentTimeMillis();
            stmt.execute(indexCommand);
            float creationCostMs = System.currentTimeMillis() - t0;

            float queryCost = -1f;
            try {
                ResultSet rs = stmt.executeQuery("EXPLAIN FORMAT=JSON " + querySql);
                if (rs != null && rs.next()) {
                    JsonObject explain = new JsonParser().parse(rs.getString(1)).getAsJsonObject();
                    String costStr = explain.getAsJsonObject("query_block")
                            .getAsJsonObject("cost_info")
                            .get("query_cost").getAsString();
                    queryCost = Float.parseFloat(costStr);
                    rs.close();
                }
            } finally {
                try { stmt.execute("DROP INDEX " + indexName + " ON " + tableName); }
                catch (Exception ignored) {}
            }
            return new float[]{queryCost, creationCostMs};
        } catch (Exception e) {
            System.out.println("[GeminiTuning] estimateCostWithMysqlIndex error: " + e.getMessage());
            return new float[]{-1f, -1f};
        }
    }

    /**
     * PostgreSQL strategy: uses the HypoPG extension to create a virtual (hypothetical)
     * index without touching the real schema. The PostgreSQL optimizer then uses it
     * when answering EXPLAIN, giving an accurate cost estimate instantly — no wait for
     * a real index build, no permanent schema change, no index accumulation bug.
     *
     * Flow: hypopg_create_index(cmd) → EXPLAIN (FORMAT JSON) → hypopg_reset()
     * Creation cost is estimated from pg_class/pg_stats statistics considering column types.
     */
    private float[] estimateCostWithHypoPG(String indexCommand, String querySql) {
        Connection dedicated = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url  = config.getProperty("urlSGBD") + config.getProperty("databaseName");
            String user = config.getProperty("userSGBD");
            String pass = config.getProperty("pwdSGBD");
            dedicated = DriverManager.getConnection(url, user, pass);

            try (Statement stmt = dedicated.createStatement()) {
                try { stmt.execute("SELECT hypopg_reset()"); } catch (Exception ignored) {}

                String safeCmd = indexCommand.replace("'", "''");
                stmt.execute("SELECT * FROM hypopg_create_index('" + safeCmd + "')");

                float queryCost = -1f;
                try (ResultSet rs = stmt.executeQuery("EXPLAIN (FORMAT JSON) " + querySql)) {
                    if (rs != null && rs.next()) {
                        JsonArray arr = new JsonParser().parse(rs.getString(1)).getAsJsonArray();
                        queryCost = arr.get(0).getAsJsonObject()
                                .getAsJsonObject("Plan")
                                .get("Total Cost").getAsFloat();
                    }
                } finally {
                    try { stmt.execute("SELECT hypopg_reset()"); } catch (Exception ignored) {}
                }
                String tableName = extractTableName(indexCommand);
                List<String> columns = extractColumnNames(indexCommand);
                List<String> includeColumns = extractIncludeColumns(indexCommand);
                float creationCost = (tableName != null && !columns.isEmpty())
                        ? estimateIndexCreationCost(stmt, tableName, columns, includeColumns)
                        : 0f;
                return new float[]{queryCost, creationCost};
            }
        } catch (Exception e) {
            System.out.println("[GeminiTuning] estimateCostWithHypoPG error: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            return new float[]{-1f, -1f};
        } finally {
            if (dedicated != null) {
                try { dedicated.close(); } catch (Exception ignored) {}
            }
        }
    }

    private float estimateIndexCreationCost(Statement stmt, String tableName,
            List<String> keyColumns, List<String> includeColumns) {
        String simple = tableName.contains(".")
                ? tableName.substring(tableName.lastIndexOf('.') + 1) : tableName;
        simple = simple.replaceAll("[\"'`]", "");

        List<String> allColumns = new ArrayList<>(keyColumns);
        allColumns.addAll(includeColumns);

        StringBuilder colList = new StringBuilder();
        for (int i = 0; i < allColumns.size(); i++) {
            if (i > 0) colList.append(",");
            colList.append("'").append(allColumns.get(i).replaceAll("[\"'`]", "").toLowerCase()).append("'");
        }

        String sql =
            "SELECT c.reltuples, c.relpages, a.attname, t.typname, " +
            "COALESCE(s.avg_width, CASE WHEN t.typlen > 0 THEN t.typlen ELSE 16 END) AS avg_width " +
            "FROM pg_class c " +
            "JOIN pg_attribute a ON a.attrelid = c.oid AND a.attnum > 0 AND NOT a.attisdropped " +
            "JOIN pg_type t ON t.oid = a.atttypid " +
            "LEFT JOIN pg_stats s ON s.tablename = c.relname AND s.attname = a.attname " +
            "WHERE LOWER(c.relname) = LOWER('" + simple + "') AND c.relkind = 'r' " +
            "AND LOWER(a.attname) IN (" + colList + ")";

        double reltuples = 0, relpages = 0, totalAvgWidth = 0, totalCpuCmp = 0;
        int found = 0;
        try (ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reltuples = rs.getDouble("reltuples");
                relpages  = rs.getDouble("relpages");
                double w  = rs.getDouble("avg_width");
                String attname = rs.getString("attname").toLowerCase();
                totalAvgWidth += w;
                if (keyColumns.contains(attname)) {
                    totalCpuCmp += cpuComparisonCost(rs.getString("typname").toLowerCase(), w);
                }
                found++;
            }
        } catch (Exception e) {
            System.out.println("[GeminiTuning] estimateIndexCreationCost error: " + e.getMessage());
            return 0f;
        }

        System.out.println("[GeminiTuning] creationCost stats: table=" + simple
                + " found=" + found + " reltuples=" + reltuples + " relpages=" + relpages);
        if (found == 0) return 0f;

        // reltuples = -1 when ANALYZE was never run; try pg_stat_user_tables first
        // (updated by autovacuum without full ANALYZE), then fall back to COUNT(*)
        if (reltuples < 1) {
            try (ResultSet stat = stmt.executeQuery(
                    "SELECT n_live_tup FROM pg_stat_user_tables WHERE LOWER(relname) = LOWER('" + simple + "')")) {
                if (stat.next()) {
                    long live = stat.getLong(1);
                    if (live > 0) reltuples = live;
                }
            } catch (Exception ignored) {}
            System.out.println("[GeminiTuning] reltuples from pg_stat_user_tables: " + reltuples);
        }
        if (reltuples < 1) {
            try (ResultSet cnt = stmt.executeQuery("SELECT COUNT(*) FROM \"" + simple + "\"")) {
                if (cnt.next()) reltuples = cnt.getLong(1);
            } catch (Exception e) {
                System.out.println("[GeminiTuning] COUNT fallback failed for " + simple + ": " + e.getMessage());
            }
            System.out.println("[GeminiTuning] reltuples from COUNT(*): " + reltuples);
        }
        if (reltuples < 1) return 0f;

        double indexEntryBytes = totalAvgWidth + 8.0;
        double indexPages      = Math.ceil(reltuples * indexEntryBytes / (8192.0 * 0.9));
        double ioCost          = (relpages + indexPages) * 1.0;
        double sortCost        = reltuples * (Math.log(Math.max(reltuples, 2)) / Math.log(2)) * totalCpuCmp;
        return (float)(ioCost + sortCost);
    }

    private List<String> extractIncludeColumns(String cmd) {
        List<String> cols = new ArrayList<>();
        int includeIdx = cmd.toUpperCase().indexOf(" INCLUDE ");
        if (includeIdx < 0) return cols;
        int open  = cmd.indexOf('(', includeIdx);
        int close = cmd.indexOf(')', open);
        if (open < 0 || close <= open) return cols;
        for (String part : cmd.substring(open + 1, close).split(",")) {
            String col = part.trim().replaceAll("[\"'`]", "").split("\\s+")[0];
            if (!col.isEmpty()) cols.add(col.toLowerCase());
        }
        return cols;
    }

    private double cpuComparisonCost(String typname, double avgWidth) {
        switch (typname) {
            case "bool":
            case "int2": case "int4": case "int8": case "oid":
            case "float4": case "float8":
            case "date": case "time": case "timetz":
            case "timestamp": case "timestamptz": case "interval":
                return 0.0025;
            case "uuid":
                return 0.0025 * 4;
            case "numeric":
                return 0.0025 * 6;
            default:
                return 0.0025 * Math.max(1.0, avgWidth / 4.0);
        }
    }

    private List<String> extractColumnNames(String cmd) {
        List<String> cols = new ArrayList<>();
        int includeIdx = cmd.toUpperCase().indexOf(" INCLUDE ");
        String keyPart = includeIdx >= 0 ? cmd.substring(0, includeIdx) : cmd;
        int open  = keyPart.indexOf('(');
        int close = keyPart.lastIndexOf(')');
        if (open < 0 || close <= open) return cols;
        for (String part : keyPart.substring(open + 1, close).split(",")) {
            String col = part.trim().replaceAll("[\"'`]", "").split("\\s+")[0];
            if (!col.isEmpty()) cols.add(col.toLowerCase());
        }
        return cols;
    }

    private String extractIndexName(String cmd) {
        String[] parts = cmd.trim().split("\\s+");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equalsIgnoreCase("INDEX")) return parts[i + 1];
        }
        return null;
    }

    private String extractTableName(String cmd) {
        int onIdx = cmd.toUpperCase().indexOf(" ON ");
        if (onIdx < 0) return null;
        String rest = cmd.substring(onIdx + 4).trim();
        int paren = rest.indexOf("(");
        return paren >= 0 ? rest.substring(0, paren).trim() : null;
    }
}

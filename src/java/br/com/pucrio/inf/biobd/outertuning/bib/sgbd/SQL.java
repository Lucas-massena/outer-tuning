/*
 * Framework para apoiar a sintonia fina de banco de dados. PUC-RIO.
 * Ana Carolina Almeida [anacrl@gmail.com].
 * Rafael Pereira de Oliveira [rpoliveira@inf.puc-rio.br].
 * PUC-RIO - LABBIO - 2014.
 */
package br.com.pucrio.inf.biobd.outertuning.bib.sgbd;

import br.com.pucrio.inf.biobd.outertuning.bib.base.Interval;
import br.com.pucrio.inf.biobd.outertuning.bib.base.Log;
import br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Rafael
 */
public class SQL {

    private int id;
    private String pid;
    private long analyzeCount;
    private boolean waitAnalysis;
    private Integer relevance;
    private String sql;
    private String database;
    public final Configuration config;
    public final Log log;

    private final ArrayList<Table> tablesQuery;
    private ArrayList<Column> fieldsQuery;
    private Schema schemaDataBase;

    private ArrayList<Plan> executions;
    private ArrayList<ActionSF> actionsSF;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public long getAnalyzeCount() {
        return analyzeCount;
    }

    public void setAnalyzeCount(long analyzeCount) {
        this.analyzeCount = analyzeCount;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isWaitAnalysis() {
        return waitAnalysis;
    }

    public void setWaitAnalysis(boolean waitAnalysis) {
        this.waitAnalysis = waitAnalysis;
    }

    public ArrayList<Plan> getExecutionsInWindow(Interval interval) {
        if (interval == null) {
            return this.executions;
        }
        ArrayList<Plan> executionsInWindow = new ArrayList<>();
        for (Plan execution : this.executions) {
            if (interval.isBetween(execution.getDateExecution()) && execution.getDuration() > 0) {
                if (!executionsInWindow.contains(execution)) {
                    executionsInWindow.add(execution);
                }
            }
        }
        return executionsInWindow;
    }

    public long getCaptureCount() {
        return this.executions.size();
    }

    public SQL() {
        this.config = new Configuration();
        this.log = new Log(this.config);
        this.tablesQuery = new ArrayList<>();
        this.executions = new ArrayList<>();
        this.actionsSF = new ArrayList<>();
        this.fieldsQuery = new ArrayList<>();
        this.setWaitAnalysis(true);
    }

    public Schema getSchemaDataBase() {
        return schemaDataBase;
    }

    public void setSchemaDataBase(Schema schemaDataBase) {
        this.schemaDataBase = schemaDataBase;
        this.setTablesQuery();
    }

    public ArrayList<Column> getFieldsQuery() {
        return fieldsQuery;
    }

    public void setFieldsQuery(ArrayList<Column> fieldsQuery) {
        this.fieldsQuery = fieldsQuery;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Table> getTablesQuery() {
        return tablesQuery;
    }

    public void setTablesQuery() {
        for (Table table : this.schemaDataBase.tables) {
            if (this.getSql() != null && !this.tablesQuery.contains(table) && this.containsFieldOrTable(this.getSql(), table.getName())) {
                table = this.setFieldsQuery(table);
                this.tablesQuery.add(table);
            }
        }
    }

    public Table setFieldsQuery(Table table) {
        ArrayList<Column> fields = table.getFields();
        for (Column field : fields) {
            if (this.containsFieldOrTable(this.getSql(), field.getName())) {
                this.fieldsQuery.add(field);
            }
        }
        return table;
    }

    public void setSql(String sql) {
        this.sql = this.removerNl(sql.trim());
    }

    public Integer getRelevance() {
        return relevance;
    }

    public void setRelevance(Integer relevance) {
        this.relevance = relevance;
    }

    public String getType() {
        if (this.getSql().toLowerCase().contains("select")) {
            return "Q";
        } else if (this.getSql().toLowerCase().contains("update")) {
            return "U";
        } else if (this.getSql().toLowerCase().contains("insert")) {
            return "I";
        } else if (this.getSql().toLowerCase().contains("delete")) {
            return "D";
        }
        return "";
    }

    public boolean hasTableInTableQuery(ArrayList<Table> tables) {
        for (Table tableCheck : tables) {
            if (this.tablesQuery.contains(tableCheck)) {
                return true;
            }
        }
        return false;
    }

    public String getComents() {
        if (this.getSql() != null) {
            int ini = this.getSql().indexOf("/*");
            if (ini >= 0) {
                int end = this.getSql().substring(ini).indexOf("*/") + ini + 2;
                return this.getSql().substring(ini, end).trim() + " ";
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public String getClauseFromSql(String clause) {
        if (this.existClause(clause)) {
            int ini;
            if (clause.toLowerCase().contains("select") || clause.toLowerCase().contains("from") || clause.toLowerCase().contains("where")) {
                ini = this.getSql().toLowerCase().indexOf(clause) + clause.length();
            } else {
                ini = this.getSql().toLowerCase().lastIndexOf(clause) + clause.length();
            }
            int end = this.getEndClause(ini);
            String clauseComplete;
            if (end > 0) {
                clauseComplete = this.getSql().toLowerCase().substring(ini, end).trim();
            } else {
                clauseComplete = this.getSql().toLowerCase().substring(ini).trim();
            }
            return " " + clause + " " + clauseComplete;
        } else {
            return "";
        }
    }

    private int getEndClause(int ini) {
        int current = this.getSql().length();
        current = this.getSmaller(current, ini, this.getSql().toLowerCase().substring(ini).indexOf(" from "));
        current = this.getSmaller(current, ini, this.getSql().toLowerCase().substring(ini).indexOf(" where "));
        current = this.getSmaller(current, ini, this.getSql().toLowerCase().substring(ini).indexOf(" group by "));
        current = this.getSmaller(current, ini, this.getSql().toLowerCase().substring(ini).indexOf(" having "));
        current = this.getSmaller(current, ini, this.getSql().toLowerCase().substring(ini).indexOf(" order by "));
        current = this.getSmaller(current, ini, this.getSql().toLowerCase().substring(ini).indexOf(" limit "));
        if (current > ini) {
            return current;
        } else {
            return 0;
        }
    }

    private int getSmaller(int current, int ini, int end) {
        if (end < 0) {
            return current;
        }
        if ((ini + end) < current) {
            return (ini + end);
        } else {
            return current;
        }
    }

    public boolean existClause(String clause) {
        return this.getSql().toLowerCase().contains(clause);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.sql);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SQL other = (SQL) obj;
        if (!Objects.equals(this.sql.toLowerCase(), other.sql.toLowerCase())) {
            return false;
        }
        if (!Objects.equals(this.pid, other.pid)) {
            return false;
        }
        return true;
    }

    public void addExecution(Plan plan) {
        this.executions.add(plan);
    }

    public String getSql() {
        return sql;
    }

    public String getSqlHTML() {
        return commandToHtml(this.getSql());
    }

    public static String commandToHtml(String command) {
        String sqlhtml = command;
        for (String nameClause : getNameClauses()) {
            sqlhtml = sqlhtml.replace(nameClause.toLowerCase(), nameClause);
            sqlhtml = sqlhtml.replace(nameClause, "<br>" + nameClause);
        }
        return sqlhtml;
    }

    protected String removerNl(String frase) {
        String padrao = "\\s{2,}";
        Pattern regPat = Pattern.compile(padrao);
        Matcher matcher = regPat.matcher(frase);
        String res = matcher.replaceAll(" ").trim();
        res = res.replaceAll("(\n|\r)+", " ");
        return res.trim();
    }

    public boolean isAlreadyCaptured() {
        return this.getCaptureCount() > 1;
    }

    public static ArrayList<String> getNameClauses() {
        ArrayList<String> result = new ArrayList<>();
        result.add("select");
        result.add("from");
        result.add("where");
        result.add("group by");
        result.add("order by");
        result.add("having");
        result.add("limit");
        result.add("delete");
        result.add("update");
        result.add("set");
        result.add("insert");
        return result;
    }

    public Column getFielFromQuery(String fieldName) {
        for (Column column : this.fieldsQuery) {
            if (column.getName().toLowerCase().equals(fieldName.toLowerCase())) {
                return column;
            }
        }
        return null;
    }

    public void debug() {
        log.msg("PID: " + id);
        log.msg("SQL: " + sql);
        log.msg("Start time: " + getTimeFirstCapture());
        log.msg("Type: " + this.getType());
        log.msg("Capture count: " + getCaptureCount());
        log.msg("Last Capture: " + getLastCapture());
        log.msg("Relevance: " + relevance);
        log.msg("Cost: " + getLastPlan().getCost());
        log.msg("Tables: ");
        for (Table tablesQuery1 : tablesQuery) {
            log.msg("\t" + tablesQuery1.getName());
        }
        log.msg("Fields Query: ");
        for (Column fieldsQuery1 : fieldsQuery) {
            log.msg("\t" + fieldsQuery1.getName());
        }
    }

    public boolean containsFieldOrTable(String clause, String field) {
        clause = clause.toLowerCase();
        field = field.toLowerCase();
        if (clause.contains(field)) {
            return clause.contains(" " + field + " ")
                    || clause.contains(" " + field + ",")
                    || clause.contains(" " + field + ";")
                    || clause.contains(" " + field + "=")
                    || clause.contains(" " + field + ">")
                    || clause.contains(" " + field + "<")
                    || clause.contains("," + field + ",")
                    || clause.contains("," + field + ";")
                    || clause.contains("," + field + "=")
                    || clause.contains("," + field + ">")
                    || clause.contains("," + field + "<")
                    || clause.contains("." + field + ",")
                    || clause.contains("." + field + ";")
                    || clause.contains("." + field + "=")
                    || clause.contains("." + field + ">")
                    || clause.contains("." + field + "<")
                    || clause.substring(clause.length() - field.length()).equals(field);
        } else {
            return false;
        }
    }

    public Date getTimeFirstCapture() {
        if (this.executions.size() > 0) {
            return this.executions.get(0).getDateExecution();
        } else {
            return null;
        }
    }

    public Date getLastCapture() {
        if (this.executions.size() > 0) {
            return this.executions.get(this.executions.size() - 1).getDateExecution();
        } else {
            return null;
        }
    }

    public Plan getLastPlan() {
        if (this.executions.size() > 0) {
            return this.executions.get(executions.size() - 1);
        }
        return null;
    }

    public void setExecutions(ArrayList<Plan> executionsIn) {
        this.executions.addAll(executionsIn);
    }

    public ArrayList<Plan> getExecutions() {
        return this.executions;
    }

    public float getCostAVG(Interval interval) {
        ArrayList<Plan> executionsInWindow = this.getExecutionsInWindow(interval);
        if (executionsInWindow.isEmpty()) {
            return 0;
        }
        float cost = 0;
        for (Plan execution : executionsInWindow) {
            cost += execution.getCost();
        }
        return cost / executionsInWindow.size();
    }

    public float getDurationAVG(Interval interval) {
        ArrayList<Plan> executionsInWindow = this.getExecutionsInWindow(interval);
        if (executionsInWindow.isEmpty()) {
            return 0;
        }
        float duration = 0;
        for (Plan execution : executionsInWindow) {
            duration += execution.getDuration();
        }
        return duration / executionsInWindow.size();
    }

    public boolean isAreadyCaptured(SQL otherSQL) {
        for (Plan execution : executions) {
            if ((execution.getDateExecution() != null) && (execution.getDateExecution().equals(otherSQL.getTimeFirstCapture()))) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<ActionSF> getActionsSF() {
        return actionsSF;
    }

    public void addAction(ActionSF action) {
        if (!this.actionsSF.contains(action)) {
            this.actionsSF.add(action);
        }
    }

}

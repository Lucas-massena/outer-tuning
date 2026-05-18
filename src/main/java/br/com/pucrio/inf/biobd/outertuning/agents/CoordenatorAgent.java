/*
 * Outer-Tuning - Framework para apoiar a sintonia fina de banco de dados. PUC-RIO.
 * Rafael Pereira de Oliveira [rpoliveira@inf.puc-rio.br].
 * Ana Carolina Almeida [anacrl@gmail.com].
 * Sergio Lifschitz [sergio@inf.puc-rio.br].
 * PUC-RIO - BioBD.
 */
package br.com.pucrio.inf.biobd.outertuning.agents;

import br.com.pucrio.inf.biobd.outertuning.bib.base.Interval;
import br.com.pucrio.inf.biobd.outertuning.bib.base.IntervalList;
import br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Heuristic;
import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.ActionSF;
import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.Plan;
import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.Schema;
import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.SQL;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Rafael
 */
public class CoordenatorAgent {

    public static class AIComparisonRow {
        public int sqlId;
        public String sqlText;
        public float originalCost;
        public String geminiCommand;
        public float geminiCost;
        public float geminiCreationCost;
        public float geminiBonus;
        public String geminiJustification;
        public String gptCommand;
        public float gptCost;
        public float gptCreationCost;
        public float gptBonus;
        public String gptJustification;
        public String claudeCommand;
        public float claudeCost;
        public float claudeCreationCost;
        public float claudeBonus;
        public String claudeJustification;
    }

    private final CopyOnWriteArrayList<SQL> lastSQLCaptured;
    private Thread threadAgentTuning;
    public OuterTuningAgent OTAgent;
    public boolean running = false;
    private final GeminiTuningAgent geminiAgent;
    private final GPTTuningAgent gptAgent;
    private final ClaudeTuningAgent claudeAgent;
    private boolean geminiSelected = false;
    private boolean gptSelected = false;
    private boolean claudeSelected = false;

    public CoordenatorAgent(Configuration configuration) {
        this.lastSQLCaptured = new CopyOnWriteArrayList<>();
        this.OTAgent = new OuterTuningAgent(configuration);
        this.geminiAgent = new GeminiTuningAgent(configuration);
        this.gptAgent = new GPTTuningAgent(configuration);
        this.claudeAgent = new ClaudeTuningAgent(configuration);
    }

    public void startCaptureWorkload() {
        this.OTAgent.initialize(lastSQLCaptured);
        if (this.threadAgentTuning == null) {
            this.threadAgentTuning = new Thread(OTAgent);
            this.threadAgentTuning.start();
            this.running = true;
            this.startAIThreads();
        }
    }

    private void startAIThreads() {
        Thread geminiThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(60000);
                    if (!lastSQLCaptured.isEmpty() && geminiSelected) {
                        Schema schema = lastSQLCaptured.get(0).getSchemaDataBase();
                        if (schema != null) {
                            geminiAgent.analyze(lastSQLCaptured, schema);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        geminiThread.setDaemon(true);
        geminiThread.start();

        Thread gptThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(60000);
                    if (!lastSQLCaptured.isEmpty() && gptSelected) {
                        Schema schema = lastSQLCaptured.get(0).getSchemaDataBase();
                        if (schema != null) {
                            gptAgent.analyze(lastSQLCaptured, schema);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        gptThread.setDaemon(true);
        gptThread.start();

        Thread claudeThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(60000);
                    if (!lastSQLCaptured.isEmpty() && claudeSelected) {
                        Schema schema = lastSQLCaptured.get(0).getSchemaDataBase();
                        if (schema != null) {
                            claudeAgent.analyze(lastSQLCaptured, schema);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        claudeThread.setDaemon(true);
        claudeThread.start();
    }

    public String capturedQueryByWindow(String windowSize) {
        StringBuilder dataLine = new StringBuilder();
        ArrayList<SQL> sqlIn = new ArrayList<>();
        IntervalList list = new IntervalList();
        ArrayList<Interval> inter = list.getIntervals(windowSize);
        float duration;
        float cost;
        int count;
        for (int i = inter.size() - 1; i >= 0; i--) {
            dataLine.append(",\n['").append(inter.get(i).getIni(this.getMaskByWindowSize(windowSize))).append("'");

            for (SQL sql : lastSQLCaptured) {
                duration = 0;
                cost = 0;
                count = 0;
                for (Plan execution : sql.getExecutions()) {
                    if (inter.get(i).isBetween(execution.getDateExecution()) && execution.getDuration() > 0) {
                        if (!sqlIn.contains(sql)) {
                            sqlIn.add(sql);
                        }
                        duration += execution.getDuration();
                        cost += execution.getCost();
                        count++;
                    }
                }
                if (count == 0) {
                    dataLine.append(",0");
                } else {
                    dataLine.append(",").append(duration);
                }
                dataLine.append(",'<b>SQL #").append(sql.getId()).append("</b><br>  ");
                dataLine.append("Execution(s): <b>").append(count).append("</b><br>");
                dataLine.append("Total Time: <b>").append(formatDecimalIDE(duration)).append("s</b><br> ");
                dataLine.append("Total cost: <b>").append(formatDecimalIDE(cost)).append("</b>'");
            }
            dataLine.append("]");
        }

        StringBuilder firstLine = new StringBuilder("['TIME'");
        for (SQL sql : lastSQLCaptured) {
            firstLine.append(",'SQL #").append(sql.getId()).append("'");
            firstLine.append(",{type: 'string', role: 'tooltip', 'p': {'html': true}}");
        }

        if (sqlIn.isEmpty()) {
            dataLine = new StringBuilder();
            firstLine = new StringBuilder("['TIME'");
            firstLine.append(",'empty'");
            for (int i = inter.size() - 1; i >= 0; i--) {
                dataLine.append(",\n['").append(inter.get(i).getIni(this.getMaskByWindowSize(windowSize))).append("',0]");
            }
        }
        firstLine.append("]");
        return firstLine.toString() + dataLine;
    }

    public ArrayList<SQL> getSQLbyId(int id) {
        ArrayList<SQL> sqlIn = new ArrayList<>();
        if (this.lastSQLCaptured.size() >= id) {
            sqlIn.add(this.lastSQLCaptured.get(id - 1));
        }
        return sqlIn;
    }

    public ArrayList<SQL> getSQLbyWindow(String windowSize, String windowSelected) {
        ArrayList<SQL> sqlIn = new ArrayList<>();
        Interval intervalSelected = this.getIntervalAsked(windowSize, windowSelected);
        for (SQL sql : lastSQLCaptured) {
            for (Plan execution : sql.getExecutions()) {
                if (intervalSelected != null && intervalSelected.isBetween(execution.getDateExecution()) && execution.getDuration() > 0) {
                    if (!sqlIn.contains(sql)) {
                        sqlIn.add(sql);
                    }
                }
            }
        }
        return sqlIn;
    }

    public Interval getIntervalAsked(String windowSize, String windowSelected) {
        Interval intervalSelected = null;
        IntervalList list = new IntervalList();
        ArrayList<Interval> inter = list.getIntervals(windowSize);
        for (Interval interval : inter) {
            if (interval.getIni(this.getMaskByDate(windowSelected)).equals(windowSelected)) {
                intervalSelected = interval;
                break;
            }
        }
        return intervalSelected;
    }

    private String getMaskByDate(String windowSelected) {
        if (windowSelected.contains("/")) {
            return "dd/MM HH:mm";
        } else {
            return "HH:mm";
        }
    }

    private String getMaskByWindowSize(String windowSize) {
        if (windowSize.contains("h")) {
            return "dd/MM HH:mm";
        } else {
            return "HH:mm";
        }
    }

    public ArrayList<Heuristic> getHeuristicsFromOntology() {
        ArrayList<Heuristic> list = new ArrayList<>(this.OTAgent.getAllHeuristics());
        list.add(GeminiTuningAgent.getHeuristic());
        if (gptAgent.isConfigured()) {
            list.add(GPTTuningAgent.getHeuristic());
        }
        if (claudeAgent.isConfigured()) {
            list.add(ClaudeTuningAgent.getHeuristic());
        }
        return list;
    }

    public void setSelectedHeuristics(Heuristic heuristics) {
        if (GeminiTuningAgent.HEURISTIC_NAME.equals(heuristics.getName())) {
            this.geminiSelected = true;
        } else if (GPTTuningAgent.HEURISTIC_NAME.equals(heuristics.getName())) {
            this.gptSelected = true;
        } else if (ClaudeTuningAgent.HEURISTIC_NAME.equals(heuristics.getName())) {
            this.claudeSelected = true;
        } else {
            this.OTAgent.selectedHeuristics.add(heuristics);
        }
    }

    public CopyOnWriteArrayList<Heuristic> getSelectedHeuristics() {
        return this.OTAgent.selectedHeuristics;
    }

    public boolean isAnyHeuristicSelected() {
        return geminiSelected || gptSelected || claudeSelected || !OTAgent.selectedHeuristics.isEmpty();
    }

    public String formatDecimalIDE(double number) {
        DecimalFormat formatter = new DecimalFormat("###,###.##", new DecimalFormatSymbols(new Locale("pt", "BR")));
        return formatter.format(number);
    }

    public ActionSF getActionSFById(String actionID) {
        for (SQL sql : lastSQLCaptured) {
            for (ActionSF actionSF : sql.getActionsSF()) {
                if (actionSF.getId().equals(actionID)) {
                    return actionSF;
                }
            }
        }
        return null;
    }

    public String getActionsFromChart() {
        ArrayList<String> result = new ArrayList<>();
        for (SQL sql : lastSQLCaptured) {
            for (ActionSF actionSF : sql.getActionsSF()) {
                String actionTemp = "['" + actionSF.getId() + "', " + actionSF.getBonus() + ", " + actionSF.getCreationCost() + ", '" + actionSF.getType() + "', " + actionSF.getSql().size() + "]";
                if (!result.contains(actionTemp)) {
                    result.add(actionTemp);
                }
            }
        }
        StringBuilder toChart = new StringBuilder();
        for (int i = 0; i < result.size(); i++) {
            toChart.append(result.get(i));
            if (i < (result.size() - 1)) {
                toChart.append(",");
            }
        }
        if (toChart.length() > 0)
            toChart.insert(0, "['ACTION_ID', 'Gain Expectancy', 'Creation Cost', 'Type', 'N. of SQL Serviced'], ");
        return toChart.toString();
    }

    public List<AIComparisonRow> getAIComparisonRows() {
        List<AIComparisonRow> rows = new ArrayList<>();
        for (SQL sql : lastSQLCaptured) {
            AIComparisonRow row = new AIComparisonRow();
            row.sqlId = sql.getId();
            row.sqlText = sql.getSql();
            row.originalCost = (float) sql.getCostAVG(null);

            for (ActionSF action : sql.getActionsSF()) {
                if (GeminiTuningAgent.HEURISTIC_NAME.equals(action.getHeuristic())) {
                    row.geminiCommand = action.getCommand();
                    row.geminiCost = action.getCost() > 0 ? action.getCost() : row.originalCost;
                    row.geminiCreationCost = action.getCreationCost();
                    row.geminiBonus = action.getBonus();
                    row.geminiJustification = action.getJustify();
                } else if (GPTTuningAgent.HEURISTIC_NAME.equals(action.getHeuristic())) {
                    row.gptCommand = action.getCommand();
                    row.gptCost = action.getCost() > 0 ? action.getCost() : row.originalCost;
                    row.gptCreationCost = action.getCreationCost();
                    row.gptBonus = action.getBonus();
                    row.gptJustification = action.getJustify();
                } else if (ClaudeTuningAgent.HEURISTIC_NAME.equals(action.getHeuristic())) {
                    row.claudeCommand = action.getCommand();
                    row.claudeCost = action.getCost() > 0 ? action.getCost() : row.originalCost;
                    row.claudeCreationCost = action.getCreationCost();
                    row.claudeBonus = action.getBonus();
                    row.claudeJustification = action.getJustify();
                }
            }

            if (row.geminiCommand != null || row.gptCommand != null || row.claudeCommand != null) {
                rows.add(row);
            }
        }
        return rows;
    }

    public String getAIComparisonChartData(List<AIComparisonRow> rows) {
        if (rows.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("['SQL', 'Original Cost', 'Gemini Cost', 'GPT Cost', 'Claude Cost']");
        for (AIComparisonRow row : rows) {
            float geminiCost = row.geminiCommand != null ? row.geminiCost : row.originalCost;
            float gptCost    = row.gptCommand    != null ? row.gptCost    : row.originalCost;
            float claudeCost = row.claudeCommand  != null ? row.claudeCost : row.originalCost;
            sb.append(",\n['SQL #").append(row.sqlId).append("', ")
              .append(row.originalCost).append(", ")
              .append(geminiCost).append(", ")
              .append(gptCost).append(", ")
              .append(claudeCost).append("]");
        }
        return "[" + sb + "]";
    }

    public boolean isRunning() {
        return this.running;
    }
}

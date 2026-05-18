package br.com.pucrio.inf.biobd.outertuning.bib.sgbd;

import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.mysql.Explain;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlanMySQL extends Plan {

    private long cost;
    private long numRow;
    private long rowSize;
    private long duration;
    private String html;
    private ArrayList<SeqScan> seqScanList;


    public PlanMySQL(String plan, Date time) {
        this.duration = System.currentTimeMillis() - time.getTime();
        setDateExecution(time);
        setPlan(plan);
        Explain explain;
        try {
            explain = new Gson().fromJson(plan, Explain.class);
            if (explain != null) {
                this.html = explain.html();
                this.cost = Optional.ofNullable(explain.getQueryBlock()).map(Explain.QueryBlock::getCostInfo).map(Explain.CostInfo::getQueryCost).map(Double::parseDouble).orElse(0D).longValue();
                List<Explain.Table> nestedLoop = Optional.ofNullable(explain.getQueryBlock()).map(Explain.QueryBlock::getNestedLoop).orElse(Collections.emptyList());
                this.numRow = nestedLoop.stream().map(Explain.Table::getTableInfo).mapToInt(Explain.TableInfo::getRowsExaminedPerScan).sum();
                this.rowSize = nestedLoop.stream().map(Explain.Table::getTableInfo).mapToInt(Explain.TableInfo::getRowsProducedPerJoin).sum();
                Function<Explain.TableInfo, SeqScan> tableInfoSeqScanFunction = tableInfo ->
                        new SeqScan(tableInfo.getTableName(), new ArrayList<>(), new Double(tableInfo.getCostInfo().getEvalCost()).longValue());
                this.seqScanList = nestedLoop.stream().map(Explain.Table::getTableInfo).map(tableInfoSeqScanFunction).collect(Collectors.toCollection(ArrayList::new));
            }
        } catch (JsonSyntaxException e) {
            System.err.println(plan);
        }

    }

    @Override
    public long getCost() {
        return this.cost;
    }

    @Override
    public long getNumRow() {
        return this.numRow;
    }

    @Override
    public long getRowSize() {
        return this.rowSize;
    }

    @Override
    public ArrayList<SeqScan> getSeqScanOperations() {
        return this.seqScanList;
    }

    @Override
    public float getDuration() {
        return TimeUnit.MILLISECONDS.toSeconds(duration);
    }

    @Override
    public String getPlanToViewHtml() {
        return this.html;
    }
}

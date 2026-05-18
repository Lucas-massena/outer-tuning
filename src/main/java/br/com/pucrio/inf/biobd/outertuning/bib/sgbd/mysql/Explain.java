package br.com.pucrio.inf.biobd.outertuning.bib.sgbd.mysql;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Optional;

public class Explain {

    public String html() {
        return Optional.ofNullable(getQueryBlock()).map(QueryBlock::getCostInfo)
                .map(CostInfo::getQueryCost)
                .map(cost -> String.format("Cost: %s <br/>", cost))
                .orElse("");
    }

    @SerializedName("select_id")
    private String selectId;

    @SerializedName("query_block")
    private QueryBlock queryBlock;

    public QueryBlock getQueryBlock() {
        return queryBlock;
    }

    public void setQueryBlock(QueryBlock queryBlock) {
        this.queryBlock = queryBlock;
    }

    public static class CostInfo {
        @SerializedName("query_cost")
        private String queryCost;

        public String getQueryCost() {
            return queryCost;
        }

        public void setQueryCost(String queryCost) {
            this.queryCost = queryCost;
        }
    }

    public static class QueryBlock {
        @SerializedName("cost_info")
        private CostInfo costInfo;
        @SerializedName("nested_loop")
        private List<Table> nestedLoop;

        public CostInfo getCostInfo() {
            return costInfo;
        }

        public void setCostInfo(CostInfo costInfo) {
            this.costInfo = costInfo;
        }

        public List<Table> getNestedLoop() {
            return nestedLoop;
        }

        public void setNestedLoop(List<Table> nestedLoop) {
            this.nestedLoop = nestedLoop;
        }
    }

    public static class TableCostInfo {
        @SerializedName("eval_cost")
        private String evalCost;
        @SerializedName("read_cost")
        private String readCost;
        @SerializedName("prefix_cost")
        private String prefixCost;
        @SerializedName("data_read_per_join")
        private String dataReadPerJoin;

        public String getEvalCost() {
            return evalCost;
        }

        public void setEvalCost(String evalCost) {
            this.evalCost = evalCost;
        }

        public String getReadCost() {
            return readCost;
        }

        public void setReadCost(String readCost) {
            this.readCost = readCost;
        }

        public String getPrefixCost() {
            return prefixCost;
        }

        public void setPrefixCost(String prefixCost) {
            this.prefixCost = prefixCost;
        }

        public String getDataReadPerJoin() {
            return dataReadPerJoin;
        }

        public void setDataReadPerJoin(String dataReadPerJoin) {
            this.dataReadPerJoin = dataReadPerJoin;
        }
    }

    public static class Table {
        @SerializedName("table")
        private TableInfo tableInfo;

        public TableInfo getTableInfo() {
            return tableInfo;
        }

        public void setTableInfo(TableInfo tableInfo) {
            this.tableInfo = tableInfo;
        }
    }

    public static class TableInfo {
        private String key;
        private String filtered;
        @SerializedName("table_name")
        private String tableName;
        @SerializedName("access_type")
        private String accessType;
        @SerializedName("attached_condition")
        private String attachedCondition;
        @SerializedName("rows_examined_per_scan")
        private Integer rowsExaminedPerScan;
        @SerializedName("rows_produced_per_join")
        private Integer rowsProducedPerJoin;
        @SerializedName("possible_keys")
        private List<String> possibleKeys;
        @SerializedName("used_columns")
        private List<String> usedColumns;
        @SerializedName("cost_info")
        private TableCostInfo costInfo;
        @SerializedName("using_join_buffer")
        private String usingJoinBuffer;

        public String getFiltered() {
            return filtered;
        }

        public void setFiltered(String filtered) {
            this.filtered = filtered;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getAccessType() {
            return accessType;
        }

        public void setAccessType(String accessType) {
            this.accessType = accessType;
        }

        public String getAttachedCondition() {
            return attachedCondition;
        }

        public void setAttachedCondition(String attachedCondition) {
            this.attachedCondition = attachedCondition;
        }

        public Integer getRowsExaminedPerScan() {
            return rowsExaminedPerScan;
        }

        public void setRowsExaminedPerScan(Integer rowsExaminedPerScan) {
            this.rowsExaminedPerScan = rowsExaminedPerScan;
        }

        public Integer getRowsProducedPerJoin() {
            return rowsProducedPerJoin;
        }

        public void setRowsProducedPerJoin(Integer rowsProducedPerJoin) {
            this.rowsProducedPerJoin = rowsProducedPerJoin;
        }

        public List<String> getUsedColumns() {
            return usedColumns;
        }

        public void setUsedColumns(List<String> usedColumns) {
            this.usedColumns = usedColumns;
        }

        public TableCostInfo getCostInfo() {
            return costInfo;
        }

        public void setCostInfo(TableCostInfo costInfo) {
            this.costInfo = costInfo;
        }

        public String getUsingJoinBuffer() {
            return usingJoinBuffer;
        }

        public void setUsingJoinBuffer(String usingJoinBuffer) {
            this.usingJoinBuffer = usingJoinBuffer;
        }
    }
}

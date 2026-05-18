/*
 * DBX - Database eXternal Tuning
 * BioBD Lab - PUC-Rio && DC - UFC  *
 */
package br.com.pucrio.inf.biobd.outertuning.bib.sgbd;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Rafael
 */
public class Table {

    private String name;
    private String schema;
    protected ArrayList<Column> fields;
    private long numberRows;
    private long numberPages;

    public long getNumberPages() {
        return numberPages;
    }

    public void setNumberPages(long numberPages) {
        this.numberPages = numberPages;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public long getNumberRows() {
        return numberRows;
    }

    public void setNumberRows(long numberRows) {
        this.numberRows = numberRows;
    }

    public Table() {
        this.fields = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase().trim();
    }

    public ArrayList<Column> getFields() {
        return fields;
    }

    public String getFieldsString() {
        String result = "";
        result = fields.stream().map((field) -> field.getName() + ", ").reduce(result, String::concat);
        return result;
    }

    public void setFields(ArrayList<Column> fields) {
        for (Column field : fields) {
            if (!this.containsField(field)) {
                this.fields.add(field);
            }
        }
    }

    private boolean containsField(Column field) {
        for (Column column : fields) {
            if (column.equals(field)) {
                return true;
            }
        }
        return false;
    }

    public Object getValue(String dataType) {
        switch (dataType) {
            case "temNome":
                return this.getName();
            case "temNumeroTuplas":
                return (int) this.getNumberRows();
            case "temNumeroPaginas":
                return (int) this.getNumberPages();
            default:
                return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
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
        final Table other = (Table) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.schema, other.schema)) {
            return false;
        }
        return true;
    }

}

/*
 * Framework para apoiar a sintonia fina de banco de dados. PUC-RIO.
 * Ana Carolina Almeida [anacrl@gmail.com].
 * Rafael Pereira de Oliveira [rpoliveira@inf.puc-rio.br].
 * PUC-RIO - LABBIO - 2014.
 */
package br.com.pucrio.inf.biobd.outertuning.executor;

import br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration;
import br.com.pucrio.inf.biobd.outertuning.bib.base.Log;
import java.util.Objects;
import org.protege.owl.portability.query.Result;
import org.protege.owl.portability.query.ResultException;

/**
 *
 * @author Rafael
 */
public final class Action {

    private int id;
    private String command;
    private String name;
    private String heuristic;
    private String status;
    private String justify;
    private float gain;
    private float cost;
    private float cost_execution;
    private boolean executed;
    private Configuration config;
    private Log log;

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public float getCost_execution() {
        return cost_execution;
    }

    public void setCost_execution(float cost_execution) {
        this.cost_execution = cost_execution;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public String getJustify() {
        if (this.justify == null) {
            return "";
        }
        return justify;
    }

    public void setJustify(String justify) {
        this.justify = justify;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(String heuristic) {
        this.heuristic = heuristic;
    }

    public Action(Result result) {
        super();
        try {
            this.config = new Configuration();
            this.log = new Log(config);
            this.setCommand(result.getValue("?comando").toString());
            this.setName(result.getValue("?vm").toString());
            this.setHeuristic(result.getValue("?nomeHeuristica").toString());
            this.setStatus(result.getValue("?situacao").toString());
            this.setCost(Float.valueOf(result.getValue("?custoCriacao").toString()));
            this.setCost_execution(Float.valueOf(result.getValue("?cost").toString()));
            this.setGain(Float.valueOf(result.getValue("?bonus").toString()));
        } catch (ResultException ex) {
            log.error(ex);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.command);
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
        final Action other = (Action) obj;
        if (!Objects.equals(this.command, other.command)) {
            return false;
        }
        return true;
    }
}

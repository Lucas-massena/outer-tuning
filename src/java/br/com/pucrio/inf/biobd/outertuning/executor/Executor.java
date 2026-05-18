/*
 * Framework para apoiar a sintonia fina de banco de dados. PUC-RIO.
 * Ana Carolina Almeida [anacrl@gmail.com].
 * Rafael Pereira de Oliveira [rpoliveira@inf.puc-rio.br].
 * PUC-RIO - LABBIO - 2014.
 */
package br.com.pucrio.inf.biobd.outertuning.executor;

import br.com.pucrio.inf.biobd.outertuning.bib.base.Log;
import br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration;
import br.com.pucrio.inf.biobd.outertuning.bib.sgbd.ConnectionSGBD;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class Executor {

    private final ArrayList<Action> actions;
    private final ArrayList<Action> candidateActions;
    protected final Configuration config;
    protected final Log log;

    public Executor() {
        super();
        this.actions = new ArrayList<>();
        this.candidateActions = new ArrayList<>();
        this.config = new Configuration();
        this.log = new Log(config);
    }

    public void executeAction(Action action) {
        try {
            log.title("");
            ConnectionSGBD conn = new ConnectionSGBD();
            log.msg("Command will be execute: " + action.getCommand());
            PreparedStatement preparedStatement = conn.prepareStatement(config.getProperty("signature") + action.getCommand());
            preparedStatement.execute();
            action.setStatus("Real");
            action.setExecuted(true);
            log.msg("Command executed: " + action.getCommand());
        } catch (SQLException ex) {
            log.error(ex);
        }
    }

    public void add(Action newAction) {
        Action actionEqual = this.containsAction(newAction);
        if (actionEqual != null) {
            this.actions.remove(actionEqual);
        }
        newAction.setId(this.actions.size());
        this.actions.add(newAction);
    }

    public void addCandidateAction(Action candidateAction) {
        Action actionEqual = this.containsCandidateAction(candidateAction);
        if (actionEqual != null) {
            this.candidateActions.remove(actionEqual);
        }
        candidateAction.setId(this.candidateActions.size());
        this.candidateActions.add(candidateAction);
    }

    public void executeActions() {
        for (Action action : actions) {
            if (!action.isExecuted() && !action.getStatus().equals("hypotetical")) {
                this.executeAction(action);
            }
        }
    }

    public Action containsAction(Action tempAction) {
        for (Action action : actions) {
            if (action.equals(tempAction)) {
                return action;
            }
        }
        return null;
    }

    public Action containsCandidateAction(Action candidateAction) {
        for (Action action : candidateActions) {
            if (action.equals(candidateAction)) {
                return action;
            }
        }
        return null;
    }

    public ArrayList<Action> getActions() {
        return this.actions;
    }

    public ArrayList<Action> getCandidateActions() {
        return this.candidateActions;
    }

    public void executeActions(ArrayList<Integer> actionsFromBlackBoard) {
        if (this.actions.size() > 0) {
            for (int num : actionsFromBlackBoard) {
                this.executeAction(this.actions.get(num));
            }
        }
    }

}

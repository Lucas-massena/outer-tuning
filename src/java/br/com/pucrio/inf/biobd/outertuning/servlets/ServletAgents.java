package br.com.pucrio.inf.biobd.outertuning.servlets;

import br.com.pucrio.inf.biobd.outertuning.agents.CoordenatorAgent;
import br.com.pucrio.inf.biobd.outertuning.bib.base.Log;
import br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Concept;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.FunctionConceptBase;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Heuristic;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;

/**
 *
 * @author Rafael
 */
@WebServlet(name = "ServletAgents", urlPatterns = {"/ServletAgents"})
public class ServletAgents extends ServletBase {

    private CoordenatorAgent controller;
    private String windowSize = "10min";
    private Memory memory;

    public void selectHeuristics() {
        this.checkObjectsMemory();
        if (controller.isRunning()) {
            this.redirect = "workload";
        } else {
            request.setAttribute("heuristicList", this.controller.getHeuristicsFromOntology());
        }
    }

    public void tuningAction() {
        this.checkObjectsMemory();
        String actionID = request.getParameter("actionid");
        if (actionID != null) {
            if (this.controller.isRunning()) {
                request.setAttribute("actionData", this.controller.getActionSFById(actionID));
            }
        }
    }

    public void executeTuningAction() {
        this.checkObjectsMemory();
        String actionID = request.getParameter("executeid");
        if (actionID != null) {
            if (this.controller.isRunning()) {
                request.setAttribute("executed", true);
                request.setAttribute("actionData", this.controller.getActionSFById(actionID));
            }
        }
        this.redirect = "tuningAction";
    }

    public void tuningActions() {
        this.checkObjectsMemory();
        if (this.controller.isRunning()) {
            request.setAttribute("actionsData", this.controller.getActionsFromChart());
            String actionID = request.getParameter("actionid");
            if (actionID != null) {
                request.setAttribute("actionData", this.controller.getActionSFById(actionID));
            }
        }
    }

    public void documentation() {
        this.setDefaultRequest();
    }

    public void index() {
        this.setDefaultRequest();
        if (controller.isRunning()) {
            this.redirect = "workload";
        }
    }

    public void workload() {
        this.checkObjectsMemory();
        this.redirect = "";
        this.setWindowSize(request.getParameter("windowSize"));
        for (Heuristic heuristic : this.controller.getHeuristicsFromOntology()) {
            if (this.request.getParameter(heuristic.getName()) != null) {
                this.controller.setSelectedHeuristics(heuristic);
            }
        }
        if (this.controller.getSelectedHeuristics().size() <= 0) {
            this.enableHeuristicsForTest();
        }
        controller.startCaptureWorkload();
        request.setAttribute("workloadData", this.controller.capturedQueryByWindow(this.windowSize));
        this.setRequestSQLDetails(request.getParameter("sql"), request.getParameter("window"));
    }

    private void checkObjectsMemory() {
        if (memory == null) {
            memory = new Memory();
        }
        if (config == null) {
            config = new Configuration();
        }
        if (log == null) {
            log = new Log(config);
        }
        if (controller == null) {
            this.createController();
        }
        this.setDefaultRequest();
    }

    private void createController() {
        if (this.controller == null) {
            if (memory.isNotRunning("TuningAgent")) {
                log.archiveLogFiles();
                log.writePID();
                this.controller = new CoordenatorAgent();
                log.msg("TuningAgent started.");
                memory.addAgent("TuningAgent");
            }
        }
    }

    private void setWindowSize(String windowSize) {
        if (windowSize != null) {
            this.windowSize = request.getParameter("windowSize");
            request.setAttribute("windowSize", this.windowSize);
        }
    }

    private void setDefaultRequest() {
        request.setAttribute("windowSize", this.windowSize);
        request.setAttribute("config", config);
    }

    private void setRequestSQLDetails(String sqlParameter, String windowParameter) {
        if (sqlParameter != null && !sqlParameter.equals("empty")) {
            try {
                int id = Integer.valueOf(URLDecoder.decode(sqlParameter.trim().split("#")[1], "UTF8"));
                request.setAttribute("sqlSelected", id);
                if (windowParameter != null) {
                    request.setAttribute("intervalAsked", this.controller.getIntervalAsked(this.windowSize, URLDecoder.decode(windowParameter, "UTF8")));
                    request.setAttribute("sqlAsked", this.controller.getSQLbyWindow(this.windowSize, windowParameter));
                } else {
                    request.setAttribute("sqlAsked", this.controller.getSQLbyId(id));
                }
            } catch (UnsupportedEncodingException ex) {
                log.error(ex);
            }
        }
    }

    public void debugConcept() {
        try {
            Class<?> c = this.controller.OTAgent.classDispatcherDebug;
            FunctionConceptBase obj = (FunctionConceptBase) c.newInstance();
            ArrayList<Concept> concepts = obj.getIndividualsForInstantiate(this.controller.OTAgent.sourceDebug);
            for (Concept concept : concepts) {
                System.out.println(concept.getMsgToPrint());
            };
        } catch (SecurityException | InstantiationException | IllegalAccessException ex) {
            log.error(ex);
        }

    }

    private void enableHeuristicsForTest() {
        ArrayList<String> heuristics = new ArrayList<>();
        heuristics.add("HeuristicaVisaoMaterializadaCandidatas");
        heuristics.add("HeuristicaVisaoMaterializadaBeneficios");
        heuristics.add("HeuristicaVisaoMaterializadaExpectativa");
        heuristics.add("HeuristicaIndicesCandidatos");
        for (Heuristic heuristic : this.controller.getHeuristicsFromOntology()) {
            if (heuristics.contains(heuristic.getName())) {
                this.controller.setSelectedHeuristics(heuristic);
            }
        }
    }

}

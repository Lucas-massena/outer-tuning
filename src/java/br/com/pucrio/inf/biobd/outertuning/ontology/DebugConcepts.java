/*
 * Framework para apoiar a sintonia fina de banco de dados. PUC-RIO.
 * Ana Carolina Almeida [anacrl@gmail.com].
 * Rafael Pereira de Oliveira [rpoliveira@inf.puc-rio.br].
 * PUC-RIO - LABBIO - 2014.
 */
package br.com.pucrio.inf.biobd.outertuning.ontology;

import br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration;
import br.com.pucrio.inf.biobd.outertuning.bib.base.Log;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Concept;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Relation;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class DebugConcepts {

    private final ArrayList<Concept> result;
    private Configuration config;
    private Log log;

    public DebugConcepts(ArrayList<Concept> result) {
        this.config = new Configuration();
        this.log = new Log(config);
        this.result = result;
    }

    private void debugConcepts() {
        if (config.getProperty("debug_pre_result_functions").equals("1")) {
            for (Concept concept : result) {
                log.msg(concept.getMsgToPrint());
                this.debugConcept(concept, 0);
            }
        }
    }

    private void debugConcept(Concept concept, int level) {
        this.debugrelations(concept.getRelation(), level + 1);
    }

    private void debugrelations(ArrayList<Relation> relations, int level) {
        for (Relation relationConcepts : relations) {
            for (Concept relationConcept : relationConcepts.getConcept()) {
                log.msg(this.getLevelT(level) + relationConcepts.getNameRelation().toUpperCase() + ": " + relationConcept.getMsgToPrint());
                this.debugConcept(relationConcept, level);
            }
        }
    }

    private String getLevelT(int level) {
        String levelT = "";
        for (int i = 0; i < level; i++) {
            levelT += "\t";
        }
        return levelT;
    }

    public void run() {
        this.debugConcepts();
    }

}

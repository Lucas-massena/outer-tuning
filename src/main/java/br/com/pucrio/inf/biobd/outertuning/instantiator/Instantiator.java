
/*
 * Framework para apoiar a sintonia fina de banco de dados. PUC-RIO.
 * Ana Carolina Almeida [anacrl@gmail.com].
 * Rafael Pereira de Oliveira [rpoliveira@inf.puc-rio.br].
 * PUC-RIO - LABBIO - 2014.
 */
package br.com.pucrio.inf.biobd.outertuning.instantiator;

import br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Concept;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Property;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Relation;
import br.com.pucrio.inf.biobd.outertuning.ontology.Semantic;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class Instantiator extends Semantic {

    protected ArrayList<Concept> conceptsForInstantiate;

    public void setConceptsForInstantiate(ArrayList<Concept> conceptsForInstantiate) {
        this.conceptsForInstantiate = conceptsForInstantiate;
    }

    public Instantiator(Configuration configuration) {
        super(configuration);
        this.conceptsForInstantiate = new ArrayList<>();
    }

    public void addConceptForInstantiate(Concept concept) {
        this.conceptsForInstantiate.add(concept);
    }

    public void instantiateConcepts() {
        for (Concept concept : this.conceptsForInstantiate) {
            OWLIndividual individual = this.addNewIndividualOfOntology(concept);

            for (Relation relation : concept.getRelation()) {
                OWLObjectProperty relationObject = model.getOWLObjectProperty(relation.getNameRelation());
                if (relation.isNotUnique() || (relation.isUnique() && !individual.hasPropertyValue(relationObject))) {
                    for (Concept relationIndividual : relation.getConcept()) {
                        individual.addPropertyValue(relationObject, this.addNewIndividualOfOntology(relationIndividual));
                    }
                }
            }
        }
        this.conceptsForInstantiate.clear();
    }

    private OWLIndividual addNewIndividualOfOntology(Concept concept) {
        if (config.getProperty("debug_params_instantiate").contains("1")) {
            log.msg(concept.getMsgToPrint());
        }
        String name = this.getNameIndividual(concept);
        OWLIndividual individualMachine = model.getOWLIndividual(name);
        if (individualMachine == null) {
            individualMachine = this.createNewIndividualOfOntology(concept, name);
        }
        return this.addPropertiesOfIndividual(individualMachine, concept);
    }

    private OWLIndividual addPropertiesOfIndividual(OWLIndividual individualMachine, Concept concept) {
        for (Property param : concept.paramOut) {
            if (param.getDataType() != null && param.getValue() != null) {
                OWLDatatypeProperty hasDescription = model.getOWLDatatypeProperty(param.getDataType());
                if (!individualMachine.hasPropertyValue(hasDescription)) {
                    try {
                        individualMachine.setPropertyValue(hasDescription, param.getValue());
                    } catch (Exception e) {
                        log.msg(hasDescription);
                        log.msg(param.getValue().getClass());
                        log.msg(param.getValue());
                        log.msg(param.getDataType());
                        log.error(e);
                    }

                } else if (param.isReplace()) {
                    Object oldObj = individualMachine.getPropertyValue(hasDescription);
                    individualMachine.removePropertyValue(hasDescription, oldObj);
                    individualMachine.setPropertyValue(hasDescription, param.getValue());
                }
            }
        }
        return individualMachine;
    }

    private OWLIndividual createNewIndividualOfOntology(Concept concept, String name) {
        if (!concept.getClassName().trim().isEmpty()) {
            OWLNamedClass namedClass = model.getOWLNamedClass(concept.getClassName());
            OWLIndividual individual = namedClass.createOWLIndividual(name);
            return individual;
        }
        return null;
    }

}

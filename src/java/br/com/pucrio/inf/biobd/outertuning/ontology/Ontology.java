/*
 * Framework para apoiar a sintonia fina de banco de dados. PUC-RIO.
 * Ana Carolina Almeida [anacrl@gmail.com].
 * Rafael Pereira de Oliveira [rpoliveira@inf.puc-rio.br].
 * PUC-RIO - LABBIO - 2014.
 */
package br.com.pucrio.inf.biobd.outertuning.ontology;

import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Concept;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Heuristic;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Property;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.SQWRLQuery;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Source;
import br.com.pucrio.inf.biobd.outertuning.executor.Action;
import br.com.pucrio.inf.biobd.outertuning.instantiator.Instantiator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.protege.owl.portability.query.Result;
import org.protege.owl.portability.query.ResultException;
import org.protege.swrlapi.exceptions.SWRLRuleEngineException;

public final class Ontology extends Semantic {

    private final Instantiator instantiator;
    private ArrayList<Source> preConditions;

    public ArrayList<Source> getPreConditions() {
        return this.preConditions;
    }

    private void readPreConditions() {
        Result conditions = this.executeSWRLQuery(this.SWRLQuery.getParameter("getAllPreConditions"));
        this.preConditions = new ArrayList<>();
        try {
            while (conditions.hasNext()) {
                Source source = new Source();
                source.setLibrary(conditions.getValue("?biblioteca").toString());
                source.setPreConditionSQWRL("getAll" + String.valueOf(conditions.getValue("?nome")));
                source.setOrder(Integer.parseInt(conditions.getValue("?ordem").toString()));
                source.setClassName(String.valueOf(conditions.getValue("?nome")));
                source.setClassJavaName(String.valueOf(conditions.getValue("?classe")));
                source.setHeuristic(String.valueOf(conditions.getValue("?heu")));
                source.setFunctionName(String.valueOf(conditions.getValue("?nomeFuncao")));
                source.paramOut = this.getAllParametersOut(source);
                this.addPreCondition(source);
                conditions.next();
            }
        } catch (ResultException ex) {
            log.error(ex);
        }
    }

    @Override
    public void readOntology() {
        super.readOntology();
        this.readPreConditions();
    }

    private ArrayList<Property> getAllParametersOut(Source source) {
        ArrayList<Property> parameters = new ArrayList<>();
        try {
            Result parametersOut = this.executeSWRLQuery(this.SWRLQuery.getAllParametersFromPreConditions(source.getFunctionName()));
            if (config.getProperty("debug_parameters_pre_conditions").equals("1")) {
                log.title(source.getClassName());
            }
            while (parametersOut.hasNext()) {
                if (config.getProperty("debug_parameters_pre_conditions").equals("1")) {
                    log.msg(parametersOut.getValue("?paramDataType") + "-" + parametersOut.getValue("?tipoParam"));
                }
                Property param = new Property();
                param.setDataType(String.valueOf(parametersOut.getValue("?paramDataType")));
                param.setType(String.valueOf(parametersOut.getValue("?tipoParam")));
                parameters.add(param);
                parametersOut.next();
            }
            if (config.getProperty("debug_parameters_pre_conditions").equals("1")) {
                log.endTitle();
            }
        } catch (ResultException ex) {
            log.error(ex);
        }
        return parameters;
    }

    public Ontology() {
        super();
        this.SWRLQuery = new SQWRLQuery();
        this.instantiator = new Instantiator();
    }

    public void addNewConcept(Concept concept) {
        this.instantiator.addConceptForInstantiate(concept);
    }

    public void instantiateConcepts() {
        this.instantiator.instantiateConcepts();
    }

    private void enableRulesHeuristc(String heuristic) {
        try {
            String query = this.SWRLQuery.getAllRulesFromHeuristic(heuristic);
            Result result = this.executeSWRLQuery(query);
            while (result.hasNext()) {
                this.enableRule(String.valueOf(result.getValue("?dado")));
                result.next();
            }
        } catch (ResultException ex) {
            log.error(ex);
        }
    }

    public void enableHeuristc(Heuristic heuristics) {
        try {
            if (config.getProperty("disable_rules").equals("0")) {
                log.msg("Enable: " + heuristics.getName());
                this.enableRulesHeuristc(heuristics.getName());
            }
        } catch (Error e) {
            log.error(e);
        }
    }

    private boolean deleteFileIfExist(File file) {
        File f = new File(config.getProperty("folderFilesOntology") + file.getName());
        if (file.getPath().contains(f.getPath())) {
            return false;
        }
        if (f.exists() && !f.isDirectory()) {
            log.msg("Delete file: " + f.getPath());
            f.delete();
        }
        return true;
    }

    public void copyFileFromOntologyDirectory(File sourceLocation) {
        if (this.deleteFileIfExist(sourceLocation)) {
            try {
                OutputStream out;
                try (InputStream in = new FileInputStream(sourceLocation)) {
                    out = new FileOutputStream(config.getProperty("folderFilesOntology") + sourceLocation.getName());
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    config.setProperty("fileOntology", config.getProperty("folderFilesOntology") + sourceLocation.getName());
                }
                out.close();
            } catch (Error | FileNotFoundException ex) {
                log.error(ex);
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    public Result getAllActions() {
        return this.executeSWRLQuery(this.SWRLQuery.getParameter("getActions"));
    }

    public Result getAllCandidateActions() {
        return this.executeSWRLQuery(this.SWRLQuery.getParameter("getCandidateActions"));
    }

    public void addNewConcepts(ArrayList<Concept> concepts) {
        for (Concept concept : concepts) {
            this.instantiator.addConceptForInstantiate(concept);
        }
    }

    public void save() {
        try {
            log.msg("vai salvar :D");
            Ontology.rulesMachine.writeInferredKnowledge2OWL();
            log.msg("salvou lol");

        } catch (SWRLRuleEngineException ex) {
            log.error(ex);
        }
    }

    public void debugConcepts() {
        if (config.getProperty("debug_pos_concepts_preconditions").equals("1")) {
            List<String> fields;
            ArrayList<String> impresso = new ArrayList<>();
            Result parametersOut;
            String conceito = "";
            try {
                for (Source source : this.preConditions) {
                    String query = this.SWRLQuery.replaceSimbolsOntology(this.getQueryFromPreConditions(source));
                    log.msg(query);
                    parametersOut = this.executeSWRLQuery(query);
                    fields = parametersOut.getColumnNames();
                    while (parametersOut.hasNext()) {
                        for (String field : fields) {
                            if (field.equals("?className")) {
                                if (!conceito.equals(parametersOut.getValue(field).toString())) {
                                    conceito = parametersOut.getValue(field).toString();
                                    impresso.clear();
                                    log.title(source.getClassName() + ": " + parametersOut.getValue(field));
                                    log.msg("\t\t" + "CONCEITO: " + source.getClassName());
                                }
                            } else if (!impresso.contains(source.getClassName() + field + parametersOut.getValue(field).toString())) {
                                log.msg("\t\t" + field.substring(0, field.length() - 4).toUpperCase() + ": " + parametersOut.getValue(field));
                            }
                            impresso.add(source.getClassName() + field + parametersOut.getValue(field).toString());
                        }
                        parametersOut.next();
                    }
                }
            } catch (ResultException ex) {
                log.error(ex);
            }
        }
    }

    private String getQueryFromPreConditions(Source source) {
        String query = source.getClassName() + "(?className) ";
        String implication = " IMP_CHAR sqwrl:select(?className";
        for (Property property : source.paramOut) {
            if (!property.getDataType().isEmpty()) {
                query += " AND_CHAR " + property.getDataType() + "(?className, ?" + property.getDataType() + "Data)";
                implication += ", ?" + property.getDataType() + "Data";
            }
        }
        return query + implication + ")";
    }

    private void addPreCondition(Source source) {
        boolean has = false;
        for (Source preCondition : preConditions) {
            if (preCondition.equals(source)) {
                has = true;
                break;
            }
        }
        if (!has) {
            this.preConditions.add(source);
        }
    }

    public String getJustify(Action action) {
        try {
            String query = this.SWRLQuery.getRuleFromJustify(action.getHeuristic());
            Result result = this.executeSWRLQuery(query);
            String justify = "";
            while (result.hasNext()) {
                justify = String.valueOf(result.getValue("?justificativa"));
                result.next();
            }
            justify = this.getJustifyDetails(action, justify);
            return justify;
        } catch (ResultException ex) {
            log.error(ex);
            return "Problemas ao gerar a justificativa. =(";
        }
    }

    public String getJustifyDetails(Action action, String justify) {
        try {
            String query = this.SWRLQuery.getRuleFromJustifyDetails(action.getName());
            Result result = this.executeSWRLQuery(query);
            while (result.hasNext()) {
                if (String.valueOf(result.getValue("?vm")).trim().equals(action.getCommand().trim())) {
                    if (justify.contains("<Consulta>")) {
                        justify = justify.replaceAll("<Consulta>", "<br><br>" + String.valueOf(result.getValue("?query")) + "<br><br>");
                    }
                    if (justify.contains("<Consulta.custoExecucao>")) {
                        justify = justify.replaceAll("<Consulta.custoExecucao>", String.valueOf(result.getValue("?custoReal")));
                    }
                    if (justify.contains("<VisaoMaterializada.custoEstimadoExecucao>")) {
                        justify = justify.replaceAll("<VisaoMaterializada.custoEstimadoExecucao>", String.valueOf(action.getCost_execution()));
                    }
                    if (justify.contains("<DLL Create View>")) {
                        justify = justify.replaceAll("<DLL Create View>", "<br><br>" + action.getCommand() + "<br><br>");
                    }
                    if (justify.contains("<VisaoMaterializada.ganhoExecucao>")) {
                        justify = justify.replaceAll("<VisaoMaterializada.ganhoExecucao>", String.valueOf(Float.valueOf(String.valueOf(result.getValue("?custoReal"))) - action.getCost_execution()));
                    }
                    break;
                }
                result.next();
            }
            return justify;
        } catch (ResultException ex) {
            log.error(ex);
            return "Problemas ao gerar a justificativa. =(";
        }
    }

}

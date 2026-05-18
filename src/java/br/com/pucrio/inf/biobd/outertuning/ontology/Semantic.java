/*
 * Framework para apoiar a sintonia fina de banco de dados. PUC-RIO.
 * Ana Carolina Almeida [anacrl@gmail.com].
 * Rafael Pereira de Oliveira [rpoliveira@inf.puc-rio.br].
 * PUC-RIO - LABBIO - 2014.
 */
package br.com.pucrio.inf.biobd.outertuning.ontology;

import br.com.pucrio.inf.biobd.outertuning.bib.base.Log;
import br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Concept;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Heuristic;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Property;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Rule;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.SQWRLQuery;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactory;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactoryException;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLImp;
import edu.stanford.smi.protegex.owl.swrl.parser.SWRLParseException;
import edu.stanford.smi.protegex.owl.swrl.parser.SWRLParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import org.protege.owl.portability.query.Result;
import org.protege.owl.portability.query.ResultException;
import org.protege.swrlapi.core.SWRLRuleEngine;
import org.protege.swrlapi.exceptions.SWRLRuleEngineException;
import org.protege.swrltab.p3.P3SWRLRuleEngineFactory;

/**
 *
 * @author Rafael
 */
public class Semantic {

    protected static OWLModel model;
    protected static SWRLRuleEngine rulesMachine;
    private static int counterIndividualsInstantiate = 0;

    protected SQWRLQuery SWRLQuery;
    private SWRLFactory factory;
    private ArrayList<Rule> disableRules;
    private ArrayList<Rule> enableRules;
    private final ArrayList<Heuristic> heuristics;
    protected final Configuration config;
    protected final Log log;

    public ArrayList<Heuristic> getAllHeuristics() {
        return heuristics;
    }

    public Semantic() {
        this.disableRules = new ArrayList<>();
        this.enableRules = new ArrayList<>();
        this.heuristics = new ArrayList<>();
        this.SWRLQuery = new SQWRLQuery();
        this.config = new Configuration();
        this.log = new Log(config);
    }

    public void readOntology() {
        this.startModelOWL();
        this.startMachineInference();
        if (config.getProperty("disable_rules").equals("0")) {
            this.readAllHeuristic();
        }
    }

    private void startModelOWL() {
        try {
            File file = new File(config.getProperty("path") + config.getProperty("fileOntology"));
            model = ProtegeOWL.createJenaOWLModelFromURI(file.toURI().toString());
            this.factory = new SWRLFactory(model);
            log.msg("file_read_successfully");
        } catch (OntologyLoadException e) {
            System.out.println(config.getProperty("fileOntology"));
            log.error(e);
        }
    }

    private void startMachineInference() {
        try {
            rulesMachine = P3SWRLRuleEngineFactory.create("Jess", model);
            rulesMachine.reset();
            rulesMachine.importSWRLRulesAndOWLKnowledge();
            if (config.getProperty("disable_rules").equals("0")) {
                this.disableAllRules();
            }
            rulesMachine.run();
            log.msg("rule_engine_started");
        } catch (SWRLRuleEngineException ex) {
            log.error(ex);
        }
    }

    private void disableAllRules() {
        try {
            this.disableRules = new ArrayList<>();
            this.enableRules = new ArrayList<>();
            Collection<SWRLImp> rulesEnablesFromFactory = this.factory.getEnabledImps();
            log.title("disable_all_rules");
            for (SWRLImp ruleEnable : rulesEnablesFromFactory) {
                Rule rule = new Rule(ruleEnable.getName(), this.getRuleComplete(ruleEnable));
                this.disableRules.add(rule);
                this.factory.deleteImp(rule.getName());
                log.msg(rule.getNameSimple());
            }
            log.msg("disable_all_rules");
        } catch (SWRLFactoryException ex) {
            log.error(ex);
        }
    }

    private String getRuleComplete(SWRLImp rule) {
        return rule.getBody().getBrowserText() + " " + SWRLParser.IMP_CHAR + " " + rule.getHead().getBrowserText();
    }

    private void readAllHeuristic() {
        try {
            Result result = this.executeSWRLQuery(this.SWRLQuery.getParameter("getNameAllHeuristics"));
            while (result.hasNext()) {
                Heuristic heuristic = new Heuristic();
                heuristic.setName(String.valueOf(result.getValue("?nome")));
                heuristic.setAuthor(String.valueOf(result.getValue("?autor")));
                heuristic.setStrategy(String.valueOf(result.getValue("?classificacao")));
                heuristic.setVersion(String.valueOf(result.getValue("?versao")));
                this.heuristics.add(heuristic);
                result.next();
            }
        } catch (ResultException ex) {
            log.error(ex);
        }
    }

    protected Result executeSWRLQuery(String query) {
        try {
            if (!this.isQueryInstantiated("SWRL" + query.hashCode())) {
                rulesMachine.createSQWRLQuery("SWRL" + query.hashCode(), query);
            }
            return rulesMachine.runSQWRLQuery("SWRL" + query.hashCode());
        } catch (ResultException ex) {
            log.msg(query);
            log.error(ex);
            return null;
        }
    }

    private boolean isQueryInstantiated(String nome) {
        try {
            Set<String> queries;
            queries = rulesMachine.getSQWRLQueryNames();
            for (String query : queries) {
                if (nome.equals(query)) {
                    return true;
                }
            }
        } catch (ResultException ex) {
            log.error(ex);
            return false;
        }
        return false;
    }

    private String getIdIndividual() {
        return String.format("%09d", ++counterIndividualsInstantiate);
    }

    protected String getNameIndividual(Concept concept) {
        if (!concept.getName().isEmpty()) {
            return concept.getName();
        } else {
            return this.infersTheNameBasedOnTheContent(concept);
        }
    }

    private String getNameHash(String name) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(name.getBytes());
            BigInteger hash = new BigInteger(1, md.digest());
            return hash.toString(16);
        } catch (NoSuchAlgorithmException ex) {
            log.error(ex);
        }
        return "";
    }

    protected void enableRule(String ruleName) {
        try {
            for (Rule rule : this.disableRules) {
                if ((rule.getName().contains(ruleName)) && (!this.enableRules.contains(rule))) {
                    log.msg("Rule enable: " + rule.getNameSimple());
                    this.factory.createImp(rule.getName(), rule.getClause());
                    this.enableRules.add(rule);
                }
            }
        } catch (SWRLParseException ex) {
            log.error(ex);
        }
    }

    public void printAllIndividualsConceptToDebug() {
        this.printAllConceptsInstantiated();
        if (config.getProperty("debug_concepts_query").equals("1")) {
            log.title("debug");
            try {
                File file = new File(config.getProperty("debug_concepts_query_file"));
                if (file.isFile() && file.getName().endsWith(".properties")) {
                    Properties queriesSWRLTemp = new Properties();
                    Properties queriesSWRL = new Properties();
                    InputStream targetStream = new FileInputStream(file);
                    queriesSWRLTemp.load(targetStream);
                    queriesSWRL.putAll(queriesSWRLTemp);
                    String queries[] = queriesSWRL.getProperty("getConceptToDebug").split(";");
                    int i = 0;
                    for (String query : queries) {
                        if (!query.isEmpty()) {
                            log.msg(this.SWRLQuery.replaceSimbolsOntology(query));
                            log.title("values");
                            Result result = this.executeSWRLQuery(this.SWRLQuery.replaceSimbolsOntology(query));
                            this.printResultSet(result);
                        }
                    }
                    targetStream.close();
                }

            } catch (Exception e) {
                log.error(e);
            }
            log.endTitle();
        }
    }

    public void printAllComandsDMLAndClause() {
        try {
            String dml = "";
            Result result = this.executeSWRLQuery(this.SWRLQuery.getParameter("getAllClausuleToDML"));
            while (result.hasNext()) {
                if (!dml.equals(result.getValue("?dml").toString())) {
                    dml = result.getValue("?dml").toString();
                }
                result.next();
            }
        } catch (ResultException ex) {
            log.error(ex);
        }
    }

    private String infersTheNameBasedOnTheContent(Concept concept) {
        String name = this.getIdIndividual();
        for (Property property : concept.paramOut) {
            switch (property.getDataType()) {
                case "temDescricao":
                case "temNome":
                case "temValorPredicadoLiteral":
                    name = property.getValue().toString();
                    break;
            }
        }
        return config.getProperty("prefixinferred") + "_" + concept.getClassName() + "_" + this.getNameHash(name);
    }

    private void printAllConceptsInstantiated() {
        if (config.getProperty("debug_all_concepts").equals("1")) {
            log.title("debug");
            String query = this.SWRLQuery.getParameter("getAllToDebug");
            Result result = this.executeSWRLQuery(query);
            this.printResultSet(result);
            log.endTitle();
        }
    }

    protected void printResultSet(Result result) {
        try {
            String fileLog = "";
            while (result.hasNext()) {
                for (String variable : result.getColumnNames()) {
                    if (!variable.equals("?all") || (variable.equals("?all") && (result.getValue(variable).toString().contains("OT_") || result.getValue(variable).toString().contains("INJECTED")))) {
                        log.msg(variable + ": " + result.getValue(variable).toString());
                        fileLog += variable + ";" + result.getValue(variable).toString() + "\n";
                    }
                }
                fileLog += "\n\n";
                result.next();
                if (result.hasNext() && result.getNumberOfColumns() > 1) {
                    log.msg("");
                }
            }
            log.writeFile("debug", fileLog);
        } catch (ResultException ex) {
            log.error(ex);
        }
    }

}

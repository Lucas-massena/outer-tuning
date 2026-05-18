/*
 * Framework para apoiar a sintonia fina de banco de dados. PUC-RIO.
 * Ana Carolina Almeida [anacrl@gmail.com].
 * Rafael Pereira de Oliveira [rpoliveira@inf.puc-rio.br].
 * PUC-RIO - LABBIO - 2014.
 */
package br.com.pucrio.inf.biobd.outertuning.dispatcher;

import br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Concept;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Source;
import br.com.pucrio.inf.biobd.outertuning.ontology.Semantic;
import org.protege.owl.portability.query.Result;
import org.protege.owl.portability.query.ResultException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class Dispatcher extends Semantic {

    public Dispatcher(Configuration configuration) {
        super(configuration);
    }

    private Object invokeMethodFromJar(String jarPath, String className, String methodName, Object params) {
        try {
            File f = new File(jarPath);
            URLClassLoader u = new URLClassLoader(new URL[]{f.toURI().toURL()});
            Class<?> c = u.loadClass(className);
            return getObject(methodName, params, c);
        } catch (InstantiationException | MalformedURLException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            log.msg(jarPath);
            log.msg(className);
            log.msg(methodName);
            log.msg(params);
            log.error(ex);
            return null;
        }
    }

    private Object invokeMethodFromClass(String className, String methodName, Object params) {
        try {
            Class<?> c = Class.forName(className);
            return getObject(methodName, params, c);
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            log.msg(className);
            log.msg(methodName);
            log.msg(params);
            log.error(ex);
            if (ex instanceof InvocationTargetException && ((InvocationTargetException) ex).getCause() != null) {
                Throwable cause = ((InvocationTargetException) ex).getCause();
                log.error("[CAUSE] " + cause.getClass().getName() + ": " + cause.getMessage());
                for (StackTraceElement el : cause.getStackTrace()) {
                    log.msg("  at " + el);
                }
            }
            return null;
        }
    }

    private Object getObject(String methodName, Object params, Class<?> c) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Method[] tp = c.getMethods();
        Class<?>[] paramsType = null;
        for (Method type : tp) {
            if (type.toString().contains(methodName)) {
                paramsType = type.getParameterTypes();
            }
        }
        Method met = c.getMethod(methodName, paramsType);
        Object obj = c.newInstance();
        return met.invoke(obj, params);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Concept> executeSource(Source source) {
        if (config.getProperty("debug_params_library").contains("1")) {
            log.msg(source.getHeuristic());
            log.msg(source.getClassName());
            log.msg(source.getClassJavaName());
            log.msg(source.getFunctionName());
            log.msg(source.getLibrary());
            log.msg("");
        }
        return (ArrayList<Concept>) this.invokeMethodFromClass(source.getClassJavaName(), source.getFunctionName(), this.setPreConditionIndividuals(source));
    }

    private Source setPreConditionIndividuals(Source source) {
        try {
            if (!this.SWRLQuery.getParameter("getAllPreCondition" + source.getClassName()).isEmpty()) {
                Result result = this.executeSWRLQuery(this.SWRLQuery.getParameter("getAllPreCondition" + source.getClassName()));
                if (result.hasNext()) {
                    source.paramIn = result;
                }
            }
        } catch (ResultException ex) {
            log.error(ex);
        }
        return source;
    }
}

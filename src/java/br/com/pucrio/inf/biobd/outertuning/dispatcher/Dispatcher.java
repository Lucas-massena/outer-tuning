/*
 * Framework para apoiar a sintonia fina de banco de dados. PUC-RIO.
 * Ana Carolina Almeida [anacrl@gmail.com].
 * Rafael Pereira de Oliveira [rpoliveira@inf.puc-rio.br].
 * PUC-RIO - LABBIO - 2014.
 */
package br.com.pucrio.inf.biobd.outertuning.dispatcher;

import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Concept;
import br.com.pucrio.inf.biobd.outertuning.bib.ontology.Source;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import br.com.pucrio.inf.biobd.outertuning.ontology.Semantic;
import org.protege.owl.portability.query.Result;
import org.protege.owl.portability.query.ResultException;

/**
 *
 * @author Rafael
 */
public class Dispatcher extends Semantic {

    private Object invokeMethodFromJar(String jarPath, String className, String methodName, Object params) {
        try {
            File f = new File(jarPath);
            URLClassLoader u = new URLClassLoader(new URL[]{f.toURI().toURL()});
            Class c = u.loadClass(className);
            Method[] tp = c.getMethods();
            Class<?>[] paramsType = null;
            for (Method type : tp) {
                if (type.toString().contains(methodName)) {
                    paramsType = type.getParameterTypes();
                }
            }
            Method met = c.getMethod(methodName, paramsType);
            Object obj = c.newInstance();
            Object result = met.invoke(obj, params);
            return result;
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
            Class c = (Class) Class.forName(className);
            Method[] tp = c.getMethods();
            Class<?>[] paramsType = null;
            for (Method type : tp) {
                if (type.toString().contains(methodName)) {
                    paramsType = type.getParameterTypes();
                }
            }
            Method met = c.getMethod(methodName, paramsType);
            Object obj = c.newInstance();
            Object result = met.invoke(obj, params);
            return result;
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            log.msg(className);
            log.msg(methodName);
            log.msg(params);
            log.error(ex);
            return null;
        }
    }

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

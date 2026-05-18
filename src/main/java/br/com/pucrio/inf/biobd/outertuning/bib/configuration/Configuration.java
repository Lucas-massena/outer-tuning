/*
 * Biblioteca de codigo fonte criada por Rafael Pereira
 * Proibido o uso sem autorizacao formal do autor
 *
 * rpoliveirati@gmail.com
 */
package br.com.pucrio.inf.biobd.outertuning.bib.configuration;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Configuration {

    public static final String PARAMETERS = "/parameters";

    private static Properties prop;

    public Configuration() {
        this.getPropertiesFromFile();
    }

    private void getPropertiesFromFile() {
        if (Configuration.prop == null) {
            try {
                Configuration.prop = new Properties();
                Properties propTemp = new Properties();
                URL resource = Configuration.class.getClassLoader().getResource(PARAMETERS);
                if (resource != null) {

                    File file = new File(resource.toURI());
                    if (file.isDirectory()) {
                        for (File f : Objects.requireNonNull(file.listFiles())) {
                            if (f.isDirectory()) {
                                continue;
                            }
                            propTemp.load(new FileInputStream(f));
                            propTemp.values().stream().map(Object::toString)
                                    .filter(s -> s.endsWith(".properties"))
                                    .map(this::readProp)
                                    .filter(Objects::nonNull)
                                    .forEach(prop::putAll);
                            prop.putAll(propTemp);
                        }
                    }
                }
            } catch (IOException | URISyntaxException e) {
                System.err.print(e);
            }
        }
    }

    private Properties readProp(String value) {
        try (InputStream is = Configuration.class.getClassLoader().getResourceAsStream(PARAMETERS + value)) {
            Properties temp = new Properties();
            temp.load(is);
            return temp;
        } catch (IOException e) {
            System.err.print(e);
            return null;
        }
    }

    public URI getResourceURI(String property) {
        try {

            URL resource = Configuration.class.getClassLoader().getResource( property);
            return resource != null ? resource.toURI() : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public String clearNameFile(String nameFile) {
        String name = nameFile.toLowerCase().replace("http://", "");
        name = name.toLowerCase().replace("/", ".");
        name = name.toLowerCase().replace("?", "_");
        name = name.toLowerCase().replace(" ", "_");
        name = name.replaceAll("[^\\p{ASCII}]", "");
        return name;
    }

    protected String removerNl(String frase) {
        String padrao = "\\s{2,}";
        Pattern regPat = Pattern.compile(padrao);
        Matcher matcher = regPat.matcher(frase);
        String res = matcher.replaceAll(" ").trim();
        return res.replaceAll("(\n|\r)+", " ");
    }

    public void setProperty(String key, String value) {
        prop.setProperty(key, value);
    }

    public String getProperty(String key) {
        return prop.getProperty(key);
    }

    public boolean containsKey(String key) {
        return prop.containsKey(key);
    }

}

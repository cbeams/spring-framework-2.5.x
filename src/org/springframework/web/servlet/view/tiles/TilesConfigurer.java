package org.springframework.web.servlet.view.tiles;

import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.context.WebApplicationContext;
import org.apache.struts.tiles.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>Helper class to configure Tiles for the Spring framework (see
 * <a href="http://jakarta.apache.org/struts">http://jakarta.apache.org/struts</a>
 * for more information about Tiles, which basically is a templating mechanism
 * for web applications).</p>
 * <p>The TilesConfigurer simply configures tiles using a set of files containing
 * definitions. The rest is done by an appropriate Resolver which could for instance
 * be the {@link org.springframework.web.servlet.view.InternalResourceViewResolver
 * InternalResourceViewResolver} or the
 * {@link org.springframework.web.servlet.view.ResourceBundleViewResolver
 * ResourceBundleViewResolver}. Usage of the TilesConfigurer is done as follows:
 * </p>
 *
 * <p>
 * <pre>
 *  &lt;bean id="tilesConfigurer" class="org.springframework.web.servlet.view.tiles.TilesConfigurer"&gt;
 *       &lt;property name="factoryClass"&gt;
 *           &lt;value&gt;org.apache.struts.tiles.xmlDefinition.I18nFactorySet&lt;/value&gt;
 *       &lt;/property&gt;
 *       &lt;property name="definitions"&gt;
 *           &lt;list&gt;
 *               &lt;value&gt;/WEB-INF/defs/general.xml&lt;/value&gt;
 *               &lt;value&gt;/WEB-INF/defs/widgets.xml&lt;/value&gt;
 *               &lt;value&gt;/WEB-INF/defs/administrator.xml&lt;/value&gt;
 *               &lt;value&gt;/WEB-INF/defs/customer.xml&lt;/value&gt;
 *               &lt;value&gt;/WEB-INF/defs/templates.xml&lt;/value&gt;
 *           &lt;/list&gt;
 *       &lt;/property&gt;
 *  &lt;/bean&gt;
 * </pre>
 * </p>
 * <p>The values in nthe list are the actual files containing the definitions.
 * @see org.springframework.web.servlet.view.tiles.TilesView
 * @author Alef Arendsen
 */
public class TilesConfigurer extends ApplicationObjectSupport {

	/** Logger */
	private final static Log log = LogFactory.getLog(TilesConfigurer.class);
    /** factoryclass  for tiles */
    private String factoryClass;
    /** definition url's mapped to descriptions */
    private List definitions;
    /** parse validation for tiles */
    private boolean validateDefinitions = false;

    /**
     * Nothing really
     */
    public TilesConfigurer() {
        super();
    }

    /**
     * Bean setter for the definitions (definition files)
     * @param definitions the files containing the definitions
     */
    public void setDefinitions(List definitions) {
        this.definitions = definitions;
    }

    /**
     * Bean setter for the factoryClass. One possible factory is the
     * {@link org.apache.struts.tiles.xmlDefinition.I18nFactorySet}.
     * @param factoryClass the factoryClass to use for Tiles
     */
    public void setFactoryClass(String factoryClass) {
        this.factoryClass = factoryClass;
    }

    /**
     * Bean setter for parser validation for tiles
     * @param validateDefinitions <code>true</code> to validate,
     * <code>false</code> otherwise
     */
    public void setValidateDefinitions(boolean validateDefinitions) {
        this.validateDefinitions = validateDefinitions;
    }

    /**
     * Initialization of the applicationcontext and the actual tiles definition
     * factory.
     * @throws ApplicationContextException if an error occurs or the
     * TilesConfigurer isn't defined in the WebApplicationContext
     */
    protected void initApplicationContext() throws ApplicationContextException {
        try {
            initDefinitionsFactory();
        } catch (DefinitionsFactoryException e) {
            throw new ApplicationContextException(
                    "Failed to initialize definitionsfactory", e);
        }
    }

    /**
     * The actual initialization of the definitions factory
     * @return the definitions factory that has been initialized
     * @throws DefinitionsFactoryException
     */
    private synchronized DefinitionsFactory initDefinitionsFactory()
    throws DefinitionsFactoryException, ApplicationContextException {

        log.info("Tiles: initializing");
        // initialize the definitionsfactoryconfiguration
        DefinitionsFactoryConfig factoryConfig = new DefinitionsFactoryConfig();
        factoryConfig.setParserValidate(this.validateDefinitions);
        factoryConfig.setFactoryClassname(factoryClass);
        StringBuffer buffer = new StringBuffer();
        if (definitions == null) {
            definitions = new ArrayList();
        }
        Iterator it = definitions.iterator();
        log.info("Tiles: adding definitions");
        while (it.hasNext()) {
            String s = (String)it.next();
            log.info("Tiles: initializing definitions file " + s);
            buffer.append(s);
            if (it.hasNext()) {
                buffer.append(",");
            }
        }
        factoryConfig.setDefinitionConfigFiles(buffer.toString());

        ApplicationContext aCtx = getApplicationContext();
        if (!(aCtx instanceof WebApplicationContext)) {
            throw new ApplicationContextException("TilesConfigurer may only" +
                    " be used in WebApplicationContext (****-servlet.xml");
        }
        WebApplicationContext wac = (WebApplicationContext)aCtx;
        ServletContext sContext = wac.getServletContext();
        // initialize the definitionfactory
        DefinitionsFactory factory =
                TilesUtil.createDefinitionsFactory(sContext, factoryConfig);
        sContext.setAttribute(TilesUtilImpl.DEFINITIONS_FACTORY, factory);
        log.info("Tiles: initialization done");
        return factory;
    }

}

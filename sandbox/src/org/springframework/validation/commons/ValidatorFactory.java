package org.springframework.validation.commons;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.Form;
import org.apache.commons.validator.Validator;
import org.apache.commons.validator.ValidatorResources;
import org.apache.commons.validator.ValidatorResourcesInitializer;
import org.springframework.core.io.Resource;
import org.springframework.validation.Errors;

/**
 * Factory that creates initialized instances of the commons validator.
 * <p>
 * The following properties must be initialized and the <code>init()</code>
 * method must be executed before the <code>getValidator()</code> or
 * <code>hasRulesForBean()</code> methods may be invoked:
 * </p>
 * <ul>
 * <li>resources</li>
 * </ul>
 * <p>
 * Alternately, an array of <code>Resources</code> may be supplied to the
 * single-argument constructor.
 * 
 * @author Daniel Miller
 */
public class ValidatorFactory implements Serializable {
    private static final String ERRORS_KEY = "org.springframework.validation.Errors";
    private static Log log = LogFactory.getLog(ValidatorFactory.class);

    private ValidatorResources validatorResources;
    private Resource[] resources;

    public ValidatorFactory() {
    }

    public ValidatorFactory(Resource[] resources) {
        this.resources = resources;
        try {
            this.init();
        } catch (IOException e) {
            log.warn("Could not initialize ValidatorFactory", e);
        }
    }

    /**
     * Initialize this ValidatorFactory instance. Initialize the
     * <code>validatorResources</code> object with <code>Resources</code>,
     * which have already been set using <code>setResources</code> or as an
     * argument to the constructor.
     */
    public void init() throws IOException {
        validatorResources = new ValidatorResources();
        for (int i = 0; i < resources.length; i++) {
            InputStream in = resources[i].getInputStream();
            if (in != null) {
                BufferedInputStream bin = new BufferedInputStream(in);
                try {
                    ValidatorResourcesInitializer.initialize(
                            validatorResources, bin, false);
                } catch (IOException e) {
                    log.error("Error loading validation rules in file: "
                            + resources[i].getFile().getName(), e);
                } finally {
                    bin.close();
                }
            } else {
                log.info("Skipping rules file '"
                        + resources[i].getFile().getName()
                        + "'. Stream was null.");
            }
        }
        validatorResources.process();
        this.resources = null; // Release resources
    }

    /**
     * Gets a new instance of a validator for the given bean (form).
     * 
     * @param beanName
     *            The name of the bean for which this validator will be created.
     */
    public Validator getValidator(String beanName, Object bean, Errors errors) {
        Validator validator = new Validator(validatorResources, beanName);
        validator.addResource(ERRORS_KEY, errors);
        validator.addResource(Validator.BEAN_KEY, bean);
        return validator;
    }

    /**
     * @param resources
     *            The resources to set.
     */
    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    /**
     * Returns true if this validator factory can create a validator that
     * supports the given <code>beanName</code> and <code>locale</code>.
     * 
     * @param beanName
     *            String name of the bean to be validated.
     * @param locale
     *            Locale of the validator to create.
     * @return true if this validator factory can create a validator for the
     *         given bean name, false otherwise.
     */
    public boolean hasRulesForBean(String beanName, Locale locale) {
        Form form = validatorResources.get(locale, beanName);
        return (form != null ? true : false);
    }

    /**
     * @return Returns the wrapped commons validatorResources.
     */
    public ValidatorResources getValidatorResources() {
        return validatorResources;
    }

    /**
     * @param validatorResources
     *            The validatorResources to set.
     */
    public void setValidatorResources(ValidatorResources validatorResources) {
        this.validatorResources = validatorResources;
    }
}
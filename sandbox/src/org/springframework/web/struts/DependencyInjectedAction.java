/*
 * Created on 19-Nov-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */

package org.springframework.web.struts;

import org.apache.struts.action.ActionServlet;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * Superclass for Struts actions needing access to a Spring-managed middle tier.
 * Looks up the Spring WebApplicationContext via the ServletContext, and
 * autowires this object by type. This enables us to you Dependency Injection on
 * subclasses as if they were actually managed by Spring.
 * @author Rod Johnson
 */
public abstract class DependencyInjectedAction extends TemplateAction {

    /**
     * Save the web application context and set properties by dependency
     * injection. Any Setter methods will be populated
     */
    public void setServlet(ActionServlet actionServlet) {
        super.setServlet(actionServlet);

        // ActionServlet may be null when an application is closed
        // down before reload, especially in WebLogic
        if (actionServlet != null) {
            Assert.state((getWebApplicationContext() instanceof ConfigurableWebApplicationContext),
                    "Cannot use this Action except with a ConfigurableWebApplicationContext");

            // Now we can autowire ourselves
            ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext)getWebApplicationContext();
            AutowireCapableBeanFactory acbf = cwac.getBeanFactory();

            // We can't perform dependency checking because of the inherited
            // setServlet method (this method)
            acbf.autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        }
    }

}
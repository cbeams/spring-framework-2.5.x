/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
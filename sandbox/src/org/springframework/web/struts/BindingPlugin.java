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

import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

public class BindingPlugin implements PlugIn {
    static final Logger logger = Logger.getLogger(BindingPlugin.class.getName());

    public void init(ActionServlet servlet, ModuleConfig config) throws ServletException {
        ConvertUtilsBean convUtils = new ConvertUtilsBean();
        PropertyUtilsBean propUtils = new BindingAwarePropertyUtilsBean();
        BeanUtilsBean beanUtils = new BeanUtilsBean(convUtils, propUtils);
        BeanUtilsBean.setInstance(beanUtils);
    }

    public void destroy() {
    }
}


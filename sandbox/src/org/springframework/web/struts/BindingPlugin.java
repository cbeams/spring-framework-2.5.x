/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
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


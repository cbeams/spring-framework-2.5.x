/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Keith Donald
 */
public interface ActionBean {
    public ActionBeanEvent execute(HttpServletRequest request, HttpServletResponse response,
            MutableAttributesAccessor model) throws RuntimeException;
}
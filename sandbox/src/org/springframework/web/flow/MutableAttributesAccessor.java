/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.util.Map;

/**
 * @author Keith Donald
 */
public interface MutableAttributesAccessor extends AttributesAccessor {
    public void setAttribute(String attributeName, Object attributeValue);

    public void setAttributes(Map attributes);
}
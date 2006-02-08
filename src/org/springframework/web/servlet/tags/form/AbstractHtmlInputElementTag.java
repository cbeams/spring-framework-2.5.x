/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.servlet.tags.form;

import org.springframework.util.ObjectUtils;

import javax.servlet.jsp.JspException;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AbstractHtmlInputElementTag extends AbstractHtmlElementTag {

    protected String onfocus;

    protected String onblur;

    protected String onchange;

    protected String accesskey;

    public static final String ONFOCUS_ATTRIBUTE = "onfocus";

    public static final String ONBLUR_ATTRIBUTE = "onblur";

    public static final String ONCHANGE_ATTRIBUTE = "onchange";

    public static final String ACCESSKEY_ATTRIBUTE = "accesskey";

    public void setOnfocus(String onfocus) {
        this.onfocus = onfocus;
    }

    public void setOnblur(String onblur) {
        this.onblur = onblur;
    }

    public void setOnchange(String onchange) {
        this.onchange = onchange;
    }

    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }

    protected void writeDefaultAttributes(TagWriter tagWriter) throws JspException {
        super.writeDefaultAttributes(tagWriter);
        writeOptionalAttribute(tagWriter, ONFOCUS_ATTRIBUTE, this.onfocus);
        writeOptionalAttribute(tagWriter, ONBLUR_ATTRIBUTE, this.onblur);
        writeOptionalAttribute(tagWriter, ONCHANGE_ATTRIBUTE, this.onchange);
        writeOptionalAttribute(tagWriter, ACCESSKEY_ATTRIBUTE, this.accesskey);
    }

    /**
     * Checks to see whether or not the value of this tag is the active value, or the
     * value to which this tag is bound.
     * <p/>Performs a two stage comparison, first comparing objects using
     * {@link org.springframework.util.ObjectUtils#nullSafeEquals} and then finally comparing the <code>String</code>
     * values. This helps when object types might be statically incompatible but logically
     * equal in certain cases (i.e. <code>String</code> and <code>Character</code>).
     */
    protected boolean isActiveValue(Object resolvedValue) throws JspException {
        Object boundValue = getValue();
        boolean equal = ObjectUtils.nullSafeEquals(resolvedValue, boundValue);

        if (!equal && (resolvedValue != null && boundValue != null)) {
            equal = resolvedValue.toString().equals(boundValue.toString());
        }
        return equal;
    }
}

package org.springframework.web.servlet.tags;

import org.springframework.web.util.ExpressionEvaluationUtils;
import java.beans.PropertyEditor;
import java.io.IOException;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.web.servlet.tags.RequestContextAwareTag;

/**
 * Tag useful for transforming reference data values from formcontroller and
 * other objects inside a <code>spring:bind</code> tag. The bind tag has a PropertyEditor 
 * that it used to transform the property of bean to a String, useable in HTML forms. 
 * This tag uses that PropertyEditor to transform objects passed into this tag.
 * @author Alef Arendsen
 * @sinces 1.0
 */
public class TransformTag extends RequestContextAwareTag{

	/** the value to transform using the appropriate property editor */
	private Object value;
	/** the variable to put the result in */
	private String var;
	/** the scope of the variable the result will be put in */
	private int scope;

    /** Constructor, setting the scope to default (PAGE) */
    public TransformTag() {
        scope = PageContext.PAGE_SCOPE;
    }

    /**
     * Sets the value to finally transform using the appropriate property editor
     * that has to be found in the BindTag
     * @param value the value
     * @throws JspException if expression evaluation fails
     */
    public void setValue(String value)
    throws JspException {
        if(ExpressionEvaluationUtils.isExpressionLanguage(value)) {
            this.value = ExpressionEvaluationUtils.evaluate(
				"value", value, java.lang.Object.class, pageContext);			
        } else {
        	this.value = value;
        }
    }

	/**
	 * Sets the scope to which to export the variable indicated. If the scope isn't one
	 * of the allowed scope, it'll be the default (PAGE)
	 * @param scope the scope (PAGE, REQUEST, APPLICATION or SESSION)
	 */
    public void setScope(String scope) 
    throws JspException {
		String tmpScope = null;
		if(ExpressionEvaluationUtils.isExpressionLanguage(scope)) {
			tmpScope = ExpressionEvaluationUtils.evaluateString(
				"scope", scope, pageContext);
		} else {
			tmpScope = scope;
		}		
        this.scope = TagUtils.getScope(tmpScope);
    }

    /**
     * Sets the name of the variable to which to export the result of the transformation
     * @param var the name of the variable
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Tag implementation
     * @return what to do next
     */
    public int doStartTagInternal()
    throws JspException {

        if(value != null) {
        	// find the bingtag (if applicable)
            BindTag tag = (BindTag)TagSupport.findAncestorWithClass(this, org.springframework.web.servlet.tags.BindTag.class);
            if(tag == null) {
                // the tag can only be used inside a bind tag
                throw new JspException("TransformTag can only be used within BindTag");
            }
            // ok, get the property editor
            PropertyEditor editor = tag.getEditor();
            String result = null;
            if(editor != null) {
            	// if an editor was found, edit the value
                editor.setValue(value);
                result = editor.getAsText();
            } else {
                // else, just do a toString
                result = value.toString();
            }
            if(var != null) {
                // if var is set?
                pageContext.setAttribute(var, result, scope);
            } else {
                try {
                	// else, just print it out
                    pageContext.getOut().print(result);
                } catch(IOException e) {
                    throw new JspException(e);
                }
            }   
        }
        return SKIP_BODY;
    }

    /**
     * Releasing of resources
     */
    public void release() {
        scope = PageContext.PAGE_SCOPE;
        var = null;
        value = null;
    }


}

package org.springframework.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.springframework.beans.factory.BeanNameAware;

/**
 * @author Juergen Hoeller
 * @since 09.04.2004
 */
public class TestAction extends Action implements BeanNameAware {

	private String beanName;

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
															 HttpServletRequest request, HttpServletResponse response){
		return new ActionForward(this.beanName);
	}

}

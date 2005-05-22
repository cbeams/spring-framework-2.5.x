package org.springframework.web.flow.config;

import junit.framework.TestCase;

import org.springframework.mock.web.flow.MockRequestContext;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.ViewDescriptorCreator;

public class TextToViewDescriptorCreatorTests extends TestCase {
	public void testStaticView() {
		TextToViewDescriptorCreator converter = new TextToViewDescriptorCreator();
		ViewDescriptorCreator creator = (ViewDescriptorCreator)converter.convert("myView");
		RequestContext context = getRequestContext();
		ViewDescriptor view = creator.createViewDescriptor(context);
		assertEquals("myView", view.getViewName());
		assertEquals(5, view.getModel().size());
	}
	
	public void testRedirectView() {
		TextToViewDescriptorCreator converter = new TextToViewDescriptorCreator();
		ViewDescriptorCreator creator = (ViewDescriptorCreator)converter.convert("redirect:myView?foo=${flowScope.foo}&bar=${requestScope.oven}");
		RequestContext context = getRequestContext();
		ViewDescriptor view = creator.createViewDescriptor(context);
		assertEquals("myView", view.getViewName());
		assertEquals(2, view.getModel().size());
	}
	
	public void testCustom() {
		
	}
	
	private RequestContext getRequestContext() {
		MockRequestContext ctx = new MockRequestContext();
		ctx.getFlowScope().setAttribute("foo", "bar");
		ctx.getFlowScope().setAttribute("bar", "car");
		ctx.getRequestScope().setAttribute("oven", "mit");
		ctx.getRequestScope().setAttribute("cat", "woman");
		ctx.getFlowScope().setAttribute("boo", new Integer(3));
		ctx.setLastEvent(new Event(this, "sample"));
		return ctx;
	}
}

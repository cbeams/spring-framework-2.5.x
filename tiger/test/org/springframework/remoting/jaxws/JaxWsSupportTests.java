package org.springframework.remoting.jaxws;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Created by IntelliJ IDEA.
 * User: Jürgen
 * Date: 28.09.2007
 * Time: 00:02:28
 * To change this template use File | Settings | File Templates.
 */
public class JaxWsSupportTests extends TestCase {

	public void testJaxWsPortAccess() {
		GenericApplicationContext ac = new GenericApplicationContext();

		GenericBeanDefinition serviceDef = new GenericBeanDefinition();
		serviceDef.setBeanClass(OrderServiceImpl.class);
		ac.registerBeanDefinition("service", serviceDef);

		GenericBeanDefinition exporterDef = new GenericBeanDefinition();
		exporterDef.setBeanClass(SimpleJaxWsServiceExporter.class);
		exporterDef.getPropertyValues().addPropertyValue("baseAddress", "http://localhost:9999/");
		ac.registerBeanDefinition("exporter", exporterDef);

		GenericBeanDefinition clientDef = new GenericBeanDefinition();
		clientDef.setBeanClass(JaxWsPortProxyFactoryBean.class);
		clientDef.getPropertyValues().addPropertyValue("wsdlDocumentUrl", "http://localhost:9999/OrderService?wsdl");
		clientDef.getPropertyValues().addPropertyValue("namespaceUri", "http://jaxws.remoting.springframework.org/");
		clientDef.getPropertyValues().addPropertyValue("serviceName", "OrderService");
		clientDef.getPropertyValues().addPropertyValue("serviceInterface", OrderService.class);
		clientDef.getPropertyValues().addPropertyValue("lookupServiceOnStartup", Boolean.FALSE);
		ac.registerBeanDefinition("client", clientDef);

		try {
			ac.refresh();
			OrderService orderService = (OrderService) ac.getBean("client", OrderService.class);
			try {
				String order = orderService.getOrder(1000);
				assertEquals("order 1000", order);
			}
			finally {
				ac.close();
			}
		}
		catch (UnsupportedOperationException ex) {
			// ignore - probably running on JDK < 1.6
		}
	}

}

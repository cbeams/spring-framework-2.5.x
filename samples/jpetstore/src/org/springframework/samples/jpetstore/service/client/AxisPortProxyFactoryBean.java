package org.springframework.samples.jpetstore.service.client;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

import org.springframework.remoting.jaxrpc.JaxRpcPortProxyFactoryBean;
import org.springframework.samples.jpetstore.domain.Item;
import org.springframework.samples.jpetstore.domain.LineItem;
import org.springframework.samples.jpetstore.domain.Order;
import org.springframework.samples.jpetstore.domain.Product;

/**
 * Axis-specific subclass of JaxRpcPortProxyFactoryBean that registers bean
 * mappings for JPetStore's domain objects. The same mappings are also
 * registered at the server, in Axis' "server-config.wsdd" file.
 *
 * <p>Note: Without such explicit bean mappings, a complex type like Order
 * cannot be transferred via SOAP.
 *
 * @author Juergen Hoeller
 * @since 27.12.2003
 * @see org.apache.axis.encoding.ser.BeanDeserializerFactory
 * @see org.apache.axis.encoding.ser.BeanSerializerFactory
 */
public class AxisPortProxyFactoryBean extends JaxRpcPortProxyFactoryBean {

	private String namespace;

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	protected void postProcessJaxRpcService(Service service) {
		TypeMappingRegistry registry = service.getTypeMappingRegistry();
		TypeMapping mapping = registry.createTypeMapping();
		registerBeanMapping(mapping, Product.class, "Product");
		registerBeanMapping(mapping, Item.class, "Item");
		registerBeanMapping(mapping, LineItem.class, "LineItem");
		registerBeanMapping(mapping, Order.class, "Order");
		registry.register("http://schemas.xmlsoap.org/soap/encoding/", mapping);
	}

	protected void registerBeanMapping(TypeMapping mapping, Class type, String name) {
		QName qName = new QName(this.namespace, name);
		mapping.register(type, qName,
		    new BeanSerializerFactory(type, qName),
		    new BeanDeserializerFactory(type, qName));
	}

}

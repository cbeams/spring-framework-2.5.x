package org.springframework.samples.jpetstore.service.client;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

import org.springframework.remoting.jaxrpc.JaxRpcServicePostProcessor;
import org.springframework.samples.jpetstore.domain.Item;
import org.springframework.samples.jpetstore.domain.LineItem;
import org.springframework.samples.jpetstore.domain.Order;
import org.springframework.samples.jpetstore.domain.Product;

/**
 * Axis-specific JaxRpcServicePostProcessor that registers bean mappings
 * for JPetStore's domain objects. The same mappings are also registered
 * at the server, in Axis' "server-config.wsdd" file.
 *
 * <p>Note: Without such explicit bean mappings, a complex type like
 * <code>org.springframework.samples.jpetstore.domain.Order</code>
 * cannot be transferred via SOAP.
 *
 * @author Juergen Hoeller
 * @since 1.1.4
 * @see org.springframework.samples.jpetstore.domain.Order
 * @see org.springframework.samples.jpetstore.domain.LineItem
 * @see org.springframework.samples.jpetstore.domain.Item
 * @see org.springframework.samples.jpetstore.domain.Product
 * @see org.apache.axis.encoding.ser.BeanDeserializerFactory
 * @see org.apache.axis.encoding.ser.BeanSerializerFactory
 */
public class BeanMappingServicePostProcessor implements JaxRpcServicePostProcessor {

	/**
	 * Default encoding style URI, as suggested by the JAX-RPC javadoc:
	 * "http://schemas.xmlsoap.org/soap/encoding/"
	 * @see javax.xml.rpc.encoding.TypeMappingRegistry#register
	 */
	public static final String DEFAULT_ENCODING_STYLE_URI = "http://schemas.xmlsoap.org/soap/encoding/";

	/**
	 * Default namespace to use for custom XML types.
	 * @see javax.xml.rpc.encoding.TypeMapping#register
	 */
	public static final String DEFAULT_TYPE_NAMESPACE_URI = "urn:JPetStore";


	private String encodingStyleUri = DEFAULT_ENCODING_STYLE_URI;

	private String typeNamespaceUri = DEFAULT_TYPE_NAMESPACE_URI;


	/**
	 * Set the encoding style URI to use for the type mapping.
	 * @see javax.xml.rpc.encoding.TypeMappingRegistry#register
	 */
	public void setEncodingStyleUri(String encodingStyleUri) {
		this.encodingStyleUri = encodingStyleUri;
	}

	/**
	 * Set the namespace to use for custom XML types.
	 * @see javax.xml.rpc.encoding.TypeMapping#register
	 */
	public void setTypeNamespaceUri(String typeNamespaceUri) {
		this.typeNamespaceUri = typeNamespaceUri;
	}


	public void postProcessJaxRpcService(Service service) {
		TypeMappingRegistry registry = service.getTypeMappingRegistry();
		TypeMapping mapping = registry.createTypeMapping();
		registerBeanMapping(mapping, Order.class, "Order");
		registerBeanMapping(mapping, LineItem.class, "LineItem");
		registerBeanMapping(mapping, Item.class, "Item");
		registerBeanMapping(mapping, Product.class, "Product");
		registry.register(this.encodingStyleUri, mapping);
	}

	protected void registerBeanMapping(TypeMapping mapping, Class type, String name) {
		QName xmlType = new QName(this.typeNamespaceUri, name);
		mapping.register(type, xmlType,
		    new BeanSerializerFactory(type, xmlType),
		    new BeanDeserializerFactory(type, xmlType));
	}

}

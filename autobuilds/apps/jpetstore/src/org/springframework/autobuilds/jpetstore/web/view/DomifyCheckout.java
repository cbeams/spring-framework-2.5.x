/*
 * DomifyCheckout.java
 * 
 * Created Feb 17, 2004
 */
package org.springframework.autobuilds.jpetstore.web.view;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.DOMOutputter;
import org.springframework.autobuilds.jpetstore.domain.*;
import org.springframework.web.servlet.view.xslt.AbstractXsltView;
import org.springframework.web.servlet.view.xslt.FormatHelper;
import org.w3c.dom.Node;


/**
 * DomifyCheckout
 * 
 * @author darren davison
 */
public class DomifyCheckout extends AbstractXsltView {

    /**
     * domify the cart model data for display in the checkout page to look like..
     * 
     * <cart subTotal="130.00">
     *   <cartItem inStock="Y" quantity="2" totalPrice="40.00">
     *     <item id="123" listPrice="20.00">
     *       <attribute>big</attribute>
     *       <attribute>yellow</attribute>
     *       <attribute>cheap</attribute>
     *       <productName>koi carp</productName>
     *     </item>
     *   </cartItem>
     *   <cartItem inStock="N" quantity="3" totalPrice="90.00">
     *     <item id="456" listPrice="30.00">
     *       ...
     *     </item>
     *   </cartItem>
     * </cart>
     * 
     * @see org.springframework.web.servlet.view.xslt.AbstractXsltView#createDomNode(java.util.Map, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected Node createDomNode(
        Map map,
        String rootName,
        HttpServletRequest req,
        HttpServletResponse resp)
        throws Exception {
        
        Locale locale = Locale.US;
        
		// build it with jdom then convert to w3c (to keep our sanity)
        Document doc = new Document();
        Element root = new Element(rootName);
        doc.setRootElement(root);
        
        Cart cart = (Cart) map.get("cart");
        root.setAttribute("subTotal", FormatHelper.currency(cart.getSubTotal(), locale));
        
        // item list for this page
		Iterator items = cart.getAllCartItems();
		while (items.hasNext()) {
			CartItem ci = (CartItem) items.next();
			Element ciXml = new Element("cartItem");
			root.addContent(ciXml);
			ciXml.setAttribute("inStock", ci.isInStock() ? "Y" : "N");
			ciXml.setAttribute("quantity", String.valueOf(ci.getQuantity()));
			ciXml.setAttribute("totalPrice", FormatHelper.currency(ci.getTotalPrice(), locale));
			
			Item item = ci.getItem();
			Element itemXml = new Element("item");
			ciXml.addContent(itemXml);
			itemXml.setAttribute("id", item.getItemId());
			itemXml.setAttribute("listPrice", FormatHelper.currency(item.getListPrice(), locale));
			if (item.getAttribute1() != null) {
				Element attrib = new Element("attribute");
				itemXml.addContent(attrib);
				attrib.setText(item.getAttribute1());
			}
			if (item.getAttribute2() != null) {
				Element attrib = new Element("attribute");
				itemXml.addContent(attrib);
				attrib.setText(item.getAttribute2());
			}
			if (item.getAttribute3() != null) {
				Element attrib = new Element("attribute");
				itemXml.addContent(attrib);
				attrib.setText(item.getAttribute3());
			}
			if (item.getAttribute4() != null) {
				Element attrib = new Element("attribute");
				itemXml.addContent(attrib);
				attrib.setText(item.getAttribute4());
			}
			if (item.getAttribute5() != null) {
				Element attrib = new Element("attribute");
				itemXml.addContent(attrib);
				attrib.setText(item.getAttribute5());
			}
			
			Element prod = new Element("productName");
			itemXml.addContent(prod);
			prod.setText(item.getProduct().getName());

		}
        
        // convert & return
        return new DOMOutputter().output(doc);
    }

}

/*
 ************************************
 * XMLC GENERATED CODE, DO NOT EDIT *
 ************************************
 */
package org.springframework.web.servlet.view.xmlc;

import org.w3c.dom.*;
import org.enhydra.xml.xmlc.dom.XMLCDomFactory;

/**
 * XMLC Document class, generated from
 * test/org/springframework/web/servlet/view/xmlc/test.html
 */
public class TestXmlObject extends org.enhydra.xml.xmlc.html.HTMLObjectImpl implements org.enhydra.xml.xmlc.XMLObject, org.enhydra.xml.xmlc.html.HTMLObject {
    private int $elementId_test = 6;

    private org.enhydra.xml.lazydom.html.LazyHTMLElement $element_Test;

    /**
     * Field that is used to identify this as the XMLC generated class
     * in an inheritance chain. Contains a reference to the class object.
     */
    public static final Class XMLC_GENERATED_CLASS = TestXmlObject.class;

    /**
     * Field containing CLASSPATH relative name of the source file
     * that this class can be regenerated from.
     */
    public static final String XMLC_SOURCE_FILE = "org/springframework/web/servlet/view/xmlc/test.html";

    /**
     * XMLC DOM factory associated with this class.
     */
    private static final org.enhydra.xml.xmlc.dom.XMLCDomFactory fDOMFactory = org.enhydra.xml.xmlc.dom.XMLCDomFactoryCache.getFactory(org.enhydra.xml.xmlc.dom.lazydom.LazyHTMLDomFactory.class);

    /**
     * Lazy DOM document
     */
    private org.enhydra.xml.lazydom.LazyDocument fLazyDocument;

    /**
     * Options used to preformat the document when compiled
     */
    private static final org.enhydra.xml.io.OutputOptions fPreFormatOutputOptions;

    /**
     * Template document shared by all instances.
     */
    private static final org.enhydra.xml.lazydom.TemplateDOM fTemplateDocument;

    /*
     * Class initializer.
     */
    static {
        org.enhydra.xml.lazydom.html.LazyHTMLDocument document = buildTemplateSubDocument();
        fTemplateDocument = new org.enhydra.xml.lazydom.TemplateDOM(document);
        fPreFormatOutputOptions = new org.enhydra.xml.io.OutputOptions();
        fPreFormatOutputOptions.setFormat(org.enhydra.xml.io.OutputOptions.FORMAT_AUTO);
        fPreFormatOutputOptions.setEncoding("ISO-8859-1");
        fPreFormatOutputOptions.setPrettyPrinting(false);
        fPreFormatOutputOptions.setEnableXHTMLCompatibility(false);
        fPreFormatOutputOptions.setUseAposEntity(true);
        fPreFormatOutputOptions.setIndentSize(4);
        fPreFormatOutputOptions.setPreserveSpace(true);
        fPreFormatOutputOptions.setOmitXMLHeader(false);
        fPreFormatOutputOptions.setOmitDocType(false);
        fPreFormatOutputOptions.setOmitEncoding(false);
        fPreFormatOutputOptions.setDropHtmlSpanIds(false);
        fPreFormatOutputOptions.setOmitAttributeCharEntityRefs(false);
        fPreFormatOutputOptions.setPublicId(null);
        fPreFormatOutputOptions.setSystemId(null);
        fPreFormatOutputOptions.setMIMEType(null);
        fPreFormatOutputOptions.markReadOnly();
    }

    /**
     * Default constructor.
     */
    public TestXmlObject() {
        buildDocument();
    }

    /**
     * Constructor with optional building of the DOM.
     */
    public TestXmlObject(boolean buildDOM) {
        if (buildDOM) {
            buildDocument();
        }
    }

    /**
     * Copy constructor.
     */
    public TestXmlObject(TestXmlObject src) {
        setDocument((Document)src.getDocument().cloneNode(true), src.getMIMEType(), src.getEncoding());
        syncAccessMethods();
    }

    /**
     * Create document as a DOM and initialize accessor method fields.
     */
    public void buildDocument() {
        fLazyDocument = (org.enhydra.xml.lazydom.html.LazyHTMLDocument)((org.enhydra.xml.xmlc.dom.lazydom.LazyDomFactory)fDOMFactory).createDocument(fTemplateDocument);
        fLazyDocument.setPreFormatOutputOptions(fPreFormatOutputOptions);
        setDocument(fLazyDocument, "text/html", "ISO-8859-1");
    }

    /**
     * Create a subtree of the document.
     */
    private static org.enhydra.xml.lazydom.html.LazyHTMLDocument buildTemplateSubDocument() {
        Node $node0, $node1, $node2, $node3, $node4;
        Element $elem0, $elem1, $elem2, $elem3;
        Attr $attr0, $attr1, $attr2, $attr3;
        
        org.enhydra.xml.lazydom.html.LazyHTMLDocument document = (org.enhydra.xml.lazydom.html.LazyHTMLDocument)fDOMFactory.createDocument(null, "HTML", null);
        document.makeTemplateNode(0);
        $elem1 = document.getDocumentElement();
        ((org.enhydra.xml.lazydom.LazyElement)$elem1).makeTemplateNode(1);
        ((org.enhydra.xml.lazydom.LazyElement)$elem1).setPreFormattedText("<HTML>");
        
        $elem2 = document.createTemplateElement("HEAD", 2, "<HEAD>");
        $elem1.appendChild($elem2);
        
        $elem3 = document.createTemplateElement("TITLE", 3, "<TITLE>");
        $elem2.appendChild($elem3);
        
        $node4 = document.createTemplateTextNode("Foo", 4, "Foo");
        $elem3.appendChild($node4);
        
        $elem2 = document.createTemplateElement("BODY", 5, "<BODY>");
        $elem1.appendChild($elem2);
        
        $elem3 = document.createTemplateElement("SPAN", 6, "<SPAN id=\"test\">");
        $elem2.appendChild($elem3);
        
        $attr3 = document.createTemplateAttribute("id", 7);
        $elem3.setAttributeNode($attr3);
        $node4 = document.createTemplateTextNode("test", 8, "test");
        $attr3.appendChild($node4);
        
        $node4 = document.createTemplateTextNode("foobar", 9, "foobar");
        $elem3.appendChild($node4);
        
        return document;
    }

    /**
     * Clone the document.
     */
    public Node cloneNode(boolean deep) {
        cloneDeepCheck(deep);
        return new TestXmlObject(this);
    }

    /**
     * Get the XMLC DOM factory associated with the class.
     */
    protected final org.enhydra.xml.xmlc.dom.XMLCDomFactory getDomFactory() {
        return fDOMFactory;
    }

    /**
     * Get the element with id <CODE>test</CODE>.
     * @see org.w3c.dom.html.HTMLElement
     */
    public org.w3c.dom.html.HTMLElement getElementTest() {
        if (($element_Test == null) && ($elementId_test >= 0)) {
            $element_Test = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_test);
        }
        return $element_Test;
    }

    /**
     * Get the value of text child of element <CODE>test</CODE>.
     * @see org.w3c.dom.Text
     */
    public void setTextTest(String text) {
        if (($element_Test == null) && ($elementId_test >= 0)) {
            $element_Test = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_test);
        }
        doSetText($element_Test, text);
    }

    /**
     * Recursive function to do set access method fields from the DOM.
     * Missing ids have fields set to null.
     */
    protected void syncWithDocument(Node node) {
        if (node instanceof Element) {
            String id = ((Element)node).getAttribute("id");
            if (id.length() == 0) {
            } else if (id.equals("test")) {
                $elementId_test = 6;
                $element_Test = (org.enhydra.xml.lazydom.html.LazyHTMLElement)node;
            }
        }
        Node child = node.getFirstChild();
        while (child != null) {
            syncWithDocument(child);
            child = child.getNextSibling();
        }
    }

}

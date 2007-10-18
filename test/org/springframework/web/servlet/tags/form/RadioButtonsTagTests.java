package org.springframework.web.servlet.tags.form;

import org.springframework.beans.TestBean;
import org.springframework.beans.Colour;
import org.springframework.beans.Pet;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.servlet.jsp.tagext.Tag;
import java.io.StringReader;
import java.util.*;

/**
 * @author Thomas Risberg
 */
public class RadioButtonsTagTests extends AbstractFormTagTests {

	private RadioButtonsTag tag;

	private TestBean bean;

	protected void onSetUp() {
		this.tag = new RadioButtonsTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}

	public void testWithMultiValueArray() throws Exception {
		this.tag.setPath("stringArray");
		this.tag.setItems(new Object[] {"foo", "bar", "baz"});
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element spanElement1 = (Element) document.getRootElement().elements().get(0);
		Element radioButtonElement1 = (Element) spanElement1.elements().get(0);
		assertEquals("input", radioButtonElement1.getName());
		assertEquals("radio", radioButtonElement1.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement1.attribute("name").getValue());
		assertEquals("checked", radioButtonElement1.attribute("checked").getValue());
		assertEquals("foo", radioButtonElement1.getStringValue());
		assertEquals("foo", radioButtonElement1.attribute("value").getValue());
		Element spanElement2 = (Element) document.getRootElement().elements().get(1);
		Element radioButtonElement2 = (Element) spanElement2.elements().get(0);
		assertEquals("input", radioButtonElement2.getName());
		assertEquals("radio", radioButtonElement2.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement2.attribute("name").getValue());
		assertEquals("checked", radioButtonElement2.attribute("checked").getValue());
		assertEquals("bar", radioButtonElement2.getStringValue());
		assertEquals("bar", radioButtonElement2.attribute("value").getValue());
		Element spanElement3 = (Element) document.getRootElement().elements().get(2);
		Element radioButtonElement3 = (Element) spanElement3.elements().get(0);
		assertEquals("input", radioButtonElement3.getName());
		assertEquals("radio", radioButtonElement3.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement3.attribute("name").getValue());
		assertNull("not checked", radioButtonElement3.attribute("checked"));
		assertEquals("baz", radioButtonElement3.getStringValue());
		assertEquals("baz", radioButtonElement3.attribute("value").getValue());
	}

	public void testWithMultiValueArrayWithDelimiter() throws Exception {
		this.tag.setDelimiter("<br/>");
		this.tag.setPath("stringArray");
		this.tag.setItems(new Object[] {"foo", "bar", "baz"});
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element spanElement1 = (Element) document.getRootElement().elements().get(0);
		Element delimiterElement1 = spanElement1.element("br");
		assertNull(delimiterElement1);
		Element radioButtonElement1 = (Element) spanElement1.elements().get(0);
		assertEquals("input", radioButtonElement1.getName());
		assertEquals("radio", radioButtonElement1.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement1.attribute("name").getValue());
		assertEquals("checked", radioButtonElement1.attribute("checked").getValue());
		assertEquals("foo", radioButtonElement1.getStringValue());
		assertEquals("foo", radioButtonElement1.attribute("value").getValue());
		Element spanElement2 = (Element) document.getRootElement().elements().get(1);
		Element delimiterElement2 = (Element) spanElement2.elements().get(0);
		assertEquals("br", delimiterElement2.getName());
		Element radioButtonElement2 = (Element) spanElement2.elements().get(1);
		assertEquals("input", radioButtonElement2.getName());
		assertEquals("radio", radioButtonElement2.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement2.attribute("name").getValue());
		assertEquals("checked", radioButtonElement2.attribute("checked").getValue());
		assertEquals("bar", radioButtonElement2.getStringValue());
		assertEquals("bar", radioButtonElement2.attribute("value").getValue());
		Element spanElement3 = (Element) document.getRootElement().elements().get(2);
		Element delimiterElement3 = (Element) spanElement3.elements().get(0);
		assertEquals("br", delimiterElement3.getName());
		Element radioButtonElement3 = (Element) spanElement3.elements().get(1);
		assertEquals("input", radioButtonElement3.getName());
		assertEquals("radio", radioButtonElement3.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement3.attribute("name").getValue());
		assertNull("not checked", radioButtonElement3.attribute("checked"));
		assertEquals("baz", radioButtonElement3.getStringValue());
		assertEquals("baz", radioButtonElement3.attribute("value").getValue());
	}

	public void testWithMultiValueMap() throws Exception {
		this.tag.setPath("stringArray");
		Map m = new LinkedHashMap();
		m.put("foo", "FOO");
		m.put("bar", "BAR");
		m.put("baz", "BAZ");
		this.tag.setItems(m);
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element spanElement1 = (Element) document.getRootElement().elements().get(0);
		Element radioButtonElement1 = (Element) spanElement1.elements().get(0);
		assertEquals("input", radioButtonElement1.getName());
		assertEquals("radio", radioButtonElement1.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement1.attribute("name").getValue());
		assertEquals("checked", radioButtonElement1.attribute("checked").getValue());
		assertEquals("FOO", radioButtonElement1.getStringValue());
		assertEquals("foo", radioButtonElement1.attribute("value").getValue());
		Element spanElement2 = (Element) document.getRootElement().elements().get(1);
		Element radioButtonElement2 = (Element) spanElement2.elements().get(0);
		assertEquals("input", radioButtonElement2.getName());
		assertEquals("radio", radioButtonElement2.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement2.attribute("name").getValue());
		assertEquals("checked", radioButtonElement2.attribute("checked").getValue());
		assertEquals("BAR", radioButtonElement2.getStringValue());
		assertEquals("bar", radioButtonElement2.attribute("value").getValue());
		Element spanElement3 = (Element) document.getRootElement().elements().get(2);
		Element radioButtonElement3 = (Element) spanElement3.elements().get(0);
		assertEquals("input", radioButtonElement3.getName());
		assertEquals("radio", radioButtonElement3.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement3.attribute("name").getValue());
		assertNull("not checked", radioButtonElement3.attribute("checked"));
		assertEquals("BAZ", radioButtonElement3.getStringValue());
		assertEquals("baz", radioButtonElement3.attribute("value").getValue());
	}

	public void testWithMultiValueMapWithDelimiter() throws Exception {
		String delimiter = " | ";
		this.tag.setDelimiter(delimiter);
		this.tag.setPath("stringArray");
		Map m = new LinkedHashMap();
		m.put("foo", "FOO");
		m.put("bar", "BAR");
		m.put("baz", "BAZ");
		this.tag.setItems(m);
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element spanElement1 = (Element) document.getRootElement().elements().get(0);
		assertEquals("", spanElement1.getText());
		Element radioButtonElement1 = (Element) spanElement1.elements().get(0);
		assertEquals("input", radioButtonElement1.getName());
		assertEquals("radio", radioButtonElement1.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement1.attribute("name").getValue());
		assertEquals("checked", radioButtonElement1.attribute("checked").getValue());
		assertEquals("FOO", radioButtonElement1.getStringValue());
		assertEquals("foo", radioButtonElement1.attribute("value").getValue());
		Element spanElement2 = (Element) document.getRootElement().elements().get(1);
		assertEquals(delimiter, spanElement2.getText());
		Element radioButtonElement2 = (Element) spanElement2.elements().get(0);
		assertEquals("input", radioButtonElement2.getName());
		assertEquals("radio", radioButtonElement2.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement2.attribute("name").getValue());
		assertEquals("checked", radioButtonElement2.attribute("checked").getValue());
		assertEquals("BAR", radioButtonElement2.getStringValue());
		assertEquals("bar", radioButtonElement2.attribute("value").getValue());
		Element spanElement3 = (Element) document.getRootElement().elements().get(2);
		assertEquals(delimiter, spanElement3.getText());
		Element radioButtonElement3 = (Element) spanElement3.elements().get(0);
		assertEquals("input", radioButtonElement3.getName());
		assertEquals("radio", radioButtonElement3.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement3.attribute("name").getValue());
		assertNull("not checked", radioButtonElement3.attribute("checked"));
		assertEquals("BAZ", radioButtonElement3.getStringValue());
		assertEquals("baz", radioButtonElement3.attribute("value").getValue());
	}

	public void testWithMultiValueWithEditor() throws Exception {
		this.tag.setPath("stringArray");
		this.tag.setItems(new Object[] {"   foo", "   bar", "   baz"});
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(this.bean, COMMAND_NAME);
		RadioButtonsTagTests.MyStringTrimmerEditor editor = new RadioButtonsTagTests.MyStringTrimmerEditor();
		bindingResult.getPropertyEditorRegistry().registerCustomEditor(String.class, editor);
		getPageContext().getRequest().setAttribute(BindingResult.MODEL_KEY_PREFIX + COMMAND_NAME, bindingResult);

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);
		assertEquals(3, editor.count);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element spanElement1 = (Element) document.getRootElement().elements().get(0);
		Element radioButtonElement1 = (Element) spanElement1.elements().get(0);
		assertEquals("input", radioButtonElement1.getName());
		assertEquals("radio", radioButtonElement1.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement1.attribute("name").getValue());
		assertEquals("checked", radioButtonElement1.attribute("checked").getValue());
		assertEquals("   foo", radioButtonElement1.attribute("value").getValue());
		Element spanElement2 = (Element) document.getRootElement().elements().get(1);
		Element radioButtonElement2 = (Element) spanElement2.elements().get(0);
		assertEquals("input", radioButtonElement2.getName());
		assertEquals("radio", radioButtonElement2.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement2.attribute("name").getValue());
		assertEquals("checked", radioButtonElement2.attribute("checked").getValue());
		assertEquals("   bar", radioButtonElement2.attribute("value").getValue());
		Element spanElement3 = (Element) document.getRootElement().elements().get(2);
		Element radioButtonElement3 = (Element) spanElement3.elements().get(0);
		assertEquals("input", radioButtonElement3.getName());
		assertEquals("radio", radioButtonElement3.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement3.attribute("name").getValue());
		assertNull("not checked", radioButtonElement3.attribute("checked"));
		assertEquals("   baz", radioButtonElement3.attribute("value").getValue());
	}

	public void testCollectionOfPets() throws Exception {
		this.tag.setPath("pets");
		List allPets = new ArrayList();
		allPets.add(new RadioButtonsTagTests.ItemPet("Rudiger"));
		allPets.add(new RadioButtonsTagTests.ItemPet("Spot"));
		allPets.add(new RadioButtonsTagTests.ItemPet("Checkers"));
		allPets.add(new RadioButtonsTagTests.ItemPet("Fluffy"));
		allPets.add(new RadioButtonsTagTests.ItemPet("Mufty"));
		this.tag.setItems(allPets);
		this.tag.setItemValue("name");
		this.tag.setItemLabel("label");

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element spanElement1 = (Element) document.getRootElement().elements().get(0);
		Element radioButtonElement1 = (Element) spanElement1.elements().get(0);
		assertEquals("input", radioButtonElement1.getName());
		assertEquals("radio", radioButtonElement1.attribute("type").getValue());
		assertEquals("pets", radioButtonElement1.attribute("name").getValue());
		assertEquals("checked", radioButtonElement1.attribute("checked").getValue());
		assertEquals("RUDIGER", radioButtonElement1.getStringValue());
		assertEquals("Rudiger", radioButtonElement1.attribute("value").getValue());
		Element spanElement2 = (Element) document.getRootElement().elements().get(1);
		Element radioButtonElement2 = (Element) spanElement2.elements().get(0);
		assertEquals("input", radioButtonElement2.getName());
		assertEquals("radio", radioButtonElement2.attribute("type").getValue());
		assertEquals("pets", radioButtonElement2.attribute("name").getValue());
		assertEquals("checked", radioButtonElement2.attribute("checked").getValue());
		assertEquals("SPOT", radioButtonElement2.getStringValue());
		assertEquals("Spot", radioButtonElement2.attribute("value").getValue());
		Element spanElement3 = (Element) document.getRootElement().elements().get(2);
		Element radioButtonElement3 = (Element) spanElement3.elements().get(0);
		assertEquals("input", radioButtonElement3.getName());
		assertEquals("radio", radioButtonElement3.attribute("type").getValue());
		assertEquals("pets", radioButtonElement3.attribute("name").getValue());
		assertNull("not checked", radioButtonElement3.attribute("checked"));
		assertEquals("CHECKERS", radioButtonElement3.getStringValue());
		assertEquals("Checkers", radioButtonElement3.attribute("value").getValue());
		Element spanElement4 = (Element) document.getRootElement().elements().get(3);
		Element radioButtonElement4 = (Element) spanElement4.elements().get(0);
		assertEquals("input", radioButtonElement4.getName());
		assertEquals("radio", radioButtonElement4.attribute("type").getValue());
		assertEquals("pets", radioButtonElement4.attribute("name").getValue());
		assertEquals("checked", radioButtonElement4.attribute("checked").getValue());
		assertEquals("FLUFFY", radioButtonElement4.getStringValue());
		assertEquals("Fluffy", radioButtonElement4.attribute("value").getValue());
		Element spanElement5 = (Element) document.getRootElement().elements().get(4);
		Element radioButtonElement5 = (Element) spanElement5.elements().get(0);
		assertEquals("input", radioButtonElement5.getName());
		assertEquals("radio", radioButtonElement5.attribute("type").getValue());
		assertEquals("pets", radioButtonElement5.attribute("name").getValue());
		assertEquals("checked", radioButtonElement5.attribute("checked").getValue());
		assertEquals("MUFTY", radioButtonElement5.getStringValue());
		assertEquals("Mufty", radioButtonElement5.attribute("value").getValue());
	}

	public void testWithNullValue() throws Exception {
		try {
			this.tag.setPath("name");
			this.tag.doStartTag();
			fail("Should not be able to render with a null value when binding to a non-boolean.");
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}

	public void testHiddenElementOmittedOnDisabled() throws Exception {
		this.tag.setPath("stringArray");
		this.tag.setItems(new Object[] {"foo", "bar", "baz"});
		this.tag.setDisabled("true");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);
		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("Both tag and hidden element rendered incorrectly", 3, rootElement.elements().size());
		Element spanElement = (Element) document.getRootElement().elements().get(0);
		Element radioButtonElement = (Element) spanElement.elements().get(0);
		assertEquals("input", radioButtonElement.getName());
		assertEquals("radio", radioButtonElement.attribute("type").getValue());
		assertEquals("stringArray", radioButtonElement.attribute("name").getValue());
		assertEquals("checked", radioButtonElement.attribute("checked").getValue());
		assertEquals("disabled", radioButtonElement.attribute("disabled").getValue());
		assertEquals("foo", radioButtonElement.attribute("value").getValue());
	}

	public void testSpanElementCustomizable() throws Exception {
		this.tag.setPath("stringArray");
		this.tag.setItems(new Object[] {"foo", "bar", "baz"});
		this.tag.setElement("element");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);
		String output = getOutput();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element spanElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("element", spanElement.getName());
	}

	private Date getDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 10);
		cal.set(Calendar.MONTH, 10);
		cal.set(Calendar.DATE, 10);
		cal.set(Calendar.HOUR, 10);
		cal.set(Calendar.MINUTE, 10);
		cal.set(Calendar.SECOND, 10);
		return cal.getTime();
	}

	protected TestBean createTestBean() {
		List colours = new ArrayList();
		colours.add(Colour.BLUE);
		colours.add(Colour.RED);
		colours.add(Colour.GREEN);

		List pets = new ArrayList();
		pets.add(new Pet("Rudiger"));
		pets.add(new Pet("Spot"));
		pets.add(new Pet("Fluffy"));
		pets.add(new Pet("Mufty"));

		this.bean = new TestBean();
		this.bean.setDate(getDate());
		this.bean.setName("Rob Harrop");
		this.bean.setJedi(true);
		this.bean.setSomeBoolean(new Boolean(true));
		this.bean.setStringArray(new String[] {"bar", "foo"});
		this.bean.setSomeIntegerArray(new Integer[] {new Integer(2), new Integer(1)});
		this.bean.setOtherColours(colours);
		this.bean.setPets(pets);
		List list = new ArrayList();
		list.add("foo");
		list.add("bar");
		this.bean.setSomeList(list);
		return this.bean;
	}


	private class MyStringTrimmerEditor extends StringTrimmerEditor {

		public int count = 0;

		public MyStringTrimmerEditor() {
			super(false);
		}

		public void setAsText(String text) {
			this.count++;
			super.setAsText(text);
		}
	}

	public class ItemPet {

		private String name;


		public ItemPet(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLabel() {
			return this.name.toUpperCase();
		}

	}

}

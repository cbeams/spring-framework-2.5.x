package org.springframework.beans.propertyeditors;

import junit.framework.TestCase;
import org.springframework.test.AssertThrows;
import org.springframework.util.ClassUtils;

import java.beans.PropertyEditor;
import java.io.File;

/**
 * @author Thomas Risberg
 */
public final class FileEditorTests extends TestCase {

	public void testClasspathFileName() throws Exception {
		PropertyEditor fileEditor = new FileEditor();
		fileEditor.setAsText("classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) +
				"/" + ClassUtils.getShortName(getClass()) + ".class");
		Object value = fileEditor.getValue();
		assertTrue(value instanceof File);
		File file = (File) value;
		assertTrue(file.exists());
	}

	public void testWithNonExistentResource() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				PropertyEditor propertyEditor = new FileEditor();
				propertyEditor.setAsText("classpath:no_way_this_file_is_found.doc");
			}
		}.runTest();
	}

	public void testWithNonExistentFile() throws Exception {
		PropertyEditor fileEditor = new FileEditor();
		fileEditor.setAsText("file:no_way_this_file_is_found.doc");
		Object value = fileEditor.getValue();
		assertTrue(value instanceof File);
		File file = (File) value;
		assertTrue(!file.exists());
	}

	public void testAbsoluteFileName() throws Exception {
		PropertyEditor fileEditor = new FileEditor();
		fileEditor.setAsText("/no_way_this_file_is_found.doc");
		Object value = fileEditor.getValue();
		assertTrue(value instanceof File);
		File file = (File) value;
		assertTrue(!file.exists());
	}

	public void testUnqualifiedFileNameFound() throws Exception {
		PropertyEditor fileEditor = new FileEditor();
		String fileName = ClassUtils.classPackageAsResourcePath(getClass()) +
				"/" + ClassUtils.getShortName(getClass()) + ".class";
		fileEditor.setAsText(fileName);
		Object value = fileEditor.getValue();
		assertTrue(value instanceof File);
		File file = (File) value;
		assertTrue(file.exists());
		assertTrue(file.getAbsolutePath().endsWith(fileName));
	}

	public void testUnqualifiedFileNameNotFound() throws Exception {
		PropertyEditor fileEditor = new FileEditor();
		String fileName = ClassUtils.classPackageAsResourcePath(getClass()) +
				"/" + ClassUtils.getShortName(getClass()) + ".clazz";
		fileEditor.setAsText(fileName);
		Object value = fileEditor.getValue();
		assertTrue(value instanceof File);
		File file = (File) value;
		assertTrue(!file.exists());
		assertTrue(file.getAbsolutePath().endsWith(fileName));
	}
}

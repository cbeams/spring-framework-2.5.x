
package org.springframework.mail.javamail.support;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

/**
 * @author robh
 */
public class ConfigurableFileTypeMapTests extends TestCase {

	public void testAgainstDefaultConfiguration() throws Exception {
		ConfigurableFileTypeMap ftm = new ConfigurableFileTypeMap();
		ftm.afterPropertiesSet();

		assertEquals("Invalid content type for HTM", "text/html", ftm.getContentType("foobar.HTM"));
		assertEquals("Invalid content type for html", "text/html", ftm.getContentType("foobar.html"));
		assertEquals("Invalid content type for c++", "text/plain", ftm.getContentType("foobar.c++"));
		assertEquals("Invalid content type for svf", "image/vnd.svf", ftm.getContentType("foobar.svf"));
		assertEquals("Invalid content type for dsf", "image/x-mgx-dsf", ftm.getContentType("foobar.dsf"));
		assertEquals("Invalid default content type", "application/octet-stream", ftm.getContentType("foobar.foo"));
	}

	public void testAgainstDefaultConfigurationWithFile() throws Exception {
		ConfigurableFileTypeMap ftm = new ConfigurableFileTypeMap();
		ftm.afterPropertiesSet();

		assertEquals("Invalid content type for HTM", "text/html", ftm.getContentType(new File("/tmp/foobar.HTM")));
	}

	public void testWithAdditionalMappings() throws Exception {
		Properties mappings = new Properties();
		mappings.setProperty("HTM", "foo/bar");
		mappings.setProperty("c++", "foo/cpp");
		mappings.setProperty("foo", "foo/bar");

		ConfigurableFileTypeMap ftm = new ConfigurableFileTypeMap();
		ftm.setMappings(mappings);
		ftm.afterPropertiesSet();

		assertEquals("Invalid content type for HTM - override didn't work", "foo/bar", ftm.getContentType("foobar.HTM"));
		assertEquals("Invalid content type for c++ - override didn't work", "foo/cpp", ftm.getContentType("foobar.c++"));
		assertEquals("Invalid content type for foo - new mapping didn't work", "foo/bar", ftm.getContentType("bar.foo"));
	}

	public void testWithCustomMappingLocation() throws Exception {
		Resource resource = new ClassPathResource("test.mime.types", getClass());

		ConfigurableFileTypeMap ftm = new ConfigurableFileTypeMap();
		ftm.setMappingLocation(resource);
		ftm.afterPropertiesSet();

		assertEquals("Invalid content type for foo", "text/foo", ftm.getContentType("foobar.foo"));
		assertEquals("Invalid content type for bar", "text/bar", ftm.getContentType("foobar.bar"));
		assertEquals("Invalid content type for fimg", "image/foo", ftm.getContentType("foobar.fimg"));
		assertEquals("Invalid content type for bimg", "image/bar", ftm.getContentType("foobar.bimg"));
	}
}

/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.web.servlet.view.velocity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.ui.velocity.VelocityEngineUtils;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class VelocityConfigurerTests extends TestCase {

	public void testVelocityEngineFactoryBeanWithConfigLocation() throws VelocityException {
		VelocityEngineFactoryBean vefb = new VelocityEngineFactoryBean();
		vefb.setConfigLocation(new FileSystemResource("myprops.properties"));
		Properties props = new Properties();
		props.setProperty("myprop", "/mydir");
		vefb.setVelocityProperties(props);
		try {
			vefb.afterPropertiesSet();
			fail("Should have thrown IOException");
		}
		catch (IOException ex) {
			// expected
		}
	}

	public void testVelocityEngineFactoryBeanWithVelocityProperties() throws VelocityException, IOException {
		VelocityEngineFactoryBean vefb = new VelocityEngineFactoryBean();
		Properties props = new Properties();
		props.setProperty("myprop", "/mydir");
		vefb.setVelocityProperties(props);
		Object value = new Object();
		Map map = new HashMap();
		map.put("myentry", value);
		vefb.setVelocityPropertiesMap(map);
		vefb.afterPropertiesSet();
		assertTrue(vefb.getObject() instanceof VelocityEngine);
		VelocityEngine ve = (VelocityEngine) vefb.getObject();
		assertEquals("/mydir", ve.getProperty("myprop"));
		assertEquals(value, ve.getProperty("myentry"));
	}

	public void testVelocityEngineFactoryBeanWithResourceLoaderPath() throws IOException, VelocityException {
		VelocityEngineFactoryBean vefb = new VelocityEngineFactoryBean();
		vefb.setResourceLoaderPath("file:/mydir");
		vefb.afterPropertiesSet();
		assertTrue(vefb.getObject() instanceof VelocityEngine);
		VelocityEngine ve = (VelocityEngine) vefb.getObject();
		assertEquals(new File("/mydir").getAbsolutePath(), ve.getProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH));
	}

	public void testVelocityEngineFactoryBeanWithNonFileResourceLoaderPath() throws Exception {
		VelocityEngineFactoryBean vefb = new VelocityEngineFactoryBean();
		vefb.setResourceLoaderPath("file:/mydir");
		vefb.setResourceLoader(new ResourceLoader() {
			public Resource getResource(String location) {
				if (!("file:/mydir".equals(location) || "file:/mydir/test".equals(location))) {
					throw new IllegalArgumentException(location);
				}
				return new InputStreamResource(new ByteArrayInputStream("test".getBytes()), "test");
			}
		});
		vefb.afterPropertiesSet();
		assertTrue(vefb.getObject() instanceof VelocityEngine);
		VelocityEngine ve = (VelocityEngine) vefb.getObject();
		assertEquals("test", VelocityEngineUtils.mergeTemplateIntoString(ve, "test", new HashMap()));
	}

	public void testVelocityConfigurer() throws IOException, VelocityException {
		VelocityConfigurer vc = new VelocityConfigurer();
		vc.setResourceLoaderPath("file:/mydir");
		vc.afterPropertiesSet();
		assertTrue(vc.createVelocityEngine() instanceof VelocityEngine);
		VelocityEngine ve = vc.createVelocityEngine();
		assertEquals(new File("/mydir").getAbsolutePath(), ve.getProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH));
	}

}

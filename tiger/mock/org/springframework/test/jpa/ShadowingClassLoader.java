package org.springframework.test.jpa;

import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author Rob Harrop
 */
class ShadowingClassLoader extends ClassLoader {

	private final ClassLoader enclosingClassLoader;

	private final List<ClassFileTransformer> classFilterTransformers = new ArrayList<ClassFileTransformer>();

	private final Map<String, Class> classCache = new HashMap<String, Class>();

	public ShadowingClassLoader(ClassLoader enclosingClassLoader) {
		Assert.notNull(enclosingClassLoader, "'enclosingClassLoader' cannot be null.");
		this.enclosingClassLoader = enclosingClassLoader;
	}

	public Class loadClass(String name) throws ClassNotFoundException {
		if (shouldShadow(name)) {
			Class cls = classCache.get(name);
			if (cls != null) {
				return cls;
			}
			return doLoadClass(name);
		}
		else {
			return this.enclosingClassLoader.loadClass(name);
		}
	}

	private boolean shouldShadow(String name) {
		if (isExcluded(name)) {
			return false;
		}
		else {
			return true;
		}
	}

	private boolean isExcluded(String name) {
		return name.equals(getClass().getName()) || name.startsWith("java.") ||
						name.startsWith("javax.") ||
						name.startsWith("org.apache.commons.logging") ||
						name.startsWith("org.xml.sax") ||
						name.startsWith("org.w3c") ||
						name.startsWith("sun");
	}

	private Class doLoadClass(String name) throws ClassNotFoundException {
		String path = name.replaceAll("\\.", "/") + ".class";
		ClassPathResource cpr = new ClassPathResource(path);
		InputStream inputStream = null;
		try {
			inputStream = cpr.getInputStream();
			byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
			bytes = applyTransformers(name, bytes);
			Class cls = defineClass(name, bytes, 0, bytes.length);
			classCache.put(name, cls);
			return cls;
		}
		catch (IOException e) {
			throw new ClassNotFoundException("Class '" + name + "' cannot be found.");
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private byte[] applyTransformers(String name, byte[] bytes) {
		String internalName = name.replaceAll("\\.", "/");
		try {
			for (ClassFileTransformer transformer : this.classFilterTransformers) {
				byte[] transformed = transformer.transform(this, internalName, null, null, bytes);
				bytes = (transformed != null ? transformed : bytes);
			}
			return bytes;
		}
		catch (IllegalClassFormatException e) {
			throw new RuntimeException(e);
		}
	}

	public void addClassFileTransformer(ClassFileTransformer classFileTransformer) {
		this.classFilterTransformers.add(classFileTransformer);
	}
}

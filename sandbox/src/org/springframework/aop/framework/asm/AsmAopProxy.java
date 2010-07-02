package org.springframework.aop.framework.asm;

import org.aopalliance.aop.AspectException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.AopProxyUtils;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author robh
 */
public class AsmAopProxy implements AopProxy {

	private static final Object PROXY_COUNT_LOCK = new Object();

	private static final String OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);

	private static int proxyCount = 0;

	private AdvisedSupport advised;

	private boolean emptyTargetSource;

	private CodeGenerationStrategySelector strategySelector = new DefaultCodeGenerationStrategySelector();

	private CodeGenerationStrategy constructorStrategy = new ProxyConstructorGenerationStrategy();

	private static Map cache = new WeakHashMap();

	public AsmAopProxy(AdvisedSupport advised) {
		if (advised.getAdvisors().length == 0 && advised.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("Cannot create AopProxy with no advisors and no target source");
		}

		this.advised = advised;
		this.emptyTargetSource = (advised.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE);
	}

	public Object getProxy(ClassLoader classLoader) {
		Class proxyClass = getProxyClass(classLoader);

		try {
			Constructor ctor = proxyClass.getConstructor(new Class[]{AdvisedSupport.class});
			return ctor.newInstance(new Object[]{this.advised});
		}
		catch (InstantiationException ex) {
			throw new AspectException("Unable to create instance of proxy class [" + proxyClass.getName() + "].", ex);
		}
		catch (IllegalAccessException ex) {
			throw new AspectException("Unable to create instance of proxy class [" + proxyClass.getName() + "].", ex);
		}
		catch (InvocationTargetException ex) {
			throw new AspectException("Unable to create instance of proxy class [" + proxyClass.getName() + "].", ex);
		}
		catch (NoSuchMethodException ex) {
			throw new AspectException("Unable to locate appropriate constructor for proxy class [" + proxyClass.getName() + "].", ex);
		}
	}

	private Class getProxyClass(ClassLoader classLoader) {
		Class targetClass = (emptyTargetSource) ? null : this.advised.getTargetSource().getTargetClass();
		Class[] proxyInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised);

		CacheKey cacheKey = new CacheKey(this.advised, targetClass, proxyInterfaces);
		Reference classReference = (Reference) cache.get(cacheKey);

		if (classReference == null) {
			Class proxyClass = generateProxyClass(targetClass, proxyInterfaces, classLoader);
			synchronized (cache) {
				cache.put(cacheKey, new WeakReference(proxyClass));
			}
			return proxyClass;
		}
		else {
			return (Class) classReference.get();
		}

	}

	public Object getProxy() {
		return getProxy(Thread.currentThread().getContextClassLoader());
	}

	private Class generateProxyClass(Class targetClass, Class[] proxyInterfaces, ClassLoader classLoader) {
		// load target information

		String targetDescriptor = (emptyTargetSource) ? null : Type.getDescriptor(targetClass);
		String targetInternalName = (emptyTargetSource) ? null : Type.getInternalName(targetClass);

		// get proxy interfaces

		String[] proxyInterfaceTypes = convertToInternalTypes(proxyInterfaces);

		String proxyInternalName = generateProxyClassInternalName(targetClass);
		String superName = (emptyTargetSource) ? OBJECT_INTERNAL_NAME : targetInternalName;
		ClassWriter cw = new ClassWriter(true);

		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, proxyInternalName, superName, null, proxyInterfaceTypes);
		generateConstructor(cw, proxyInternalName, targetDescriptor, targetInternalName);
		proxyMethods(cw, targetClass, proxyInterfaces, proxyInternalName, targetInternalName, targetDescriptor);
		cw.visitEnd();

		byte[] bytes = cw.toByteArray();

		try {
			ClassReader cr = new ClassReader(bytes);
			FileOutputStream fos = new FileOutputStream("c:/tmp/Test.java");
			ClassVisitor cv = new ASMifierClassVisitor(new PrintWriter(fos));
			cr.accept(cv, true);

			FileOutputStream fos2 = new FileOutputStream("c:/tmp/Test.class");
			fos2.write(bytes);
			fos2.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return defineClass(bytes, classLoader);
	}

	private Class defineClass(byte[] bytes, ClassLoader classLoader) {
		// todo: extract into helper class
		try {
			Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
			defineClass.setAccessible(true);

			try {
				Object[] args = new Object[]{null, bytes, new Integer(0), new Integer(bytes.length)};
				return (Class) defineClass.invoke(classLoader, args);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
			finally {
				defineClass.setAccessible(false);
			}
		}
		catch (NoSuchMethodException e) {
			// todo: add real exception here
			throw new RuntimeException(e);
		}
	}

	private void proxyMethods(ClassWriter cw, Class targetClass, Class[] proxyInterfaces, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		Method[] methods = getMethodsToProxy(targetClass, proxyInterfaces);

		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (canProxy(method)) {
				proxyMethod(cw, method, proxyInternalName, targetInternalName, targetDescriptor, targetClass);
			}
		}
	}

	private void proxyMethod(ClassWriter cw, Method method, String proxyInternalName, String targetInternalName, String targetDescriptor, Class targetClass) {
		CodeGenerationStrategy strategy = this.strategySelector.select(this.advised, method, targetClass);
		strategy.generate(cw, method, this.advised, proxyInternalName, targetInternalName, targetDescriptor);
	}

	private void generateConstructor(ClassWriter cw, String proxyInternalName, String targetDescriptor, String targetInternalName) {
		this.constructorStrategy.generate(cw, null, this.advised, proxyInternalName, targetInternalName, targetDescriptor);
	}

	private String[] convertToInternalTypes(Class[] classes) {
		String[] types = new String[classes.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = Type.getInternalName(classes[i]);
		}
		return types;
	}

	private String generateProxyClassInternalName(Class targetClass) {
		StringBuffer sb = new StringBuffer();
		if (targetClass != null) {
			sb.append(Type.getInternalName(targetClass));
		}
		else {
			sb.append("NoTarget");
		}
		sb.append("$$SpringProxy$$");
		sb.append(getAndIncrementProxyCount());
		return sb.toString();
	}

	private int getAndIncrementProxyCount() {
		synchronized (PROXY_COUNT_LOCK) {
			return proxyCount++;
		}
	}

	private boolean canProxy(Method method) {
		int modifiers = method.getModifiers();
		return (Modifier.isPublic(modifiers) && !(Modifier.isFinal(modifiers)));
	}

	private Method[] getMethodsToProxy(Class targetClass, Class[] proxyInterfaces) {
		List methods = new ArrayList();

		if (targetClass != null) {
			methods.addAll(Arrays.asList(targetClass.getMethods()));
		}

		if (proxyInterfaces != null) {
			for (int i = 0; i < proxyInterfaces.length; i++) {
				Class proxyInterface = proxyInterfaces[i];
				if (targetClass != null && !(proxyInterface.isAssignableFrom(targetClass))) {
					methods.addAll(Arrays.asList(proxyInterface.getMethods()));
				}
			}
		}

		return (Method[]) methods.toArray(new Method[methods.size()]);
	}

	private static class CacheKey {

		private AdvisedSupport advised;

		private Class targetClass;

		private Class[] proxyInterfaces;

		private int hashCode;

		public CacheKey(AdvisedSupport advised, Class targetClass, Class[] proxyInterfaces) {
			this.advised = advised;
			this.targetClass = targetClass;
			this.proxyInterfaces = proxyInterfaces;

			// calculate hashCode
			int code = 17;
			code = 37 * code + advised.hashCode();

			if (targetClass != null) {
				code = 37 * code + targetClass.getName().hashCode();
			}

			for (int i = 0; i < proxyInterfaces.length; i++) {
				code = 37 * code + proxyInterfaces[i].getName().hashCode();

			}

			this.hashCode = code;
		}

		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;

			if (obj instanceof CacheKey) {
				CacheKey other = (CacheKey) obj;
				return ((other.advised.equals(advised)) &&
						(other.targetClass == targetClass) &&
						(Arrays.equals(other.proxyInterfaces, proxyInterfaces)));
			}
			else {
				return false;
			}
		}

		public int hashCode() {
			return hashCode;
		}

	}
}

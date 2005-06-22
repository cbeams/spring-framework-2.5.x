
package org.springframework.aop.framework.asm;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aopalliance.aop.AspectException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.ASMifierClassVisitor;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.AopProxyUtils;

/**
 * @author robh
 */
public class AsmAopProxy implements AopProxy {

	private static final String CONSTRUCTOR_INTERNAL_NAME = "<init>";

	private static final Object PROXY_COUNT_LOCK = new Object();

	private static final String OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);

	private static int proxyCount = 0;

	private AdvisedSupport advised;

	private boolean emptyTargetSource;

	private CodeGenerationStrategySelector strategySelector = new DefaultCodeGenerationStrategySelector();

	private CodeGenerationStrategy constructorStrategy = new ProxyConstructorGenerationStrategy();

	public AsmAopProxy(AdvisedSupport advised) {
		if (advised.getAdvisors().length == 0 && advised.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("Cannot create AopProxy with no advisors and no target source");
		}

		this.advised = advised;
		this.emptyTargetSource = (advised.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE);
	}

	public Object getProxy(ClassLoader classLoader) {
		Class proxyClass = generateProxyClass();

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

	public Object getProxy() {
		return getProxy(Thread.currentThread().getContextClassLoader());
	}

	private Class generateProxyClass() {
		// load target information
		Class targetClass = (emptyTargetSource) ? null : this.advised.getTargetSource().getTargetClass();
		String targetDescriptor = (emptyTargetSource) ? null : Type.getDescriptor(targetClass);
		String targetInternalName = (emptyTargetSource) ? null : Type.getInternalName(targetClass);

		// get proxy interfaces
		Class[] proxyInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised);
		String[] proxyInterfaceTypes = convertToInternalTypes(proxyInterfaces);

		String proxyInternalName = generateProxyClassInternalName(targetClass);
		String superName = (emptyTargetSource) ? OBJECT_INTERNAL_NAME : targetInternalName;
		ClassWriter cw = new ClassWriter(true);

		cw.visit(Constants.V1_5, Constants.ACC_PUBLIC + Constants.ACC_SUPER, proxyInternalName, superName, proxyInterfaceTypes, null);
		generateConstructor(cw, proxyInternalName, targetDescriptor, targetInternalName);
		proxyMethods(cw, targetClass, proxyInterfaces, proxyInternalName, targetInternalName, targetDescriptor);
		cw.visitEnd();

		byte[] bytes = cw.toByteArray();

		try {
			ClassReader cr = new ClassReader(bytes);
			FileOutputStream fos = new FileOutputStream("d:/tmp/Test.java");
			ClassVisitor cv = new ASMifierClassVisitor(new PrintWriter(fos));
			cr.accept(cv, true);

			FileOutputStream fos2 = new FileOutputStream("d:/tmp/Test.class");
			fos2.write(bytes);
			fos2.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		SpringProxyClassLoader cl = new SpringProxyClassLoader();
		return cl.loadFromBytes(bytes);
	}


	private void proxyMethods(ClassWriter cw, Class targetClass, Class[] proxyInterfaces,
																					String proxyInternalName, String targetInternalName, String targetDescriptor) {
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

	private String getMethodDescriptor(Class returnType, Class[] argumentTypes) {
		Type asmReturnType = Type.getType(returnType);

		Type[] asmArgumentTypes = new Type[argumentTypes.length];
		for (int i = 0; i < asmArgumentTypes.length; i++) {
			asmArgumentTypes[i] = Type.getType(argumentTypes[i]);
		}

		return Type.getMethodDescriptor(asmReturnType, asmArgumentTypes);
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

	private static class SpringProxyClassLoader extends ClassLoader {

		private Class loadFromBytes(byte[] bytes) {
			return defineClass(null, bytes, 0, bytes.length);
		}
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
}

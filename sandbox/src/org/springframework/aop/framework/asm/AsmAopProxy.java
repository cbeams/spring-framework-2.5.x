
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
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.ASMifierClassVisitor;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.Advised;

/**
 * @author robh
 */
public class AsmAopProxy implements AopProxy {

	private static final String NO_ARG_CONSTRUCTOR_DESCRIPTOR = "()V";

	private static final String CONSTRUCTOR_INTERNAL_NAME = "<init>";

	private static final String TARGET_FIELD_NAME = "__target";

	private static final String ADVISED_FIELD_NAME = "__advised";

	private static final Object PROXY_COUNT_LOCK = new Object();

	private static final String GET_TARGET_SOURCE_METHOD = "getTargetSource";

	private static final String ADVISED_SUPPORT_INTERNAL_NAME = Type.getInternalName(AdvisedSupport.class);

	private static final String ADVISED_SUPPORT_DESCRIPTOR = Type.getDescriptor(AdvisedSupport.class);

	private static final String GET_TARGET_SOURCE_DESCRIPTOR = "()Lorg/springframework/aop/TargetSource;";

	private static final String TARGET_SOURCE_INTERNAL_NAME = "org/springframework/aop/TargetSource";

	private static final String GET_TARGET_METHOD = "getTarget";

	private static final String EXCEPTION_INTERNAL_NAME = "java/lang/Exception";

	private static final String AOP_CONFIG_EXCEPTION_INTERNAL_NAME = "org/springframework/aop/framework/AopConfigException";

	private static final String EXCEPTION_CONSTRUCTOR_DESCRIPTOR = "(Ljava/lang/String;Ljava/lang/Throwable;)V";

	private static final String GET_TARGET_DESCRIPTOR = "()Ljava/lang/Object;";

	private static final String GET_TARGET_CLASS_METHOD = "getTargetClass";

	private static final String GET_TARGET_CLASS_DESCRIPTOR = "()Ljava/lang/Class;";

	private static final String OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);

	private static final String VALUE_OF_METHOD = "valueOf";

	private static final String CLASS_INTERNAL_NAME = "java/lang/Class";

	private static final String GET_METHOD_METHOD = "getMethod";

	private static final String GET_METHOD_DESCRIPTOR = "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;";

	private static final String CLASS_DESCRIPTOR = Type.getDescriptor(Class.class);

	private static final String TYPE_FIELD = "TYPE";

	private static final String GET_ADVISOR_CHAIN_FACTORY_DESCRIPTOR = "()Lorg/springframework/aop/framework/AdvisorChainFactory;";

	private static final String GET_ADVISOR_CHAIN_FACTORY_METHOD = "getAdvisorChainFactory";

	private static int proxyCount = 0;

	private AdvisedSupport advised;

	private boolean emptyTargetSource;

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
		for (int i = 0; i < proxyInterfaceTypes.length; i++) {
			String proxyInterfaceType = proxyInterfaceTypes[i];
			System.out.println(proxyInterfaceType);
		}

		String proxyInternalName = generateProxyClassInternalName(targetClass);
		String superName = (emptyTargetSource) ? OBJECT_INTERNAL_NAME : targetInternalName;
		ClassWriter cw = new ClassWriter(true);

		cw.visit(Constants.V1_5, Constants.ACC_PUBLIC + Constants.ACC_SUPER, proxyInternalName, superName, proxyInterfaceTypes, null);
		generateConstructor(cw, targetDescriptor, targetInternalName, proxyInternalName);
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


	private void proxyMethod(ClassWriter cw, Method method, String proxyInternalName,
																				 String targetInternalName, String targetDescriptor, Class targetClass) {
		String descriptor = Type.getMethodDescriptor(method);
		String[] exceptionTypes = convertToInternalTypes(method.getExceptionTypes());
		String methodName = method.getName();

		List chain = this.advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(this.advised, null, method, targetClass);

		CodeVisitor cv = cw.visitMethod(Constants.ACC_PUBLIC, methodName, descriptor, exceptionTypes, null);

		
		if (chain.isEmpty()) {
			proxyDirectToSuper(cv, method, proxyInternalName, targetInternalName, targetDescriptor);
		}
		else {
			proxyWithAdvice(cv, method, proxyInternalName, targetInternalName, targetDescriptor);
		}

	}

	private void proxyWithAdvice(CodeVisitor cv, Method method, String proxyInternalName, String targetInternalName, String targetDescriptor)
	{
		String methodName = method.getName();

		// TODO: need to calculate the size of the parameters
		Class[] parameterTypes = method.getParameterTypes();
		int parameterCount = parameterTypes.length;
		int localThis = 0;
		int localAdvised = 1 + parameterCount;
		int localTargetSource = 2 + parameterCount;
		int localTargetClass = 3 + parameterCount;
		int localTarget = 4 + parameterCount;
		int localArgs = 5 + parameterCount;
		int localArgTypes = 6 + parameterCount;
		int localMethod = 7 + parameterCount;
		int localAdviceChain = 8 + parameterCount;
		int localMethodInvocation = 9 + parameterCount;

		// start the try block
		Label openTry = new Label();
		cv.visitLabel(openTry);

		// load this
		cv.visitVarInsn(Constants.ALOAD, localThis);

		// load advised
		cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);
		cv.visitVarInsn(Constants.ASTORE, localAdvised);

		// load the target source
		cv.visitVarInsn(Constants.ALOAD, localAdvised);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
		cv.visitVarInsn(Constants.ASTORE, localTargetSource);

		// load the target class
		cv.visitVarInsn(Constants.ALOAD, localTargetSource);
		cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_CLASS_METHOD, GET_TARGET_CLASS_DESCRIPTOR);
		cv.visitVarInsn(Constants.ASTORE, localTargetClass);


		// load the target
		if (this.advised.getTargetSource().isStatic()) {
			// load the target directly
			cv.visitVarInsn(Constants.ALOAD, localThis);
			cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, TARGET_FIELD_NAME, targetDescriptor);
		}
		else {
			// load the target from the target source
			cv.visitVarInsn(Constants.ALOAD, localAdvised);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
			cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);
			cv.visitTypeInsn(Constants.CHECKCAST, targetInternalName);
		}
		cv.visitVarInsn(Constants.ASTORE, localTarget);

		// bundle up the arguments into an object[]
		bundleArgs(cv, parameterTypes, localArgs);

		// create the arg types array
		bundleArgTypes(cv, parameterTypes, localArgTypes);

		// now get the method via reflection
		cv.visitVarInsn(Constants.ALOAD, localTargetClass);
		cv.visitLdcInsn(methodName);
		cv.visitVarInsn(Constants.ALOAD, localArgTypes);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, CLASS_INTERNAL_NAME, GET_METHOD_METHOD, GET_METHOD_DESCRIPTOR);
		cv.visitVarInsn(Constants.ASTORE, localMethod);

		// get the AdvisorChainFactory
		cv.visitVarInsn(Constants.ALOAD, localAdvised);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_ADVISOR_CHAIN_FACTORY_METHOD, GET_ADVISOR_CHAIN_FACTORY_DESCRIPTOR);

		// get the advice chain
		cv.visitVarInsn(Constants.ALOAD, localAdvised);
		cv.visitInsn(Constants.ACONST_NULL);
		cv.visitVarInsn(Constants.ALOAD, localMethod);
		cv.visitVarInsn(Constants.ALOAD, localTargetClass);
		cv.visitMethodInsn(Constants.INVOKEINTERFACE, "org/springframework/aop/framework/AdvisorChainFactory", "getInterceptorsAndDynamicInterceptionAdvice", "(Lorg/springframework/aop/framework/Advised;Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/Class;)Ljava/util/List;");
		cv.visitVarInsn(Constants.ASTORE, localAdviceChain);

		// create the ReflectiveMethodInvocation object
		cv.visitTypeInsn(Constants.NEW, "org/springframework/aop/framework/ReflectiveMethodInvocation");
		cv.visitInsn(Constants.DUP);

		// load the args
		cv.visitVarInsn(Constants.ALOAD, localThis);
		cv.visitVarInsn(Constants.ALOAD, localTarget);
		cv.visitVarInsn(Constants.ALOAD, localMethod);
		cv.visitVarInsn(Constants.ALOAD, localArgs);
		cv.visitVarInsn(Constants.ALOAD, localTargetClass);
		cv.visitVarInsn(Constants.ALOAD, localAdviceChain);

		// create invoke the constructor
		cv.visitMethodInsn(Constants.INVOKESPECIAL, "org/springframework/aop/framework/ReflectiveMethodInvocation", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;Ljava/lang/Class;Ljava/util/List;)V");
		cv.visitVarInsn(Constants.ASTORE, localMethodInvocation);

		// invoke proceed
		cv.visitVarInsn(Constants.ALOAD, localMethodInvocation);
		cv.visitMethodInsn(Constants.INVOKEINTERFACE, "org/aopalliance/intercept/MethodInvocation", "proceed", "()Ljava/lang/Object;");

		// cast return value if required else pop
		Class returnType = method.getReturnType();

		if (returnType != void.class) {
			cv.visitTypeInsn(Constants.CHECKCAST, Type.getInternalName(returnType));
		} else {
			cv.visitInsn(Constants.POP);
		}

		// close the try block
		Label closeTry = new Label();
		cv.visitLabel(closeTry);

		// exit block
		Label exit = new Label();

		// return if required
		if (returnType != void.class) {
			cv.visitInsn(getReturnOpcodeForType(returnType));
		} else {
			cv.visitJumpInsn(Constants.GOTO, exit);
		}

		// create the catch blocks for the exceptions on the method
		int stackThrowable = 1 + parameterCount;


		// create the catch block for NoSuchMethodException
		Label catchNoSuchMethodException = new Label();
		cv.visitLabel(catchNoSuchMethodException);
		cv.visitVarInsn(Constants.ASTORE, stackThrowable);
		cv.visitTypeInsn(Constants.NEW, "java/lang/IllegalStateException");
		cv.visitInsn(Constants.DUP);
		cv.visitLdcInsn("Unable to access joinpoint via reflection");
		cv.visitVarInsn(Constants.ALOAD, stackThrowable);
		cv.visitMethodInsn(Constants.INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
		cv.visitInsn(Constants.ATHROW);

		// create the catch block for remaining Throwable
		Class[] exceptionTypes = method.getExceptionTypes();
		Label[] handlerLabels = new Label[exceptionTypes.length];

		for (int i = 0; i < exceptionTypes.length; i++) {
			Label handlerLabel = new Label();
			cv.visitLabel(handlerLabel);
			cv.visitVarInsn(Constants.ASTORE, 2);
			cv.visitVarInsn(Constants.ALOAD, 2);
			cv.visitInsn(Constants.ATHROW);
			handlerLabels[i] = handlerLabel;
		}

		Label catchThrowable = new Label();
		cv.visitLabel(catchThrowable);
		cv.visitVarInsn(Constants.ASTORE, stackThrowable);
		cv.visitTypeInsn(Constants.NEW, "java/lang/reflect/UndeclaredThrowableException");
		cv.visitInsn(Constants.DUP);
		cv.visitVarInsn(Constants.ALOAD, stackThrowable);
		cv.visitMethodInsn(Constants.INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V");
		cv.visitInsn(Constants.ATHROW);

		// close
		cv.visitLabel(exit);

		// return now for void
		if (returnType == void.class) {
			cv.visitInsn(getReturnOpcodeForType(method.getReturnType()));
		}

		for (int i = 0; i < handlerLabels.length; i++) {
			cv.visitTryCatchBlock(openTry, closeTry, handlerLabels[i], Type.getInternalName(exceptionTypes[i]));
		}

		cv.visitTryCatchBlock(openTry, closeTry, catchNoSuchMethodException, Type.getInternalName(NoSuchMethodException.class));
		cv.visitTryCatchBlock(openTry, closeTry, catchThrowable, Type.getInternalName(Throwable.class));
		cv.visitMaxs(0, 0);
	}

	private void bundleArgTypes(CodeVisitor cv, Class[] parameterTypes, int stackIndex) {
		int size = parameterTypes.length;

		if (size == 0) {
			cv.visitInsn(Constants.ACONST_NULL);
			cv.visitVarInsn(Constants.ASTORE, stackIndex);
		}
		else {
			visitIntegerInsn(size, cv);
			cv.visitTypeInsn(Constants.ANEWARRAY, CLASS_INTERNAL_NAME);
			cv.visitVarInsn(Constants.ASTORE, stackIndex);

			for (int i = 0; i < parameterTypes.length; i++) {
				Class parameterType = parameterTypes[i];

				// load the array
				cv.visitVarInsn(Constants.ALOAD, stackIndex);

				// load the index
				visitIntegerInsn(i, cv);

				// load the class
				if (parameterType.isPrimitive()) {
					visitGetPrimitiveType(cv, parameterType);
				}
				else {
					cv.visitLdcInsn(Type.getType(parameterType));
				}

				// store in array
				cv.visitInsn(Constants.AASTORE);
			}
		}
	}

	private void bundleArgs(CodeVisitor cv, Class[] parameterTypes, int stackIndex) {
		// create the object array
		int size = parameterTypes.length;

		if (size == 0) {
			cv.visitInsn(Constants.ACONST_NULL);
			cv.visitVarInsn(Constants.ASTORE, stackIndex);
		}
		else {
			visitIntegerInsn(size, cv);
			cv.visitTypeInsn(Constants.ANEWARRAY, OBJECT_INTERNAL_NAME);
			cv.visitVarInsn(Constants.ASTORE, stackIndex);

			int stackCount = 1;
			for (int i = 0; i < parameterTypes.length; i++) {
				Class parameterType = parameterTypes[i];

				// load the array
				cv.visitVarInsn(Constants.ALOAD, stackIndex);

				// load the index
				visitIntegerInsn(i, cv);

				// load the argument
				cv.visitVarInsn(getLoadOpcodeForType(parameterType), stackCount);

				if (parameterType.isPrimitive()) {
					// wrap primtive
					visitWrapPrimitive(cv, parameterType);
				}

				// store in array
				cv.visitInsn(Constants.AASTORE);

				stackCount += getFrameSpaceSize(parameterType);

			}
		}
	}

	private void visitWrapPrimitive(CodeVisitor cv, Class parameterType) {
		if (byte.class == parameterType) {
			cv.visitMethodInsn(Constants.INVOKESTATIC, Type.getInternalName(Byte.class), VALUE_OF_METHOD, "(B)Ljava/lang/Byte;");
		}
		else if (short.class == parameterType) {
			cv.visitMethodInsn(Constants.INVOKESTATIC, Type.getInternalName(Short.class), VALUE_OF_METHOD, "(S)Ljava/lang/Short;");
		}
		else if (int.class == parameterType) {
			cv.visitMethodInsn(Constants.INVOKESTATIC, Type.getInternalName(Integer.class), VALUE_OF_METHOD, "(I)Ljava/lang/Integer;");
		}
		else if (long.class == parameterType) {
			cv.visitMethodInsn(Constants.INVOKESTATIC, Type.getInternalName(Long.class), VALUE_OF_METHOD, "(J)Ljava/lang/Long;");
		}
		else if (float.class == parameterType) {
			cv.visitMethodInsn(Constants.INVOKESTATIC, Type.getInternalName(Float.class), VALUE_OF_METHOD, "(F)Ljava/lang/Float;");
		}
		else if (double.class == parameterType) {
			cv.visitMethodInsn(Constants.INVOKESTATIC, Type.getInternalName(Double.class), VALUE_OF_METHOD, "(D)Ljava/lang/Double;");
		}
		else if (char.class == parameterType) {
			cv.visitMethodInsn(Constants.INVOKESTATIC, Type.getInternalName(Character.class), VALUE_OF_METHOD, "(C)Ljava/lang/Character;");
		}
		else if (boolean.class == parameterType) {
			cv.visitMethodInsn(Constants.INVOKESTATIC, Type.getInternalName(Boolean.class), VALUE_OF_METHOD, "(Z)Ljava/lang/Boolean;");
		}
		else {
			throw new IllegalArgumentException("Cannot wrap non-primitive value: " + parameterType.getName());
		}
	}

	private void visitGetPrimitiveType(CodeVisitor cv, Class parameterType) {
		if (byte.class == parameterType) {
			cv.visitFieldInsn(Constants.GETSTATIC, Type.getInternalName(Byte.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (short.class == parameterType) {
			cv.visitFieldInsn(Constants.GETSTATIC, Type.getInternalName(Short.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (int.class == parameterType) {
			cv.visitFieldInsn(Constants.GETSTATIC, Type.getInternalName(Integer.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (long.class == parameterType) {
			cv.visitFieldInsn(Constants.GETSTATIC, Type.getInternalName(Long.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (float.class == parameterType) {
			cv.visitFieldInsn(Constants.GETSTATIC, Type.getInternalName(Float.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (double.class == parameterType) {
			cv.visitFieldInsn(Constants.GETSTATIC, Type.getInternalName(Double.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (char.class == parameterType) {
			cv.visitFieldInsn(Constants.GETSTATIC, Type.getInternalName(Character.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (boolean.class == parameterType) {
			cv.visitFieldInsn(Constants.GETSTATIC, Type.getInternalName(Boolean.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else {
			throw new IllegalArgumentException("Cannot get type for non-primitive value: " + parameterType.getName());
		}
	}

	private void visitIntegerInsn(int intValue, CodeVisitor cv) {
		switch (intValue) {
			case 0:
				cv.visitInsn(Constants.ICONST_0);
				break;
			case 1:
				cv.visitInsn(Constants.ICONST_1);
				break;
			case 2:
				cv.visitInsn(Constants.ICONST_2);
				break;
			case 3:
				cv.visitInsn(Constants.ICONST_3);
				break;
			case 4:
				cv.visitInsn(Constants.ICONST_4);
				break;
			case 5:
				cv.visitInsn(Constants.ICONST_5);
				break;
			default:
				cv.visitIntInsn(Constants.BIPUSH, intValue);
		}
	}


	private void proxyDirectToSuper(CodeVisitor cv, Method method, String proxyInternalName, String targetInternalName, String targetDescriptor)
	{
		String descriptor = Type.getMethodDescriptor(method);
		String methodName = method.getName();

		// load this
		cv.visitVarInsn(Constants.ALOAD, 0);

		if (advised.getTargetSource().isStatic()) {
			cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, TARGET_FIELD_NAME, targetDescriptor);
		}
		else {
			cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
			cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);
			cv.visitTypeInsn(Constants.CHECKCAST, targetInternalName);
		}

		// load args
		Class[] parameterTypes = method.getParameterTypes();
		int stackCount = 1;
		int argCount = parameterTypes.length;
		if (argCount > 0) {
			for (int x = 0; x < argCount; x++) {
				Class parameterType = parameterTypes[x];
				int frameSpaceSize = getFrameSpaceSize(parameterType);
				cv.visitVarInsn(getLoadOpcodeForType(parameterTypes[x]), stackCount);
				stackCount += frameSpaceSize;
			}
		}

		// invoke super
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, targetInternalName, methodName, descriptor);

		// return
		cv.visitInsn(getReturnOpcodeForType(method.getReturnType()));
		cv.visitMaxs(0, 0);
	}


	private void generateConstructor(final ClassWriter cw, final String targetDescriptor, final String targetInternalName, final String proxyClassInternalName)
	{

		CodeVisitor cv;

		// add field to store advised
		cw.visitField(Constants.ACC_PRIVATE, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR, null, null);


		boolean staticTargetSource = this.advised.getTargetSource().isStatic();

		// field to early load static TargetSource
		if (staticTargetSource && !(emptyTargetSource)) {
			cw.visitField(Constants.ACC_PRIVATE, TARGET_FIELD_NAME, targetDescriptor, null, null);
		}

		// add constructor to pass in the target
		String descriptor = getMethodDescriptor(void.class, new Class[]{AdvisedSupport.class});
		cv = cw.visitMethod(Constants.ACC_PUBLIC, CONSTRUCTOR_INTERNAL_NAME, descriptor, null, null);

		// invoke super
		String superName = (this.emptyTargetSource) ? OBJECT_INTERNAL_NAME : targetInternalName;

		cv.visitVarInsn(Constants.ALOAD, 0);
		cv.visitMethodInsn(Constants.INVOKESPECIAL, superName, CONSTRUCTOR_INTERNAL_NAME, NO_ARG_CONSTRUCTOR_DESCRIPTOR);


		// store advised in field
		cv.visitVarInsn(Constants.ALOAD, 0);
		cv.visitVarInsn(Constants.ALOAD, 1);
		cv.visitFieldInsn(Constants.PUTFIELD, proxyClassInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);

		// store target in field if needed;
		if (staticTargetSource && !(emptyTargetSource)) {
			Label openTry = new Label();
			cv.visitLabel(openTry);
			cv.visitVarInsn(Constants.ALOAD, 0);
			cv.visitVarInsn(Constants.ALOAD, 1);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
			cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);
			cv.visitTypeInsn(Constants.CHECKCAST, targetInternalName);
			cv.visitFieldInsn(Constants.PUTFIELD, proxyClassInternalName, TARGET_FIELD_NAME, targetDescriptor);
			Label closeTry = new Label();
			cv.visitLabel(closeTry);
			Label exit = new Label();
			cv.visitJumpInsn(Constants.GOTO, exit);
			Label handler = new Label();
			cv.visitLabel(handler);
			cv.visitVarInsn(Constants.ASTORE, 2);
			cv.visitTypeInsn(Constants.NEW, AOP_CONFIG_EXCEPTION_INTERNAL_NAME);
			cv.visitInsn(Constants.DUP);
			cv.visitLdcInsn("Unable to obtain target from static TargetSource");
			cv.visitVarInsn(Constants.ALOAD, 2);
			cv.visitMethodInsn(Constants.INVOKESPECIAL, AOP_CONFIG_EXCEPTION_INTERNAL_NAME, CONSTRUCTOR_INTERNAL_NAME, EXCEPTION_CONSTRUCTOR_DESCRIPTOR);
			cv.visitInsn(Constants.ATHROW);
			cv.visitLabel(exit);
			cv.visitInsn(Constants.RETURN);
			cv.visitTryCatchBlock(openTry, closeTry, handler, EXCEPTION_INTERNAL_NAME);
		}
		else {
			cv.visitInsn(Constants.RETURN);
		}


		// close
		cv.visitMaxs(0, 0);
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

	private int getLoadOpcodeForType(Class type) {
		if (type.isPrimitive()) {
			if (type == double.class) {
				return Constants.DLOAD;
			}
			else if (type == float.class) {
				return Constants.FLOAD;
			}
			else if (type == long.class) {
				return Constants.LLOAD;
			}
			else {
				return Constants.ILOAD;
			}
		}
		else {
			return Constants.ALOAD;
		}
	}

	private int getReturnOpcodeForType(Class type) {
		if (type.isPrimitive()) {
			if (type == double.class) {
				return Constants.DRETURN;
			}
			else if (type == float.class) {
				return Constants.FRETURN;
			}
			else if (type == long.class) {
				return Constants.LRETURN;
			}
			else if (type == void.class) {
				return Constants.RETURN;
			}
			else {
				return Constants.IRETURN;
			}
		}
		else {
			return Constants.ARETURN;
		}
	}

	private int getFrameSpaceSize(Class type) {
		if ((type == double.class) || (type == long.class)) {
			return 2;
		}
		else {
			return 1;
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

package org.springframework.aop.framework.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopContext;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * @author robh
 */
public abstract class AbstractCodeGenerationStrategy implements CodeGenerationStrategy {

	protected static final String NO_ARG_CONSTRUCTOR_DESCRIPTOR = "()V";

	protected static final String CONSTRUCTOR_INTERNAL_NAME = "<init>";

	protected static final String TARGET_FIELD_NAME = "__target";

	protected static final String ADVISED_FIELD_NAME = "__advised";

	protected static final Object PROXY_COUNT_LOCK = new Object();

	protected static final String GET_TARGET_SOURCE_METHOD = "getTargetSource";

	protected static final String ADVISED_SUPPORT_INTERNAL_NAME = Type.getInternalName(AdvisedSupport.class);

	protected static final String ADVISED_SUPPORT_DESCRIPTOR = Type.getDescriptor(AdvisedSupport.class);

	protected static final String GET_TARGET_SOURCE_DESCRIPTOR = "()Lorg/springframework/aop/TargetSource;";

	protected static final String TARGET_SOURCE_INTERNAL_NAME = "org/springframework/aop/TargetSource";

	protected static final String GET_TARGET_METHOD = "getTarget";

	protected static final String EXCEPTION_INTERNAL_NAME = "java/lang/Exception";

	protected static final String AOP_CONFIG_EXCEPTION_INTERNAL_NAME = "org/springframework/aop/framework/AopConfigException";

	protected static final String EXCEPTION_CONSTRUCTOR_DESCRIPTOR = "(Ljava/lang/String;Ljava/lang/Throwable;)V";

	protected static final String GET_TARGET_DESCRIPTOR = "()Ljava/lang/Object;";

	protected static final String GET_TARGET_CLASS_METHOD = "getTargetClass";

	protected static final String GET_TARGET_CLASS_DESCRIPTOR = "()Ljava/lang/Class;";

	protected static final String OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);

	protected static final String VALUE_OF_METHOD = "valueOf";

	protected static final String CLASS_INTERNAL_NAME = "java/lang/Class";

	protected static final String GET_METHOD_METHOD = "getMethod";

	protected static final String GET_METHOD_DESCRIPTOR = "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;";

	protected static final String CLASS_DESCRIPTOR = Type.getDescriptor(Class.class);

	protected static final String TYPE_FIELD = "TYPE";

	protected static final String GET_ADVISOR_CHAIN_FACTORY_DESCRIPTOR = "()Lorg/springframework/aop/framework/AdvisorChainFactory;";

	protected static final String GET_ADVISOR_CHAIN_FACTORY_METHOD = "getAdvisorChainFactory";

	protected static final String SET_CURRENT_PROXY_METHOD = "setCurrentProxy";

	protected static final String AOP_CONTEXT_INTERNAL_NAME = Type.getInternalName(AopContext.class);

	protected static final String SET_CURRENT_PROXY_DESCRIPTOR = "(Ljava/lang/Object;)Ljava/lang/Object;";

	protected static final String UNDECLARED_THROWABLE_EXCEPTION_INTERNAL_NAME = Type.getInternalName(UndeclaredThrowableException.class);

	protected static final String SINGLE_ARG_EXCEPTION_CONSTRUCTOR_DESCRIPTOR = "(Ljava/lang/Throwable;)V";

	protected static final String RELEASE_TARGET_METHOD = "releaseTarget";

	protected static final String RELEASE_TARGET_DESCRIPTOR = "(Ljava/lang/Object;)V";

	protected int getLoadOpcodeForType(Class type) {
		if (type.isPrimitive()) {
			if (type == double.class) {
				return Opcodes.DLOAD;
			}
			else if (type == float.class) {
				return Opcodes.FLOAD;
			}
			else if (type == long.class) {
				return Opcodes.LLOAD;
			}
			else {
				return Opcodes.ILOAD;
			}
		}
		else {
			return Opcodes.ALOAD;
		}
	}

	protected int getStoreOpcodeForType(Class type) {
		if (type.isPrimitive()) {
			if (type == double.class) {
				return Opcodes.DSTORE;
			}
			else if (type == float.class) {
				return Opcodes.FSTORE;
			}
			else if (type == long.class) {
				return Opcodes.LSTORE;
			}
			else {
				return Opcodes.ISTORE;
			}
		}
		else {
			return Opcodes.ASTORE;
		}
	}

	protected int getReturnOpcodeForType(Class type) {
		if (type.isPrimitive()) {
			if (type == double.class) {
				return Opcodes.DRETURN;
			}
			else if (type == float.class) {
				return Opcodes.FRETURN;
			}
			else if (type == long.class) {
				return Opcodes.LRETURN;
			}
			else if (type == void.class) {
				return Opcodes.RETURN;
			}
			else {
				return Opcodes.IRETURN;
			}
		}
		else {
			return Opcodes.ARETURN;
		}
	}

	protected int calculateInitialLocalsOffset(Class[] args) {
		int localsSize = 0;
		for (int i = 0; i < args.length; i++) {
			localsSize += getLocalsSizeForType(args[i]);
		}
		return localsSize;
	}

	protected int getLocalsSizeForType(Class type) {
		if ((type == double.class) || (type == long.class)) {
			return 2;
		}
		else {
			return 1;
		}
	}

	protected void visitWrapPrimitive(MethodVisitor mv, Class parameterType) {
		if (byte.class == parameterType) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class), VALUE_OF_METHOD, "(B)Ljava/lang/Byte;");
		}
		else if (short.class == parameterType) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Short.class), VALUE_OF_METHOD, "(S)Ljava/lang/Short;");
		}
		else if (int.class == parameterType) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Integer.class), VALUE_OF_METHOD, "(I)Ljava/lang/Integer;");
		}
		else if (long.class == parameterType) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Long.class), VALUE_OF_METHOD, "(J)Ljava/lang/Long;");
		}
		else if (float.class == parameterType) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Float.class), VALUE_OF_METHOD, "(F)Ljava/lang/Float;");
		}
		else if (double.class == parameterType) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Double.class), VALUE_OF_METHOD, "(D)Ljava/lang/Double;");
		}
		else if (char.class == parameterType) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Character.class), VALUE_OF_METHOD, "(C)Ljava/lang/Character;");
		}
		else if (boolean.class == parameterType) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Boolean.class), VALUE_OF_METHOD, "(Z)Ljava/lang/Boolean;");
		}
		else {
			throw new IllegalArgumentException("Cannot wrap non-primitive value: " + parameterType.getName());
		}
	}

	protected void visitGetPrimitiveType(MethodVisitor mv, Class parameterType) {
		if (byte.class == parameterType) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Byte.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (short.class == parameterType) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Short.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (int.class == parameterType) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Integer.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (long.class == parameterType) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Long.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (float.class == parameterType) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Float.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (double.class == parameterType) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Double.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (char.class == parameterType) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Character.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else if (boolean.class == parameterType) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Boolean.class), TYPE_FIELD, CLASS_DESCRIPTOR);
		}
		else {
			throw new IllegalArgumentException("Cannot get type for non-primitive value: " + parameterType.getName());
		}
	}

	protected void visitUnwrapPrimtiveType(MethodVisitor mv, Class parameterType) {
		if (byte.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Byte.class);
			mv.visitTypeInsn(Opcodes.CHECKCAST, wrapperInternalName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperInternalName, "byteValue", "()B");
		}
		else if (short.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Short.class);
			mv.visitTypeInsn(Opcodes.CHECKCAST, wrapperInternalName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperInternalName, "shortValue", "()S");
		}
		else if (int.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Integer.class);
			mv.visitTypeInsn(Opcodes.CHECKCAST, wrapperInternalName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperInternalName, "intValue", "()I");
		}
		else if (long.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Long.class);
			mv.visitTypeInsn(Opcodes.CHECKCAST, wrapperInternalName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperInternalName, "longValue", "()J");
		}
		else if (float.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Float.class);
			mv.visitTypeInsn(Opcodes.CHECKCAST, wrapperInternalName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperInternalName, "floatValue", "()F");
		}
		else if (double.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Double.class);
			mv.visitTypeInsn(Opcodes.CHECKCAST, wrapperInternalName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperInternalName, "doubleValue", "()D");
		}
		else if (char.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Character.class);
			mv.visitTypeInsn(Opcodes.CHECKCAST, wrapperInternalName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperInternalName, "charValue", "()C");
		}
		else if (boolean.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Boolean.class);
			mv.visitTypeInsn(Opcodes.CHECKCAST, wrapperInternalName);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperInternalName, "booleanValue", "()Z");
		}
		else {
			throw new IllegalArgumentException("Cannot unwrap non-primitive value: " + parameterType.getName());
		}
	}

	protected void visitIntegerInsn(int intValue, MethodVisitor mv) {
		switch (intValue) {
			case 0:
				mv.visitInsn(Opcodes.ICONST_0);
				break;
			case 1:
				mv.visitInsn(Opcodes.ICONST_1);
				break;
			case 2:
				mv.visitInsn(Opcodes.ICONST_2);
				break;
			case 3:
				mv.visitInsn(Opcodes.ICONST_3);
				break;
			case 4:
				mv.visitInsn(Opcodes.ICONST_4);
				break;
			case 5:
				mv.visitInsn(Opcodes.ICONST_5);
				break;
			default:
				mv.visitIntInsn(Opcodes.BIPUSH, intValue);
		}
	}

	protected String getMethodDescriptor(Class returnType, Class[] argumentTypes) {
		Type asmReturnType = Type.getType(returnType);

		Type[] asmArgumentTypes = new Type[argumentTypes.length];
		for (int i = 0; i < asmArgumentTypes.length; i++) {
			asmArgumentTypes[i] = Type.getType(argumentTypes[i]);
		}

		return Type.getMethodDescriptor(asmReturnType, asmArgumentTypes);
	}

	protected String[] convertToInternalTypes(Class[] classes) {
		String[] types = new String[classes.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = Type.getInternalName(classes[i]);
		}
		return types;
	}
}


package org.springframework.aop.framework.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.Constants;
import org.objectweb.asm.CodeVisitor;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopContext;

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

	protected int getLoadOpcodeForType(Class type) {
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

	protected int getStoreOpcodeForType(Class type) {
		if (type.isPrimitive()) {
			if (type == double.class) {
				return Constants.DSTORE;
			}
			else if (type == float.class) {
				return Constants.FSTORE;
			}
			else if (type == long.class) {
				return Constants.LSTORE;
			}
			else {
				return Constants.ISTORE;
			}
		}
		else {
			return Constants.ASTORE;
		}
	}

	protected int getReturnOpcodeForType(Class type) {
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

	protected int getFrameSpaceSize(Class type) {
		if ((type == double.class) || (type == long.class)) {
			return 2;
		}
		else {
			return 1;
		}
	}

	protected void visitWrapPrimitive(CodeVisitor cv, Class parameterType) {
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

	protected void visitGetPrimitiveType(CodeVisitor cv, Class parameterType) {
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

	protected void visitUnwrapPrimtiveType(CodeVisitor cv, Class parameterType) {
		if (byte.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Byte.class);
			cv.visitTypeInsn(Constants.CHECKCAST, wrapperInternalName);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, wrapperInternalName, "byteValue", "()B");
		}
		else if (short.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Short.class);
			cv.visitTypeInsn(Constants.CHECKCAST, wrapperInternalName);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, wrapperInternalName, "shortValue", "()S");
		}
		else if (int.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Integer.class);
			cv.visitTypeInsn(Constants.CHECKCAST, wrapperInternalName);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, wrapperInternalName, "intValue", "()I");
		}
		else if (long.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Long.class);
			cv.visitTypeInsn(Constants.CHECKCAST, wrapperInternalName);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, wrapperInternalName, "longValue", "()J");
		}
		else if (float.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Float.class);
			cv.visitTypeInsn(Constants.CHECKCAST, wrapperInternalName);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, wrapperInternalName, "floatValue", "()F");
		}
		else if (double.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Double.class);
			cv.visitTypeInsn(Constants.CHECKCAST, wrapperInternalName);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, wrapperInternalName, "doubleValue", "()D");
		}
		else if (char.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Character.class);
			cv.visitTypeInsn(Constants.CHECKCAST, wrapperInternalName);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, wrapperInternalName, "charValue", "()C");
		}
		else if (boolean.class == parameterType) {
			String wrapperInternalName = Type.getInternalName(Boolean.class);
			cv.visitTypeInsn(Constants.CHECKCAST, wrapperInternalName);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, wrapperInternalName, "booleanValue", "()Z");
		}
		else {
			throw new IllegalArgumentException("Cannot unwrap non-primitive value: " + parameterType.getName());
		}
	}

	protected void visitIntegerInsn(int intValue, CodeVisitor cv) {
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

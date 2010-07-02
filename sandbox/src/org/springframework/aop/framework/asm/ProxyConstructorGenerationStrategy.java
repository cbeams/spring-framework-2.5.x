package org.springframework.aop.framework.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AdvisedSupport;

import java.lang.reflect.Method;

/**
 * @author robh
 */
public class ProxyConstructorGenerationStrategy extends AbstractCodeGenerationStrategy {

	public void generate(ClassWriter cw, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {

		MethodVisitor mv;

		// add field to store advised
		cw.visitField(Opcodes.ACC_PRIVATE, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR, null, null);

		TargetSource targetSource = advised.getTargetSource();

		boolean staticTargetSource = targetSource.isStatic();
		boolean emptyTargetSource = (targetSource == AdvisedSupport.EMPTY_TARGET_SOURCE);

		// field to early load static TargetSource
		if (staticTargetSource && !(emptyTargetSource)) {
			cw.visitField(Opcodes.ACC_PRIVATE, TARGET_FIELD_NAME, targetDescriptor, null, null);
		}

		// add constructor to pass in the target
		String descriptor = getMethodDescriptor(void.class, new Class[]{AdvisedSupport.class});
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, CONSTRUCTOR_INTERNAL_NAME, descriptor, null, null);

		// invoke super
		String superName = (emptyTargetSource) ? OBJECT_INTERNAL_NAME : targetInternalName;

		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, CONSTRUCTOR_INTERNAL_NAME, NO_ARG_CONSTRUCTOR_DESCRIPTOR);

		// store advised in field
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitFieldInsn(Opcodes.PUTFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);

		// store target in field if needed;
		if (staticTargetSource && !(emptyTargetSource)) {
			Label openTry = new Label();
			mv.visitLabel(openTry);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);
			mv.visitTypeInsn(Opcodes.CHECKCAST, targetInternalName);
			mv.visitFieldInsn(Opcodes.PUTFIELD, proxyInternalName, TARGET_FIELD_NAME, targetDescriptor);
			Label closeTry = new Label();
			mv.visitLabel(closeTry);
			Label exit = new Label();
			mv.visitJumpInsn(Opcodes.GOTO, exit);
			Label handler = new Label();
			mv.visitLabel(handler);
			mv.visitVarInsn(Opcodes.ASTORE, 2);
			mv.visitTypeInsn(Opcodes.NEW, AOP_CONFIG_EXCEPTION_INTERNAL_NAME);
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn("Unable to obtain target from static TargetSource");
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, AOP_CONFIG_EXCEPTION_INTERNAL_NAME, CONSTRUCTOR_INTERNAL_NAME, EXCEPTION_CONSTRUCTOR_DESCRIPTOR);
			mv.visitInsn(Opcodes.ATHROW);
			mv.visitLabel(exit);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitTryCatchBlock(openTry, closeTry, handler, EXCEPTION_INTERNAL_NAME);
		}
		else {
			mv.visitInsn(Opcodes.RETURN);
		}

		// close
		mv.visitMaxs(0, 0);
	}

}


package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.TargetSource;

/**
 * @author robh
 */
public class ProxyConstructorGenerationStrategy extends AbstractCodeGenerationStrategy {

	public void generate(ClassWriter cw, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {

		CodeVisitor cv;

		// add field to store advised
		cw.visitField(Constants.ACC_PRIVATE, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR, null, null);

		TargetSource targetSource = advised.getTargetSource();

		boolean staticTargetSource = targetSource.isStatic();
		boolean emptyTargetSource = (targetSource == AdvisedSupport.EMPTY_TARGET_SOURCE);

		// field to early load static TargetSource
		if (staticTargetSource && !(emptyTargetSource)) {
			cw.visitField(Constants.ACC_PRIVATE, TARGET_FIELD_NAME, targetDescriptor, null, null);
		}

		// add constructor to pass in the target
		String descriptor = getMethodDescriptor(void.class, new Class[]{AdvisedSupport.class});
		cv = cw.visitMethod(Constants.ACC_PUBLIC, CONSTRUCTOR_INTERNAL_NAME, descriptor, null, null);

		// invoke super
		String superName = (emptyTargetSource) ? OBJECT_INTERNAL_NAME : targetInternalName;

		cv.visitVarInsn(Constants.ALOAD, 0);
		cv.visitMethodInsn(Constants.INVOKESPECIAL, superName, CONSTRUCTOR_INTERNAL_NAME, NO_ARG_CONSTRUCTOR_DESCRIPTOR);


		// store advised in field
		cv.visitVarInsn(Constants.ALOAD, 0);
		cv.visitVarInsn(Constants.ALOAD, 1);
		cv.visitFieldInsn(Constants.PUTFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);

		// store target in field if needed;
		if (staticTargetSource && !(emptyTargetSource)) {
			Label openTry = new Label();
			cv.visitLabel(openTry);
			cv.visitVarInsn(Constants.ALOAD, 0);
			cv.visitVarInsn(Constants.ALOAD, 1);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
			cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);
			cv.visitTypeInsn(Constants.CHECKCAST, targetInternalName);
			cv.visitFieldInsn(Constants.PUTFIELD, proxyInternalName, TARGET_FIELD_NAME, targetDescriptor);
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

}

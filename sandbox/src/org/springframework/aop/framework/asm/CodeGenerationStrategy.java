
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public interface CodeGenerationStrategy {

	void generate(ClassWriter cw, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor);
}

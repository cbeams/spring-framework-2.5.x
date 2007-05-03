/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.core;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import org.springframework.util.ClassUtils;

/**
 * Implementation of ParameterNameDiscover that uses the LocalVariableTable
 * information in the method attributes to discover parameter names. Returns
 * <code>null</code> if the class file was compiled without debug information.
 *
 * <p>Uses ObjectWeb's ASM library for analyzing class files.
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class LocalVariableTableParameterNameDiscoverer implements ParameterNameDiscoverer {

	private static Log logger = LogFactory.getLog(LocalVariableTableParameterNameDiscoverer.class);


	public String[] getParameterNames(Method method) {
		ParameterNameDiscoveringVisitor visitor = null;
		try {
			visitor = visitMethod(method);
			if (visitor.foundTargetMember()) {
				return visitor.getParameterNames();
			} 
		} 
		catch (IOException ex) {
			// We couldn't load the class file, which is not fatal as it
			// simply means this method of discovering parameter names won't work.
			if (logger.isDebugEnabled()) {
				logger.debug("IOException whilst attempting to read '.class' file for class [" +
						method.getDeclaringClass().getName() +
						"] - unable to determine parameter names for method: " + method, ex);
			}
		}
		return null;
	}

	public String[] getParameterNames(Constructor ctor) {
		ParameterNameDiscoveringVisitor visitor = null;
		try {
			visitor = visitConstructor(ctor);
			if (visitor.foundTargetMember()) {
				return visitor.getParameterNames();
			} 
		}
		catch (IOException ex) {
			// We couldn't load the class file, which is not fatal as it
			// simply means this method of discovering parameter names won't work.
			if (logger.isDebugEnabled()) {
				logger.debug("IOException whilst attempting to read '.class' file for class [" +
						ctor.getDeclaringClass().getName() +
						"] - unable to determine parameter names for constructor: " + ctor,
						ex);
			}
		}
		return null;
	}

	/**
	 * Visit the given method and discover its parameter names.
	 */
	private ParameterNameDiscoveringVisitor visitMethod(Method method) throws IOException {
		ClassReader classReader = createClassReader(method.getDeclaringClass());
		FindMethodParameterNamesClassVisitor classVisitor = new FindMethodParameterNamesClassVisitor(method);
		classReader.accept(classVisitor, false);
		return classVisitor;
	}

	/**
	 * Visit the given constructor and discover its parameter names.
	 */
	private ParameterNameDiscoveringVisitor visitConstructor(Constructor ctor) throws IOException {
		ClassReader classReader = createClassReader(ctor.getDeclaringClass());
		FindConstructorParameterNamesClassVisitor classVisitor = new FindConstructorParameterNamesClassVisitor(ctor);
		classReader.accept(classVisitor, false);
		return classVisitor;
	}

	/**
	 * Create a ClassReader for the given class.
	 */
	private ClassReader createClassReader(Class clazz) throws IOException {
		return new ClassReader(clazz.getResourceAsStream(ClassUtils.getClassFileName(clazz)));
	}


	/**
	 * Helper class that looks for a given member name and descriptor, and then
	 * attempts to find the parameter names for that member.
	 */
	private static abstract class ParameterNameDiscoveringVisitor extends EmptyVisitor {

		private String methodNameToMatch;

		private String descriptorToMatch;

		private int numParamsExpected;
		
		/*
		 * the nth entry contains the slot index of the LVT table entry holding the
		 * argument name for the nth parameter
		 */
		private int[] lvtSlotIndex;

		private boolean foundTargetMember = false;

		private String[] parameterNames;
		
		public ParameterNameDiscoveringVisitor(String name, boolean isStatic, Class[] paramTypes) {
			this.methodNameToMatch = name;
			this.numParamsExpected = paramTypes.length;
			computeLVTSlotIndices(isStatic, paramTypes);
		}
		
		public void setDescriptorToMatch(String descriptor) {
			this.descriptorToMatch = descriptor;			
		}

		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (name.equals(this.methodNameToMatch) &&
				desc.equals(this.descriptorToMatch)) {
				this.foundTargetMember = true;
				return new LocalVariableTableVisitor(isStatic(access), this,this.numParamsExpected,this.lvtSlotIndex);	
			} 
			else {
				// not interested in this method...
				return null;
			}
		}
		
		private boolean isStatic(int access) {
			return ((access & Opcodes.ACC_STATIC) > 0);
		}

		public boolean foundTargetMember() {
			return this.foundTargetMember;
		}
		
		public String[] getParameterNames() {
			if (!foundTargetMember()) {
				throw new IllegalStateException("Can't ask for parameter names when target member has not been found");
			}
			
			return this.parameterNames;
		}
		
		public void setParameterNames(String[] names) {
			this.parameterNames = names;
		}
		
		private void computeLVTSlotIndices(boolean isStatic, Class[] paramTypes) {
			this.lvtSlotIndex = new int[paramTypes.length];
			int nextIndex = (isStatic ? 0 : 1);
			for (int i = 0; i < paramTypes.length; i++) {
				this.lvtSlotIndex[i] = nextIndex;
				if (isWideType(paramTypes[i])) {
					nextIndex += 2;
				}
				else {
					nextIndex++;
				}
			}
		}
		
		private boolean isWideType(Class aType) {
			return (aType ==Long.TYPE || aType == Double.TYPE);
		}
	}


	private static class FindMethodParameterNamesClassVisitor extends ParameterNameDiscoveringVisitor {
		
		public FindMethodParameterNamesClassVisitor(Method method) {
			super(method.getName(),Modifier.isStatic(method.getModifiers()),method.getParameterTypes());
			setDescriptorToMatch(Type.getMethodDescriptor(method));
		}
	}


	private static class FindConstructorParameterNamesClassVisitor extends ParameterNameDiscoveringVisitor {
		
		public FindConstructorParameterNamesClassVisitor(Constructor cons) {
			super("<init>",false,cons.getParameterTypes());
			Type[] pTypes = new Type[cons.getParameterTypes().length];
			for (int i = 0; i < pTypes.length; i++) {
				pTypes[i] = Type.getType(cons.getParameterTypes()[i]);
			}
			setDescriptorToMatch(Type.getMethodDescriptor(Type.VOID_TYPE,pTypes));
		}
	}


	private static class LocalVariableTableVisitor extends EmptyVisitor {

		private boolean isStatic;
		private ParameterNameDiscoveringVisitor memberVisitor;
		private int numParameters;
		private int[] lvtSlotIndices;
		private String[] parameterNames;
		private boolean hasLVTInfo = false;

		public LocalVariableTableVisitor(
				boolean isStatic, ParameterNameDiscoveringVisitor memberVisitor, int numParams, int[] lvtSlotIndices) {
			this.isStatic = isStatic;
			this.numParameters = numParams;
			this.parameterNames = new String[this.numParameters];
			this.memberVisitor = memberVisitor;
			this.lvtSlotIndices = lvtSlotIndices;
		}

		public void visitLocalVariable(
				String name, String description, String signature, Label start, Label end, int index) {
			this.hasLVTInfo = true;
			if (isMethodArgumentSlot(index)) {
				this.parameterNames[parameterNameIndexForSlot(index)] = name;
			}
		}

		public void visitEnd() {
			if (this.hasLVTInfo || this.isStatic && numParameters == 0) {
				 // visitLocalVariable will never be called for static no args methods
				 // which doesn't use any local variables.
				 // This means that hasLVTInfo could be false for that kind of methods
				 // even if the class has local variable info.
				this.memberVisitor.setParameterNames(this.parameterNames);
			}
		}

		/**
		 * An lvt entry describes an argument (as opposed to a local var) if 
		 * it appears in the lvtSlotIndices table
		 */
		private boolean isMethodArgumentSlot(int index) {
			for (int i = 0; i < this.lvtSlotIndices.length; i++) {
				if (this.lvtSlotIndices[i] == index) {
					return true;
				}
			}
			return false;
		}

		private int parameterNameIndexForSlot(int slot) {
			for (int i = 0; i < this.lvtSlotIndices.length; i++) {
				if (this.lvtSlotIndices[i] == slot) {
					return i;
				}
			}
			throw new IllegalStateException(
					"Asked for index for a slot which failed the isMethodArgumentSlot test: " + slot);
		}
	}

}

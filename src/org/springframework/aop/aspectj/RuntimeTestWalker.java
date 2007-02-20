/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.aspectj;

import java.lang.reflect.Field;

import org.aspectj.weaver.ast.And;
import org.aspectj.weaver.ast.Call;
import org.aspectj.weaver.ast.FieldGetCall;
import org.aspectj.weaver.ast.HasAnnotation;
import org.aspectj.weaver.ast.ITestVisitor;
import org.aspectj.weaver.ast.Instanceof;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Not;
import org.aspectj.weaver.ast.Or;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.internal.tools.MatchingContextBasedTest;
import org.aspectj.weaver.reflect.ReflectionVar;
import org.aspectj.weaver.reflect.ShadowMatchImpl;
import org.aspectj.weaver.tools.ShadowMatch;

/**
 * <p>This class encapsulates some AspectJ internal knowledge that should be
 * pushed back into the AspectJ project in a future release. 
 *
 * <p>It relies on implementation specific knowledge in AspectJ to break
 * encapsulation and do something AspectJ was not designed to do :- query
 * the types of runtime tests that will be performed. The code here should
 * migrate to ShadowMatch.getVariablesInvolvedInRuntimeTest() or some similar
 * operation. 
 *
 * <p>See https://bugs.eclipse.org/bugs/show_bug.cgi?id=151593
 *
 * @author Adrian Colyer
 * @since 2.0
 */
public class RuntimeTestWalker {

	private Test runtimeTest;
	
	public RuntimeTestWalker(ShadowMatch shadowMatch) {
		ShadowMatchImpl shadowMatchImplementation = (ShadowMatchImpl) shadowMatch;
		try {
			Field testField = shadowMatchImplementation.getClass().getDeclaredField("residualTest");
			testField.setAccessible(true);
			this.runtimeTest = (Test) testField.get(shadowMatch);
		}
		catch(NoSuchFieldException noSuchFieldEx) {
			throw new IllegalStateException("the version of aspectjtools.jar / aspectjweaver.jar " +
					"on the classpath is incompatible with this version of Spring:- expected field " +
					"'runtimeTest' is not present on ShadowMatchImpl class");
		}
		catch (IllegalAccessException illegalAccessEx) {
			// famous last words... but I don't see how this can happen given the
			// setAccessible call above
			throw new IllegalStateException("Unable to access ShadowMatchImpl.runtimeTest field.");
		}
	}

	/**
	 * If the test uses any of the this, target, at_this, at_target, and at_annotation vars,
	 * then it tests subtype sensitive vars.
	 */
	public boolean testsSubtypeSensitiveVars() {
		return new SubtypeSensitiveVarTypeTestVisitor().testsSubtypeSensitiveVars(this.runtimeTest);
	}


	private static class SubtypeSensitiveVarTypeTestVisitor implements ITestVisitor {

		private static final int AT_THIS_VAR = 3;
		private static final int AT_TARGET_VAR = 4;
		private static final int AT_ANNOTATION_VAR = 8;

		private final Object thisObj = new Object();
		private final Object targetObj = new Object();
		private final Object[] argsObjs = new Object[0];

		private boolean testsSubtypeSensitiveVars = false;

		public boolean testsSubtypeSensitiveVars(Test aTest) {
			aTest.accept(this);
			return this.testsSubtypeSensitiveVars;
		}
		
		public void visit(And e) {
			e.getLeft().accept(this);
			e.getRight().accept(this);
		}

		public void visit(Or e) {
			e.getLeft().accept(this);
			e.getRight().accept(this);
		}

		public void visit(Not e) {
			e.getBody().accept(this);
		}

		public void visit(Instanceof i) {
			ReflectionVar v = (ReflectionVar) i.getVar();
			Object varUnderTest = v.getBindingAtJoinPoint(thisObj,targetObj,argsObjs);
			if ((varUnderTest == thisObj) || (varUnderTest == targetObj)) {
				this.testsSubtypeSensitiveVars = true;
			}
		}

		public void visit(HasAnnotation hasAnn) {
			// if you thought things were bad before, now we sink to new levels
			// of horror...
			ReflectionVar v = (ReflectionVar) hasAnn.getVar();
			try {
				Field varTypeField = ReflectionVar.class.getDeclaredField("varType");
				varTypeField.setAccessible(true);
				Integer varTypeValue = (Integer) varTypeField.get(v);
				int varType = varTypeValue.intValue();
				if ((varType == AT_THIS_VAR) ||
					(varType == AT_TARGET_VAR) ||
					(varType == AT_ANNOTATION_VAR)) {
					this.testsSubtypeSensitiveVars = true;
				}
			}
			catch(NoSuchFieldException noSuchFieldEx) {
				throw new IllegalStateException("the version of aspectjtools.jar / aspectjweaver.jar " +
						"on the classpath is incompatible with this version of Spring:- expected field " +
						"'varType' is not present on ReflectionVar class");
			}
			catch(IllegalAccessException illegalAccessEx) {
				// famous last words... but I don't see how this can happen given the setAccessible call
				// above
				throw new IllegalStateException("Unable to access ReflectionVar.varType field.");
			}
		}

		public void visit(Literal e) {
			// NO-OP
		}

		public void visit(Call e) {
			// NO-OP
		}

		public void visit(FieldGetCall e) {
			// NO-OP
		}

		public void visit(MatchingContextBasedTest arg0) {
			// NO-OP
		}
		
	}
}

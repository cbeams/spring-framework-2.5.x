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

package org.springframework.aop.aspectj;

import java.lang.reflect.Field;

import org.aspectj.weaver.ResolvedType;
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
 * <p>See <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=151593"/>.
 *
 * @author Adrian Colyer
 * @author Ramnivas Laddad
 * @since 2.0
 */
public class RuntimeTestWalker {

	private final Test runtimeTest;
	

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
			// Famous last words... but I don't see how this can happen given the
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

	public boolean testThisInstanceOfResidue(Object thiz) {
		return new ThisInstanceOfResidueTestVisitor(thiz).thisInstanceOfMatches(this.runtimeTest);
	}

	private static class TestVisitorAdapter implements ITestVisitor {

		protected static final int THIS_VAR = 0;
		protected static final int AT_THIS_VAR = 3;
		protected static final int AT_TARGET_VAR = 4;
		protected static final int AT_ANNOTATION_VAR = 8;

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
		}

		public void visit(Literal literal) {
		}

		public void visit(Call call) {
		}

		public void visit(FieldGetCall fieldGetCall) {
		}

		public void visit(HasAnnotation hasAnnotation) {
		}

		public void visit(MatchingContextBasedTest matchingContextTest) {
		}
		
		protected int getVarType(ReflectionVar v) {
			try {
				Field varTypeField = ReflectionVar.class.getDeclaredField("varType");
				varTypeField.setAccessible(true);
				Integer varTypeValue = (Integer) varTypeField.get(v);
				return varTypeValue.intValue();
			}
			catch (NoSuchFieldException noSuchFieldEx) {
				throw new IllegalStateException("the version of aspectjtools.jar / aspectjweaver.jar " +
						"on the classpath is incompatible with this version of Spring:- expected field " +
						"'varType' is not present on ReflectionVar class");
			}
			catch (IllegalAccessException illegalAccessEx) {
				// Famous last words... but I don't see how this can happen given the
				// makeAccessible call above
				throw new IllegalStateException("Unable to access ReflectionVar.varType field.");
			}
		}
	}


	/**
	 * Check if residue of this(TYPE) kind. See SPR-2979 for more details.
	 */
	private static class ThisInstanceOfResidueTestVisitor extends TestVisitorAdapter {

		private Object thiz;

		private boolean matches = true;
		
		public ThisInstanceOfResidueTestVisitor(Object thiz) {
			this.thiz = thiz;
		}
		
		public boolean thisInstanceOfMatches(Test test) {
			test.accept(this);
			return matches;
		}

		public void visit(Instanceof i) {
			ResolvedType type = (ResolvedType)i.getType();
			int varType = getVarType((ReflectionVar)i.getVar());
			// We are concerned only about this() pointcut.
			// TODO: Optimization: Process only if this() specifies a type and not identifier.
			if (varType != THIS_VAR) {
				return;
			}
			try {
				Class typeClass = Class.forName(type.getName());
				// Don't use ReflectionType.isAssignableFrom() as it won't be aware of (Spring) mixins
				if (!typeClass.isAssignableFrom(thiz.getClass())) {
					this.matches = false;
				}
			}
			catch (ClassNotFoundException ex) {
				this.matches = false;
			}
		}
	}


	private static class SubtypeSensitiveVarTypeTestVisitor extends TestVisitorAdapter {

		private final Object thisObj = new Object();
		private final Object targetObj = new Object();
		private final Object[] argsObjs = new Object[0];

		private boolean testsSubtypeSensitiveVars = false;

		public boolean testsSubtypeSensitiveVars(Test aTest) {
			aTest.accept(this);
			return this.testsSubtypeSensitiveVars;
		}
		
		public void visit(Instanceof i) {
			ReflectionVar v = (ReflectionVar) i.getVar();
			Object varUnderTest = v.getBindingAtJoinPoint(thisObj,targetObj,argsObjs);
			if ((varUnderTest == thisObj) || (varUnderTest == targetObj)) {
				this.testsSubtypeSensitiveVars = true;
			}
		}

		public void visit(HasAnnotation hasAnn) {
			// If you thought things were bad before, now we sink to new levels of horror...
			ReflectionVar v = (ReflectionVar) hasAnn.getVar();
			int varType = getVarType(v);
				if ((varType == AT_THIS_VAR) || (varType == AT_TARGET_VAR) || (varType == AT_ANNOTATION_VAR)) {
				this.testsSubtypeSensitiveVars = true;
			}
		}
	}

}

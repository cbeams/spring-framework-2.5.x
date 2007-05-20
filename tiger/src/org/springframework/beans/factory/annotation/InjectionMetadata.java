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

package org.springframework.beans.factory.annotation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.PropertyValues;

/**
 * Internal class for managing injection metadata.
 * Used by {@link CommonAnnotationBeanPostProcessor}
 * and {@link AutowiredAnnotationBeanPostProcessor}.
 *
 * @author Juergen Hoeller
 * @since 2.1
 */
class InjectionMetadata {

	private Set<InjectedElement> resourceFields = new LinkedHashSet<InjectedElement>();

	private Set<InjectedElement> resourceMethods = new LinkedHashSet<InjectedElement>();


	public void addInjectedField(InjectedElement element) {
		this.resourceFields.add(element);
	}

	public void addInjectedMethod(InjectedElement element) {
		this.resourceMethods.add(element);
	}


	public void injectFields(Object target) throws Throwable {
		for (Iterator it = this.resourceFields.iterator(); it.hasNext();) {
			InjectedElement element = (InjectedElement) it.next();
			element.inject(target, null);
		}
	}

	public void injectMethods(Object target, PropertyValues pvs) throws Throwable {
		for (Iterator it = this.resourceMethods.iterator(); it.hasNext();) {
			InjectedElement element = (InjectedElement) it.next();
			element.inject(target, pvs);
		}
	}


	public static abstract class InjectedElement {

		protected final Member member;

		public InjectedElement(Member member) {
			this.member = member;
		}

		protected abstract void inject(Object target, PropertyValues pvs) throws Throwable;

		protected final void makeMemberAccessible() {
			if (!Modifier.isPublic(this.member.getModifiers()) ||
					!Modifier.isPublic(this.member.getDeclaringClass().getModifiers())) {
				((AccessibleObject) this.member).setAccessible(true);
			}
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof InjectedElement)) {
				return false;
			}
			InjectedElement otherElement = (InjectedElement) other;
			if (this.member instanceof Field) {
				return this.member.equals(otherElement.member);
			}
			else {
				return (otherElement.member instanceof Method &&
						this.member.getName().equals(otherElement.member.getName()) &&
						Arrays.equals(((Method) this.member).getParameterTypes(),
								((Method) otherElement.member).getParameterTypes()));
			}
		}

		public int hashCode() {
			return this.member.getClass().hashCode() * 29 + this.member.getName().hashCode();
		}
	}

}

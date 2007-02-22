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

package org.springframework.beans.factory.aspectj;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Concrete aspect that uses the Configurable annotation to identify which
 * classes need autowiring. The bean name to look up will be taken from the
 * Configurable annotation if specified, otherwise the default will be the FQN
 * of the class being configured.
 * 
 * <p>
 * <b>Implementation details:</b> 
 * This aspect advises object creation for &#64;Configurable class instances. 
 * There are two cases that needs to be handled:
 * <ol>
 *   <li>Normal object creation through 'new': This is taken care of by advising
 *       initialization() join points.</li>
 *   <li>Object creation through deserialization: Since no constructor is invoked
 *       during deserialzation, the aspect needs to advise a method that a
 *       deserialization mechanism is going to invoke. Ideally, we shouldn't require
 *       user classes to implement any specific method. This implies that we need
 *       to <i>introduce</i> the chosen method. We should also handle the cases
 *       where the chosen method is already implemented in classes (in which case,
 *       the user's implementation for that method should take precedence over the 
 *       ITDed implementation). There are a few choices for the chosen method:
 *       <ul>
 *       <li>readObject(ObjectOutputStream): Java requires that the method must be
 *           <code>private</p>. Since aspects cannot introduce a private member, 
 *           while preserving its name, this option is ruled out.</li>
 * 		 <li>readResolve(): Java doesn't pose any restriction on access specifier. 
 *           Problem solved! There is one (minor) limitation of this approach in 
 *           that if a user class already has this method, that method must be 
 *           <code>public</code>. However, this shouldn't be a big burden, since
 *           use cases that need classes to implement readResolve() (custom enums, 
 *           for example) are unlikely to be marked as &#64;Configurable</li>, and in any case
 *           asking to make that method public shouldn't pose undue burden. 
 *       </ul>
 * The minor collaboration needed by user classes (implementation of readResolve(), 
 * if any, must be public) can be lifted as well if we were to use experimental feature 
 * in AspectJ -- the hasmethod() PCD.
 * </ol>
 * </p>
 *
 * @author Rod Johnson
 * @author Ramnivas Laddad
 * @since 2.0
 * @see org.springframework.beans.factory.annotation.Configurable
 * @see org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver
 */
public aspect AnnotationBeanConfigurerAspect extends AbstractBeanConfigurerAspect {

	public AnnotationBeanConfigurerAspect() {
		setBeanWiringInfoResolver(new AnnotationBeanWiringInfoResolver());
	}

	/**
	 * The creation of a new bean (an object with the
	 * &#64;Configurable annotation)
	 * 
	 * Besides selecting the normal construction using initialize(), it also
	 * selects the readResolve() method in Serializable+.
	 * 
	 * It filters out any creation that doesn't corresponds to the most specific
	 * class of the object (and not any of the base classes in the hierarchy).
	 * This avoids multiple configuration of a bean with a base type
	 * carrying &#64;Configurable (see SPR-2485)
	 *  
	 * See implementation detail in aspect's documentation
	 */
	protected pointcut beanCreation(Object beanInstance) :
		beanConstructor(beanInstance)
		&& if(((ConfigurableSupport)beanInstance).constructorDepth == 1);

	/**
	 * Select execution of any constructor or deserialization of a bean. 
	 * This selects any constructor of readResolve() method in the class
	 * hierarchy of a &#64;Configurable object.
	 */
	private pointcut beanConstructor(ConfigurableSupport beanInstance) :
		(initialization(ConfigurableSupport.new(..))
		 || (execution(Object Serializable+.readResolve() throws ObjectStreamException)))
		&& this(beanInstance);

	
	/*
	 * Machinery to monitor constructor method depth so that the join point in the
	 * most specific class of the bean may be selected
	 */
	/**
	 * Monitor the depth of constructor call (along with the after counterpart) 
	 */
	before(ConfigurableSupport beanInstance) : beanConstructor(beanInstance) {
		beanInstance.constructorDepth++;
	}
	
	/*
	 * We could use 'after returning' to gain some efficiency without any harm
	 * (configuration won't take place for any failed creation anyway). However, it
	 * seems that efficiency gained isn't worth the confusion that might arise from
	 * mismatched call depth. 
	 */
	after(ConfigurableSupport beanInstance)  : beanConstructor(beanInstance) {
		beanInstance.constructorDepth--;
	}

	/**
	 * Declare all &#64;Configurable to implement the nested interface to allow
	 * monitoring contructor depth
	 */
	declare parents: @Configurable * implements ConfigurableSupport;

	/**
	 * Mixin to allow monitoring depth 
	 */
	private interface ConfigurableSupport {
	}

	/**
	 * Introduce a <b>private</b> member to avoid conflict with any existing
	 * member of any target class. Also ensure that the field is marked
	 * <b>transient</b> to avoid issues with tools such as JPA. Further, in any
	 * case, saving this state is useless at it is a temporary state used during
	 * object construction.
	 */
	private transient int ConfigurableSupport.constructorDepth = 0;

	
	/**
	 * Declare any class implementing Serializable annotated with
	 * @Configurable as also implementing ConfigurableDeserializationSupport. 
	 * This allows us to introduce the readResolve() method and select it with the 
	 * beanCreation() pointcut.
	 * 
	 * Here is an improved version that uses the hasmethod() pointcut and lifts
	 * even the minor requirement on user classes:
	 * declare parents: &#64;Configurable Serializable+ 
	 *		            && !hasmethod(Object readResolve() throws ObjectStreamException) 
	 *		            implements ConfigurableDeserializationSupport;
     *
	 */
	declare parents: @Configurable Serializable+ 
			       implements ConfigurableDeserializationSupport;

	/**
	 * A marker interface to which the readResolve() is introduced
	 */
	interface ConfigurableDeserializationSupport extends Serializable {
	}

	/**
	 * Introduce the readResolve() method so that we can advise its execution to configure
	 * the beans
	 * 
	 * Note if a method with the same signature already exists in a Serializable
	 * class annotated with @Configurable, that implementation will take precedence 
	 * (a good thing, since we are merely interested in an opportunity to detect
	 * deserialization.)
	 */
	public Object ConfigurableDeserializationSupport.readResolve() throws ObjectStreamException {
		return this;
	}
}
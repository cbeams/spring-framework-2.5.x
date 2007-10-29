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

package org.springframework.beans.factory.aspectj;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Concrete aspect that uses the {@link Configurable }
 * annotation to identify which classes need autowiring.
 *
 * <p>The bean name to look up will be taken from the
 * <code>&#64;Configurable</code> annotation if specified, otherwise the
 * default bean name to look up will be the FQN of the class being configured.
 * 
 * <h2>Implementation details:</h2> 
 *
 * <p>This aspect advises object creation for <code>&#64;Configurable</code>
 * class instances. There are two cases that needs to be handled:
 *
 * <ol>
 *   <li>Normal object creation via the '<code>new</code>' operator: this is
 *       taken care of by advising <code>initialization()</code> join points.</li>
 *   <li>Object creation through deserialization: since no constructor is
 *       invoked during deserialization, the aspect needs to advise a method that a
 *       deserialization mechanism is going to invoke. Ideally, we should not
 *       require user classes to implement any specific method. This implies that
 *       we need to <i>introduce</i> the chosen method. We should also handle the cases
 *       where the chosen method is already implemented in classes (in which case,
 *       the user's implementation for that method should take precedence over the 
 *       introduced implementation). There are a few choices for the chosen method:
 *       <ul>
 *       <li>readObject(ObjectOutputStream): Java requires that the method must be
 *           <code>private</p>. Since aspects cannot introduce a private member, 
 *           while preserving its name, this option is ruled out.</li>
 * 		 <li>readResolve(): Java doesn't pose any restriction on an access specifier. 
 *           Problem solved! There is one (minor) limitation of this approach in 
 *           that if a user class already has this method, that method must be 
 *           <code>public</code>. However, this shouldn't be a big burden, since
 *           use cases that need classes to implement readResolve() (custom enums, 
 *           for example) are unlikely to be marked as &#64;Configurable, and
 *           in any case asking to make that method <code>public</code> should not
 *           pose any undue burden.</li>
 *       </ul>
 *       The minor collaboration needed by user classes (i.e., that the 
 *       implementation of <code>readResolve()</code>, if any, must be 
 *       <code>public</code>) can be lifted as well if we were to use an 
 *       experimental feature in AspectJ - the <code>hasmethod()</code> PCD.</li>
 * </ol>
 *
 * @author Rod Johnson
 * @author Ramnivas Laddad
 * @author Adrian Colyer
 * @since 2.0
 * @see org.springframework.beans.factory.annotation.Configurable
 * @see org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver
 */
public aspect AnnotationBeanConfigurerAspect extends AbstractBeanConfigurerAspect {

	/**
	 * Create a new instance of the <code>AnnotationBeanConfigurerAspect</code>
	 * aspect that uses an {@link AnnotationBeanWiringInfoResolver}.
	 */
	public AnnotationBeanConfigurerAspect() {
		setBeanWiringInfoResolver(new AnnotationBeanWiringInfoResolver());
	}


	/**
	 * The initialization of a new object where the class of the object
	 * to be constructed is annotated with the <code>&#64;Configurable</code>
	 * annotation.
	 * 
	 * <p>Besides selecting the normal construction using <code>initialize()</code>,
	 * it also picks out the <code>readResolve()</code> method in <code>Serializable+</code>.
	 * 
	 * <p>Additionally, this pointcut filters out any creation that does not
	 * correspond to the most specific class of the object (and not any of the
	 * base classes in the hierarchy). This avoids repeated re-configuration of
	 * a bean with a base type that is also annotated with the
	 * <code>&#64;Configurable</code> annotation (see SPR-2485).
	 *  
	 * <p>See implementation detail in aspect's documentation.
	 */
	protected pointcut beanCreation(Object beanInstance) :
		 ( mostSpecificInitializer() ||
		   configurableObjectResolution() )
		 && this(beanInstance);

	/**
	 * The initialization of a new object where the class of the object
	 * to be constructed is annotated with the <code>&#64;Configurable</code>
	 * annotation.
	 * 
	 * <p>This pointcut selects the initialization corresponding to
	 * a constructor of the top-most class that has <code>&#64;Configurable</code>.
	 * Any constructor of such base class will not have properties
	 * injected to be used in the constructor. This design decision is 
	 * guided by the fact that such base class' constructors should not be
	 * expecting to be injected since a non-<code>&#64;Configurable</code> subtype
	 * will easily invalidate any such expectation.
	 */
	protected pointcut beanInitialization(Object beanInstance) :
		initialization((@Configurable *)+.new(..)) && this(beanInstance); 

	/**
	 * Are dependencies to be injected prior to the construction of an object?
	 */
	protected boolean preConstructionConfiguration(Object beanInstance) {
		Configurable configurable = beanInstance.getClass().getAnnotation(Configurable.class);
		return configurable.preConstruction();
	}
	
	/**
	 * Matches the most-specific initialization join point 
	 * (most concrete class) for the initialization of an instance
	 * of an <code>&#64;Configurable</code> type.
	 */
	private pointcut mostSpecificInitializer() :
		initialization((@Configurable *)+.new(..)) && 
		if(thisJoinPoint.getSignature().getDeclaringType() == thisJoinPoint.getThis().getClass());
	
	/**
	 * Matches the readResolve execution join point for a serializable
	 * type that is also <code>&#64;Configurable</code>.
	 */
	private pointcut configurableObjectResolution() :
		execution(Object ConfigurableDeserializationSupport+.readResolve() throws ObjectStreamException) &&
		@this(Configurable);
		
	/**
	 * Declare any class implementing Serializable annotated with
	 * <code>&#64;Configurable</code> as also implementing ConfigurableDeserializationSupport.
	 * This allows us to introduce the readResolve() method and select it with the 
	 * beanCreation() pointcut.
	 * 
	 * <p>Here is an improved version that uses the hasmethod() pointcut and lifts
	 * even the minor requirement on user classes:
	 *
	 * <pre class="code">declare parents: &#64;Configurable Serializable+
	 *		            && !hasmethod(Object readResolve() throws ObjectStreamException) 
	 *		            implements ConfigurableDeserializationSupport;
	 * </pre>
     *
	 */
	declare parents: @Configurable Serializable+ 
			       implements ConfigurableDeserializationSupport;


	/**
	 * A marker interface to which the <code>readResolve()</code> is introduced.
	 */
	interface ConfigurableDeserializationSupport extends Serializable {
	}


	/**
	 * Introduce the <code>readResolve()</code> method so that we can advise its
	 * execution to configure the object.
	 * 
	 * <p>Note if a method with the same signature already exists in a
	 * <code>Serializable</code> class annotated with <code>&#64;Configurable</code>,
	 * that implementation will take precedence (a good thing, since we are
	 * merely interested in an opportunity to detect deserialization.)
	 */
	public Object ConfigurableDeserializationSupport.readResolve() throws ObjectStreamException {
		return this;
	}

}
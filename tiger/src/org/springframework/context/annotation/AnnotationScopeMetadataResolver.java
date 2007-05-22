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

package org.springframework.context.annotation;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * An implementation of {@link ScopeMetadataResolver} that checks for
 * a {@link Scope} annotation on the bean class.
 * 
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.1
 * @see Scope
 */
public class AnnotationScopeMetadataResolver implements ScopeMetadataResolver {

	private Class<? extends Annotation> scopeAnnotationType = Scope.class;
	
	private ScopedProxyMode scopedProxyMode;
	
	
	public AnnotationScopeMetadataResolver() {
		this(ScopedProxyMode.NO);
	}
	
	public AnnotationScopeMetadataResolver(ScopedProxyMode scopedProxyMode) {
		this.scopedProxyMode = scopedProxyMode;
	}


	public void setScopeAnnotationType(Class<? extends Annotation> scopeAnnotationType) {
		this.scopeAnnotationType = scopeAnnotationType;
	}
	

	public ScopeMetadata resolveScopeMetadata(BeanDefinition definition) {
		ScopeMetadata metadata = new ScopeMetadata();
		if (definition instanceof AnnotatedBeanDefinition) {
			AnnotatedBeanDefinition annDef = (AnnotatedBeanDefinition) definition;
			Map<String, Object> attributes =
					annDef.getMetadata().getAnnotationAttributes(this.scopeAnnotationType.getName());
			if (attributes != null) {
				metadata.setScopeName((String) attributes.get("value"));
			}
			if (!metadata.getScopeName().equals(BeanDefinition.SCOPE_SINGLETON)) {
				metadata.setScopedProxyMode(this.scopedProxyMode);
			}
		}
		return metadata;
	}

}

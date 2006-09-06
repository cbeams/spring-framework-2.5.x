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

package org.springframework.orm.jpa.persistenceunit;

import javax.persistence.spi.ClassTransformer;

import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.instrument.classloading.SimpleThrowawayClassLoader;
import org.springframework.util.ClassUtils;

/**
 * Subclass of MutablePersistenceUnitInfo that adds instrumentation
 * hooks based on Spring's LoadTimeWeaver abstraction.
 *
 * <p>This class is restricted to package visibility, in contrast
 * to its superclass.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Costin Leau
 * @since 2.0
 * @see #setLoadTimeWeaver
 * @see PersistenceUnitManager
 */
class SpringPersistenceUnitInfo extends MutablePersistenceUnitInfo {

	private LoadTimeWeaver loadTimeWeaver;


	/**
	 * Set the LoadTimeWeaver SPI strategy interface used by Spring
	 * to add instrumentation to the current class loader.
	 */
	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}

	public ClassLoader getClassLoader() {
		if (this.loadTimeWeaver != null) {
			return this.loadTimeWeaver.getInstrumentableClassLoader();
		}
		else {
			return ClassUtils.getDefaultClassLoader();
		}
	}

	/**
	 * Method called by PersistenceProvider to add instrumentation to
	 * the current environment.
	 */
	public void addTransformer(ClassTransformer classTransformer) {
		if (this.loadTimeWeaver == null) {
			throw new IllegalStateException("Cannot apply class transformer without LoadTimeWeaver specified");
		}
		this.loadTimeWeaver.addTransformer(new ClassFileTransformerAdapter(classTransformer));
	}

	public ClassLoader getNewTempClassLoader() {
		if (this.loadTimeWeaver != null) {
			return this.loadTimeWeaver.getThrowawayClassLoader();
		}
		else {
			return new SimpleThrowawayClassLoader(ClassUtils.getDefaultClassLoader());
		}
	}

}

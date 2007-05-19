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

package org.springframework.context.weaving;

import org.springframework.instrument.classloading.LoadTimeWeaver;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the application context's default LoadTimeWeaver.
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see org.springframework.context.ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME
 */
public interface LoadTimeWeaverAware {

	/**
	 * Set the LoadTimeWeaver of this object's containing application context.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's <code>afterPropertiesSet</code> or a
	 * custom init-method. Invoked after ApplicationContextAware's
	 * <code>setApplicationContext</code>.
	 * <p><b>NOTE:</b> This method will only be called if there actually is a
	 * default LoadTimeWeaver available in the application context. If there is none,
	 * the method will simply not get invoked, assuming that the implementing object
	 * is able to deactivate its weaving dependency accordingly.
	 * @param loadTimeWeaver the LoadTimeWeaver instance
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
	 */
	void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver);

}

/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.aop.framework.autoproxy.metadata;

import org.springframework.aop.framework.autoproxy.target.PoolingAttribute;
import org.springframework.metadata.support.MapAttributes;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;


/**
 * Hardcoded to return the same attributes as are on the TxClass and TxClassWithClassAttribute.
 * @author Rod Johnson
 */
public class DummyAttributes extends MapAttributes {

	public DummyAttributes() throws Exception {
		register(TxClassImpl.class.getMethod("defaultTxAttribute", null), 
			new Object[] { new DefaultTransactionAttribute()});
		Object[] echoAtts = new Object[] { 
			new RuleBasedTransactionAttribute(),
			new RollbackRuleAttribute("java.lang.Exception"),
			new NoRollbackRuleAttribute("ServletException")
		};
		register(TxClassImpl.class.getMethod("echoException", new Class[] { Exception.class }), echoAtts);
		
		PoolingAttribute pa = new PoolingAttribute(10);
		register(TxClassWithClassAttribute.class, new Object[] { new DefaultTransactionAttribute(), pa });
		register(TxClassWithClassAttribute.class.getMethod("echoException", new Class[] { Exception.class }), echoAtts);
		
		register(ThreadLocalTestBean.class, new Object[] { new ThreadLocalAttribute() });
		register(PrototypeTestBean.class, new Object[] { new PrototypeAttribute() });
		
		register(ModifiableTestBean.class, new Object[] { new ModifiableAttribute() });
	}
	
	
}

/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
 * @version $Id: DummyAttributes.java,v 1.2 2003-12-15 17:14:43 johnsonr Exp $
 */
public class DummyAttributes extends MapAttributes {

	public DummyAttributes() throws Exception {
		register(TxClass.class.getMethod("defaultTxAttribute", null), 
			new Object[] { new DefaultTransactionAttribute()});
		Object[] echoAtts = new Object[] { 
			new RuleBasedTransactionAttribute(),
			new RollbackRuleAttribute("java.lang.Exception"),
			new NoRollbackRuleAttribute("ServletException")
		};
		register(TxClass.class.getMethod("echoException", new Class[] { Exception.class }), echoAtts);
		
		PoolingAttribute pa = new PoolingAttribute(10);
		register(TxClassWithClassAttribute.class, new Object[] { new DefaultTransactionAttribute(), pa });
		register(TxClassWithClassAttribute.class.getMethod("echoException", new Class[] { Exception.class }), echoAtts);
		
		register(ThreadLocalTestBean.class, new Object[] { new ThreadLocalAttribute() });
		register(PrototypeTestBean.class, new Object[] { new PrototypeAttribute() });
		
		register(ModifiableTestBean.class, new Object[] { new ModifiableAttribute() });
	}
	
	
}

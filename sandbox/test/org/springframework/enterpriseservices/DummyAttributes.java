/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.enterpriseservices;

import org.springframework.enterpriseservices.mod.ModifiableAttribute;
import org.springframework.enterpriseservices.mod.ModifiableTestBean;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;


/**
 * Hardcoded to return the same attributes as are on the TxClass and TxClassWithClassAttribute.
 * @author Rod Johnson
 * @version $Id: DummyAttributes.java,v 1.1 2003-11-22 09:05:40 johnsonr Exp $
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
		
		PoolingAttribute pa = new PoolingAttribute();
		pa.setSize(10);
		register(TxClassWithClassAttribute.class, new Object[] { new DefaultTransactionAttribute(), pa });
		register(TxClassWithClassAttribute.class.getMethod("echoException", new Class[] { Exception.class }), echoAtts);
		
		
		
		register(ModifiableTestBean.class, new Object[] { new ModifiableAttribute() });
	}
	
	
}

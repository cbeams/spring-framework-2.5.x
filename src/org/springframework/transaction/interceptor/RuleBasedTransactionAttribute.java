/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TransactionAttribute implementation that works out whether a 
 * given exception should cause transaction rollback by applying
 * a number of rollback rules, both positive and negative.
 * If no rules are relevant to the exception, it behaves
 * like DefaultTransactionAttribute (rolling back on
 * runtime exceptions).
 * <br>
 * The TransactionAttributeEditor property editor creates objects
 * of this class.
 * @since 09-Apr-2003
 * @version $Id: RuleBasedTransactionAttribute.java,v 1.2 2003-08-18 16:22:09 jhoeller Exp $
 * @author Rod Johnson
 */
public class RuleBasedTransactionAttribute extends DefaultTransactionAttribute {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private List rollbackRules;
	
	public RuleBasedTransactionAttribute() {
		this.rollbackRules = new ArrayList();
	}

	public RuleBasedTransactionAttribute(int propagationBehavior, List rollbackRules) {
		super(propagationBehavior);
		this.rollbackRules = rollbackRules;
	}

	public void setRollbackRules(List rollbackRules) {
		this.rollbackRules = rollbackRules;
	}

	public List getRollbackRules() {
		return rollbackRules;
	}

	/**
	 * Winning rule is the shallowest rule (that is, the closest
	 * in the inheritance hierarchy to the exception). If no rule applies (-1),
	 * return false.
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#rollbackOn(java.lang.Throwable)
	 */
	public boolean rollbackOn(Throwable t) {
		logger.debug("Applying rules to determine whether transaction should rollback on " + t);
		RollbackRuleAttribute winner = null;
		int deepest = Integer.MAX_VALUE;

		if (this.rollbackRules != null) {
			for (Iterator it = this.rollbackRules.iterator(); it.hasNext();) {
				RollbackRuleAttribute rule = (RollbackRuleAttribute) it.next();
				int depth = rule.getDepth(t);
				if (depth >= 0 && depth < deepest) {
					deepest = depth;
					winner = rule;
				}
			}
		}
		logger.debug("Winning rollback rule is: " + winner);
		
		// User superclass behaviour (rollback on unchecked)
		// if no rule matches
		if (winner == null) {
			logger.debug("No relevant rollback rule found: applying superclass default");
			return super.rollbackOn(t);
		}
			
		return !(winner instanceof NoRollbackRuleAttribute);
	}

	public String toString() {
		StringBuffer result = getDefinitionDescription();
		TreeSet rules = new TreeSet();
		for (Iterator it = rollbackRules.iterator(); it.hasNext();) {
			RollbackRuleAttribute rule = (RollbackRuleAttribute) it.next();
			String sign = (rule instanceof NoRollbackRuleAttribute) ? COMMIT_RULE_PREFIX : ROLLBACK_RULE_PREFIX;
			rules.add(sign + rule.getExceptionName());
		}
		for (Iterator it = rules.iterator(); it.hasNext();) {
			result.append(',');
			result.append(it.next());
		}
		return result.toString();
	}

}

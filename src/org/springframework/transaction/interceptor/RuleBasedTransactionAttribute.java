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
 * @author Rod Johnson
 */
public class RuleBasedTransactionAttribute extends DefaultTransactionAttribute {
	
	/**
	 * Static for optimal serializability
	 */
	protected static final Log logger = LogFactory.getLog(RuleBasedTransactionAttribute.class);

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

/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.enums.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ToStringBuilder;

/**
 * @author keith
 */
public class TypeMapping implements InitializingBean {

	private String table;

	private Class enumClass = GenericLabeledCodedEnum.class;

	private String codeColumn = "CODE";

	private String labelColumn = "LABEL";

	public TypeMapping() {

	}

	public TypeMapping(String table) {
		setTable(table);
	}

	public void afterPropertiesSet() {
		if (table == null) {
			Assert.isTrue(enumClass != GenericLabeledCodedEnum.class,
					"The type's Enum class must be specified");
			table = ClassUtils.getShortName(enumClass);
			table = table.replace('.', '_');
		}
	}

	public Class getEnumClass() {
		return enumClass;
	}

	public void setEnumClass(Class clazz) {
		Assert.notNull(clazz, "enumClass is required");
		this.enumClass = clazz;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		Assert.hasText(table, "table is required");
		this.table = table;
	}

	public String getCodeColumn() {
		return codeColumn;
	}

	public void setCodeColumn(String codeColumn) {
		Assert.hasText(codeColumn, "codeColumn is required");
		this.codeColumn = codeColumn;
	}

	public String getLabelColumn() {
		return labelColumn;
	}

	public void setLabelColumn(String labelColumn) {
		Assert.hasText(labelColumn, "labelColumn is required");
		this.labelColumn = labelColumn;
	}

	public String toString() {
		return new ToStringBuilder(this).appendProperties().toString();
	}
}
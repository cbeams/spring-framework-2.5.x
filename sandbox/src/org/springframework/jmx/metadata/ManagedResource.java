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

package org.springframework.jmx.metadata;

/**
 * Provides metadata for use when registering instances of this class with a JMX
 * server. Only valid when used on a <code>Class</code>.
 *
 * @author Rob Harrop
 * @see org.springframework.jmx.assembler.MetadataModelMBeanInfoAssembler
 * @see org.springframework.jmx.naming.MetadataNamingStrategy
 */
public class ManagedResource extends AbstractJmxAttribute {

	private String objectName;

	private boolean log = false;

	private String logFile;

	private String persistPolicy = "Never";

	private int persistPeriod;

	private String persistName;

	private String persistLocation;

	public String getPersistLocation() {
		return persistLocation;
	}

	public void setPersistLocation(String persistLocation) {
		this.persistLocation = persistLocation;
	}

	public String getPersistName() {
		return persistName;
	}

	public void setPersistName(String persistName) {
		this.persistName = persistName;
	}

	public int getPersistPeriod() {
		return persistPeriod;
	}

	public void setPersistPeriod(int persistPeriod) {
		this.persistPeriod = persistPeriod;
	}

	public String getPersistPolicy() {
		return persistPolicy;
	}

	public void setPersistPolicy(String persistPolicy) {
		this.persistPolicy = persistPolicy;
	}

	public String getObjectName() {
		return this.objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public boolean isLog() {
		return log;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
}

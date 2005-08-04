/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.mock.web.portlet;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PreferencesValidator;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;

/**
 * Mock implementation of the PortletPreferences interface.
 *
 * @author John A. Lewis
 */
public class MockPortletPreferences implements PortletPreferences {

    private PreferencesValidator preferencesValidator;
    
	private final Map preferences = CollectionFactory.createLinkedMapIfPossible(16);

	private final Map readOnly = new HashMap();
    
    //---------------------------------------------------------------------
	// PortletPreferences methods
	//---------------------------------------------------------------------
	
    public boolean isReadOnly(String key) {
        Assert.notNull(key);
        return this.readOnly.containsKey(key);
    }

    public String getValue(String key, String def) {
        Assert.notNull(key);
		String[] values = (String[]) this.preferences.get(key);
		return (values != null && values.length > 0 ? values[0] : def);
    }

    public String[] getValues(String key, String[] def) {
        Assert.notNull(key);
		String[] values = (String[]) this.preferences.get(key);
		return (values != null && values.length > 0 ? values : def);    }

    public void setValue(String key, String value) throws ReadOnlyException {
        Assert.notNull(key);
        if (isReadOnly(key))
            throw new ReadOnlyException ("preference '" + key + "' is read-only");
        this.preferences.put(key, new String[] {value});
    }

    public void setValues(String key, String[] values) throws ReadOnlyException {
        Assert.notNull(key);
        if (isReadOnly(key))
            throw new ReadOnlyException ("preference '" + key + "' is read-only");
        this.preferences.put(key, values);
    }

    public Enumeration getNames() {
		return Collections.enumeration(this.preferences.keySet());
    }

    public Map getMap() {
		return Collections.unmodifiableMap(this.preferences);
    }

    public void reset(String key) throws ReadOnlyException {
        Assert.notNull(key);
        preferences.remove(key);
    }

    public void store() throws IOException, ValidatorException {
        if (preferencesValidator != null)
            preferencesValidator.validate(this);
    }

    
    //---------------------------------------------------------------------
	// MockPortletPreferences methods
	//---------------------------------------------------------------------
    
    public void setPreferencesValidator(
            PreferencesValidator preferencesValidator) {
        this.preferencesValidator = preferencesValidator;
    }
    
    public void setReadOnly(String key, boolean readOnly) {
        Assert.notNull(key);
        if (readOnly == true)
            this.readOnly.put(key, null);
        else
            this.readOnly.remove(key);
    }

}

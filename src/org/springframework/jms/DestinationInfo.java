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

package org.springframework.jms;

/**
 * Information about a JMS Destination.  Queue and Topic subclasses
 * contain information specific to those desintation types.
 * 
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public abstract class DestinationInfo {

    /**
     * The name of the destination
     */
    private String _name;

    /**
     * The array of JNDI names for the destination
     */
    private String[] _jndiNames;

    /**
     * The array of JNDI names for the destination
     * @return the array of JNDI names for the destination or null if 
     * there are no JNDI names for this destination.
     */
    public String[] getJndiNames() {
        return _jndiNames;
    }

    /**
     * Get the name of the destination.
     * @return the name of the destination.
     */
    public String getName() {
        return _name;
    }

    /**
     * Set the JNDI names of the destination.
     * @param names the JNDI names of the destination.
     */
    public void setJndiNames(String[] names) {
        _jndiNames = names;
    }

    /**
     * Set the name of the destination.
     * @param name
     */
    public void setName(String name) {
        _name = name;
    }

}

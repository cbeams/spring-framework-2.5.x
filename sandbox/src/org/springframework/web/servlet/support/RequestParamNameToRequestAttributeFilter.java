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

package org.springframework.web.servlet.support;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.filter.GenericFilterBean;

/**
 * <p>
 * Very simple filter, normally meant to be used to handle param submissions from an image
 * submit button instead of a normal submit button. Whereas a normal submit button has a name
 * and a value attribute, so multiple submit buttons may be used to send in differing values
 * for the same name, an image submit button has only a name, and will send in x and y values
 * for the image location, in the format name.x and name.y.
 * </p>
 * 
 * <p>
 * This filter looks at each reuqest param, and tries to extract a param name and param value.
 * This is then set as a Request Attribute (since the Request Params can not be added to).
 * </p>
 * 
 * <p>
 * Given a configured param name prefix of <code>_pname_<code> and a param value prefix of
 * <code>_pvalue_</code>, an image button could for example be written in the HTML file as:
 * <pre>
 * 
 *  
 *       &lt;button type=&quot;image&quot; name=&quot;_pname_event_pvalue_clear&quot;
 *   
 *  
 * </pre>   
 * </p>
 * on clicking it, it would send a value of <code>_pname_event_pvalue_clear.x</code> (.y). The filter
 * would extract the name value pair of <code>event/clear</code>.</p>
 * 
 * Note that every request param will be scanned for the prefix. In a normal servlet based app that 
 * does anything significant this will not add any significant overhead. Be careful that any param
 * value prefix you use does not actually conflict with (is a part of) any param names, or the filter
 * will get confused.
 * 
 * @author Colin Sampaleanu
 * @since 1.1.4 sandbox
 */
public class RequestParamNameToRequestAttributeFilter extends GenericFilterBean {

    String inputNamePrefix;

    String inputValuePrefix;

    String inputSuffixToStrip = ".x";

    String outputAttributeNamePrefix = "";

    /**
     * Incoming request param names which start with this value will be matched. The output
     * param name will be the characters between the param name prefix and the param value
     * prefix.
     * 
     * @param paramNamePrefix
     *            The prefix to set
     */
    public void setInputNamePrefix(String paramNamePrefix) {
        this.inputNamePrefix = paramNamePrefix;
    }

    /**
     * The param value prefix. The output param name will be the characters between the param
     * name prefix and the param value prefix.
     * 
     * @param paramValuePrefix
     *            The paramValuePrefix to set.
     */
    public void setInputValuePrefix(String paramValuePrefix) {
        this.inputValuePrefix = paramValuePrefix;
    }

    /**
     * After the param name prefix matches a candidate incoming request param, if this suffix
     * is specified, the candidate reuqest param must also end in this suffix, and the suffix
     * is actually stripped from the incoming request param name before further processing.
     * This defaults to ".x" to match the format of one of the two params sent by an image
     * button (the other ending in ".y".
     * 
     * @param suffixToStrip
     *            The suffix to match and strip
     */
    public void setInputSuffixToStrip(String suffixToStrip) {
        if (suffixToStrip == null)
            inputSuffixToStrip = "";
        this.inputSuffixToStrip = suffixToStrip;
    }

    /**
     * This string, if set to anything, will be prepended to any attribute name produced.
     * 
     * @param outputParamNamePrefix
     *            The prefix to add to any attribute name produced.
     */
    public void setOutputAttributeNamePrefix(String outputParamNamePrefix) {
        if (outputParamNamePrefix == null)
            outputParamNamePrefix = "";
        this.outputAttributeNamePrefix = outputParamNamePrefix;
    }

    // javadoc in superclass
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
            throws IOException, ServletException {

        Enumeration e = req.getParameterNames();
        while (e.hasMoreElements()) {
            String nameIn = (String) e.nextElement();
            if (nameIn.startsWith(inputNamePrefix) && nameIn.endsWith(inputSuffixToStrip)) {
            	int pvaluePrefixIndex = nameIn.indexOf(inputValuePrefix, inputNamePrefix.length());
                if (pvaluePrefixIndex == -1)
                    continue;
                String name = outputAttributeNamePrefix
                        + nameIn.substring(inputNamePrefix.length(), pvaluePrefixIndex);
                String value = nameIn.substring(pvaluePrefixIndex + inputValuePrefix.length(),
                        nameIn.length() - inputSuffixToStrip.length());
                req.setAttribute(name, value);
            }
        }
        filterChain.doFilter(req, resp);
    }
}

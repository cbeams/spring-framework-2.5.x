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
package org.springframework.web.struts;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.RequestProcessor;

public class BindingRequestProcessor extends RequestProcessor {
    private static final String ROLLBACK_MULTIPART_MSG = "Rolling back multipart request";

    private static final String NO_INPUT_FORM_MSG = "Validation failed but no input form available";

    private static final String RETURNING_TO_MSG = "Validation failed, returning to '";

    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Wrap multipart requests with a special wrapper
        request = processMultipart(request);

        // Identify the path component we will use to select a mapping
        String path = processPath(request, response);
        if (path == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Processing a '" + request.getMethod() + "' for path '" + path + "'");
        }

        // Select a Locale for the current user if requested
        processLocale(request, response);

        // Set the content type and no-caching headers if requested
        processContent(request, response);
        processNoCache(request, response);

        // General purpose preprocessing hook
        if (!processPreprocess(request, response)) {
            return;
        }
        processCachedMessages(request, response);

        // Identify the mapping for this request
        ActionMapping mapping = processMapping(request, response, path);
        if (mapping == null) {
            return;
        }

        // Check for any role required to perform this action
        if (!processRoles(request, response, mapping)) {
            return;
        }

        // Process any ActionForm bean related to this request
        ActionForm form = processActionForm(request, response, mapping);
        if (!(form instanceof BindingActionForm)) {
            // if it's not a binding action form, treat in standard struts
            // fashion; that is, do action form population, then validation
            processPopulate(request, response, form, mapping);
            if (!processValidate(request, response, form, mapping)) {
                return;
            }
        }
        else {
            // else we defer population and validation to the action code
            // allowing POJO-based binding of request parameters
        }

        // Process a forward or include specified by this mapping
        if (!processForward(request, response, mapping)) {
            return;
        }
        if (!processInclude(request, response, mapping)) {
            return;
        }

        // Create or acquire the Action instance to process this request
        Action action = processActionCreate(request, response, mapping);
        if (action == null) {
            return;
        }

        // Call the Action instance itself
        ActionForward forward = processActionPerform(request, response, action, form, mapping);

        if (form instanceof BindingActionForm) {
            BindingActionForm baf = (BindingActionForm)form;
            if (baf.hasErrors()) {
                ActionErrors errors = (ActionErrors)request.getAttribute(Globals.ERROR_KEY);
                if (errors == null) {
                    request.setAttribute(Globals.ERROR_KEY, baf.getActionErrors());
                }
                else {
                    errors.add(baf.getActionErrors());
                }
            }
        }
        // Process the returned ActionForward instance
        processForwardConfig(request, response, forward);
    }

}
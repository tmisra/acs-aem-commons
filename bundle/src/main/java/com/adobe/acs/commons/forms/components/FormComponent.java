/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2013 Adobe
 * %%
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
 * #L%
 */
package com.adobe.acs.commons.forms.components;

import com.adobe.acs.commons.forms.Form;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

public interface FormComponent {
    /**
     *
     * @return "unique" name of the form
     */
    String getFormName();

    /**
     * Get the data from the HTTP Request and move into the Map-based Form abstraction.
     * @param request SlingRequest obj
     * @return the object extracted from the request
     */
    Form getForm(SlingHttpServletRequest request);

    /**
     * Validate the provided form data. Create any Error records on the form itself.
     *
     * @param form the Form
     * @return the parameter form updated with any errors/adjustments made during validation
     */
    Form validate(Form form);

    /**
     * Save the data to the underlying data store; implementation specific. This could be CRX or external data store.
     *
     * @param form the Form
     * @return true of the Form was successful "saved" (what constitutes "saved" is an implemenation detail)
     */
    boolean save(Form form);

    /**
     * Handle successful form submission. Typically includes a 302 redirect to a Success page.
     * @param form the Form
     * @param request SlingRequest obj
     * @param response SlingResponse obj
     */
    void onSuccess(Form form, SlingHttpServletRequest request, SlingHttpServletResponse response) throws Exception;

    /**
     * Handle unsuccessful form submission. Typically includes a 302 redirect back to self.
     * @param form the Form
     * @param request SlingRequest obj
     * @param response SlingResponse obj
     */
    void onFailure(Form form, SlingHttpServletRequest request, SlingHttpServletResponse response) throws Exception;
}
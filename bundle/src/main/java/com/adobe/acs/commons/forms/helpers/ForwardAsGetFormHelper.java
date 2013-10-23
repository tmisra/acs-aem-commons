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
package com.adobe.acs.commons.forms.helpers;

import com.adobe.acs.commons.forms.Form;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Interface used for working with ACS-AEM-Commons forms.
 */
public interface ForwardAsGetFormHelper extends FormHelper {
    String REQUEST_ATTR_FORM_KEY = ForwardAsGetFormHelper.class.getName() + "__Form_";

    /**
     * Creates the action URL when posting to a Page (non AJAX call).
     *
     * @param page the page containing the Forms component to POST to
     * @return the constructed action URI to the page
     */
    String getAction(Page page);

    /**
     * Creates a synthetic GET request that can be used in the context of a real
     * POST request to retrieve GET renditions of resources.
     *
     * This method is best used for AJAX-based forms, as the result on error is
     * only the markup associated with the error-ing form (not the whole page)
     *
     * @param form the Form
     * @param resource the Resource to forward the Request to
     * @param request SlingRequest obj
     * @param response SlingResponse obj
     * @param options custom RequestDispatcherOptions obj
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    void forwardAsGet(Form form, Resource resource,
                             SlingHttpServletRequest request,
                             SlingHttpServletResponse response,
                             RequestDispatcherOptions options) throws ServletException, IOException;

    /**
     * Creates a synthetic GET request that can be used in the context of a real
     * POST request to retrieve GET renditions of resources.
     *
     * This method is best used for AJAX-based forms, as the result on error is
     * only the markup associated with the error-ing form (not the whole page)
     *
     * Same as above, but uses empty RequestDispatcherOptions.
     *
     * @param form the Form
     * @param resource the Resource to forward the Request to
     * @param request SlingRequest obj
     * @param response SlingResponse obj
     * @throws ServletException
     * @throws IOException
     */
    void forwardAsGet(Form form, Resource resource,
                             SlingHttpServletRequest request,
                             SlingHttpServletResponse response) throws ServletException, IOException;

    /**
     * Creates a synthetic GET request that can be used in the context of a real
     * POST request to retrieve GET renditions of resources.
     *
     * This method is best used for full POST-back forms that perform a full synchronous POST
     * on submission.
     *
     * Forces resourceType to "cq/Page" and removes all selectors.
     *
     * @param form the Form
     * @param page the CQ Page to forward the Request to
     * @param request SlingRequest obj
     * @param response SlingResponse obj
     * @throws ServletException
     * @throws IOException
     */
    void forwardAsGet(Form form, Page page,
                             SlingHttpServletRequest request,
                             SlingHttpServletResponse response) throws ServletException, IOException;

    /**
     * Same as forwardAsGet(Form form, Page pae, SlingHttpServletRequest request, SlingHttpServletResponse) except
     * allows RequestDispatcherOptions to be passed in.
     *
     * Note; this WILL force a resourceType of "cq/Page" even if a previous "setForceResourceType" as been set on the
     * options.
     *
     * @param form the Form
     * @param page the CQ Page to forward the Request to
     * @param request SlingRequest obj
     * @param response SlingResponse obj
     * @param options custom RequestDispatcherOptions obj
     * @throws ServletException
     * @throws IOException
     */
    void forwardAsGet(Form form, Page page,
                             SlingHttpServletRequest request,
                             SlingHttpServletResponse response,
                             RequestDispatcherOptions options) throws ServletException, IOException;


    /**
     /**
     * Creates a synthetic GET request that can be used in the context of a real
     * POST request to retrieve GET renditions of resources.
     *
     * This method is best used for a customized scenarios where the current resource or currentPage do not suffice.
     *
     * Note: BrowserMap JS may auto-redirect the result is a CQ Page that loads BrowserMap.
     *
     * @param form the Form
     * @param path the JCR Path to forward the Request to
     * @param request SlingRequest obj
     * @param response SlingResponse obj
     * @param options custom RequestDispatcherOptions obj
     * @throws ServletException
     * @throws IOException
     */
    void forwardAsGet(Form form, String path,
                             SlingHttpServletRequest request,
                             SlingHttpServletResponse response,
                             RequestDispatcherOptions options) throws ServletException, IOException;

}
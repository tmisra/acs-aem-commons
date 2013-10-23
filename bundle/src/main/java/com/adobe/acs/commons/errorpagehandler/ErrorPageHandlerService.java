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
package com.adobe.acs.commons.errorpagehandler;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;

/**
 * Error Page Handling Service which facilitates the resolution of errors against author-able pages for discrete
 * content trees.
 *
 * This service is used via the ACS-AEM-Commons error page handler implementation to create author-able error pages.
 */
public interface ErrorPageHandlerService {
    final int DEFAULT_STATUS_CODE = SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR;

    /**
     * Determines if this Service is "enabled". If it has been configured to be "Disabled" the Service still exists
     * however it should not be used. This OSGi Property toggle allows error page handler to be toggled on an off
     * without via OSGi means without throwing Null pointers, etc.
     *
     * @return true is the Service should be considered enabled
     */
    boolean isEnabled();

    /**
     * Find the JCR full path to the most appropriate Error Page.
     *
     * @param request SlingRequest obj
     * @param errorResource the resource requested that caused the error
     * @return the path to the error page to handle this error'ing request
     */
    String findErrorPage(SlingHttpServletRequest request, Resource errorResource);

    /**
     * Get Error Status Code from Request or Default (500) if no status code can be found.
     *
     * @param request SlingRequest obj
     * @return
     */
    int getStatusCode(SlingHttpServletRequest request);

    /**
     * Get the Error Page's name (all lowercase) that should be used to render the page for this error.
     *
     * This only resolves to HTTP error codes, and NOT exceptions names (ex. Throwable, etc.)
     *
     * @param request SlingRequest obj
     * @return the name of the error page to look for
     */
    String getErrorPageName(SlingHttpServletRequest request);

    /**
     * Determine is the request is a 404 and if so handles the request appropriately base on some CQ idiosyncrasies.
     *
     * Mainly forces an authentication request in Authoring modes (!WCMMode.DISABLED)
     *
     * @param request SlingRequest obj
     * @param response SlingReponse obj
     */
    void doHandle404(SlingHttpServletRequest request, SlingHttpServletResponse response);

    /**
     * Returns the Exception Message (Stacktrace) from the Request.
     *
     * @param request SlingRequest obj
     * @return String representation of the Exception and Stacktrace (for 500'ing requests)
     */
    String getException(SlingHttpServletRequest request);

    /**
     * Returns a String representation of the RequestProgress trace.
     *
     * @param request SlingRequest obj
     * @return String representation of the Sling Request Progress (for 500'ing requests)
     */
    String getRequestProgress(SlingHttpServletRequest request);

    /**
     * Reset response attributes to support printing out a new page (rather than one that potentially error'ed out).
     * This includes clearing clientlib inclusion state, and resetting the response.
     *
     * If the response is committed, and it hasn't been closed by code, check the response AND jsp buffer sizes and
     * ensure they are large enough to NOT force a buffer flush.
     *
     * @param request SlingRequest obj
     * @param response SlingResponse obj
     * @param statusCode status code to set on the SlingResponse
     */
    void resetRequestAndResponse(SlingHttpServletRequest request, SlingHttpServletResponse response, int statusCode);
}

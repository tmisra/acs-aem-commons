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
package com.adobe.acs.commons.forms.impl;

import com.adobe.acs.commons.forms.helpers.PostFormHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.request.RequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@SlingFilter(
        label = "ACS AEM Commons - Post-Redirect-Get Multi-Step Forms Filter",
        description = "Include Filter that handles internal routing of Multi-Step Post-Redirect-Get Form submissions.",
        metatype = false,
        generateComponent = true,
        generateService = true,
        order = 0,
        scope = SlingFilterScope.INCLUDE)
public class FormsPostRedirectGetSlingFilterImpl implements Filter {
    private static final Logger log = LoggerFactory.getLogger(FormsPostRedirectGetSlingFilterImpl.class);
    private static final String REQUEST_ATTR_PREVIOUSLY_PROCESSED =
            FormsPostRedirectGetSlingFilterImpl.class.getName() + "__Previously_Processed";

    @Reference
    private PostFormHelper formHelper;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (!(servletRequest instanceof  SlingHttpServletRequest)
                || !(servletResponse instanceof SlingHttpServletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) servletRequest;
        final SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) servletResponse;

        /**
         * Fail fast and early!
         *
         * Must be:
         *  - HTTP GET Request
         *  - Have Forms Sling Suffix
         *    - At this point, 99% of includes will be passed over
         *  - Must contain Form Selector Query Parameter
         *  - Include is not a product of a previous forward by this Filter
         */

        if (!StringUtils.equals("GET", slingRequest.getMethod())
                || !formHelper.hasValidSuffix(slingRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        /* Ensure there is a valid form selector as part of Query Params */
        final String formSelector = formHelper.getFormSelector(slingRequest);
        if (formSelector == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        /* Ensure this is not a product of a previous forward; This is to be absolutely sure we are not hitting an
         * infinite loop condition */
        if (slingRequest.getAttribute(REQUEST_ATTR_PREVIOUSLY_PROCESSED) != null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        final RequestDispatcherOptions options = new RequestDispatcherOptions();

        options.setReplaceSelectors(formSelector);
        options.setReplaceSuffix(slingRequest.getRequestPathInfo().getSuffix());

        if (log.isDebugEnabled()) {
            log.debug("Post-Redirect-Get Form Filter; Internal forward to resource: {} ",
                    slingRequest.getResource());
            log.debug("Post-Redirect-Get Form Filter; Internal forward to path: {} ",
                    slingRequest.getResource().getPath());
            log.debug("Post-Redirect-Get Filter; Internal forward w/ replace selectors: {} ",
                    options.getReplaceSelectors());
            log.debug("Post-Redirect-Get Filter; Internal forward w/ suffix: {} ",
                    options.getReplaceSuffix());
        }

        // Avoid accidental infinite loops with API consumers doing their own Fws and Includes
        slingRequest.setAttribute(REQUEST_ATTR_PREVIOUSLY_PROCESSED, Boolean.TRUE);
        slingRequest.getRequestDispatcher(slingRequest.getResource(), options).forward(slingRequest, slingResponse);
    }

    @Override
    public void destroy() {
    }

    private String getParameter(SlingHttpServletRequest slingRequest, String param) {
        final RequestParameter requestParameter =
                slingRequest.getRequestParameter(param);
        if (requestParameter == null) { return null; }
        return StringUtils.stripToNull(requestParameter.getString());
    }
}
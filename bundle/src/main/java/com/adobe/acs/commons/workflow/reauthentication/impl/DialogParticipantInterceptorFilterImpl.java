package com.adobe.acs.commons.workflow.reauthentication.impl;

import com.adobe.acs.commons.workflow.reauthentication.FingerprintService;
import com.day.cq.workflow.exec.WorkItem;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;


@SlingFilter(
        label = "Re-authentication Filter",
        description = "...",
        metatype = false,
        generateComponent = true,
        generateService = true,
        order = 0,
        scope = SlingFilterScope.REQUEST)
public class DialogParticipantInterceptorFilterImpl implements javax.servlet.Filter {
    private static final Logger log = LoggerFactory.getLogger(DialogParticipantInterceptorFilterImpl.class);
    private static final String RP_PASSWORD = "./jcr:content/hidden:password";
    private static final String RP_ITEM = "item";
    private static final String RP_CMD = "cmd";


    @Reference
    private FingerprintService fingerprintService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(!(servletRequest instanceof  SlingHttpServletRequest) ||
                !(servletResponse instanceof SlingHttpServletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) servletRequest;

        if(!this.accepts(slingRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        /** ACCEPTED **/

        final String userId = slingRequest.getResourceResolver().getUserID();
        final String password = slingRequest.getRequestParameter(RP_PASSWORD).getString();
        final String item = slingRequest.getRequestParameter(RP_ITEM).getString();

        if(fingerprintService.isValidCredentials(userId, password)) {
            final WorkItem workItem = fingerprintService.getWorkItemFromPath(item);
            fingerprintService.writeFingerprint(userId, workItem.getWorkflow());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }

    private boolean accepts(final SlingHttpServletRequest slingRequest) {
        if(!StringUtils.equalsIgnoreCase(
                slingRequest.getMethod(),
                "POST")) {
            return false;
        }

        if(!StringUtils.equals(
                slingRequest.getRequestPathInfo().getResourcePath(),
                "/bin/workflow/inbox")) {
            return false;
        }

        if(!slingRequest.getRequestParameterMap().keySet().contains(RP_CMD)) {
            return false;
        }

        if(!slingRequest.getRequestParameterMap().keySet().contains(RP_PASSWORD)) {
            log.debug("No password field of: {}", RP_PASSWORD);
            return false;
        }

        if(!slingRequest.getRequestParameterMap().keySet().contains(RP_ITEM)) {
            return false;
        }

        return true;
    }
}




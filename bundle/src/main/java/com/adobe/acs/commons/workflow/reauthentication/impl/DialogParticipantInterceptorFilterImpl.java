package com.adobe.acs.commons.workflow.reauthentication.impl;

import com.adobe.acs.commons.workflow.reauthentication.FingerprintService;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.metadata.MetaDataMap;
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
        label = "Workflow Re-Authentication Step Filter",
        description = "",
        metatype = false,
        generateComponent = true,
        generateService = true,
        order = 0,
        scope = SlingFilterScope.REQUEST)
public class DialogParticipantInterceptorFilterImpl implements javax.servlet.Filter {
    private static final Logger log = LoggerFactory.getLogger(DialogParticipantInterceptorFilterImpl.class);
    private static final String RP_PASSWORD = "./jcr:content/hidden:password";
    private static final String RP_ITEM = "item";
    private static final String RP_BACKROUTE = "backroute";
    private static final String RP_ROUTE = "route";
    private static final String RP_CMD = "cmd";
    private static final String CMD_ADVANCE_BACK = "advanceBack";


    private static final String PROP_DIALOG_PATH = "DIALOG_PATH";
    private static final String PARTICIPANT_STEP = "PARTICIPANT";
    private static final String PASSWORD_DIALOG_PATH = "/apps/acs-commons/extensions/workflow/reauthentication/dialog";


    @Reference
    private FingerprintService fingerprintService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof SlingHttpServletRequest) ||
                !(servletResponse instanceof SlingHttpServletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) servletRequest;
        final SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) servletResponse;

        if (!this.accepts(slingRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        /** ACCEPTED **/
        final String userId = slingRequest.getResourceResolver().getUserID();
        final String password = slingRequest.getRequestParameter(RP_PASSWORD).getString();
        final String item = slingRequest.getRequestParameter(RP_ITEM).toString();
        final WorkItem workItem = fingerprintService.getWorkItemFromPath(item);

        if (fingerprintService.isValidCredentials(userId, password)) {
            fingerprintService.recordSuccess(userId, workItem);
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            fingerprintService.recordFailure(userId, workItem);
            slingResponse.setStatus(401);
        }
    }

    @Override
    public void destroy() {
    }

    private boolean accepts(final SlingHttpServletRequest slingRequest) {
        // ONLY POST Requests
        if (!StringUtils.equalsIgnoreCase(
                slingRequest.getMethod(),
                "POST")) {
            return false;
        }

        // ONLY Requests to the AEM WF REST Endpoint
        if (!StringUtils.equals(
                slingRequest.getRequestPathInfo().getResourcePath(),
                "/bin/workflow/inbox")) {
            return false;
        }

        // ONLY Requests with a Request Parameter named "item"
        if (!slingRequest.getRequestParameterMap().keySet().contains(RP_ITEM)) {
            return false;
        }

        // IGNORE all back-routing
        if (slingRequest.getRequestParameterMap().keySet().contains(RP_BACKROUTE)) {
            return false;
        } else if (slingRequest.getRequestParameterMap().keySet().contains(RP_CMD)) {
            if (StringUtils.equals(slingRequest.getRequestParameter(RP_CMD).toString(), CMD_ADVANCE_BACK)) {
                return false;
            }
        }

        // Get the WorkItem to inspect the step in more detail
        final String item = slingRequest.getRequestParameter(RP_ITEM).toString();
        final WorkItem workItem = fingerprintService.getWorkItemFromPath(item);

        // ONLY Participant Steps
        if (!StringUtils.equals(PARTICIPANT_STEP, workItem.getNode().getType())) {
            return false;
        }

        // Get the WF Model's step definition
        final MetaDataMap modelStepMetaDataMap = workItem.getWorkflow().getWorkflowModel().getNode(workItem.getNode().getId()).getMetaDataMap();

        // ONLY WF Model's who have a DIALOG_PATH (Dialog Participant Step) AND the DIALOG_PATH is the custom Password input
        if (!modelStepMetaDataMap.containsKey(PROP_DIALOG_PATH)) {
            return false;
        } else if (!StringUtils.equals(PASSWORD_DIALOG_PATH, modelStepMetaDataMap.get(PROP_DIALOG_PATH, ""))) {
            // If not the expected Password collection dialog, then this is not a candidate
            return false;
        }

        log.debug("Accepting request as requiring Password confirmation");
        return true;
    }
}




package com.adobe.acs.commons.workflow.reauthentication.impl;

import com.adobe.acs.commons.workflow.reauthentication.FingerprintService;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.Route;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author david
 */

@Component(
        label = "Re-authentication Check Workflow Process Step",
        description = "Sample Workflow Process implementation",
        metatype = false,
        immediate = false
)
@Properties({
        @Property(
                name = Constants.SERVICE_DESCRIPTION,
                value = "Sample Workflow Process implementation.",
                propertyPrivate = true
        ),
        @Property(
                label = "Workflow Label",
                name = "process.label",
                value = "Re-authentication Check",
                propertyPrivate = true
        )
})
@Service
public class ReauthenticationCheckWorkflowProcessImpl implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(ReauthenticationCheckWorkflowProcessImpl.class);

    @Reference
    private FingerprintService fingerprintService;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        // Get the Workflow data (the data that is being passed through for this work item)
        final WorkflowData workflowData = workItem.getWorkflowData();

        final String type = workflowData.getPayloadType();

        // Check if the payload is a path in the JCR
        if (!StringUtils.equals(type, "JCR_PATH")) {
            return;
        }

        if(fingerprintService.hasValidFingerprint(workItem)) {
            log.info("Fingerprint is valid; proceed to next step");
            workflowSession.complete(workItem, workflowSession.getRoutes(workItem).get(0));
        } else {
            final List<Route> routes = workflowSession.getBackRoutes(workItem);
            final Route backRoute = routes.get(0);

            log.info("Fingerprint is invalid; proceed to previous step");

            workflowSession.complete(workItem, backRoute);
        }
    }
}
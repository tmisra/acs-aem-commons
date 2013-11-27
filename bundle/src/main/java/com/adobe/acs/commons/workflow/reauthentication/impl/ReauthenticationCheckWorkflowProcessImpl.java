package com.adobe.acs.commons.workflow.reauthentication.impl;

import com.adobe.acs.commons.workflow.reauthentication.FingerprintService;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.Route;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.Workflow;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.felix.scr.annotations.*;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        final List<Route> routes = workflowSession.getBackRoutes(workItem);
        final Route backRoute = routes.get(0);

        final Workflow workflow = workItem.getWorkflow();

        if(fingerprintService.hasValidFingerprint(workflow)) {
            fingerprintService.recordSuccess(workItem);
            fingerprintService.clearFingerprint(workflow);

            workflowSession.complete(workItem, workflowSession.getRoutes(workItem).get(0));
        } else {
            fingerprintService.recordFailure(workItem);
            workflowSession.complete(workItem, backRoute);
        }
    }
}
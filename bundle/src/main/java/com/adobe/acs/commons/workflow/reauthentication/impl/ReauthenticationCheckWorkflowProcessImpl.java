package com.adobe.acs.commons.workflow.reauthentication.impl;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.Route;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
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
    ResourceResolverFactory resourceResolverFactory;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        // Get the Workflow data (the data that is being passed through for this work item)
        final WorkflowData workflowData = workItem.getWorkflowData();

        final String type = workflowData.getPayloadType();

        // Check if the payload is a path in the JCR
        if (!StringUtils.equals(type, "JCR_PATH")) {
            return;
        }

        Session session = workflowSession.getSession();
        // Get the path to the JCR resource from the payload
        final String path = workflowData.getPayload().toString();

        // Get data from a previous WF Step
        String previouslySetVal = getPersistedData(workItem, "set-in-previous-wf-step", "a default value");


        List<Route> routes = workflowSession.getBackRoutes(workItem);
        Route backRoute = routes.get(0);

        for(final Route route : routes) {
            //log.debug("All Back-routes: {} ~> {}", route.getId(), route.getName());
        }

        log.debug("My Back-routes: {} ~> {}", backRoute.getId(), backRoute.getName());
        workflowSession.complete(workItem, backRoute);

    }

    /**
     * Helper methods *
     */

    private <T> boolean persistData(WorkItem workItem, WorkflowSession workflowSession, String key, T val) {
        WorkflowData data = workItem.getWorkflow().getWorkflowData();
        if (data.getMetaDataMap() == null) {
            return false;
        }

        data.getMetaDataMap().put(key, val);
        workflowSession.updateWorkflowData(workItem.getWorkflow(), data);


        return true;


    }

    private <T> T getPersistedData(WorkItem workItem, String key, Class<T> type) {
        MetaDataMap map = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        return map.get(key, type);
    }

    private <T> T getPersistedData(WorkItem workItem, String key, T defaultValue) {
        MetaDataMap map = workItem.getWorkflow().getWorkflowData().getMetaDataMap();
        return map.get(key, defaultValue);
    }







       /*
    private void advance(SlingHttpServletResponse response,
                         RequestParameterMap params, WorkflowSession wfSession,
                         Map<String, String> p) throws WorkflowException {

        Iterator<String> itemIds = p.keySet().iterator();
        while (itemIds.hasNext()) {
            String itemId = itemIds.next();
            String routeId = p.get(itemId);

            WorkItem item = wfSession.getWorkItem(itemId);
            List<Route> routes = wfSession.getRoutes(item, true);

            findRouteAndAdvance(wfSession, params, routeId, item, routes);

        }
    }


    private void findRouteAndAdvance(WorkflowSession wfSession,
                                     RequestParameterMap params, String routeId, WorkItem item,
                                     List<Route> routes) throws WorkflowException {


        if (FAILURE_ROUTE.equals(routeId) && FAILURE_ITEM_TYPE.equals(item.getItemSubType())) {
            // rety the current workitem
            WorkflowConsoleUtil.retryStep(wfSession, item);
        } else {
            boolean foundRoute = false;
            for (Route route : routes) {
                String cRouteId = route.getId();

                if (cRouteId.equals(routeId)) {
                    updateMetaData(params, item);
                    wfSession.complete(item, route);
                    foundRoute = true;
                    break;
                } else if (routeId.contains("@")) {
                    // check for members of a group
                    WorkflowNode node = route.getDestinations().get(0).getTo();
                    if (node.getType().equals("PARTICIPANT")) {
                        String participant = node.getMetaDataMap().get("PARTICIPANT", String.class);
                        if (routeId.startsWith(cRouteId + "@")
                                && routeId.endsWith("@" + participant)) {
                            updateMetaData(params, item);
                            wfSession.complete(item, route);
                            foundRoute = true;
                            break;
                        }
                    }
                }
            }

            if (!foundRoute) {
                throw new WorkflowException("Route not found");
            }
        }
    }
 */



}
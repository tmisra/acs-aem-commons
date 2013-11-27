package com.adobe.acs.commons.workflow.reauthentication.impl;

import com.adobe.acs.commons.workflow.reauthentication.FingerprintService;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.Workflow;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.day.text.Text;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.io.UnsupportedEncodingException;

@Component
@Service
public class FingerprintServiceImpl implements FingerprintService {
    private static final Logger log = LoggerFactory.getLogger(DialogParticipantInterceptorFilterImpl.class);

    private static final String SECRET = "MY SECRET TOKEN!";

    @Reference
    private SlingRepository slingRepository;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private WorkflowService workflowService;

    @Override
    public boolean isValidCredentials(final String userId, final String password) {
        final SimpleCredentials credentials = new SimpleCredentials(userId, password.toCharArray());

        Session passwordValidationSession = null;
        try {
            passwordValidationSession = slingRepository.login(credentials);
            return true;
        } catch (LoginException e) {
            return false;
        } catch (RepositoryException e) {
            log.error(e.getMessage());
            return false;
        } finally {
            if(passwordValidationSession != null) {
                passwordValidationSession.logout();
            }
        }
    }

    @Override
    public void writeFingerprint(final String userId, final Workflow workflow) throws UnsupportedEncodingException {
        final String fingerprint = this.makeFingerprint(userId, workflow);
        this.setFingerprint(userId, fingerprint, workflow);
    }

    @Override
    public void clearFingerprint(final Workflow workflow) {
        this.setFingerprint(null, null, workflow);
    }

    @Override
    public boolean hasValidFingerprint(final Workflow workflow) {

        final MetaDataMap map = workflow.getWorkflowData().getMetaDataMap();
        final String userId = map.get(PN_FINGERPRINTER, "");
        final String fingerprint = map.get(PN_FINGERPRINT, "");

        return isValidFingerprint(userId, fingerprint, workflow);
    }

    @Override
    public boolean isValidFingerprint(final String userId, final String fingerprint, final Workflow workflow) {
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(fingerprint)) {
            return false;
        }

        try {
            return StringUtils.equals(fingerprint, this.makeFingerprint(userId, workflow));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public WorkItem getWorkItemFromPath(final String workItemPath) {
        ResourceResolver adminResourceResolver = null;

        try {
            adminResourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            final WorkflowSession workflowSession = workflowService.getWorkflowSession(adminResourceResolver.adaptTo(Session.class));
            return workflowSession.getWorkItem(workItemPath);
        } catch (org.apache.sling.api.resource.LoginException e) {
            log.error("Error logging in as admin to check to get Work Item");
            return null;
        } catch (WorkflowException e) {
            log.error("Error logging in as admin to check to get Work Item");
            return null;
        } finally {
            if(adminResourceResolver != null) {
                adminResourceResolver.close();
            }
        }
    }

    @Override
    public void recordSuccess(final WorkItem workItem) {
        log.info("Fingerprint was successfully validated for: {}", workItem);
    }

    @Override
    public void recordFailure(WorkItem workItem) {
        log.info("Fingerprint was unable to be validated for: {}", workItem);
    }

    private void setFingerprint(final String userId, final String fingerprint, final Workflow workflow) {
        Session adminSession = null;

        try {
            adminSession = slingRepository.loginAdministrative(null);

            final Node node = adminSession.getNode(workflow.getId() + "/data/metaData");

            JcrUtil.setProperty(node, PN_FINGERPRINTER, userId);
            JcrUtil.setProperty(node, PN_FINGERPRINT, fingerprint);

            node.getSession().save();

        } catch (Exception e) {
            log.error("Error saving fingerprint data");
        } finally {
            if(adminSession != null) {
                adminSession.logout();
            }
        }
    }


    private String makeFingerprint(final String userId, final Workflow workflow) throws UnsupportedEncodingException {
        return Text.md5(SECRET + "@" + userId + "@" + workflow.getId(), "UTF-8");
    }
}

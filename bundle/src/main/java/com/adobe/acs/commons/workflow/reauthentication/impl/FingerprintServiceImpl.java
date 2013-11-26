package com.adobe.acs.commons.workflow.reauthentication.impl;

import com.adobe.acs.commons.workflow.reauthentication.FingerprintService;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.workflow.exec.WorkItem;
import com.day.text.Text;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
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
    public void writeFingerprint(final String userId, final String workItemPath) {
        Session adminSession = null;

        try {
            adminSession = slingRepository.loginAdministrative(null);

            final Node node = JcrUtil.createPath(this.getFingerprintPath(workItemPath),
                    JcrConstants.NT_UNSTRUCTURED, JcrConstants.NT_UNSTRUCTURED, adminSession, true);

            final String fingerprint = this.getFingerprint(userId, workItemPath);

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

    @Override
    public boolean hasValidFingerprint(final WorkItem workItem) {
        final String workItemPath = workItem.getId();
        log.debug("WORK ITEM PATH: {}", workItemPath);
        final String fingerprintPath = this.getFingerprintPath(workItemPath);

        ResourceResolver adminResourceResolver = null;

        try {
            adminResourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

            final Resource resource = adminResourceResolver.getResource(fingerprintPath);
            if(resource == null) { return false; }

            final ValueMap properties = resource.adaptTo(ValueMap.class);
            final String userId = properties.get(PN_FINGERPRINTER, "");
            final String fingerprint = properties.get(PN_FINGERPRINT, "");

            if(StringUtils.isBlank(userId) || StringUtils.isBlank(fingerprint)) {
                return false;
            }

            try {
                return StringUtils.equals(fingerprint, this.getFingerprint(userId, workItemPath));
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage());
                return false;
            }
        } catch (org.apache.sling.api.resource.LoginException e) {
            log.error("Error logging in as admin to check if finger print is valid");
            return false;
        } finally {
            if(adminResourceResolver != null) {
                adminResourceResolver.close();
            }
        }
    }

    @Override
    public boolean isValidFingerprint(final String fingerprint, final String userId, final String workItemPath) {
        try {
            return StringUtils.equals(fingerprint, this.getFingerprint(userId, workItemPath));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    private String getFingerprintPath(final String workItemPath) {
        return "/etc/workflow/fingerprints" + StringUtils.removeStart(workItemPath, "/etc/workflow");
    }

    private String getFingerprint(final String userId, final String workItemPath) throws UnsupportedEncodingException {
        return Text.md5(SECRET + "@" + userId + "@" + workItemPath, "UTF-8");
    }
}

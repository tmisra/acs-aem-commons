package com.adobe.acs.commons.workflow.reauthentication;


import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.Workflow;

import java.io.UnsupportedEncodingException;

public interface FingerprintService {
    public static String PN_FINGERPRINTER = "fingerprinter";
    public static String PN_FINGERPRINT = "fingerprint";

    public boolean isValidCredentials(String userId, String password);

    public void writeFingerprint(String userId, Workflow workflow) throws UnsupportedEncodingException;

    public void clearFingerprint(Workflow workflow);

    public boolean hasValidFingerprint(Workflow workflow);

    public boolean isValidFingerprint(String userId, String fingerprint, Workflow workflow);

    public WorkItem getWorkItemFromPath(String workItemPath);

    public void recordSuccess(String userId, WorkItem workItem);

    public void recordFailure(String userId, WorkItem workItem);

}

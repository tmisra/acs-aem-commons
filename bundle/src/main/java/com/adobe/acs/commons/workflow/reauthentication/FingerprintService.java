package com.adobe.acs.commons.workflow.reauthentication;

import com.day.cq.workflow.exec.WorkItem;

public interface FingerprintService {
    public static String PN_FINGERPRINTER = "userId";
    public static String PN_FINGERPRINT = "fingerprint";

    public boolean isValidCredentials(String userId, String password);

    public void writeFingerprint(String userId, String workItemPath);

    public boolean hasValidFingerprint(WorkItem workItem);

    public boolean isValidFingerprint(String fingerprint, String userId, String workItemPath);
}

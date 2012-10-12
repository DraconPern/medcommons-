/*
 * $Id: MergePolicyViolationException.java 2721 2008-07-04 22:58:11Z ssadedin $
 * Created on 04/07/2008
 */
package net.medcommons.router.services.ccrmerge;

/**
 * Thrown when a merge fails because of a policy constraining merges is 
 * violated by the merge.
 * 
 * @author ssadedin
 */
public class MergePolicyViolationException extends MergeException {
    
    private PolicyResult result;

    public synchronized PolicyResult getResult() {
        return result;
    }

    public synchronized void setResult(PolicyResult result) {
        this.result = result;
    }

    public MergePolicyViolationException() {
    }

    public MergePolicyViolationException(String message) {
        super(message);
    }

    public MergePolicyViolationException(Throwable cause) {
        super(cause);
    }

    public MergePolicyViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MergePolicyViolationException(PolicyResult policyResult) {
        super(policyResult.reason);
        this.result = policyResult;
    }

}

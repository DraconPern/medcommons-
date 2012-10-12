/*
 * $Id$
 * Created on 19/07/2007
 */
package net.medcommons.router.selftest;

public class SelfTestFailure extends SelfTestResult {
    public SelfTestFailure(String description) {
        this.setStatus("Failed");
        this.setMessage(description);
    }

}

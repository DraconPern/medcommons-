/*
 * $Id: LogonForm.java 2832 2008-08-15 19:26:36Z sdoyle $
 * Created on Jan 14, 2005
 */
package net.medcommons.router.services.wado.actions;

import net.medcommons.modules.configuration.Configuration;

import org.apache.struts.action.ActionForm;


/**
 * A simple form that lets a user upload a file
 * @author ssadedin
 */
public class LogonForm extends ActionForm {
    
  private String userid;
  
  private String password;
  private String trackingNumber;
  private String pin;
  private boolean demoLogin;
      
  /**
   * 
   */
  public LogonForm() {
    super();
    this.demoLogin = Configuration.getProperty("DemoMode", false);
  }

    /**
     * 
     * @uml.property name="password"
     */
    public String getPassword() {
        return password;
    }

    /**
     * 
     * @uml.property name="password"
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     * @uml.property name="userid"
     */
    public String getUserid() {
        return userid;
    }

    /**
     * 
     * @uml.property name="userid"
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

/**
 * 
 * @uml.property name="pin"
 */
public String getPin() {
    return pin;
}

/**
 * 
 * @uml.property name="pin"
 */
public void setPin(String pin) {
    this.pin = pin;
}

/**
 * 
 * @uml.property name="trackingNumber"
 */
public String getTrackingNumber() {
    return trackingNumber;
}

/**
 * 
 * @uml.property name="trackingNumber"
 */
public void setTrackingNumber(String trackingNumber) {
    this.trackingNumber = trackingNumber;
}

/**
 * 
 * @uml.property name="demoLogin"
 */
public boolean getDemoLogin() {
    return demoLogin;
}

/**
 * 
 * @uml.property name="demoLogin"
 */
public void setDemoLogin(boolean demoLogin) {
    this.demoLogin = demoLogin;
}

}

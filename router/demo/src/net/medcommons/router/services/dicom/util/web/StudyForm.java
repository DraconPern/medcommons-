/*
 * $Id: $
 * Created on Aug 8, 2004
 */
package net.medcommons.router.services.dicom.util.web;

import net.medcommons.router.services.transfer.MCOrder;

import org.apache.struts.action.ActionForm;


/**
 * @TODO This file should be renamed to OrderForm?
 * @author ssadedin
 */
public class StudyForm extends ActionForm {
  
  private MCOrder order;
  
  public StudyForm() {
    
    // By default, create an empty order
    this.setOrder(new MCOrder());
  }
  
  /**
   * @return Returns the order.
   */
  public MCOrder getOrder() {
    return order;
  }
  /**
   * @param order The order to set.
   */
  public void setOrder(MCOrder order) {
    this.order = order;
  }
}

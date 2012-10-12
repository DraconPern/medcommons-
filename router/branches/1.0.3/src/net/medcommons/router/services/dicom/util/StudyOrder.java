/*
 * 
 * Created on Nov 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.services.dicom.util;

import java.util.Date;

import net.medcommons.router.services.transfer.MCOrder;

/**
   * Used to cache study with order information. Used to keep track of which
   * order is associated with which study.
   * 
   * Should move this to ImportSeriesManager. This way the timeouts 
   * can remove the studyOrders while they are cleaning up the database.
   *
   * @author sean
   *
   * To change the template for this generated type comment go to
   * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
   */
  public class StudyOrder {
    public StudyOrder() {
      timeCreated = new Date();
    }

    Date timeCreated = null;

    private MCStudy study = null;

    private MCOrder order = null;
    
    public void setStudy(MCStudy study){
      this.study = study;
    }
    public MCStudy getStudy(){
      return(this.study);
    }
    public void setOrder(MCOrder order){
      this.order = order;
    }
    
    public MCOrder getOrder(){
      return(this.order);
    }

    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("studyOrder[");
      buff.append(study.getStudyInstanceUID());
      buff.append(",");
      buff.append(order.getOrderGuid());
      buff.append(",");
      buff.append(timeCreated);
      buff.append("]");
      return (buff.toString());
    }
  }

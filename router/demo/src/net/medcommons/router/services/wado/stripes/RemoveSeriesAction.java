/*
 * $Id: RemoveSeriesAction.java 3662 2010-04-09 19:19:29Z ssadedin $
 * 
 * Copyright MedCommons Inc. 2005
 */
package net.medcommons.router.services.wado.stripes;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;

/**
 * Removes a specified series from the reply CCR
 * 
 * @author ssadedin
 */
public class RemoveSeriesAction extends CCRActionBean {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(RemoveSeriesAction.class);
  
  private int seriesIndex;
  
  /**
   * If set to true, the provided index is treated as an index into the 
   * displayable references (ie. into the visible references as specified by
   * {@link CCRDocument#getDisplayReferences()}
   */
  boolean displayIndex = true;

  @DefaultHandler
  public Resolution execute() throws Exception {
      
      MCSeries series = null; 
      HttpServletRequest request = this.ctx.getRequest();
      try {
          request.setAttribute("ccr", ccr);
	      series = ccr.getDisplayReferences().get(seriesIndex-1);
          
          boolean found = (ccr.removeReference(series.getMcGUID())!=null);
	      
          if(found) {
              request.setAttribute("deleteStatus", "ok");
              ccr.getSeriesList().remove(series);
              ccr.setGuid(null);
          }
          else {
              request.setAttribute("deleteStatus", "fail");
              request.setAttribute("deleteError", "Reference " + seriesIndex + " having guid " + series.getMcGUID() + " not found in CCR references");
          }
      }
      catch(Exception e) {
          log.error("Failed removing series " + seriesIndex + " (guid=" + (series==null?null:series.getMcGUID()) + ")", e);          
	      request.setAttribute("deleteStatus", "fail");
	      request.setAttribute("deleteError", e.toString());
      }
	 
      return new ForwardResolution("/deleteResult.jsp");
   }  
	
	public int getSeriesIndex() {
	    return seriesIndex;
	}
	
	
	public void setSeriesIndex(int seriesIndex) {
	    this.seriesIndex = seriesIndex;
	}

    public boolean getDisplayIndex() {
        return displayIndex;
    }

    public void setDisplayIndex(boolean displayIndex) {
        this.displayIndex = displayIndex;
    }
}

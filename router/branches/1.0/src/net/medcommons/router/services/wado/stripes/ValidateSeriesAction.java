/*
 * $Id$
 */
package net.medcommons.router.services.wado.stripes;

import java.util.HashMap;
import java.util.List;

import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.wado.actions.NotificationForm;
import net.medcommons.router.services.wado.actions.SaveReplyAction;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;

/**
 * Changes the specified series from unconfirmed to confirmed state
 */
public class ValidateSeriesAction extends CCRActionBean {

  /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(ValidateSeriesAction.class);
	
	private int seriesIndex = -1;
	
	private boolean save = false;
	
	@DefaultHandler
	public Resolution validate() {
	    
	    HashMap<String, String> result = new HashMap<String, String>();
        try {
            
            // Find the series to be validated
            List<MCSeries> seriesList = ccr.getSeriesList();
            if(seriesIndex >= seriesList.size() || seriesIndex < 0) {
                throw new IllegalArgumentException("Series index " 
                                + seriesIndex + " is out of range. Available CCR references = " + seriesList.size());
            }
            
            MCSeries series = seriesList.get(seriesIndex);
            log.info("Confirming series " + seriesIndex + " in CCR (guid="+series.getMcGUID()+")");
            ccr.removeConfirmationFlag(series);
            series.setValidationRequired(false);
            ccr.syncFromJDom();
            
            SaveReplyAction sra = new SaveReplyAction();         
            sra.saveCcr(NotificationForm.get(this.ctx.getRequest()), this.ctx.getRequest(), true);
            result.put("status", "ok");
            result.put("guid", ccr.getGuid());
        }
        catch (Exception e) { 
            log.error("Failed to validate series " + seriesIndex,e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }
        
        return new JavaScriptResolution(result);
	}

    public int getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(int seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    public boolean getSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }
}
    


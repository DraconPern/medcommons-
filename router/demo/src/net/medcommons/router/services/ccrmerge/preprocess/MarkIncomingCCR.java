package net.medcommons.router.services.ccrmerge.preprocess;

import java.util.Collections;
import java.util.List;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.jdom.JDOMException;

public interface MarkIncomingCCR {
	
	public CCRDocument markIncomingCCR(CCRDocument doc) throws JDOMException,PHRException;
	
	public CCRDocument clearMarkedAttributes(CCRDocument doc) throws JDOMException,PHRException;
	
}

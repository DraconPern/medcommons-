//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.stripes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.services.interfaces.InsufficientRightsException;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.modules.utils.metrics.TimeSampledMetric;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.wado.*;
import net.medcommons.router.services.wado.utils.AccountUtil;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.*;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * Renders a WADO Image back to the response based on the WADOForm in the
 * session
 */
@UrlBinding("/wado/{storageId}/")
public class WADOImageAction extends BaseActionBean {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(WADOImageAction.class);

  /**
   * Keep track of how many connections have been aborted.
   */
  private static int abortCount = 0;

  private static int imageCount = 0;
  
  public static String PIPELINE_ITK = "ITK";
  public static String PIPELINE_DCM4CHE2 = "dcm4che2";
  
  private static String pipelineConfig = null;
  
  static {
       initializeImagePipeline();
   }

  @ImportProperties
  WADOParameterForm params = new WADOParameterForm();
  
  @DefaultHandler
  public Resolution render() throws Exception {
      
      HttpServletRequest request = ctx.getRequest();
      HttpServletResponse response = ctx.getResponse();
      
      InputStream inputStream = null;
      File inputFile = null;
      
      try {
          response.reset();
          
          if(AccountUtil.isRealAccountId(params.getStorageId()) && !session.checkPermissions(params.getStorageId(), "R"))
              throw new InsufficientRightsException("Access credentials do not permit display of images for patient " + params.getStorageId());
          
          response.setHeader("Cache-Control", "public, max-age=3600");
          
          updateKBPerSecond(response);
          
          params.adjust();
          
          // If no fname provided then use the first image and default settings
          if(Str.blank(params.getFname()))
              resolveDefaults();
          
          LocalFileRepository  localFileRepository = (LocalFileRepository) RepositoryFactory.getLocalRepository();
          if (pipelineConfig.equals(PIPELINE_ITK)){
              inputFile = localFileRepository.getDocumentFile(params.getStorageId(), params.getMcGUID(),params.getFname());
          }
          else{
              inputStream = localFileRepository.getDocument(params.getStorageId(), params.getMcGUID(),params.getFname());
          }
          //if (timeActive) {
          //  calculateTime(1, TIMESTR_FILE);
          //}
          
          log.info("Encoding image " + (++imageCount) + " series=" + params.getSeriesUID() /*+ ", image=" + imageFile */);
          log.info(params.getWindowWidth() + ", " + params.getWindowCenter());
          if (params.getWindowWidth() < 2){
              log.info("Setting image window width to 2 to avoid dcm4che bug");
              params.setWindowWidth(2); 
              
          }
          
          MCSeries series = null;
          for(MCSeries s : ctx.getActiveCCR().getSeriesList()) {
              if(s.getMcGUID().equals(params.getMcGUID())) {
                  series = s;
                  break;
              }
          }
          
          try {
              int priority = getPriority(request);
              
              EncodeJob job = null;
              
              if (pipelineConfig.equals(PIPELINE_ITK)){
                  
                  job = 
                      new WADOStreamImageJob(
                              request.getSession().getId(), 
                              priority, 
                              params, 
                              response, 
                              response.getOutputStream(), 
                              inputFile);
                  
              }
              else if (pipelineConfig.equals(PIPELINE_DCM4CHE2)){
                  job = 
                      new WADOImageJob2(
                              request.getSession().getId(), 
                              priority, 
                              params, 
                              response, 
                              response.getOutputStream(), 
                              inputStream,
                              series);
              }
              else {
                  throw new IllegalArgumentException("Unknown image pipeline configuration " + pipelineConfig);
              }
              
              EncoderManager.getInstance().encode(job);
              
              
          } 
          catch (IOException exIo) {
              // IOException can occur just because the browser aborted reading.
              // just log info for that rather than throwing up
              log.info("Browser aborted read.[" + abortCount++ + "]");
              //exIo.printStackTrace();
          }
          catch (EncodeException exEnc) {
              log.warn("Encoding failure for image fname=" + params.getFname() + " frame=" + params.getFrameNumber(),exEnc);
          }    
          return null;
      }
      finally {
          //log.info("Exiting wado action thread " + super.toString());
      }
  }

  /**
   * Determine the file name of the image to return.
   * <p>
   * For now we just return the first image - we should actually obey the WADO spec and
   * try and figure out the file name from the SOP Instance UID.
 * @throws JDOMException 
 * @throws IOException 
   */
  private void resolveDefaults() throws IOException, JDOMException {
      LocalFileRepository repo = (LocalFileRepository) RepositoryFactory.getLocalRepository();
      DicomMetadata meta = repo.loadMetadata(params.getStorageId(), params.getMcGUID());
      params.setFname(meta.getDocumentName());
      params.setWindowCenter(Integer.parseInt(meta.getWindowCenter()));
      params.setWindowWidth(Integer.parseInt(meta.getWindowWidth()));
  }

  /**
   * Sets a cookie in response to indicate average bytes per second going 
   * back to viewer.
   * 
   * @param response
   */
  private void updateKBPerSecond(HttpServletResponse response) {
      
      // SS:  This seems to be done globally ... I'm sure that
      // the expected behavior would be that it is a per-user metric.
      // FIXME
      TimeSampledMetric metric = (TimeSampledMetric)Metric.getInstance("imageBytesPerSecond"); 
      double kbytesPerSecond = Math.round(metric.getGradient().doubleValue() / 1024.0);
      Cookie c = new Cookie("imageKBytesPerSecond", Double.toString(kbytesPerSecond));
      c.setPath("/");
      response.addCookie(c);
  }
  
  /**
   * Inspect cookies sent with this request to determine if a job priority
   * has been assigned.
   */
  private int getPriority(HttpServletRequest request) {
      int priority = EncodeJob.PRIORITY_HIGH;
      
      Cookie [] cookies = request.getCookies();
      if(cookies != null) {
          for (int i = 0; i < cookies.length; i++) {
              if("priority".equals(cookies[i].getName())) {
                  log.debug("Found priority cookie with value " + cookies[i].getValue());
                  if("low".equals(cookies[i].getValue())) {
                      priority = EncodeJob.PRIORITY_LOW;
                  }
                  else
                      if("med".equals(cookies[i].getValue())) {
                          priority = EncodeJob.PRIORITY_MED;
                      }            
              }
          }
      }
      return priority;
  }

  private static void initializeImagePipeline() {
      try {
          String pipeline = Configuration.getProperty("Image_Pipeline");
          if (PIPELINE_ITK.equalsIgnoreCase(pipeline)){
              pipelineConfig = PIPELINE_ITK;
          }
          else {
              pipelineConfig = PIPELINE_DCM4CHE2; // Olde tried and true
          }
          
      }
      catch(ConfigurationException e) {
          log.error("Can't get Image_Pipeline configuration value", e);
      }
      log.info("Image pipeline configuration: " + pipelineConfig);
  }

  public void checkRights() throws ServiceException {
      if(AccountUtil.isRealAccountId(params.getStorageId()) && !session.checkPermissions(params.getStorageId(), "R"))
          throw new InsufficientRightsException("Access credentials do not permit display of images for patient " + params.getStorageId());
  }

public WADOParameterForm getParams() {
    return params;
}

public void setParams(WADOParameterForm params) {
    this.params = params;
}
}
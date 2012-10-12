/*
 * $Id: $
 * Created on Aug 27, 2004
 */
package net.medcommons.router.services.wado;

import java.text.DecimalFormat;

/**
 * Defines constants used by the WADO Viewer
 * @author ssadedin
 */
public interface WADOConstants {
  
  /**
   * Default font used for annotations
   */
  final static String DEFAULT_FONT = "sansserif";
  
  int defaultBufferSize = 8096;
  
  
  final String PIXEL_REPRESENTATION_MONOCHROME1 = "MONOCHROME1";
  
  final String PIXEL_REPRESENTATION_MONOCHROME2 = "MONOCHROME2";
  
  final String PIXEL_REPRESENTATION_RBG = "RGB";

  final static int INTERPOLATION_FAST = 0;
  
  final static int INTERPOLATION_SMOOTH = 1;

  /**
   * Content type for jpeg images
   */
  final String CONTENT_TYPE_JPEG = "image/jpeg";

  /**
   * Content type for dicom images
   */
  final String CONTENT_TYPE_DICOM = "image/dicom";
  
  /**
   * Content type for png images
   */
  final String CONTENT_TYPE_PNG = "image/png";
  
  final static DecimalFormat magFormat = new DecimalFormat("#0.00");  
}

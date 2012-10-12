package net.medcommons.router.services.xds.consumer.web.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;



public class CCRByteArrayReader extends ByteArrayInputStream {

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CCRByteArrayReader.class);
	
	BOMMarker bomMarker = null;
	public enum BOMMarker{
		UTF8(new int[]{0xEF, 0xBB, 0xBF}),
		
		UTF16_BIG_ENDIAN(new int[]{0xFE, 0xFF}),
		UTF16_LITTLE_ENDIAN(new int[]{0xFF, 0xFE}),
		UTF32_BIG_ENDIAN(new int[]{0x00, 0x00, 0xFE, 0xFF}),
		UTF32_LITTLE_ENDIAN(new int[]{0xFF, 0xFE, 0x00, 0x00}),
		
		DEFAULT(new int[]{0x00});
		byte[] markers;
		BOMMarker(int[] markers){
			this.markers = new byte[markers.length];
			for (int i=0;i<markers.length;i++)
				this.markers[i] = (byte) markers[i];
		}
		
	}
	private byte[] initialBytes = new byte[8];
	public CCRByteArrayReader(byte[] bytes){
		
		super(bytes);
		
		System.arraycopy(bytes, 0, initialBytes, 0, initialBytes.length);
		String initialString = "";
		for (int i=0;i<initialBytes.length;i++){
			initialString +=  Integer.toHexString(initialBytes[i]&0xff) + " ";
    		
    	}
		if (log.isDebugEnabled())
		    log.debug("Initial bytes:" + initialString);
    	
		try{
			removeBOM();
		}
		catch(Exception e){
			log.error("Error detecting/skipping BOM", e);
		}
		
	}
	/**
	 * Detect the encoding; then reset stream marker to pass 
	 * the BOM characters.
	 * @throws IOException
	 */
	private void removeBOM() throws IOException{

	      for(BOMMarker enc : BOMMarker.values()){
	    	  byte [] buff = new byte[enc.markers.length];
	    	
	    	  System.arraycopy(initialBytes, 0, buff, 0, buff.length);
	    	  if (Arrays.equals(buff, enc.markers)){
	    		  bomMarker=enc;
	    		  break;
	    	  }
	    	  
	      }
	      if (bomMarker == null){
	    	  bomMarker = BOMMarker.DEFAULT;
	    	  reset();
	    	  if (log.isDebugEnabled())
	    	      log.debug("No BOM in stream ");
	      }
	      else{
	    	  reset();
	    	  skip(bomMarker.markers.length);
	    	 
	    	  if (log.isDebugEnabled())
	    	      log.debug("Read past BOM in stream:" + bomMarker.name());
	      }
		  

	}
}

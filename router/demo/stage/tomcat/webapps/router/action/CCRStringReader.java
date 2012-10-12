package net.medcommons.router.services.xds.consumer.web.action;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import org.apache.log4j.Logger;
/**
 * Wrapper class for StringReader to remove BOM (Byte Order Mark)
 * at the start of the file if it exists.
 * 
 * The encoding can be obtained via the getEncoding() method.
 * 
 * Test derived from: 
 * http://unicode.org/Public/PROGRAMS/SCSU/CompressMain.java
 * Unicode values from
 * http://unicode.org/faq/utf_bom.html
 * @author sdoyle
 *
 */
public class CCRStringReader  extends StringReader{
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CCRStringReader.class);
	
	private final static int BOM_SIZE = 1;
	private Encoding encoding = null;
	private BOMMarkers bomMarker = null;
	private byte[] initialBytes;
	
	public enum Encoding{
		UTF8 (new String("\uEFBBBF").toCharArray()),
		UTF16_BIG_ENDIAN(new String("\uFEFF").toCharArray()),
		UTF16_LITTLE_ENDIAN(new String("\uFFFE").toCharArray()),
		UTF32_BIG_ENDIAN(new String("\u0000FEFF").toCharArray()),
		UTF32_LITTLE_ENDIAN(new String("\uFFFE0000").toCharArray()),
		DEFAULT(new String("DEFAULT").toCharArray());
		char []marker;
		Encoding(char []marker){
			this.marker = marker;
		}
	};
	
	public enum BOMMarkers{
		UTF8(new int[]{0xEF, 0xBB, 0xBF}),
		
		UTF16_BIG_ENDIAN(new int[]{0xFE, 0xFF}),
		UTF16_LITTLE_ENDIAN(new int[]{0xFF, 0xFE}),
		UTF32_BIG_ENDIAN(new int[]{0x00, 0x00, 0xFE, 0xFF}),
		UTF32_LITTLE_ENDIAN(new int[]{0xFF, 0xFE, 0x00, 0x00}),
		
		DEFAULT(new int[]{0x00});
		byte[] markers;
		BOMMarkers(int[] markers){
			this.markers = new byte[markers.length];
			for (int i=0;i<markers.length;i++)
				this.markers[i] = (byte) markers[i];
		}
	}

	public  CCRStringReader(String s){
    	super(s);
    	initialBytes = s.substring(0,7).getBytes();
    	for (int i=0;i<initialBytes.length;i++){
    		log.info("byte " + i + " " + Integer.toHexString(initialBytes[i]));
    	}
    	
    	try{
    		removeBOM();
    	}
    	catch(IOException e){
    		log.error("Error testing for BOM in stream ", e);
    	}
		
    }
	/**
	 * Detect the encoding; then reset stream marker to pass 
	 * the BOM characters.
	 * @throws IOException
	 */
	private void removeBOM3() throws IOException{

	      for(BOMMarkers enc : BOMMarkers.values()){
	    	  byte [] buff = new byte[enc.markers.length];
	    	
	    	  System.arraycopy(initialBytes, 0, buff, 0, buff.length);
	    	  if (Arrays.equals(buff, enc.markers)){
	    		  bomMarker=enc;
	    		  break;
	    	  }
	    	  
	      }
	      if (bomMarker == null){
	    	  bomMarker = BOMMarkers.DEFAULT;
	    	  reset();
	    	  log.info("No BOM in stream ");
	      }
	      else{
	    	  reset();
	    	  skip(encoding.marker.length);
	    	 
	    	  log.info("Read past BOM in stream:" + bomMarker.name());
	      }
		  

	}
	/**
	 * Detect the encoding; then reset stream marker to pass 
	 * the BOM characters.
	 * @throws IOException
	 */
	private void removeBOM2() throws IOException{
		 char[] buffer = new char[8];// 8 bytes max
		 int n; 
		
	      n = read(buffer, 0, buffer.length);
	      if (n==0){
	    	  throw new IOException("Zero bytes read from stream");
	      }
	      
	      for(Encoding enc : Encoding.values()){
	    	  char [] buff = new char[enc.marker.length];
	    
	    	  System.arraycopy(buffer, 0, buff, 0, buff.length);
	    	  if (Arrays.equals(buff, enc.marker)){
	    		  encoding=enc;
	    		  break;
	    	  }
	    	  
	      }
	      if (encoding == null){
	    	  encoding = Encoding.DEFAULT;
	    	  reset();
	    	  log.info("No BOM in stream ");
	      }
	      else{
	    	  reset();
	    	  read(buffer,0,encoding.marker.length);
	    	  log.info("Read past BOM in stream:" + encoding.name());
	      }
		  

	}
	private void removeBOM() throws IOException{
		char bom[] = new char[BOM_SIZE];
		 /**
		    * Read-ahead four bytes and check for BOM marks. Extra bytes are
		    * unread back to the stream, only BOM bytes are skipped.
		    */
		 
		      int n, unread;
		      n = read(bom, 0, bom.length);
		      char BOMCHAR = new String("\uFEFF").charAt(0);
		      Character firstChar = new Character(bom[0]);
		      log.info("firstChar  = " +firstChar + ", BOMCHAR = " + BOMCHAR);
		      if (firstChar==BOMCHAR){
			   log.info("Removed BOM");
		      }
		      else{
		    	  log.info("No BOM in stream ");
		    	  reset(); // No BOM - reset to start.
		      }
	}
	
	/**
	 * Returns the encoding detected in the constructor.
	 * @return
	 */
	public Encoding getEncoding(){
		return(this.encoding);
	}
}

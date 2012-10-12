package net.medcommons.modules.crypto;

public class Utils {
	 /**
	   * A nibble->char mapping for printing out bytes. 
	   */
	  private static final char[] digits = { '0', '1', '2', '3', '4', '5', '6',
	        '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };        
	  private static final char[] hex_digits = { '0', '1', '2', '3', '4', '5', '6',
	        '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }; 
	  /**
		 * Encodes the specified bytes in hexidecimal.
		 * @param data
		 * @param offset
		 * @param length
		 * @return
		 */
		public static String encodeBytes(byte[] data, int offset, int length) {
		    int size = 2 * length;
		    char[] buf = new char[size];
		    int low_mask = 0x0f;
		    int high_mask = 0xf0;
		    int buf_pos = 0;
		    byte b;

		    int j = 0;
		    for (int i = offset; i < (offset + length); ++i) {
		      b = data[i];
		      buf[buf_pos++] = digits[(high_mask & b) >> 4];
		      buf[buf_pos++] = digits[(low_mask & b)];
		      ++j;
		    }

		    return new String(buf);
		  }      
		
		public static byte[] decodeString(String s){
			byte [] decoded = null;
			
			return(decoded);
		}
		  /**
		   * Encodes the given bytes into a base 16 string based on the given
		   * digits.
		   * 
		   * @param data
		   * @param offset
		   * @param length
		   * @param code_digits
		   * @return
		   */
		  private static char[] encodeBytes(byte[] data, int offset, int length, char[] code_digits) {
		      int size = 2 * length;
		      char[] buf = new char[size];
		      int low_mask = 0x0f;
		      int high_mask = 0xf0;
		      int buf_pos = 0;
		      byte b;
		      
		      int j = 0;
		      for (int i = offset; i < (offset + length); ++i) {
		          b = data[i];
		          buf[buf_pos++] = code_digits[(high_mask & b) >> 4];
		          buf[buf_pos++] = code_digits[(low_mask & b)];
		          ++j;
		      }
		      return buf;
		  }  
		/**
		   * Encodes the given bytes as a hex string
		   * @param data
		   * @param offset
		   * @param length
		   * @return
		   */
		  public static String hexEncodeBytes(byte[] data, int offset, int length) {
		    char[] buf = encodeBytes(data, offset, length, hex_digits);
		    return new String(buf);
		  }
}

package net.medcommons.cxp.utils;

public class IdHandling {
	 /**
     * Strips out all non-numeric characters out of an integer ID. A user
     * may have used spaces or dashes to segment the string (makes it more 
     * human-readable, but also makes it invalid.
     * @param id
     * @return
     */
    public static String normalizeId(String id){
    	if (id == null) return null;
	    String acceptableCharacters = "0123456789";
	      String result = "";
	      for ( int i = 0; i < id.length(); i++ ) {
	          if ( acceptableCharacters.indexOf(id.charAt(i)) >= 0 )
	             result += id.charAt(i);
	          }
	      return(result);
    }
}

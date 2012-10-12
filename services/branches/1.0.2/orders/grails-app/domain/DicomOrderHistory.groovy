import groovy.util.XmlSlurper;import grails.converters.JSON
/**
 * Class for tracking order history / changes
 * 
 * @author ssadedin
 */
public class DicomOrderHistory {
	
    DicomOrder	dicomOrder
	
    static constraints = {
      ddlStatus(maxSize: 30)
      remoteIp(maxSize:60)
      remoteUser(nullable: true, maxSize: 100)
      remoteHost(nullable: true, maxSize: 255)
      geoIp(nullable: true, maxSize: 80)
      notificationSent(nullable: true)
    }
    
    /**
     * cache of geo ip addresses
     */
    static geoIps = [:]
    
    /**
     * Look up geolocation of ip address and set on geoIp field
     */
    def locateIp() {
    	synchronized(geoIps) {
    		if(!geoIps[remoteIp]) {
				try {					def xml = new XmlSlurper().parse("http://ipinfodb.com/ip_query.php?ip=${remoteIp}")										if(xml.CountryCode == "RD")
						geoIps[remoteIp] = "Internal"
					else
			    		geoIps[remoteIp] = xml.City.text()?:"Unknown" + ", " + xml.CountryCode.text()?:"Unknown"
				}
				catch(Exception ex) {
					log.info "Failed to get geolocation for ip address ${remoteIp}: " + ex.message
					geoIps[remoteIp] = "Unknown Location"
				}
		  }
    	  this.geoIp = geoIps[remoteIp]
    	}
    }
 

	String ddlStatus
	String description
	String remoteIp
	String remoteUser
	String remoteHost
	String geoIp	Boolean notificationSent = false
    Date dateCreated
}

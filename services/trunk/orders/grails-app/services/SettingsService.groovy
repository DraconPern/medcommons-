import groovy.util.ConfigObject;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * Provides access to MedCommons settings - 
 */ 
class SettingsService {
    
    private static Logger log = Logger.getLogger(SettingsService)
    
    private static Map settings
	
	private static long settingsLoadTimeMs = 0L
	
    def config = ApplicationHolder.getApplication().config
	
	private static long settingsTimeoutMs = 300000
	
    Map get() {
		
		synchronized(this) {
    		if(System.currentTimeMillis()-settingsLoadTimeMs > settingsTimeoutMs) 
    		    settings = null
    		
            if(settings == null) {
    			
    			log.info "Loading settings ..."
    			
            	settings = [:]
            	            
    	        // Override settings with properties from table directly
    	        MCProperties.list().each { prop ->
    	        	println "$prop.id => $prop.value"
    	        	settings[prop.id] = prop.value
    	        }
            	
                settings += parseSettings(config.settingsFile)
                settings += parseSettings(config.settingsFile.replaceAll('settings.php$',"local_settings.php"))
                
				settingsLoadTimeMs = System.currentTimeMillis()
            }
    		
    		if(settings.acOrdersSettingsTimeoutMs)
    		    settingsTimeoutMs = settings.acOrdersSettingsTimeoutMs
		}
        
        return settings
    }
    
    static Map parseSettings(String fileName) {
        
        def results = [:]
        new File(fileName).text.split(";").each { expr ->
            // println expr
            
            // Try and match as a variable assignment
            def matcher = (expr =~ /\$([a-zA-Z0-9_]*).*= *["']([^'"]*)["'] */)
            matcher.each { all, name, value ->
                // println "Found setting: " + name
                results[name] = value
            }
			
			// Find integer values as integers
			
            def m2 = (expr =~ /\$([a-zA-Z0-9_]*).*= *([0-9][0-9]*) */)

			m2.each { all, name, value ->
                // println "Found integer setting: " + name + " = $value"
				if(results.containsKey(name))
				    return
                results[name] = Long.parseLong(value)
            }
		
			
        }
        
        return results
    }

}

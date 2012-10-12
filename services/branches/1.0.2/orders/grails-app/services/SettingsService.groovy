import groovy.util.ConfigObject;

import java.util.Map;

import org.apache.log4j.Logger;


class SettingsService {
    
    private static Logger log = Logger.getLogger(SettingsParser.class)
    
    static Map settings
    
    Map getSettings(ConfigObject config) {
        if(settings == null) {
        	settings = [:]
        	            
	        // Override settings with properties from table directly
	        MCProperties.list().each { prop ->
	        	println "$prop.id => $prop.value"
	        	settings[prop.id] = prop.value
	        }
        	
            settings += parseSettings(config.settingsFile)
            settings += parseSettings(config.settingsFile.replaceAll('settings.php$',"local_settings.php"))
            
            log.info "Timeout notifications go to : " + settings.acNotifyDDLTimeout
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
        }
        
        return results
    }

}

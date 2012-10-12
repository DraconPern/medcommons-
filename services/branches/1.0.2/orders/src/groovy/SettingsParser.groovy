
import org.apache.log4j.Logger;


public class SettingsParser {
    
    private static Logger log = Logger.getLogger(SettingsParser.class)
    
    static Map settings
    
    static Map get(def config) {
        if(settings == null) {
            settings = parseSettings(config.settingsFile)
            settings += parseSettings(config.settingsFile.replaceAll('settings.php$',"local_settings.php"))
            
	        // Override settings with properties from table directly
	        MCProperties.list().each { prop ->
	        	settings[prop.id] = prop.value
	        }
	
	            
            
            println "Timeout notifications go to : " + settings.acNotifyDDLTimeout
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
    
    static void testParseSettings() {
        
        def settings = new SecurityFilters().parseSettings(/c:\xampp\htdocs\mc\include\settings.php/)
        
        println "Secret = " + settings?.SECRET
        
    }
    
    static void main(String [] args) {
        testParseSettings()
    }
}

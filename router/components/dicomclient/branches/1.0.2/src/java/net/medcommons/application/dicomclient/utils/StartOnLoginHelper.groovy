/**
 * 
 */
package net.medcommons.application.dicomclient.utils

import org.apache.log4j.Loggerimport javax.jnlp.BasicServiceimport javax.jnlp.ServiceManagerimport javax.jnlp.UnavailableServiceExceptionimport net.medcommons.application.dicomclient.ContextManager

/**
 * Utility class that wraps registry access for automating startup
 * on boot.
 * 
 * @author ssadedin
 */
public class StartOnLoginHelper {
     
     public static Logger log = Logger.getLogger(StartOnLoginHelper.class)
     
     def runKey = /HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Run/

     /**
      * Returns true if automatic startup is supported on the current OS.
      * For now that means windows.
      */
     boolean isStartupSupported() {
         if(System.properties['os.name']?.toLowerCase()?.indexOf("windows") < 0)
             return false
             
         float ver = Float.parseFloat(System.properties['os.version'])
         log.info "OS version is " + ver
         return ver >= 5.1f
     }
     
     
     boolean isStartupEnabled() {
         "reg query $runKey /v DDL".execute().text =~ "javaws" 
     }
     
     def getURL() {
         return PollGroupCommand.resolveGatewayRoot() + "/router/ddl/start?quiet=true"
     }
     
     /**
      * Runs registry command to enable startup on login
      */
     def enableStartOnLogin() {
         if(isStartupEnabled())
             return
             
         def url = getURL()
             
         def out = ('reg add '+runKey+' /v DDL /t REG_SZ /d "javaws '+url+'"').execute().text
         
         log.info "Enabled start on login to url $url , received output: " + out.trim()
     }
    
     /**
      * Runs registry command to disable startup on login
      */
     def disableStartOnLogin() {
         if(isStartupEnabled()) {
             def cmd = "reg delete $runKey /v DDL /f"
             log.info "Executing: " + cmd
             def out = cmd.execute().text
             
             log.info "Disabled start on login, received output: " + out.trim()
         }
     }
     
     static void main(def args) {
         println "Testing  StartOnLogin"
         
         def sol = new StartOnLoginHelper()
         if(!sol.isStartupSupported()) {
             println "Startup not supported!"
             return
         }
             
     }
}

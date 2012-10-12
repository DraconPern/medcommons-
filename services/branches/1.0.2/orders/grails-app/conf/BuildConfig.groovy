import org.apache.ivy.plugins.resolver.URLResolver;
import org.apache.ivy.core.settings.IvySettings

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"
	
//grails.project.war.file = "target/${appName}-${appVersion}.war"
	
def createMedCommonsResolver() {
  def p04 = new URLResolver();
  p04.name = "p04"
  p04.m2compatible = false
  p04.addIvyPattern("http://ci.myhealthespace.com/repo/[module]/ivy-[revision].xml")
  p04.addArtifactPattern(
		  "http://ci.myhealthespace.com/repo/[module]/[artifact]-[revision].[ext]")
  
  def ivySettings = new IvySettings()
  ivySettings.defaultInit()
  p04.settings = ivySettings
  return p04
}  	
	
	
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits( "global" ) {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {        
        grailsPlugins()
        grailsHome()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        
        println "=================== Creating MC Resolver!"
        resolver createMedCommonsResolver()
        println "=================== DONE MC Resolver!"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
    	
        runtime('medcommons:medcommons-utils:1.0.54') {
        	exclude(name: "groovy-all")
        }
    }

}



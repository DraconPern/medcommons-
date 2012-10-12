// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]
// The default codec used to encode data with ${}
grails.views.default.codec="html" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

settingsFile = "/var/www/php/settings.php"

grails.serverURL = "https://"+InetAddress.getLocalHost().getHostName()

// set per-environment serverURL stem for creating absolute links
environments {
    
    development {
        grails.serverURL = "http://localhost:8080"
        appliance = "http://mc/"
        settingsFile = /c:\xampp\htdocs\mc\include\settings.php/
        security = "ip"
        // security = "group"
        smtp.server = "mail.bigpond.com"
        context = "orders"
    }

    test {
        grails.serverURL = "https://ci.myhealthespace.com"
        appliance = grails.serverURL + "/"
        security = "group"
        smtp.server = "localhost"
        context = "orders"
    }
    
    ci {
        grails.serverURL = "https://ci.myhealthespace.com"
        appliance = grails.serverURL + "/"
        security = "group"
        smtp.server = "localhost"
        context = "orders"
    }

    qa {
        appliance = grails.serverURL + "/"
        security = "group"
        smtp.server = "localhost"
        context = "orders"
    }

    production {
        // obsolete - see general configuration used above
        // grails.serverURL = "https://timc.medcommons.net"
        appliance = grails.serverURL + "/"
        security = "group"
        smtp.server = "localhost" 
        context = "orders"
    }

    timctest {
        grails.serverURL = "https://ci.myhealthespace.com"
        appliance = grails.serverURL + "/"
        security = "ip"
        smtp.server = "localhost"
        context = "timc"
    }
    
    timcqa {
        grails.serverURL = "https://qatest.myhealthespace.com"
        appliance = grails.serverURL + "/"
        security = "ip"
        smtp.server = "localhost"
        context = "timc"
    }
    
    timc {
        grails.serverURL = "https://timc.medcommons.net"
        appliance = grails.serverURL + "/"
        security = "ip"
        smtp.server = "localhost"
        context = "timc"
    }
}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    /*appenders {
        console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    }*/
    
    root {
      debug 'stdout'
      additivity = true
    }    

    info 'grails.app',
         'org.apache.commons', 
         'org.apache.tomcat.util' 

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
	       'org.codehaus.groovy.grails.web.pages', //  GSP
	       'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	       'org.codehaus.groovy.grails."web.mapping.filter', // URL mapping
	       'org.codehaus.groovy.grails."web.mapping', // URL mapping
	       'org.codehaus.groovy.grails.commons', // core / classloading
	       'org.codehaus.groovy.grails.plugins', // plugins
	       'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
	       'org.springframework',
	       'org.hibernate'

    warn   'org.mortbay.log'
}

     

import org.codehaus.groovy.grails.commons.ApplicationHolder;

// Place your Spring DSL code here
beans = {
    mailSender(org.springframework.mail.javamail.JavaMailSenderImpl) {
       host = ApplicationHolder.application.config.smtp.server
    }
    // You can set default email bean properties here, eg: from/to/subject
    mailMessage(org.springframework.mail.SimpleMailMessage) {
       from = 'ssadedin@medcommons.net'
    }
}
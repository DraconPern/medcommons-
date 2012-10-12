/*
 * $Id$
 * Created on 19/07/2007
 */
package net.medcommons.router.selftests;

import static net.medcommons.router.services.wado.stripes.SelfTestAction.register;

/**
 * Register Self Tests Here
 * 
 * @author ssadedin
 */
public class All {
    static {
        register(0,new CommonsServerConnectivity());
        register(0,new AccountServerConnectivity());
        register(1,new NodeRegistration());
        register(1,new HostnameResolution());
        register(1,new RepositoryPermissions());
        register(4,new QueryAccountServer());
        register(4,new RegisterTrackingNumbers()); 
        register(5,new QueryTrackingNumbers());
        register(5,new DownloadStatusUpdateSelfTest());
        register(6,new OAuthService());
        register(6,new AccountTranslationService());
        register(6,new JavaImageIOInstallation());
        register(6,new Backups());
    }
}

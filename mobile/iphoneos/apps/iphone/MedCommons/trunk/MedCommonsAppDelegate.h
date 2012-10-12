//
//  MedCommonsAppDelegate.h
//  ForensicFoto
//
//  Created by bill donner on 9/4/09.
//  Copyright MEDCOMMONS, INC. 2009. All rights reserved.
//
#if (!ENABLE_APD_LOGGING)
#define APD_LOG(format,...) 
#else
#define APD_LOG(format,...)  CONSOLE_LOG(format ,## __VA_ARGS__)
#endif
@class 	 Reachability;
@interface MedCommonsAppDelegate : NSObject <UIApplicationDelegate>
{

	UIImageView *helpView;



    IBOutlet UIView* contentView;
   
	
    IBOutlet UITextField* remoteHostLabel;
    
    Reachability* hostReach;
    Reachability* internetReach;
    Reachability* wifiReach;
	
	UIWindow *window;
	
	

}
@end



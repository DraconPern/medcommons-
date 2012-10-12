//
//  AppDelegate.h
//  MCProvider
//
//  Created by Bill Donner on 3/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

@class DataManager;

//@class DocumentsManager;

@class SettingsViewController;

@class SettingsManager;

@class StyleManager;

@interface AppDelegate : MCApplicationDelegate
{
@private

    SettingsViewController       *SettingsViewController_;
    NSString                 *sessionRandomID_;
  //  UINavigationController   *navigationController_;
    // This is the UI idiom we are targeting; on iPhone/iPod t
    //ouch device, only
    // UIUserInterfaceIdiomPhone is allowed; on iPad device, both
    // UIUserInterfaceIdiomPad and UIUserInterfaceIdiomPhone are allowed:
    //
    UIUserInterfaceIdiom      targetIdiom_;
	UIWindow	*window;

	
}

@property (nonatomic, retain, readonly)  DataManager             *dataManager;
//@property (nonatomic, retain, readonly)  DocumentsManager        *documentsManager;
@property (nonatomic, retain, readwrite) SettingsViewController      *SettingsViewController;
//@property (nonatomic, retain, readonly)  UINavigationController  *navigationController;
@property (nonatomic, retain, readonly)  SettingsManager         *settingsManager;
@property (nonatomic, assign, readonly)  UIUserInterfaceIdiom     targetIdiom;

+ (AppDelegate *) sharedInstance;

- (void) dieFromMisconfiguration: (NSString *) msg;




@end

//
// NSObject convenience additions:
//
@interface NSObject (AppDelegate)

@property (nonatomic, assign, readonly) AppDelegate *appDelegate;

+ (AppDelegate *) appDelegate;

@end

//
//  SettingsManager.h
//  MCProvider
//
//  Created by J. G. Pusey on 6/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DictionaryAdditions.h"




@interface SettingsManager : NSObject
{
@private

    NSDictionary   *bundleInfo_;
    NSDictionary   *environment_;//
    NSUserDefaults *userDefaults_;
}


@property (nonatomic, assign, readonly) NSString        *plistForSamples;

@property (nonatomic, assign, readonly)  BOOL            debugTrace;

@property (nonatomic, assign, readonly)  BOOL            bonjourWithPeers;
@property (nonatomic, assign, readonly)  BOOL            wifiWebserver;
@property (nonatomic, assign, readonly)  BOOL            disableMediaPlayer;

+ (SettingsManager *) sharedInstance;

- (NSDictionary *) readOnlySettingsDictionary;

- (NSDictionary *) readWriteSettingsDictionary;

- (void) synchronizeReadWriteSettings;

@end

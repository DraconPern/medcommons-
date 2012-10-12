//
//  SettingsManager.h
//  MCProvider
//
//  Created by J. G. Pusey on 6/18/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import <UIKit/UIKit.h>




@interface SettingsManager : NSObject
{
@private

    NSDictionary   *bundleInfo_;
    NSDictionary   *environment_;//
    NSUserDefaults *userDefaults_;
}
@property (nonatomic, assign, readonly) NSUInteger galleryTrigger;
@property (nonatomic, assign, readonly) NSUInteger recentsToKeep;
@property (nonatomic, assign, readonly) NSUInteger snapshotGalleryCount;


@property (nonatomic, assign, readonly) NSString        *plistForSmallSampleSet;
@property (nonatomic, assign, readonly) NSString        *plistForFullSampleSet;

@property (nonatomic, assign, readonly) NSString        *plistForVideoHelperSet;
@property (nonatomic, assign, readonly)  BOOL            debugTrace;
@property (nonatomic, assign, readonly)  BOOL            bonjourWithPeers;
@property (nonatomic, assign, readonly)  BOOL            wifiWebserver;
@property (nonatomic, assign, readonly)  BOOL            zipArchives;
@property (nonatomic, assign, readonly)  BOOL            disableMediaPlayer;
@property (nonatomic, assign, readwrite) BOOL            collabFeatures;
@property (nonatomic, assign, readwrite) BOOL            normalMode;

+ (SettingsManager *) sharedInstance;

- (NSDictionary *) readOnlySettingsDictionary;

- (NSDictionary *) readWriteSettingsDictionary;

- (void) synchronizeReadWriteSettings;


@end

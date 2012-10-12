//
//  ShooterStorage.h
//  MedPad
//
//  Created by bill donner on 4/11/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "PersonStore.h"

@interface ShooterStorageConnector : PersonStore

- (void) addAttrs: (NSDictionary *) attrs
          atIndex: (NSInteger) index;

- (NSDictionary *) attrsAtIndex: (NSInteger) index;

- (NSInteger) countPartPics;

- (NSInteger) countVideos;

- (NSString *) findFreePatientStorePath;

- (NSString *) fullPhotoSpecAtIndex: (NSInteger) index;

- (NSString *) fullSubjectPhotoSpec;

- (NSString *) fullVideoSpecAtIndex: (NSInteger) index;

- (BOOL) hasSubjectPhoto;

- (BOOL) hasPhotoAtIndex: (NSInteger) index;

- (BOOL) hasVideoAtIndex: (NSInteger) index;

- (NSString *) newSubjectPhotoSpec;

- (NSInteger) nextFreeIndex;

- (NSString *) photoSpecAtIndex: (NSInteger) index;

- (void) setPhotoSpec: (NSString *) url
              atIndex: (NSInteger) index
       withPhotoAttrs: (NSDictionary *) attrs;

- (void) setSubjectPhotoSpec: (NSString *) url
              withPhotoAttrs: (NSDictionary *) attrs;

- (void) setVideoSpec: (NSString *) url
              atIndex: (NSInteger) index
       withVideoAttrs: (NSDictionary *) attrs;

- (NSString *) subjectPhotoSpec;

- (void) swapPhotoPart: (NSInteger) p
              withPart: (NSInteger) q;

- (void) swapSubjectPhotoWithPart: (NSInteger) p;

- (void) trashPhotoPart: (NSInteger) p;

- (void) trashSubjectPhoto;

- (NSString *) videoSpecAtIndex: (NSInteger) index;

@end

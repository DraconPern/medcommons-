//
//  DataStore.h
//  MCProvider
//
//  Created by J. G. Pusey on 5/5/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DataStore : NSObject

+ (NSString *) pathForArchive:(NSString *) key;

+ (NSString *) pathForCacheEntryWithKey: (NSString *) key;

+ (NSString *) pathForGroupWithIdentifier: (NSString *) identifier;

+ (NSString *) pathForMediaDataWithIdentifier: (NSString *) identifier
                                    mediaType: (NSString *) mediaType;

+ (NSString *) pathForMemberWithIdentifier: (NSString *) identifier;

+ (NSString *) pathForSharedDocuments;

+ (NSString *) pathForSharedDocumentWithName: (NSString *) name
                                      ofType: (NSString *) type;


+ (NSString *) pathForTuneListWithName: (NSString *) name;

+ (NSString *) pathForTuneLists;

+ (NSString *) pathForItunesInbox;
+ (NSString *) pathForOnTheFlyArchive;

+ (NSString *) pathForDBLists;

+ (NSString *) pathForDBListWithName: (NSString *) name;
+ (NSString *) pathForTemporaryDocuments;
+ (NSString *) pathForThumbnails;


@end

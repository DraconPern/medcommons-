//
//  DataManager.h
//  MCProvider
//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

//#define ENABLE_APD_LOGGING   1    //- AppDelegate
//#define ENABLE_CACHE_LOGGING 1
//#define ENABLE_CAM_LOGGING   1    //- camera
//#define ENABLE_LLC_LOGGING   1  //- low level comms trace with medcommons server

#import <UIKit/UIKit.h>

#define PLIST_VERSION_NUMBER @"1.0.0"


#if defined (APP_STORE_FINAL)
#define CONSOLE_LOG(format, ...)
#else
#define CONSOLE_LOG(format, ...) CFShow ([NSString stringWithFormat: format, ## __VA_ARGS__]);
#endif

#if !ENABLE_APD_LOGGING
#define APD_LOG(format, ...)
#else
#define APD_LOG(format, ...)         CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

#if !ENABLE_CACHE_LOGGING
#define CACHE_LOG(format, ...)
#else
#define CACHE_LOG(format, ...)       CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

#if !ENABLE_CAM_LOGGING
#define CAM_LOG(format, ...)
#else
#define CAM_LOG(format, ...)         CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

#if !ENABLE_LANDSCAPE_LOGGING
#define LANDSCAPE_LOG(format, ...)
#else
#define LANDSCAPE_LOG(format, ...)   CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

#if !ENABLE_LLC_LOGGING
#define LLC_LOG(format, ...)
#else
#define LLC_LOG(format, ...)         CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

#if !ENABLE_PAN_LOGGING
#define PAN_LOG(format, ...)
#else
#define PAN_LOG(format, ...)         CONSOLE_LOG (format, ## __VA_ARGS__)
#endif
//singleton datamanager

@class ImageCache;

@interface DataManager : NSObject <UIAlertViewDelegate>
{
@private
	
	BOOL				first;
	NSString 		    *alphabetIndex;
	UIColor				*appColor;
	
	NSMutableArray *titleNodesGroupedByFirstLetter;// has outer level for A-Z and #, each has own array of particular titles for the letter
	
	// these items are rebuilt every time we add a new archive from iTunes
	NSMutableDictionary *titlesDictionary;	
	NSMutableArray *allTitles; // used for searches

	
	// recent items are not affected by new content
	//NSMutableArray *recentItems;// has just one big bucket for now

	// archive information is adjusted based on user actions and forces a rebuild of the titlesDictionary and all titles
	NSMutableArray *archives;
	NSMutableArray *archivelogos;
	NSMutableArray *archiveheaders;
	
	// stats that are maintained here
	NSNumber *totalFiles_;
	NSNumber *uniqueTunes_;
	NSNumber *fileSpaceTotal_;
	
	// look into weeding this out
	
    ImageCache           *imageCache_;
	
	
	UIAlertView *av; // only one, used for file loading dialogs
	UIAlertView *zipalert;
	NSString *incoming; 
	NSString *iname; 
	BOOL displayIncomingInfo;
	NSString *progressString;

}



@property (nonatomic, retain, readwrite) UIColor	*appColor;
@property (nonatomic, retain, readwrite) ImageCache *imageCache;
@property (nonatomic, retain, readwrite) NSNumber	*fileSpaceTotal;

@property (nonatomic, retain, readwrite) NSNumber	*totalFiles;

@property (nonatomic, retain, readwrite) NSNumber	*uniqueTunes;

@property (nonatomic, retain, readwrite) NSString *alphabetIndex;


@property (nonatomic, retain) NSMutableArray *allTitles;

@property (nonatomic, retain) NSMutableArray *titleNodesGroupedByFirstLetter;

@property (nonatomic, retain) NSMutableDictionary *titlesDictionary;


//@property (nonatomic, retain) NSMutableArray *recentItems;


@property (nonatomic, retain) NSMutableArray *archives;
@property (nonatomic, retain) NSMutableArray *archivelogos;

@property (nonatomic, retain) NSMutableArray *archiveheaders;


+ (DataManager *) sharedInstance;

-(NSString *) allocInfoProgressString;

-(void) setProgressString:(NSString *)s;
-(void) showIncomingAsProgressString;

+(NSString *) shortNameFromArchiveIndex: (NSUInteger ) s;
+(NSUInteger) indexFromArchiveName: (NSString *) archive;
+(NSString *) archiveNameFromPath:(NSString *)path;
+(NSString *) shortNameFromArchiveName: (NSString * ) s;

+(NSArray *) allocItemsFromArchive: (NSString *)archive;

+(void) onceOnlyMasterIndexInitialization;

+(void) cleanUpOldDB;
+(NSString *) reWind: (NSString * ) s;
+(NSString *) goBack: (NSString * ) s;
+(NSString *) goForward: (NSString * ) s;
+(NSString *) fastForward: (NSString * ) s;

+(BOOL) archiveExists: (NSString *)file;
//+(void) writeRecents; // writes back 
+(void) writeRefNodeItems:(NSArray *)items  toPropertyList:(NSString *)plistname;

+(void) deleteAllArchives;
-(BOOL) processIncomingFromItunes;

-(NSMutableArray *) allocReadRecents; // gets saved
-(NSMutableArray *) allocLoadRefNodeItems: (NSString * ) path ; 

-(NSMutableArray *) allocLoadRefNodeItemsFromString: (NSString * ) contents  ; 
- (void)newSetListFromFile:(NSString *)URL name:(NSString *)plistname;

-(NSUInteger)  itemCountForList:(NSString * ) path ; 
-(void) factoryReset;

+(void) writeAllTunes;

-(void) convertDirectoryToArchive:(NSString *) str;
-(void) buildNewDB;
- (void) dismissZipExpansionWaitIndicators;

+(NSInteger) recoverDB;
+(NSUInteger) zipcountItunesInbox;

+(void) finishDBSetup;

-(void) updateRecents:(id)t;

@end

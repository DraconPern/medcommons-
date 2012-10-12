
#import <MobileCoreServices/MobileCoreServices.h>
#import "ArchivesManager.h"
#import "DataStore.h"

#define CACHE_EXT @"tmp"
#define IMAGE_EXT @"png"
#define MEDIA_EXT @"dat"
#define PLIST_EXT @"plist"

static NSString *ItunesInboxDirectory;
static NSString *CachesDirectory;
static NSString *DocumentsDirectory;
static NSString *TemporaryDirectory;

@implementation DataStore

#pragma mark Public Class Methods

+ (NSString *) pathForArchive:(NSString *) key;
{
    NSAssert (DocumentsDirectory != nil,
              @"Nil temporary directory!");


    return [DocumentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"/%@",key]];
}

+ (NSString *) pathForTemporaryDocuments;
{
	return TemporaryDirectory;
}


+ (NSString *) pathForCacheEntryWithKey: (NSString *) key
{
    NSAssert (TemporaryDirectory != nil,
              @"Nil temporary directory!");

    NSString *tmpName = [NSString stringWithFormat: @"cache-%@", key];
    NSString *tmpPath = [tmpName stringByAppendingPathExtension: CACHE_EXT];

    return [TemporaryDirectory stringByAppendingPathComponent: tmpPath];
}
+ (NSString *) pathForGroupWithIdentifier: (NSString *) identifier
{
    NSAssert (TemporaryDirectory != nil,
              @"Nil temporary directory!");

    NSString *tmpName = [NSString stringWithFormat: @"group-%@", identifier];
    NSString *tmpPath = [tmpName stringByAppendingPathExtension: PLIST_EXT];

    return [TemporaryDirectory stringByAppendingPathComponent: tmpPath];
}

+ (NSString *) pathForMediaDataWithIdentifier: (NSString *) identifier
                                    mediaType: (NSString *) mediaType
{
    NSAssert (TemporaryDirectory != nil,
              @"Nil temporary directory!");

    NSString *tmpExt = ([(NSString *) kUTTypeImage isEqualToString: mediaType] ?
                        IMAGE_EXT :
                        MEDIA_EXT); // dunno what to do for kUTTypeMovie ...
    NSString *tmpName = [NSString stringWithFormat: @"media-%@", identifier];
    NSString *tmpPath = [tmpName stringByAppendingPathExtension: tmpExt];

    return [TemporaryDirectory stringByAppendingPathComponent: tmpPath];
}

+ (NSString *) pathForMemberWithIdentifier: (NSString *) identifier
{
    NSAssert (TemporaryDirectory != nil,
              @"Nil temporary directory!");

    NSString *tmpName = [NSString stringWithFormat: @"member-%@", identifier];
    NSString *tmpPath = [tmpName stringByAppendingPathExtension: PLIST_EXT];

    return [TemporaryDirectory stringByAppendingPathComponent: tmpPath];
}
+ (NSString *) pathForItunesInbox
{
    NSAssert (ItunesInboxDirectory != nil,
              @"Nil ItunesInboxDirectory directory!");
	
    return ItunesInboxDirectory;
}
+ (NSString *) pathForSharedDocuments
{
    NSAssert (DocumentsDirectory != nil,
              @"Nil documents directory!");

    return DocumentsDirectory;
}

+ (NSString *) pathForSharedDocumentWithName: (NSString *) name
                                      ofType: (NSString *) type
{
    NSAssert (DocumentsDirectory != nil,
              @"Nil documents directory!");

    NSString *tmpPath = [name stringByAppendingPathExtension: type];

    return [DocumentsDirectory stringByAppendingPathComponent: tmpPath];
}

+ (NSString *) pathForTuneLists;
{
    NSAssert (DocumentsDirectory != nil,
              @"Nil documents directory!");
	
    return [DocumentsDirectory stringByAppendingString :@"/_plists/"];
}
+ (NSString *) pathForThumbnails;
{
    NSAssert (DocumentsDirectory != nil,
              @"Nil documents directory!");
	
    return [DocumentsDirectory stringByAppendingString :@"/_thumbs/"];
}

+ (NSString *) pathForOnTheFlyArchive;
{
    NSAssert (DocumentsDirectory != nil,
              @"Nil documents directory!");
	
    return [DocumentsDirectory stringByAppendingFormat :@"/%@/",
										[ArchivesManager nameForOnTheFlyArchive]];
}


+ (NSString *) pathForDBLists;
{
    NSAssert (DocumentsDirectory != nil,
              @"Nil documents directory!");
	
    return [DocumentsDirectory stringByAppendingString :@"/_db/"];
}

+ (NSString *) pathForTuneListWithName: (NSString *) name;
{
    NSAssert (DocumentsDirectory != nil,
              @"Nil documents directory!");
	
    NSString *tmpPath = [name stringByAppendingPathExtension: @"plist"];
	
    return [[DataStore pathForTuneLists] stringByAppendingString:tmpPath];
}

+ (NSString *) pathForDBListWithName: (NSString *) name;
{
    NSAssert (DocumentsDirectory != nil,
              @"Nil documents directory!");
	
    NSString *tmpPath = [name stringByAppendingPathExtension: @"plist"];
	
    return [[DataStore pathForDBLists] stringByAppendingString:tmpPath];
}

#pragma mark Overridden NSObject Methods

+ (void) initialize
{
    ItunesInboxDirectory = [[NSSearchPathForDirectoriesInDomains (NSDocumentDirectory,
                                                                         NSUserDomainMask,
                                                                         YES)
                                    lastObject]
                                   retain];     // must hold on to this forever ...

    NSAssert (ItunesInboxDirectory != nil,
              @"Nil application support directory!");

    CachesDirectory = [[NSSearchPathForDirectoriesInDomains (NSCachesDirectory,
                                                             NSUserDomainMask,
                                                             YES)
                        lastObject]
                       retain];     // must hold on to this forever ...

    NSAssert (CachesDirectory != nil,
              @"Nil caches directory!");
	
	
    DocumentsDirectory =[ [NSString stringWithFormat:@"%@/%@",
						   [NSSearchPathForDirectoriesInDomains (NSDocumentDirectory,
																 NSUserDomainMask,
																 YES)
							lastObject],
						   @"DONOTDISTURB" ]
						 retain];      // must hold on to this forever ...

    NSAssert (DocumentsDirectory != nil,
              @"Nil documents directory!");

    TemporaryDirectory = [NSTemporaryDirectory () retain];  // must hold on to this forever ...

    NSAssert (TemporaryDirectory != nil,
              @"Nil temporary directory!");

}

- (id) init
{
    NSAssert (NO, @"Class does not support instantiation!");

    return nil;
}

@end

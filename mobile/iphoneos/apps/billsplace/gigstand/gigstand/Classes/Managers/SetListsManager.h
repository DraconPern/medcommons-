//
//  SetListsManager.h
//

#import <CoreData/CoreData.h>
#import <UIKit/UIKit.h>
#import <MediaPlayer/MediaPlayer.h>

//singleton datamanager

@class ListItemInfo,ListInfo;
@interface SetListsManager : NSObject //<UIAlertViewDelegate>
{	
	NSInteger dummy;

}

@property (nonatomic) NSInteger dummy;

+ (SetListsManager *) sharedInstance;

+(void) setup;

// setlist support
+(NSMutableArray *) makeSetlistsScan;
+(NSMutableArray *) makeSetlistsScanNoRecents;
+(NSMutableArray *) listOfTunes: (NSString * ) path ; 
+(NSUInteger)  itemCountForList:(NSString * ) path ;
+(void) updateRecents:(NSString *)newtune;
+(ListItemInfo *) insertListItemUnique:(NSString *)tune onList:(NSString *)list top:(BOOL)onTop;
+(ListInfo *) insertList:(NSString *)list;
+(ListInfo *) insertListUnique:(NSString *)list;

+(BOOL) removeOldestOnList:(NSString *)listName ;
+(void) updateTune:(NSString *)tune after: (NSString *) previous list: (NSString *) list;

+(void) updateTune:(NSString *)tune before: (NSString *) existing list: (NSString *) list;
+(BOOL) deleteList:(NSString *)list;
+(NSString *) picSpecForList:(NSString *) listName;

+(NSMutableArray *)listOfTunesFromFile: (NSString *) filePath;

+(void) makeSetList :(NSString *)list items:(NSArray *) items;

+(BOOL) removeTune:(NSString *) tune list:(NSString *) list;
@end

//
//  SetListsManager.h
//


#import <UIKit/UIKit.h>
#import <MediaPlayer/MediaPlayer.h>
#define PLIST_VERSION_NUMBER @"1.0.1"
#define STDERR_OUT [NSHomeDirectory() stringByAppendingPathComponent:@"tmp/stderr.txt"]

//singleton datamanager


@interface SetListsManager : NSObject //<UIAlertViewDelegate>
{

}

// note that ALL these routines are actually non-oo
+ (SetListsManager *) sharedInstance;
// setlist support
+(NSMutableArray *) newSetlistsScan;
+(NSMutableArray *) newSetlistsScanNoRecents;
+(NSMutableArray *) allocListOfTunes: (NSString * ) path ; 
+(NSUInteger)  itemCountForList:(NSString * ) path ;
+(void) rewriteTuneList:(NSArray *)items  toPropertyList:(NSString *)plistname;
+(void) updateRecents:(NSString *)newtune;


@end

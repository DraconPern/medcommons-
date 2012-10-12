//
//  DataManager.h
//


#import <UIKit/UIKit.h>
#import <MediaPlayer/MediaPlayer.h>
#define PLIST_VERSION_NUMBER @"1.0.1"
#define STDERR_OUT [NSHomeDirectory() stringByAppendingPathComponent:@"tmp/stderr.txt"]


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

@class ProgressString;

@class OneTuneViewController;

@interface DataManager : NSObject //<UIAlertViewDelegate>
{
	


@private	
	
	UIView *tVView; // for auxillary monitor
	CGRect tVFrame; // for aux monitor
	UIWindow *tVWindow; // for aux
	
	// these are set at startup and never ever messed with 
	NSDate *starttime;
	NSString *applicationName;
	NSString *applicationVersion;
	UIDocumentInteractionController *docInteractionController;
	MPMusicPlayerController *appMusicPlayer;
	

	// these are used locally and arent very exciting
	ProgressString	*progressString; //self->progressString = [[ProgressString alloc] init];
}

@property (nonatomic, retain) NSDate *starttime;
@property (nonatomic, retain) NSString *applicationName;
@property (nonatomic, retain) NSString *applicationVersion;

@property (nonatomic, retain) UIDocumentInteractionController *docInteractionController;
@property (nonatomic, retain)	MPMusicPlayerController *appMusicPlayer;

@property (nonatomic, retain) ProgressString	*progressString;
-(CGRect) frameForTVScreen;
-(void) setTVScreenBounds:(CGRect) rect window:(UIWindow *) winx;

-(void)setupTVScreen:(UIView *) viewx ;

// note that ALL these routines are actually non-oo
+ (DataManager *) sharedInstance;

// display properties
+(float) statusBarHeight;
+(float) standardThumbSize;
+(float) standardRowSize;
+(float) infoRowSize;
+(float) navBarHeight;
+(float) searchBarHeight;
+(float) topFudgeFactor;
+(UIColor *) applicationColor;

+(NSString *) noHungarian:(NSString *)htitle;
// painting the screen

+(UIView *)allocAppTitleView:(NSString *)titletext;
+(UIView *)allocTitleView:(NSString *)titletext;
+(CGRect) busyOverlayFrame:(CGRect)frame;

// thumbnail makers
+(UIImage *) allocSquareThumbFS:(NSString *) path  size: (float)length; 
+(UIImage *) allocSquareThumbRS:(NSString *) path  size: (float)length; 

// make a document viewer

+(UINavigationController *) allocOneTuneViewController:(NSString *)path title:(NSString *)title items:(NSArray *)items;

// document properties
+(BOOL) isPDF:(NSString *)filespec;
+(BOOL) isOpaque:(NSString *)filename;

// menus for opening documents in other programs

+(BOOL) docMenuForURL: (NSURL *) fileURL barButton: (UIBarButtonItem *) vew;
+(BOOL) docMenuForURL: (NSURL *) fileURL inView: (UIView *) vew;

// filesystem

+(float)getTotalDiskSpaceInBytes;


// inbox is the conceptual merge of emails, iTunes, and any samples

+(NSUInteger) incomingInboxDocsCount;
+(NSMutableArray *) newInboxDocumentsList;

// audio playback, currently limited to iPod player
+(void) playMusic:(NSString *)title;
+(BOOL) canPlayTune:(NSString *)s;


+(BOOL) isFileMusic :(NSString *)stype;

+(BOOL) isFileVideo :(NSString *)stype;
//

+ (NSString *) newBlurb:(NSString *)title;

+(NSString *) newLongPath:(NSString *)filePath forArchive:(NSString *)archive;

@end

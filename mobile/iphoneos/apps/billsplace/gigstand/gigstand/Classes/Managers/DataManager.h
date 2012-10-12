//
//  DataManager.h
//


#import <UIKit/UIKit.h>
#import <MediaPlayer/MediaPlayer.h>

#define MODAL_POPOVER YES
#define GigStandWorldViewHasChanged  @"GigStandWorldViewHasChanged"

#define  TITLE_WIDTH 140.0f
#define  STANDARD_NAV_HEIGHT 44.0f
#define  TOP_TOOBAR_HEIGHT 44.0f
#define  ADDRESS_BAR_HEIGHT 44.0f
#define  STATUS_BAR_HEIGHT 20.0f
#define FACTOR 1.0f


#define NAV_BAR_HEIGHT 44.f
#define SEARCH_BAR_HEIGHT 50.f
#define STANDARD_THUMB_SIZE 60.f
#define LARGER_THUMB_SIZE 70.f
#define STANDARD_ROW_SIZE 60.f
#define LARGER_ROW_SIZE 70.f

#define INFO_ROW_SIZE 40.f


#define NORMAL_TOOLBAR_HEIGHT        44.0f
#define NARROW_TOOLBAR_HEIGHT        32.0f

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

@class HTTPServer;

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
	
	
	NSString *myLocalIP;
	NSUInteger myLocalPort;
	
	// these are used locally and arent very exciting
	ProgressString	*progressString; //self->progressString = [[ProgressString alloc] init];
	
	BOOL inSim; //true if running under simulator
	HTTPServer *httpServer;
}
@property () BOOL inSim;
@property (nonatomic, retain) NSDate *starttime;
@property (nonatomic, retain) NSString *applicationName;
@property (nonatomic, retain) NSString *applicationVersion;

@property (nonatomic, retain) UIDocumentInteractionController *docInteractionController;
@property (nonatomic, retain)	MPMusicPlayerController *appMusicPlayer;

@property (nonatomic, retain) ProgressString	*progressString;

@property (nonatomic, retain) NSString *myLocalIP;
@property (nonatomic) NSUInteger myLocalPort;


-(CGRect) frameForTVScreen;
-(void) setTVScreenBounds:(CGRect) rect window:(UIWindow *) winx;

-(void)setupTVScreen:(UIView *) viewx ;

-(void) httpServerGo;

// note that ALL these routines are actually non-oo
+ (DataManager *) sharedInstance;

// display properties
+(float) statusBarHeight;
+(float) standardThumbSize;
+(float) standardRowSize;
+(float) infoRowSize;
+(float) navBarHeight;
+(float) searchBarHeight;
+(float) toolBarHeight;
+(float) topFudgeFactor;
+(UIColor *) applicationColor;

+(NSString *) noHungarian:(NSString *)htitle;
// painting the screen

+(UIView *)makeAppTitleView:(NSString *)titletext;
+(UIView *)makeTitleView:(NSString *)titletext;
+(CGRect) busyOverlayFrame:(CGRect)frame;

// thumbnail makers
+(UIImage *) makeThumbFS:(NSString *) path  size: (float)length; 
+(UIImage *) makeThumbRS:(NSString *) path  size: (float)length; 

// make a document viewer

+(UIViewController *) makeOneTuneViewController:(NSString *)path title:(NSString *)title items:(NSArray *)items;

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

+(NSString *) makeBlurb:(NSString *)title;

+(NSString *) deriveLongPath:(NSString *)filePath forArchive:(NSString *)archive;


+(void) worldViewPulse;
+(BOOL) modalPopOversEnabled;


+(UIImage *)captureView:(UIView *)view scale:(float)scalefactor;

+(UIImage *)xcaptureView:(UIView *)view ;


+(NSString *) saveImageToSnapshotsGallery:  (UIImage *) image tune: (NSString *) tune;

+(NSArray *) list:(NSArray *) list bringToTop:(NSArray *) special;

@end

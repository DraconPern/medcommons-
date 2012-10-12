//
//  DataManager.h
//  MedPad
//
//  Created by bill donner on 4/11/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

// put some data like constant stuff in here too to avoid having a MedCommons.h file

#define PLEASE_SHOOT_PATIENT_IMAGE @"Silhouette.png"


#define TRASH_CAN_IMAGE @"totrash.png"

#define BASE_PATH @"mcImage"
#define BASE_TYPE @"png"

#define MAIN_LOGO @"logoGray_310x65"
#define MAIN_LOGO_IMAGE @"logoGray_310x65.png"
#define MAIN_LOGO_TYPE @"png"

#define BREADCRUMBS_PUSH    [(BreadCrumbs *)[[DataManager sharedInstance] ffBreadCrumbs] push:[[self class] description]]
#define BREADCRUMBS_POP   [(BreadCrumbs *)[[DataManager sharedInstance] ffBreadCrumbs] pop ]

#define TRY_RECOVERY [[DataManager sharedInstance] tryRecovery:self]



#if defined(APP_STORE_FINAL)
#define MY_ASSERT(STATEMENT) do {(void)sizeof(STATEMENT);} while(0)
#else
#define MY_ASSERT(STATEMENT) do { assert(STATEMENT);} while(0)
#endif

#if defined(APP_STORE_FINAL)
#define CONSOLE_LOG(format,...)
#else
#define CONSOLE_LOG(format,...)  CFShow([NSString stringWithFormat:[NSString stringWithString:format],## __VA_ARGS__]);
#endif

#if (!ENABLE_PAN_LOGGING)
#define PAN_LOG(format,...)
#else
#define PAN_LOG(format,...)  CONSOLE_LOG(format ,## __VA_ARGS__)
#endif

#if (!ENABLE_CAM_LOGGING)
#define CAM_LOG(format,...)
#else
#define CAM_LOG(format,...)  CONSOLE_LOG(format ,## __VA_ARGS__)
#endif

#if (!ENABLE_CACHE_LOGGING)
#define CACHE_LOG(format,...)
#else
#define CACHE_LOG(format,...)  CONSOLE_LOG(format ,## __VA_ARGS__)
#endif

#if (!ENABLE_LANDSCAPE_LOGGING)
#define LANDSCAPE_LOG(format,...)
#else
#define LANDSCAPE_LOG(format,...)  CONSOLE_LOG(format ,## __VA_ARGS__)
#endif

#if (!ENABLE_BREADCRUMBS_LOGGING)
#define BREADCRUMBS_LOG(format,...)
#else
#define BREADCRUMBS_LOG(format,...)  CONSOLE_LOG(format ,## __VA_ARGS__)
#endif

//singleton datamanager
@class SegmentMap,RootViewController,DetailViewController,AppDelegate,GPSDevice,BreadCrumbs,ShooterStorageConnector,CustomViews,PersonInFocus;
@interface DataManager : NSObject {
    BOOL sameplist; // has custom methods below
    NSInteger currentScene;
    NSInteger currentBlock; // these point into the master plist
    NSDictionary *masterPlist; // this is loaded directly
    SegmentMap *leftDetailSM;
    SegmentMap *leftDetailReversedSM;
    SegmentMap *rightDetailSM;
    SegmentMap *rightRootSM;
    SegmentMap *leftRootSM;
    SegmentMap *bottomRootSM;
    RootViewController *rootController;
    DetailViewController *detailController;
    AppDelegate *appDelegate;

    GPSDevice *ffGPSDevice;
    BreadCrumbs *ffBreadCrumbs;
    CustomViews *ffCustomViews;
    ShooterStorageConnector *ffPatientStore;

    PersonInFocus *ffPatientWrapper;

    NSString *ffMCmcid;
    NSNumber *ffNextFileIndex;

    NSString *ffAppLogoImage;
    NSString *ffMCusername;
    NSString *ffMCpassword;
    NSString *ffMCauth;
    NSString *ffMCpracticename;
    NSMutableArray *ffMCpatientlist;
    NSString *ffMCappliance;

    NSString *ffMCprovidermcid;

    NSOperationQueue *ffSharedOperationQueue;
}

@property (nonatomic, retain)  AppDelegate *appDelegate;
@property (nonatomic, retain)  RootViewController *rootController;
@property (nonatomic, retain)  DetailViewController *detailController;
@property (nonatomic, retain)  SegmentMap *leftDetailSM;
@property (nonatomic, retain)  SegmentMap *leftDetailReversedSM;
@property (nonatomic, retain)  SegmentMap *rightDetailSM;
@property (nonatomic, retain)  SegmentMap *rightRootSM;
@property (nonatomic, retain)  SegmentMap *leftRootSM;
@property (nonatomic, retain)  SegmentMap *bottomRootSM;
@property (nonatomic, retain)  NSDictionary *masterPlist;
@property (nonatomic, retain)  GPSDevice *ffGPSDevice;
@property (nonatomic, retain)  BreadCrumbs *ffBreadCrumbs;
@property (nonatomic, retain)   CustomViews *ffCustomViews;
@property (nonatomic, retain)  ShooterStorageConnector *ffPatientStore;


@property (nonatomic, retain)  PersonInFocus *ffPatientWrapper;

@property (nonatomic, retain)   NSString *ffMCmcid;
@property (nonatomic, retain)   NSNumber *ffNextFileIndex;
@property (nonatomic, retain)  NSString *ffAppLogoImage;

@property (nonatomic, retain)  NSString *ffMCusername;
@property (nonatomic, retain)  NSString *ffMCpassword;
@property (nonatomic, retain)  NSString *ffMCauth;
@property (nonatomic, retain)  NSString *ffMCpracticename;
@property (nonatomic, retain)  NSMutableArray *ffMCpatientlist;
@property (nonatomic, retain)  NSString *ffMCappliance;

@property (nonatomic, retain)  NSString *ffMCprovidermcid;

@property (nonatomic, retain)  NSOperationQueue *ffSharedOperationQueue;

+ (DataManager *) sharedInstance;
-(void) setScene:(NSInteger) _sm;
-(NSInteger) currentScene;
-(id) dieFromMisconfiguration:(NSString *) msg;
-(NSDictionary *)  currentSceneContext;
-(NSDictionary *) currentBlock: (int) blocknum forScene: (NSDictionary *) scene;
-(NSArray *) allScenes;
-(BOOL) samePlist;
-(void) setSamePlistFlag:(BOOL) b;

- (void) invokeActionBlockMethod: (NSDictionary *) block;

- (void) invokeSavedActionBlockMethod: (NSString *) selectorName
                              inClass: (NSString *) className
                           withObject: (id) obj;

- (void) performSelector: (SEL) selector
                 inClass: (Class) klass
              withObject: (NSDictionary *) obj;

-(BOOL) tryRecovery:(UIViewController *)who;

@end




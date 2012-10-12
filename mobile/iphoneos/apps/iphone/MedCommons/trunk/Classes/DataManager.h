//
//  DataManager.h
//  MedCommons
//
//  Created by bill donner on 1/29/10.
// 
//
@class DashboardMainController,PatientStore,RESTComms,Reachability,GPSDevice,BreadCrumbs,DashboardPatient,AsciiInt,HistoryCase,CustomViews;

@interface DataManager : NSObject {
	NSString *ffAppKey;
	NSString *ffVersion;
	NSString *ffClassPrefix;
	NSString *ffAppServicesPath;
	DashboardMainController *ffOuterController;
	PatientStore *ffPatientStore;
	RESTComms *ffRESTComms;
	CustomViews *ffCustomViews;
    NSMutableDictionary *ffImageCache;
	NSString *ffAppLogoImage;
	NSString *ffAppBitsPath;
	NSString *ffAppMetaPath;
	NSString *ffAppDataStore;
	NSString *ffHostReachStatus;
	NSString *ffInternetReachStatus;
	NSString *ffWifiReachStatus;
	NSString *ffSummaryReachStatus;
	NSString *ffHostReachImage;
	NSString *ffInternetReachImage;
	NSString *ffWifiReachImage;
    NSString *ffSummaryReachImage;
	NSString *ffMCusername;
	NSString *ffMCpassword;
	NSString *ffMCauth;
	NSString *ffMCpracticename;
	NSMutableArray *ffMCpatientlist;
    NSString *ffMCappliance;
	NSString *ffMCpracticecursor;
	NSString *ffMCprovidermcid;
	NSString *ffMCmcid;
	UIWindow *ffMainWindow;
	NSOperationQueue *ffSharedOperationQueue;
	GPSDevice *ffGPSDevice;
	BreadCrumbs *ffBreadCrumbs;
	DashboardPatient *ffPatientWrapper;
	AsciiInt *ffNextFileIndex;
	NSMutableArray *ffHistory;
	
	BOOL inSelfMode; // has custom methods below 
	NSString* selectedPatientIdx;
	
}

@property (nonatomic, retain)  NSString *ffAppKey;
@property (nonatomic, retain)  NSString *ffVersion;
@property (nonatomic, retain)  NSString *ffClassPrefix;
@property (nonatomic, retain)  NSString *ffAppServicesPath;
@property (nonatomic, retain)  DashboardMainController *ffOuterController;
@property (nonatomic, retain)  PatientStore *ffPatientStore;
@property (nonatomic, retain)  NSMutableDictionary *ffImageCache;
@property (nonatomic, retain)  RESTComms *ffRESTComms;
@property (nonatomic, retain)  CustomViews *ffCustomViews;
@property (nonatomic, retain)  NSString *ffAppLogoImage;
@property (nonatomic, retain)  NSString *ffAppBitsPath;
@property (nonatomic, retain)  NSString *ffAppMetaPath;
@property (nonatomic, retain)  NSString *ffAppDataStore;
@property (nonatomic, retain)  NSString *ffHostReachStatus;
@property (nonatomic, retain)  NSString *ffInternetReachStatus;
@property (nonatomic, retain)  NSString *ffWifiReachStatus;
@property (nonatomic, retain)  NSString *ffSummaryReachStatus;
@property (nonatomic, retain)  NSString *ffHostReachImage;
@property (nonatomic, retain)  NSString *ffInternetReachImage;
@property (nonatomic, retain)  NSString *ffWifiReachImage;
@property (nonatomic, retain)  NSString *ffSummaryReachImage;
@property (nonatomic, retain)  NSString *ffMCusername;
@property (nonatomic, retain)  NSString *ffMCpassword;
@property (nonatomic, retain)  NSString *ffMCauth;
@property (nonatomic, retain)  NSString *ffMCpracticename;
@property (nonatomic, retain)  NSMutableArray *ffMCpatientlist;
@property (nonatomic, retain)  NSString *ffMCappliance;
@property (nonatomic, retain)  NSString *ffMCpracticecursor;
@property (nonatomic, retain)  NSString *ffMCprovidermcid;
@property (nonatomic, retain)  NSString *ffMCmcid;
@property (nonatomic, retain)  UIWindow *ffMainWindow;
@property (nonatomic, retain)  NSOperationQueue *ffSharedOperationQueue;
@property (nonatomic, retain)  GPSDevice *ffGPSDevice;
@property (nonatomic, retain)  BreadCrumbs *ffBreadCrumbs;
@property (nonatomic, retain)  DashboardPatient *ffPatientWrapper;
@property (nonatomic, retain)  AsciiInt *ffNextFileIndex;
@property (nonatomic, retain)  NSMutableArray *ffHistory;

+ (DataManager *) sharedInstance;       
-(void) setSelfMode:(BOOL) _sm;
-(BOOL) selfMode;
-(BOOL) tryRecovery:(UIViewController *)who;
-(NSString *)selectedPatientIndex;
-(void) setSelectedPatientIndex:(NSString *) row;
-(HistoryCase *) historyCase:(int)casenum;
-(int) historyCount;
-(void) saveToHistory:(NSDictionary *) dict  withGeneralAttrs: (NSDictionary *) attrs  andWithPhotoAttributes :(NSMutableArray *) attrdicts;
@end
@interface AsciiInt : NSObject {
NSInteger myint;
NSString *mytag;
}
-(void) initWithStringVal:(NSString *) s andWithTag:(NSString *)tag;
-(NSString *) bump;
@end
@interface BreadCrumbs : NSObject {
	NSMutableArray *crumbs;
	NSMutableArray  *inrecoverycrumbs;
	NSString *patientIndex;
}
-(BreadCrumbs *) init;
-(id) pop;
-(void) push:(NSString *)obj;
-(NSString *)popRecoveryCrumb;
@end

#define APPLICATION_VERSION @"0.531"
#define MAXGROUPS 3

#define ENABLE_BREADCRUMBS_LOGGING 0
#define ENABLE_LLC_LOGGING 0
#define ENABLE_APD_LOGGING 1
#define ENABLE_PAN_LOGGING 0
#define ENABLE_CAM_LOGGING 0
#define ENABLE_CACHE_LOGGING 0
#define ENABLE_LANDSCAPE_LOGGING 0


#define MOBILE_PATH @"acct/m/iphone"
#define MOBILE_SHARE_SUFFIX @".php?mode=0.400"

#define kHowManySlots  11


#define MAIN_LOGO @"logoGray_310x65"
#define MAIN_LOGO_IMAGE @"logoGray_310x65.png"
#define MAIN_LOGO_TYPE @"png"

#define BLACK_LOGO @"mclogoblack"
#define BLACK_LOGO_IMAGE @"mclogoblack.png"
#define BLACK_LOGO_TYPE @"png"

#define DESIGNER_LOGO @"martoonsLogo"
#define DESIGNER_LOGO_IMAGE @"martoonsLogo.png"
#define DESIGNER_LOGO_TYPE @"png"

#define PLEASE_SHOOT_PATIENT_IMAGE @"Silhouette.png"


#define TRASH_CAN_IMAGE @"totrash.png"

#define BASE_PATH @"mcImage"
#define BASE_TYPE @"png"
#define DOCSFOLDER [NSHomeDirectory() stringByAppendingPathComponent:@"Documents"]
#define ROOTFOLDER NSHomeDirectory()

#define CACHEDDASHBOARDPATH [NSHomeDirectory() stringByAppendingPathComponent:@"Documents/MedCommonsDashboard.json"]
#define HISTORYPATH [NSHomeDirectory() stringByAppendingPathComponent:@"Documents/mcHistory.dat"]

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

#define BREADCRUMBS_PUSH    [(BreadCrumbs *)[[DataManager sharedInstance] ffBreadCrumbs] push:[[self class] description]] 
#define BREADCRUMBS_POP   [(BreadCrumbs *)[[DataManager sharedInstance] ffBreadCrumbs] pop ]

#define TRY_RECOVERY [[DataManager sharedInstance] tryRecovery:self]


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

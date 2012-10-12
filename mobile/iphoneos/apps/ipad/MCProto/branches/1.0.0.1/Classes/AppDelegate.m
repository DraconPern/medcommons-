//
//  AppDelegate.m
//  MedPad
//
//  Created by bill donner on 3/4/10.
//  Copyright Apple Inc 2010. All rights reserved.
//
#import "MCCustom+View.h"
#import "WebPaneController.h"
#import "AppDelegate.h"
#import "RootViewController.h"
#import "DetailViewController.h"
#import "DataManager.h"
#import "SegmentedControl.h"
#import "SegmentMap.h"
#import "BreadCrumbs.h"
#import "GPSDevice.h"





@implementation AppDelegate

//JGP   @synthesize detailViewController;
//JGP   @synthesize rootViewController;
@synthesize splitViewController;
@synthesize window;

- (id) dieFromMisconfiguration: (NSString *) msg
{
    // put up a fatal error message, usually because of a misconfiguration
    CGRect bounds = [[UIScreen mainScreen] bounds];

    //put up something other than the white background
    UIImageView *imageView = [[[UIImageView alloc]
                               initWithImage: [UIImage imageNamed: @"nAppleIcon_512x512.png"]]
                              autorelease];

    imageView.center = CGPointMake (bounds.size.width / 2.0f,
                                    bounds.size.height / 2.0f);

    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle: @"MedPad"
                                                        message: msg
                                                       delegate: self
                                              cancelButtonTitle: @"OK"
                                              otherButtonTitles: nil];

    [alertView show];
    [alertView release];

    [window addSubview: imageView];
    [window makeKeyAndVisible];

    return nil;
}



#pragma mark setup controllers based on settings bundle
-(void) setupControllers
{
    DataManager *mcs =  [DataManager sharedInstance];

    NSUserDefaults *uprefs = [NSUserDefaults standardUserDefaults]  ;
	
    // figure out our personality if any

    //NSDictionary *environment =    [[[NSBundle mainBundle] infoDictionary]   objectForKey:@"LSEnvironment"];
    //NSString *appkey = [environment objectForKey:@"appkey"];
    //NSString *fixedcontent = [environment objectForKey:@"fixedcontent"];
    //NSString *uberservices = [environment objectForKey:@"servicesurl"];
    // if an appkey was defined then
    //
    //  mcs.ffOuterController = [[DashboardMainController alloc] init];
    //  mcs.ffRESTComms = [[RESTComms alloc] init];

    mcs.ffCustomViews   = [[MCCustomViews alloc] init];
    //  mcs.ffImageCache = [[NSMutableDictionary alloc] init];
    //  mcs.ffAppServicesPath = uberservices;
    //  mcs.ffAppKey = appkey;
    //  mcs.ffVersion = APPLICATION_VERSION;
    //  mcs.ffClassPrefix = @"FF";
    mcs.ffAppLogoImage = @"icon_cross2.png";
    //  mcs.ffAppBitsPath = [environment objectForKey:@"bitsurl"];
    //  mcs.ffAppMetaPath = [environment objectForKey:@"metaurl"];
    //  mcs.ffAppDataStore =[environment objectForKey:@"mids"];


    ;
    if (([uprefs boolForKey:@"MCReset"]) && (NO == [uprefs boolForKey:@"MCReset"]))
    {
        mcs.ffMCpassword =[uprefs objectForKey:@"MCPassword"];
        mcs.ffMCusername =[uprefs objectForKey:@"MCUsername"];
        mcs.ffMCappliance =[uprefs objectForKey:@"MCAppliance"];
    }
    else

    {
        mcs.ffMCappliance=@"healthurl.medcommons.net";
        mcs.ffMCusername =@"sigmundfranklin@medcommons.net";
        mcs.ffMCpassword =@"tester";
    }


    APD_LOG (@">>restart %@ %@ %@ %@",mcs.ffMCappliance, mcs.ffMCusername,
             [uprefs valueForKey:@"breadcrumbs"] ,
             [uprefs valueForKey:@"patientcrumbs"]);

    APD_LOG (@"App %@  version %@ is calling remote web services %@ with appkey %@",
             [[[NSBundle mainBundle] infoDictionary]   objectForKey:@"CFBundleName"],
             [[[NSBundle mainBundle] infoDictionary]   objectForKey:@"CFBundleVersion"],
             uberservices,
             appkey);

    NSOperationQueue * theQueue = [[NSOperationQueue alloc] init];
    mcs.ffSharedOperationQueue = theQueue; // set this up for background processing
    [theQueue release];

    GPSDevice *device =[[GPSDevice alloc] init]; // make a GPS thing, but the camera controller will turn it on and off to save power
    mcs.ffGPSDevice = device;
    [device release];

    BreadCrumbs *bc =[[BreadCrumbs alloc] init];
    mcs.ffBreadCrumbs = bc;
    [bc release];



    mcs.ffNextFileIndex = [NSNumber numberWithInt:9990000];


    //  [mcs setSelectedPatientIndex:   [uprefs valueForKey:@"patientcrumbs"]];
    //
    //  mcs.ffHistory = [[NSMutableArray alloc] init];

    //  return [mcs ffOuterController]; // dont put this
}

- (NSString *) handleOpenURL: (NSURL *) url
{
    if (!url)
    {
        // The URL is nil. There's nothing more to do.
        return nil;
    }

    NSString *URLString = [url absoluteString];

    if (!URLString)
    {
        // The URL's absoluteString is nil. There's nothing more to do.
        return nil;
    }

    NSString *key = nil;
    NSString *value = nil;
    NSString *querystring = [url query];
    NSArray  *queryComponents = [querystring componentsSeparatedByString: @"&"];
    NSString *queryComponent;

    for (queryComponent in queryComponents)
    {
        NSArray *query = [queryComponent componentsSeparatedByString: @"="];

        if ([query count] == 2)
        {
            key = [query objectAtIndex: 0];
            value = [query objectAtIndex: 1];
        }
    }

    return [value retain];  // ??? JGP
}

- (NSDictionary *) readMasterConfig: (NSDictionary *) launchOptions
{
    NSUserDefaults *uprefs = [NSUserDefaults standardUserDefaults];
    NSString       *surl;

    if (launchOptions)
    {
        // check for remote launch
        //    UIApplicationLaunchOptionsURLKey = x-medpad-services://prototyper?plist=foo.bar;
        //    UIApplicationLaunchOptionsSourceApplicationKey = "com.apple.webapp-F8C7BED646CF40ADBCB297C21380A32D";

        NSLog(@"LaunchOptions URL %@",
              [[launchOptions objectForKey: @"UIApplicationLaunchOptionsURLKey"] absoluteString]);
        NSLog(@"LaunchOptions SourceApplicationKey %@",
              [launchOptions objectForKey: @"UIApplicationLaunchOptionsSourceApplicationKey"]);

        surl = [self handleOpenURL: [launchOptions objectForKey: @"UIApplicationLaunchOptionsURLKey"]];

        [[DataManager sharedInstance] setSamePlistFlag: NO];
    }
    else
    {

        NSLog (@"no launch options lastURL %@",
               [uprefs objectForKey: @"MCLastURL"]);

        if ([uprefs boolForKey: @"MCUseSamples"] && (YES == [uprefs boolForKey:@"MCUseSamples"]))
            surl = [uprefs objectForKey: @"MCSampleURL"];
        else
            surl = [uprefs objectForKey: @"MCEnteredURL"];

        if (!surl)
            //return [self dieFromMisconfiguration:@"Please set plist in settings->MedPad"];
        {
            if ([uprefs objectForKey: @"MCLastURL"])
                surl = [uprefs objectForKey: @"MCLastURL"];   // if nothing then nothing
            else
                surl = @"http://medcommons.net/MedCommonsPrototyper/samples/HelloMedPad.xml";
        }

        // figure out whether this is the same plist as last time because that is the only way we can honor the saved scene and block numbers
        if ([uprefs objectForKey: @"MCLastURL"] &&
            [[uprefs objectForKey: @"NCLastURL"] isEqual: surl])
            [[DataManager sharedInstance] setSamePlistFlag: YES];
        else
            [[DataManager sharedInstance] setSamePlistFlag: NO];
    }

    NSURL *url = [NSURL URLWithString: [surl stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]]];

    NSLog (@"Reading plist %@", url);

    NSDictionary *temp = [[NSDictionary alloc] initWithContentsOfURL: url];

    if (!temp)  //
        return [self dieFromMisconfiguration: [NSString stringWithFormat: @"Error reading plist %@", url]];
    //  else
    //      [temp retain];

    [uprefs setObject: [url description]
               forKey: @"MCLastURL"] ; // stash the last url we are running with

    return temp;
}

- (void) writeLinksToLog
{
    // called only from special build to make link farm
#define LOGLinks(format, ...)  CFShow ([NSString stringWithFormat: [NSString stringWithString: format], ## __VA_ARGS__])

    DataManager *dm = [DataManager sharedInstance];
    NSArray     *scenes = [dm allScenes];

    if (!scenes)
        return;

    for (NSDictionary *scene in scenes)
    {
        NSArray *blocks = [scene objectForKey: @"blocks"];
        int      blockcount = [blocks count];

        for (int ind = 0; ind < blockcount; ind++)
        {
            NSDictionary *block = [dm currentBlock:ind forScene: scene];

            if (!block)
                return;

            NSString *blocktype = [block objectForKey: @"Action"];

            if ([@"ShowDetail" isEqual: blocktype])
            {
                LOGLinks  (@"<li><a  title='%@ - %@' href='%@' >%@</a></li>\n\r",
                           [[block objectForKey: @"Info"]
                            stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]],
                           [[block objectForKey: @"Label"]
                            stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]],
                           [[block objectForKey: @"URL"]
                            stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]],
                           [[block objectForKey: @"Title"]
                            stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]]);
            }
        }
    }
}

#pragma mark -

- (void) rootSegmentIndex: (MySegmentedControl *) sender
                 atOrigin: (int) origin
{
    // all the root guys come thru here because there is no special processing as with the detail segments
    NSUInteger   index = sender.selectedSegmentIndex - origin;

    NSLog (@"dispatching hit: %d  type: %@",
           sender.selectedSegmentIndex,
           [sender.blocktypes objectAtIndex: index]);

    DataManager *dm = [DataManager sharedInstance];
    NSString    *blockType = [sender.blocktypes objectAtIndex: index];

//    if ([@"InvokeMethod" isEqual: blockType])
//    {
//        [dm invokeSavedActionBlockMethod: [sender.methods objectAtIndex: index]
//                                 inClass: [sender.classes objectAtIndex: index]
//                              withObject: [sender.args objectAtIndex: index]];
//    }
//    else 
	if ([@"ShowDetail" isEqual: blockType])
    {
        [[self originalDetailViewController] displayDetailWebView: [sender.urls objectAtIndex: index]
                                                        backcolor: [UIColor yellowColor]
                                                            title: [sender.titles objectAtIndex: index]];
    }
    else if ([@"ReplaceScene" isEqual: blockType])
    {
        // change the scene and refresh the table
        NSArray  *scenes = [dm allScenes];
        NSString *tscene = [[sender.urls objectAtIndex: index]
                            stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];
        int       itscene = [tscene intValue];

        if ((itscene < 0) || (itscene >= [scenes count]))
        {
            [dm dieFromMisconfiguration: @"Destination ReplaceScene does not exist"];

            return;
        }

        [dm setScene: itscene];

        [dm.rootController remotePoke: [sender.titles objectAtIndex: index]];
    }
    else if ([@"GotoScene" isEqual: blockType])
    {
        // change the scene and refresh the table

        NSArray  *scenes = [dm allScenes];
        NSString *tscene = [[sender.urls objectAtIndex: index]
                            stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];
        int       itscene = [tscene intValue];

        if ((itscene < 0) || (itscene >= [scenes count]))
        {
            [dm dieFromMisconfiguration: @"Destination GotoScene does not exist"];

            return;
        }

        [dm setScene: itscene];

        [dm.rootController remotePoke: [sender.titles objectAtIndex: index]];

        ///VVVVV is what differentiates this
        [[self originalDetailViewController] displayDetailWebView: [sender.urls objectAtIndex:index]
                                                        backcolor: [UIColor greenColor]
                                                            title: [sender.titles objectAtIndex:index]];
    }
    // ignore unknown blocktypes for now
}

- (void) rootSegmentHandler: (MySegmentedControl *) sender
{
    [self rootSegmentIndex: sender atOrigin: 0];
}

- (void) leftDetailSegmentHandler: (MySegmentedControl *) sender
{
    // these are fiddly because there is always an arrow spacer on the details
    DataManager *dm = [DataManager sharedInstance];

    if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
        [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
    {
        if (sender.selectedSegmentIndex == 0)
            [[self originalDetailViewController] toggleLeftPanel];
        else
            [self rootSegmentIndex: sender
                          atOrigin: 1];
    }
    else
        [self rootSegmentIndex: sender
                      atOrigin: 0];

    //NSLog(@"leftDetail hit: %d  type: %@ url: %@   ",
    //            sender.selectedSegmentIndex, [sender.blocktypes objectAtIndex:sender.selectedSegmentIndex-1],[sender.urls objectAtIndex:sender.selectedSegmentIndex-1]);
}

- (void) rightDetailSegmentHandler: (MySegmentedControl *) sender
{
    //  NSLog(@"rightDetail hit: %d  type: %@ url: %@   ",
    //        sender.selectedSegmentIndex, [sender.blocktypes objectAtIndex:sender.selectedSegmentIndex],[sender.urls objectAtIndex:sender.selectedSegmentIndex]);
    DataManager *dm = [DataManager sharedInstance];

    if ([dm.masterPlist objectForKey: @"gotoSafariButton"] &&
        [[dm.masterPlist objectForKey: @"gotoSafariButton"] isEqual: @"yes"])
    {
        if (sender.selectedSegmentIndex == (sender.numberOfSegments - 1))
            [[self originalDetailViewController] viewInSafari];
        else
            [self rootSegmentIndex: sender
                          atOrigin: 0]; // otherwise handle normally
    }
    else
        [self rootSegmentIndex: sender
                      atOrigin: 0];
}

#pragma mark Application lifecycle

-(void) alertView: (UIAlertView *) alertView
clickedButtonAtIndex: (NSInteger) buttonIndex
{
    exit (1);
}

- (BOOL) application: (UIApplication *) application
didFinishLaunchingWithOptions: (NSDictionary *) launchOptions
{
	
	
	NSLog(@"NSUserDefaults dump: %@", [[NSUserDefaults standardUserDefaults] dictionaryRepresentation]);
	
    DataManager *dm = [DataManager sharedInstance];

    dm.appDelegate = self;

    NSDictionary *config = [self readMasterConfig: launchOptions]; // handles its own errors

    if (config)
    {
        //  build top left and right for detail view
        NSMutableArray      *details;
        NSMutableArray      *segments;
        NSMutableArray      *urls;
        NSMutableArray      *blocktypes;
        NSMutableArray      *titles;
        NSMutableArray      *classes;
        NSMutableArray      *methods;
        NSMutableArray      *args;
        NSMutableDictionary *odict;
        NSString            *size;

        dm.masterPlist = config; // store this for everone to see
                                 //  this rootController returned here is not useful, get it from the init routine
                                 // dm.rootController = [splitViewController.viewControllers objectAtIndex:0];

        //  dm.detailController = [splitViewController.viewControllers objectAtIndex:1];
        // build the top right and left and bottom button decorators for the Root View
        //////
        /////
        segments = [NSMutableArray array];
        urls = [NSMutableArray array];
        titles = [NSMutableArray array];
        blocktypes = [NSMutableArray array];
        classes = [NSMutableArray array];
        methods = [NSMutableArray array];
        args = [NSMutableArray array];

        //size=@"0";

        if ([config objectForKey: @"leftRootSegment"])
        {
            odict = [config objectForKey: @"leftRootSegment"];
            details = [odict objectForKey: @"Buttons"];
            size = [odict objectForKey: @"Width"];

            if (!size || !details)
            {
                [self dieFromMisconfiguration: @"width and buttons missing in <leftRootSM/>"];

                return NO;
            }

            for (NSDictionary *dict in details)
            {
                if ([dict objectForKey: @"Action"])
                    [blocktypes addObject: [dict objectForKey: @"Action"]];
                else
                    [blocktypes addObject: @"missing-action"];

                if ([dict objectForKey: @"Label"])
                    [segments addObject: [dict objectForKey: @"Label"]];
                else
                    [segments addObject: @"missing-label"];

                if ([dict objectForKey: @"Title"])
                    [titles addObject: [dict objectForKey: @"Title"]];
                else
                    [titles addObject: @"missing-title"];

                if ([dict objectForKey: @"URL"])
                    [urls addObject: [dict objectForKey: @"URL"]];
                else
                    [urls addObject: @"no-url"];

                if ([dict objectForKey: @"class"])
                    [classes addObject: [dict objectForKey: @"class"]];
                else
                    [classes addObject: @""];

                if ([dict objectForKey: @"method"])
                    [methods addObject: [dict objectForKey: @"method"]];
                else
                    [methods addObject: @""];

                if ([dict objectForKey: @"args"])
                    [args addObject: [dict objectForKey: @"args"]];
                else
                    [args addObject: @""]; // bill - this was typo said "urls" instead of args
            }

            dm.leftRootSM = [[[SegmentMap alloc] initWithSegments: segments
                                                           ofSize: [size intValue]
                                                    andWithTarget: self
                                                    andWithAction: @selector (rootSegmentHandler:)
                                                andWithBlockTypes: blocktypes
                                                    andWithTitles: titles
                                                      andWithUrls: urls
                                                   andWithClasses: classes
                                                   andWithMethods: methods
                                                      andWithArgs: args] retain];   // autorelease ??? -- JGP
        }
        else
            dm.leftRootSM = nil;

        segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
        urls = [NSMutableArray array];          // ditto ... -- JGP
        titles = [NSMutableArray array];
        blocktypes = [NSMutableArray array];
        classes = [NSMutableArray array];
        methods = [NSMutableArray array];
        args = [NSMutableArray array];

        if ([config objectForKey:@"rightRootSegment"])
        {
            odict = [config objectForKey: @"rightRootSegment"];
            details = [odict objectForKey: @"Buttons"];
            size = [odict objectForKey: @"Width"];

            if (!size || !details)
            {
                [self dieFromMisconfiguration: @"width and buttons missing in <righttRootSM/>"];

                return NO;
            }

            for (NSDictionary *dict in details)
            {
                if ([dict objectForKey: @"Action"])
                    [blocktypes addObject: [dict objectForKey: @"Action"]];
                else
                    [blocktypes addObject: @"missing-action"];

                if ([dict objectForKey: @"Label"])
                    [segments addObject: [dict objectForKey: @"Label"]];
                else
                    [segments addObject: @"missing-label"];

                if ([dict objectForKey: @"Title"])
                    [titles addObject: [dict objectForKey: @"Title"]];
                else
                    [titles addObject: @"missing-title"];

                if ([dict objectForKey: @"URL"])
                    [urls addObject: [dict objectForKey: @"URL"]];
                else
                    [urls addObject: @"no-url"];

                if ([dict objectForKey: @"class"])
                    [classes addObject: [dict objectForKey: @"class"]];
                else
                    [classes addObject: @""];

                if ([dict objectForKey: @"method"])
                    [methods addObject: [dict objectForKey: @"method"]];
                else
                    [methods addObject: @""];

                if ([dict objectForKey: @"args"])
                    [args addObject: [dict objectForKey: @"args"]];
                else
                    [args addObject: @""]; // bill - this was typo said "urls" instead of args
            }

            dm.rightRootSM = [[[SegmentMap alloc] initWithSegments: segments
                                                            ofSize: [size intValue]
                                                     andWithTarget: self
                                                     andWithAction: @selector (rootSegmentHandler:)
                                                 andWithBlockTypes: blocktypes
                                                     andWithTitles: titles
                                                       andWithUrls: urls
                                                    andWithClasses: classes
                                                    andWithMethods: methods
                                                       andWithArgs: args] retain];  // autorelease ??? -- JGP
        }
        else
            dm.rightRootSM = nil;

        segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
        urls = [NSMutableArray array];          // ditto ... -- JGP
        titles = [NSMutableArray array];
        blocktypes = [NSMutableArray array];
        classes = [NSMutableArray array];
        methods = [NSMutableArray array];
        args = [NSMutableArray array];

        if ([config objectForKey: @"bottomRootSegment"])
        {
            odict = [config objectForKey: @"bottomRootSegment"];
            details = [odict objectForKey: @"Buttons"];
            size = [odict objectForKey: @"Width"];

            if (!size || !details)
            {
                [self dieFromMisconfiguration: @"width and buttons missing in <bottomRootSM/>"];

                return NO;
            }

            for (NSDictionary *dict in details)
            {
                if ([dict objectForKey: @"Action"])
                    [blocktypes addObject: [dict objectForKey: @"Action"]];
                else
                    [blocktypes addObject: @"missing-action"];

                if ([dict objectForKey: @"Label"])
                    [segments addObject: [dict objectForKey: @"Label"]];
                else
                    [segments addObject: @"missing-label"];

                if ([dict objectForKey: @"Title"])
                    [titles addObject: [dict objectForKey: @"Title"]];
                else
                    [titles addObject: @"missing-title"];

                if ([dict objectForKey: @"URL"])
                    [urls addObject: [dict objectForKey: @"URL"]];
                else
                    [urls addObject: @"no-url"];

                if ([dict objectForKey: @"class"])
                    [classes addObject: [dict objectForKey: @"class"]];
                else
                    [classes addObject: @""];

                if ([dict objectForKey: @"method"])
                    [methods addObject: [dict objectForKey: @"method"]];
                else
                    [methods addObject: @""];

                if ([dict objectForKey: @"args"])
                    [args addObject: [dict objectForKey: @"args"]];
                else
                    [args addObject: @""]; // bill - this was typo said "urls" instead of args
            }

            dm.bottomRootSM = [[[SegmentMap alloc] initWithSegments: segments
                                                             ofSize: [size intValue]
                                                      andWithTarget: self
                                                      andWithAction: @selector (rootSegmentHandler:)
                                                  andWithBlockTypes: blocktypes
                                                      andWithTitles: titles
                                                        andWithUrls: urls
                                                     andWithClasses: classes
                                                     andWithMethods: methods
                                                        andWithArgs: args] retain]; // autorelease ??? -- JGP
        }
        else
            dm.bottomRootSM = nil;

        segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
        urls = [NSMutableArray array];          // ditto ... -- JGP
        titles = [NSMutableArray array];
        blocktypes = [NSMutableArray array];
        classes = [NSMutableArray array];
        methods = [NSMutableArray array];
        args = [NSMutableArray array];

        if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
            [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
        {
            [segments addObject: @"<<<<"];

            size = @"50";
        }
        else
            size = @"0";

        if ([config objectForKey: @"leftDetailSegment"])
        {
            odict = [config objectForKey: @"leftDetailSegment"];
            details = [odict objectForKey: @"Buttons"];
            size = [odict objectForKey: @"Width"];

            if (!size || !details)
            {
                [self dieFromMisconfiguration: @"width and buttons missing in <leftDetailSM/>"];

                return NO;
            }

            for (NSDictionary *dict in details)
            {
                if ([dict objectForKey: @"Action"])
                    [blocktypes addObject: [dict objectForKey: @"Action"]];
                else
                    [blocktypes addObject: @"missing-action"];

                if ([dict objectForKey: @"Label"])
                    [segments addObject: [dict objectForKey: @"Label"]];
                else
                    [segments addObject: @"missing-label"];

                if ([dict objectForKey: @"Title"])
                    [titles addObject: [dict objectForKey: @"Title"]];
                else
                    [titles addObject: @"missing-title"];

                if ([dict objectForKey: @"URL"])
                    [urls addObject: [dict objectForKey: @"URL"]];
                else
                    [urls addObject: @"no-url"];

                if ([dict objectForKey: @"class"])
                    [classes addObject: [dict objectForKey: @"class"]];
                else
                    [classes addObject: @""];

                if ([dict objectForKey: @"method"])
                    [methods addObject: [dict objectForKey: @"method"]];
                else
                    [methods addObject: @""];

                if ([dict objectForKey: @"args"])
                    [args addObject: [dict objectForKey: @"args"]];
                else
                    [args addObject: @""]; // bill - this was typo said "urls" instead of args
            }
        }

        dm.leftDetailSM = [[[SegmentMap alloc] initWithSegments: segments
                                                         ofSize: [size intValue]
                                                  andWithTarget: self//dm.detailController  // these handled in the detailcontroller
                                                  andWithAction: @selector(leftDetailSegmentHandler:)
                                              andWithBlockTypes: blocktypes
                                                  andWithTitles: titles
                                                    andWithUrls: urls
                                                 andWithClasses: classes
                                                 andWithMethods: methods
                                                    andWithArgs: args] retain]; // autorelease ??? -- JGP

        segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
        urls = [NSMutableArray array];          // ditto ... -- JGP
        titles = [NSMutableArray array];
        blocktypes = [NSMutableArray array];
        classes = [NSMutableArray array];
        methods = [NSMutableArray array];
        args = [NSMutableArray array];

        if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
            [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
        {
            [segments addObject: @">>>>"];

            size = @"50";
        }
        else
            size = @"0";

        if ([config objectForKey: @"leftDetailSegment"])
        {
            odict = [config objectForKey: @"leftDetailSegment"];
            details = [odict objectForKey: @"Buttons"];
            size = [odict objectForKey: @"Width"];

            for (NSDictionary *dict in details)
            {
                if ([dict objectForKey: @"Action"])
                    [blocktypes addObject: [dict objectForKey: @"Action"]];
                else
                    [blocktypes addObject: @"missing-action"];

                if ([dict objectForKey: @"Label"])
                    [segments addObject: [dict objectForKey: @"Label"]];
                else
                    [segments addObject: @"missing-label"];

                if ([dict objectForKey: @"Title"])
                    [titles addObject: [dict objectForKey: @"Title"]];
                else
                    [titles addObject: @"missing-title"];

                if ([dict objectForKey: @"URL"])
                    [urls addObject: [dict objectForKey: @"URL"]];
                else
                    [urls addObject: @"no-url"];

                if ([dict objectForKey: @"class"])
                    [classes addObject: [dict objectForKey: @"class"]];
                else
                    [classes addObject: @""];

                if ([dict objectForKey: @"method"])
                    [methods addObject: [dict objectForKey: @"method"]];
                else
                    [methods addObject: @""];

                if ([dict objectForKey: @"args"])
                    [args addObject: [dict objectForKey: @"args"]];
                else
                    [args addObject: @""]; // bill - this was typo said "urls" instead of args
            }
        }

        dm.leftDetailReversedSM = [[[SegmentMap alloc] initWithSegments: segments
                                                                 ofSize: [size intValue]
                                                          andWithTarget: self//dm.detailController  // these handled in the detailcontroller
                                                          andWithAction: @selector (leftDetailSegmentHandler:)
                                                      andWithBlockTypes: blocktypes
                                                          andWithTitles: titles
                                                            andWithUrls: urls
                                                         andWithClasses: classes
                                                         andWithMethods: methods
                                                            andWithArgs: args] retain]; // autorelease ??? -- JGP

        segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
        urls = [NSMutableArray array];          // ditto ... -- JGP
        titles = [NSMutableArray array];
        blocktypes = [NSMutableArray array];
        classes = [NSMutableArray array];
        methods = [NSMutableArray array];
        args = [NSMutableArray array];

        if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
            [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
        {
            [segments addObject: @"<<<<"];

            size = @"50";
        }
        else
            size = @"0";

        if ([config objectForKey: @"rightDetailSegment"])
        {
            odict = [config objectForKey: @"rightDetailSegment"];
            details = [odict objectForKey: @"Buttons"];
            size = [odict objectForKey: @"Width"];

            if (!size || !details)
            {
                [self dieFromMisconfiguration: @"width and buttons missing in <rightDetailSM/>"];

                return NO;
            }

            for (NSDictionary *dict in details)
            {
                if ([dict objectForKey: @"Action"])
                    [blocktypes addObject: [dict objectForKey: @"Action"]];
                else
                    [blocktypes addObject: @"missing-action"];

                if ([dict objectForKey: @"Label"])
                    [segments addObject: [dict objectForKey: @"Label"]];
                else
                    [segments addObject: @"missing-label"];

                if ([dict objectForKey: @"Title"])
                    [titles addObject: [dict objectForKey: @"Title"]];
                else
                    [titles addObject: @"missing-title"];

                if ([dict objectForKey: @"URL"])
                    [urls addObject: [dict objectForKey: @"URL"]];
                else
                    [urls addObject: @"no-url"];

                if ([dict objectForKey: @"class"])
                    [classes addObject: [dict objectForKey: @"class"]];
                else
                    [classes addObject: @""];

                if ([dict objectForKey: @"method"])
                    [methods addObject: [dict objectForKey: @"method"]];
                else
                    [methods addObject: @""];

                if ([dict objectForKey: @"args"])
                    [args addObject: [dict objectForKey: @"args"]];
                else
                    [args addObject: @""]; // bill - this was typo said "urls" instead of args
            }
        }

        if ([dm.masterPlist objectForKey: @"gotoSafariButton"] &&
            [[dm.masterPlist objectForKey: @"gotoSafariButton"] isEqual: @"yes"])
            [segments addObject: @"Safari"];

        dm.rightDetailSM = [[[SegmentMap alloc] initWithSegments: segments
                                                          ofSize: [size intValue]
                                                   andWithTarget: self// dm.detailController  // these handled in the detailcontroller
                                                   andWithAction: @selector (rightDetailSegmentHandler:)
                                               andWithBlockTypes: blocktypes
                                                   andWithTitles: titles
                                                     andWithUrls: urls
                                                  andWithClasses: classes
                                                  andWithMethods: methods
                                                     andWithArgs: args] retain];    // autorelease ??? -- JGP

        segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
        urls = [NSMutableArray array];          // ditto ... -- JGP
        titles = [NSMutableArray array];
        blocktypes = [NSMutableArray array];
        classes = [NSMutableArray array];
        methods = [NSMutableArray array];
        args = [NSMutableArray array];

        if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
            [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
        {
            [segments addObject: @">>>>"];

            size = @"50";
        }
        else
            size = @"0";

        if ([config objectForKey: @"leftDetailSegment"])
        {
            odict = [config objectForKey: @"leftDetailSegment"];
            details = [odict objectForKey: @"Buttons"];
            size = [odict objectForKey: @"Width"];

            for (NSDictionary *dict in details)
            {
                if ([dict objectForKey: @"Action"])
                    [blocktypes addObject: [dict objectForKey: @"Action"]];
                else
                    [blocktypes addObject: @"missing-action"];

                if ([dict objectForKey: @"Label"])
                    [segments addObject: [dict objectForKey: @"Label"]];
                else
                    [segments addObject: @"missing-label"];

                if ([dict objectForKey: @"Title"])
                    [titles addObject: [dict objectForKey: @"Title"]];
                else
                    [titles addObject: @"missing-title"];

                if ([dict objectForKey: @"URL"])
                    [urls addObject: [dict objectForKey: @"URL"]];
                else
                    [urls addObject: @"no-url"];

                if ([dict objectForKey: @"class"])
                    [classes addObject: [dict objectForKey: @"class"]];
                else
                    [classes addObject: @""];

                if ([dict objectForKey: @"method"])
                    [methods addObject: [dict objectForKey: @"method"]];
                else
                    [methods addObject: @""];

                if ([dict objectForKey: @"args"])
                    [args addObject: [dict objectForKey: @"args"]];
                else
                    [args addObject: @""]; // bill - this was typo said "urls" instead of args
            }
        }

        dm.leftDetailReversedSM = [[[SegmentMap alloc] initWithSegments: segments
                                                                 ofSize: [size intValue]
                                                          andWithTarget: self //dm.detailController  // these handled in the detailcontroller
                                                          andWithAction: @selector (leftDetailSegmentHandler:)
                                                      andWithBlockTypes: blocktypes
                                                          andWithTitles: titles
                                                            andWithUrls: urls
                                                         andWithClasses: classes
                                                         andWithMethods: methods
                                                            andWithArgs: args] retain]; // autorelease ??? -- JGP

        segments = [NSMutableArray array];      // [segments removeAllObjects] ??? -- JGP
        urls = [NSMutableArray array];          // ditto ... -- JGP
        titles = [NSMutableArray array];
        blocktypes = [NSMutableArray array];
        classes = [NSMutableArray array];
        methods = [NSMutableArray array];
        args = [NSMutableArray array];

        //        if ([dm.masterPlist objectForKey: @"landscapeWidenButton"] &&
        //            [[dm.masterPlist objectForKey: @"landscapeWidenButton"] isEqual: @"yes"])
        //        {
        //            [segments addObject: @"<<<<"];
        //
        //            size = @"50";
        //        }
        //        else
        //            size = @"0";

        if ([config objectForKey: @"rightDetailSegment"])
        {
            odict = [config objectForKey: @"rightDetailSegment"];
            details = [odict objectForKey: @"Buttons"];
            size = [odict objectForKey: @"Width"];

            if (!size || !details)
            {
                [self dieFromMisconfiguration: @"width and buttons missing in <rightDetailSM/>"];

                return NO;
            }

            for (NSDictionary *dict in details)
            {
                if ([dict objectForKey: @"Action"])
                    [blocktypes addObject: [dict objectForKey: @"Action"]];
                else
                    [blocktypes addObject: @"missing-action"];

                if ([dict objectForKey: @"Label"])
                    [segments addObject: [dict objectForKey: @"Label"]];
                else
                    [segments addObject: @"missing-label"];

                if ([dict objectForKey: @"Title"])
                    [titles addObject: [dict objectForKey: @"Title"]];
                else
                    [titles addObject: @"missing-title"];

                if ([dict objectForKey: @"URL"])
                    [urls addObject: [dict objectForKey: @"URL"]];
                else
                    [urls addObject: @"no-url"];

                if ([dict objectForKey: @"class"])
                    [classes addObject: [dict objectForKey: @"class"]];
                else
                    [classes addObject: @""];

                if ([dict objectForKey: @"method"])
                    [methods addObject: [dict objectForKey: @"method"]];
                else
                    [methods addObject: @""];

                if ([dict objectForKey: @"args"])
                    [args addObject: [dict objectForKey: @"args"]];
                else
                    [args addObject: @""]; // bill - this was typo said "urls" instead of args
            }
        }

        if ([dm.masterPlist objectForKey: @"gotoSafariButton"] &&
            [[dm.masterPlist objectForKey: @"gotoSafariButton"] isEqual: @"yes"])
            [segments addObject:@"Safari"];

        dm.rightDetailSM = [[[SegmentMap alloc] initWithSegments: segments
                                                          ofSize: [size intValue]
                                                   andWithTarget: self  // dm.detailController  // these handled in the detailcontroller
                                                   andWithAction: @selector (rightDetailSegmentHandler:)
                                               andWithBlockTypes: blocktypes
                                                   andWithTitles: titles
                                                     andWithUrls: urls
                                                  andWithClasses: classes
                                                  andWithMethods: methods
                                                     andWithArgs: args] retain];

        NSString *lastscene = [[NSUserDefaults standardUserDefaults] objectForKey: @"lastscene"];
        int       scene;

        if (lastscene && [dm samePlist])
            scene = [lastscene intValue];
        else
            scene = 0;

        [dm setScene: scene];

        //[self writeLinksToLog];

       [self setupControllers];
		
		// bill - move this down
        [splitViewController.view bringSubviewToFront:
		    [self originalDetailViewController].view]; // give this priority so we can go full width easily

        // Add the split view controller's view to the window and display.
        [window addSubview: splitViewController.view];
        [window makeKeyAndVisible];

        return YES;
    }
    else
    {
        // didnt work out, but error handling has already started
        return NO;
    }
}

- (void) goodnight: (id) foo
{
    exit (0);
}

- (void) applicationWillTerminate: (UIApplication *) application
{
    // Save data if appropriate
}

#pragma mark -
#pragma mark Memory management

- (void) dealloc
{
    [splitViewController release];
    [window release];

    [super dealloc];
}

#pragma mark -
#pragma mark JGP Hacks

- (UIViewController *) currentDetailViewController
{
    return [self.splitViewController.viewControllers objectAtIndex: 1];
}

- (DetailViewController *) originalDetailViewController
{
    DataManager *dm = [DataManager sharedInstance];

    if ([self currentDetailViewController] != dm.detailController)
        [self restoreDetailViewController];

    return dm.detailController;
}

- (void) replaceDetailViewController: (UIViewController *) vc
         splitViewControllerDelegate: (id <UISplitViewControllerDelegate>) svcd
{
    DataManager          *dm = [DataManager sharedInstance];
    DetailViewController *dvc = dm.detailController;
    UIPopoverController  *pc = dvc.popoverController;

    if (pc)
        [pc dismissPopoverAnimated: YES];

    UINavigationController *nc = [[[UINavigationController alloc] initWithRootViewController: vc]
                                  autorelease];

    nc.navigationBar.barStyle = UIBarStyleBlack;

    //
    // Big ugly kluge!!!
    //
    if (pc)
        nc.navigationBar.topItem.leftBarButtonItem = dvc.navigationBar.topItem.leftBarButtonItem;

    UISplitViewController *svc = self.splitViewController;
    NSArray               *oldVCs = svc.viewControllers;
    NSArray               *newVCs = [NSArray arrayWithObjects:
                                     [oldVCs objectAtIndex: 0],
                                     nc,
                                     nil];

    svc.delegate = svcd;
    svc.viewControllers = newVCs;
}

- (void) restoreDetailViewController
{
    DataManager           *dm = [DataManager sharedInstance];
    UISplitViewController *svc = self.splitViewController;
    NSArray               *oldVCs = svc.viewControllers;
    NSArray               *newVCs = [NSArray arrayWithObjects:
                                     [oldVCs objectAtIndex: 0],
                                     dm.detailController,
                                     nil];

    svc.viewControllers = newVCs;
}

#pragma mark -

@end


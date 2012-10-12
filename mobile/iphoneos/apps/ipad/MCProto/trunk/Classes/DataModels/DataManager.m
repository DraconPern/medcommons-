//
//  DataManager.m
//  MedPad
//
//  Created by bill donner on 4/11/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "DataManager.h"
#import "BreadCrumbs.h"


@implementation DataManager

static DataManager *_sharedInstance;

@synthesize masterPlist;
@synthesize rootController;
@synthesize detailController;
@synthesize appDelegate;

@synthesize leftDetailSM;
@synthesize leftDetailReversedSM;
@synthesize rightDetailSM;
@synthesize rightRootSM;
@synthesize leftRootSM;
@synthesize bottomRootSM;

@synthesize ffGPSDevice;
@synthesize ffBreadCrumbs;
@synthesize ffPatientStore;
@synthesize ffPatientWrapper;

@synthesize ffNextFileIndex;
@synthesize  ffCustomViews;
@synthesize ffAppLogoImage;

@synthesize ffMCusername;
@synthesize ffMCpassword;
@synthesize ffMCauth;
@synthesize ffMCpracticename;
@synthesize ffMCpatientlist;
@synthesize ffMCappliance;

@synthesize ffMCprovidermcid;
@synthesize ffMCmcid;

@synthesize ffSharedOperationQueue;

- (id) init
{
    if (self = [super init])
    {

        // bill - 9 mar 10 persist this

    }
    return self;
}

+ (DataManager *) sharedInstance
{
    if (!_sharedInstance)
        _sharedInstance = [[DataManager alloc] init];

    return _sharedInstance;
}

- (BOOL) samePlist
{
    return sameplist;
}

- (void) setSamePlistFlag: (BOOL) b
{
    sameplist = b;
}

- (void) setScene: (NSInteger) _sm
{
    currentScene = _sm ;

    NSString *ascene = [NSString stringWithFormat: @"%d", currentScene];

    [[NSUserDefaults standardUserDefaults] setValue: ascene
                                             forKey: @"lastscene"]; // bill 9 mar 10 - persist this
}

- (NSInteger) currentScene
{
    return currentScene;
}

- (id) dieFromMisconfiguration: (NSString *) msg
{
    return [appDelegate dieFromMisconfiguration: msg];

}

- (NSArray *) allScenes
{
    NSDictionary *environment = [[DataManager sharedInstance] masterPlist] ;
    NSArray      *scenes = [environment objectForKey: @"scenes"];

    if (!scenes)
        return [[DataManager sharedInstance] dieFromMisconfiguration: @"No scenes in plist file"];

    return scenes;
}

- (NSDictionary *) currentSceneContext
{
    DataManager *dm = [DataManager sharedInstance];
    NSInteger    curScene = [dm currentScene];      // why not just use currentScene instance variable ??? -- JGP
    NSArray     *scenes = [self allScenes];

    if ((curScene < 0) || (curScene >= [scenes count]))
        return [dm dieFromMisconfiguration: [NSString stringWithFormat: @"Current scene %d is out of range",
                                             curScene]];

    return [scenes objectAtIndex: curScene];
}

- (NSDictionary *) currentBlock: (int) blocknum
                       forScene: (NSDictionary *) scene
{
    NSArray *blocks = [scene objectForKey: @"blocks"];

    if (!blocks)
        return [[DataManager sharedInstance]
                dieFromMisconfiguration: [NSString stringWithFormat: @"No blocks in scene %@",
                                          [scene objectForKey: @"name"]]];

    if ((blocknum < 0) || (blocknum >= [blocks count]))
        blocknum = 0; // hack but wrong
                      //return [[DataManager sharedInstance] dieFromMisconfiguration:[NSString stringWithFormat:

    return [blocks objectAtIndex: blocknum];
}

- (void) invokeActionBlockMethod: (NSDictionary *) block
{
    SEL           selector = NSSelectorFromString ([block objectForKey: @"method"]);
    Class         klass = NSClassFromString ([block objectForKey: @"class"]);
    NSDictionary *arg = [block objectForKey: @"args"];

    [self performSelector: selector
                  inClass: klass
               withObject: arg];
}

- (void) invokeSavedActionBlockMethod: (NSString *) selectorName
                              inClass: (NSString *) className
                           withObject: (id) arg
{
    SEL   selector = NSSelectorFromString (selectorName);
    Class klass = NSClassFromString (className);

    [self performSelector: selector
                  inClass: klass
               withObject: arg];
}

- (void) performSelector: (SEL) selector
                 inClass: (Class) klass
              withObject: (NSDictionary *) arg
{
    if (selector &&
        klass &&
        [klass instancesRespondToSelector: selector])
    {
        NSObject *action = [[klass alloc] init];

        if (action)
        {
#if 0
            [action performSelectorOnMainThread: selector
                                     withObject: arg
                                  waitUntilDone: [NSThread isMainThread]];  // ???
#else
            [action performSelector: selector
                         withObject: arg
                         afterDelay: 0.0f];
#endif
            [action release];
        }
    }
}
-(BOOL) tryRecovery:(UIViewController *)who
{
    // pop off a breadcrumb, if we have one then go to that class
    NSString *s = [ffBreadCrumbs popRecoveryCrumb];
    if (s)
    {
        UIViewController *viewc = [[NSClassFromString(s) alloc] init];
        [who.navigationController pushViewController:viewc  animated:NO];
        [viewc release];
        return YES;
    }
    return NO;
}
@end

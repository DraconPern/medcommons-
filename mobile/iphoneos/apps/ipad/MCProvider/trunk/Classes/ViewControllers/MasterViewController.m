//
//  MasterViewController.m
//  MCProvider
//
//  Created by Bill Donner on 2/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "DataManager.h"
#import "DetailViewController.h"
#import "MasterViewController.h"
#import "MasterViewDataSource.h"
#import "SessionManager.h"
//#import "SegmentMap.h"

@interface MasterViewController ()

- (void) tableViewReset: (UITableView *) tabView;

@end

@implementation MasterViewController

@synthesize safariButton = safariButton_;

#pragma mark -
#pragma mark View lifecycle

-(id) initWithGroup
{
    self = [super initWithStyle:UITableViewStylePlain];
    withGroup = YES;
    return self;
}


-(id) init
{
    self = [super initWithStyle:UITableViewStylePlain];
    withGroup = NO;
    return self;
}
- (void) remotePoke: (NSString *) title
{
    self.title = [title copy];

    [self.tableView reloadData];
}

- (void) viewDidLoad
{
    [super viewDidLoad];

    DataManager *dm = self.appDelegate.dataManager;

    self.tableView.dataSource = [[MasterViewDataSource alloc] initWithGroup:withGroup];
    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;

    self.clearsSelectionOnViewWillAppear = YES;


    if ([dm.masterPlist objectForKey: @"LeftBlockStyle"] &&
        [[dm.masterPlist objectForKey: @"LeftBlockStyle"] isEqual: @"narrow"])
        self.tableView.rowHeight = 44.0f;
    else
        self.tableView.rowHeight = 100.0f;

    NSDictionary *scene = [dm currentSceneContext];

    if (!scene)
        return; // really should die here horribly

    self.title = [scene objectForKey: @"smalltitle"];

    NSString *cellstyle = [scene objectForKey: @"cellstyle"];

    if (!cellstyle)
        cellstyle = @"plain";

    if ([@"plain" isEqual: cellstyle])
        self.tableView.backgroundColor = [UIColor whiteColor];
    else
        self.tableView.backgroundColor = [UIColor blackColor];

//    if (dm.smBottomMaster) // if a bottom was specified then add it
//    {
//        self.navigationController.toolbarHidden = NO;
//        self.navigationController.toolbar.barStyle = UIBarStyleBlack;
//
//        //  UIToolbar *tb = [[UIToolbar alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 320.0f, 44.0f)];
//        // tb.barStyle = UIBarStyleBlack;
//        //
//        NSMutableArray *tbitems = [NSMutableArray array];
//        //
//
//        [tbitems addObject: dm.smBottomMaster.segmentBarItem];
//
//        self.toolbarItems = tbitems;    // animate this?
//
//    }

    [self tableViewReset: self.tableView];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];

    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}
//jane.hernandez747@gmail.com
#pragma mark -
#pragma mark Table view delegate

- (void) __tableView: (UITableView *) tabView selectRow: (NSUInteger) row
{
    AppDelegate  *appDel = self.appDelegate;
    DataManager  *dm = appDel.dataManager;
    NSDictionary *scene = [dm currentSceneContext];

    if (!scene)
        return;

    NSDictionary *block = [dm currentBlock: row
                                  forScene: scene];

    if (!block)
        return;

    if (![block objectForKey: @"Action"])
    {
        [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                      @"No blocktype found in scene %@ <block/> %d",
                                      [scene objectForKey: @"name"],
                                      row]];

        return;
    }

    NSString *blockType = [block objectForKey: @"Action"];

    /*
     When a row is selected, set the detail view controller's detail item to the item associated with the selected row.
     */
    if ([@"InvokeMethod" isEqual: blockType])
    {
        if (![block objectForKey: @"class"] ||
            ![block objectForKey: @"method"])
        {
            [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                          @"Must specify class and method in scene %@ <block/> %d",
                                          [scene objectForKey:@"name"],
                                          row]];

            return;
        }

        [dm invokeActionBlockMethod: block];
    }
    else if ([@"ShowDetail" isEqual: blockType])
    {
        //NSLog (@"Setting detailItem to %d",indexPath.row);

        UIViewController *vc = appDel.detailNavigationController.topViewController;

        if ([vc isKindOfClass: [DetailViewController class]])
        {
            DetailViewController *dvc = (DetailViewController *) vc;

            dvc.detailItem = [NSString stringWithFormat:
                              @"%d",
                              row]; // just set as string
        }
    }

    else if ([@"ReplaceScene" isEqual: blockType])
    {
        // change the scene and refresh the table
        if (![block objectForKey: @"scene"])
        {
            [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                          @"No scene found in ReplaceScene <block/> %d",
                                          row]];

            return;
        }

        NSArray  *scenes = [dm allScenes];
        NSString *tscene = [[block objectForKey: @"scene"] stringByTrimmingWhitespace];

        //NSLog (@"switching to scene %@",tscene);

        NSUInteger itscene = [tscene intValue];

        if (itscene >= [scenes count])
        {
            [dm dieFromMisconfiguration: @"Destination ReplaceScene does not exist"];

            return;
        }

        dm.currentScene = itscene;

        self.title = [[scenes objectAtIndex: itscene] objectForKey: @"smalltitle"];

        [self.tableView reloadData];
    }
    else if ([@"GotoScene" isEqual: blockType])
    {
        // change the scene and refresh the table -- also set the detail item
        if (![block objectForKey:@"scene"])
        {
            [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                          @"No scene found in GotoScene <block/> %d",
                                          row]];

            return;
        }

        NSArray  *scenes = [dm allScenes];
        NSString *tscene = [[block objectForKey:@"scene"] stringByTrimmingWhitespace];

        //NSLog (@"switching to scene %@",tscene);

        NSUInteger itscene = [tscene intValue];

        if (itscene >= [scenes count])
        {
            [dm dieFromMisconfiguration: @"Destination GotoScene does not exist"];

            return;
        }

        dm.currentScene = itscene;

        self.title = [[scenes objectAtIndex: itscene] objectForKey: @"smalltitle"];

        [self.tableView reloadData];

        UIViewController *vc = appDel.detailNavigationController.topViewController;

        if ([vc isKindOfClass: [DetailViewController class]])
        {
            DetailViewController *dvc = (DetailViewController *) vc;

            dvc.detailItem = nil;
        }
    }
    else    // barf on unknown blocktypes
    {
        [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                      @"Unknown blocktype %@",
                                      blockType]];

        return;
    }
}

- (void) tableView: (UITableView *) tabView didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    [self.appDelegate dismissMasterPopoverAnimated: YES];

    // run a little wifi test to please our users

    [self.appDelegate.sessionManager checkNetworkStatus]; // delay a bit to let things settle

    [self __tableView: tabView selectRow: idxPath.row];
}

- (void) tableViewReset: (UITableView *) tabView
{  // called to get first block to repaint
    [self __tableView: tabView selectRow: 0];
}

#pragma mark -
#pragma mark Memory management

- (void) didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];

    // Relinquish ownership any cached data, images, etc. that aren't in use.
    [AsyncImageView clearCache]; // take it back to zero entries
}

- (void) viewDidUnload
{
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}


- (void) dealloc
{
    [safariButton_ release];

    [super dealloc];
}

@end

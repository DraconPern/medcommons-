//
//  RootViewController.m
//  MedPad
//
//  Created by bill donner on 2/24/10.
//  Copyright Apple Inc 2010. All rights reserved.
//

#import "AppDelegate.h"
#import "RootViewController.h"
#import "DetailViewController.h"
#import "WebPaneController.h"
#import "DataManager.h"
#import "SegmentMap.h"


#define BARBUTTON(title, selector)                                          \
[[[UIBarButtonItem alloc] initWithTitle: title                      \
style: UIBarButtonItemStylePlain    \
target: self                        \
action: selector]                   \
autorelease]

#define IMGBARBUTTON(image, selector)                                       \
[[[UIBarButtonItem alloc] initWithImage: image                      \
style: UIBarButtonItemStylePlain    \
target: self                        \
action:selector]                    \
autorelease]

#define SYSBARBUTTON(item, selector)                                        \
[[[UIBarButtonItem alloc] initWithBarButtonSystemItem: item         \
target: self            \
action: selector]   \
autorelease]

#define CUSTOMBARBUTTON(view)                               \
[[[UIBarButtonItem alloc] initWithCustomView: view] \
autorelease]

@implementation RootViewController

//JGP   @synthesize detailViewController;
@synthesize safariButton;

#pragma mark -
#pragma mark View lifecycle

- (void) remotePoke: (NSString *) tit
{
    //  NSLog(@"root remotePoke %@",tit);

    self.title = [tit retain];              // copy ??? -- JGP

    [self.tableView reloadData];
}


- (void) viewDidLoad
{
    //  NSLog(@"root viewDidLoad");

    [super viewDidLoad];

    DataManager *dm = [DataManager sharedInstance];

    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
    self.clearsSelectionOnViewWillAppear = YES;

    if ([dm.masterPlist objectForKey: @"LeftBlockStyle"] &&
        [[dm.masterPlist objectForKey: @"LeftBlockStyle"] isEqual: @"narrow"])
        self.tableView.rowHeight = 44.0f;
    else
        self.tableView.rowHeight = 100.0f;

    dm.rootController = self; // record this for manipulation from above

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

    self.navigationController.navigationBar.topItem.leftBarButtonItem = dm.leftRootSM.segmentBarItem ;
    self.navigationController.navigationBar.topItem.rightBarButtonItem = dm.rightRootSM.segmentBarItem;

    //self.navigationController.toolbarHidden = NO;//
    //self.navigationController.toolbar.barStyle = UIBarStyleBlack;
    //  UIToolbar *tb = [[UIToolbar alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 320.0f, 44.0f)];
    //  tb.barStyle = UIBarStyleBlack;
    //
    //  NSMutableArray *tbitems = [NSMutableArray array];
    //
    //  if (dm.bottomRootSM) // if a bottom was specified then add it
    //
    //      [tbitems addObject:dm.bottomRootSM.segmentBarItem];
    //  self.toolbarItems =  tbitems;
    //  //self.toolbarItems addSubview:tb];
    //  //
    //  [tb release];
}

- (void) viewWillAppear: (BOOL) animated
{
    //   DataManager *dm = [DataManager sharedInstance];

    //    NSLog (@"root viewWillAppear - current scene is %d context is %@",
    //           [dm currentScene],
    //           [dm currentSceneContext]);

    [super viewWillAppear: animated];

    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
}

// Ensure that the view controller supports rotation and that the split view can therefore show in both portrait and landscape.
- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) interfaceOrientation
{
    return YES;
}

#pragma mark -
#pragma mark Size for popover

// The size the view should be when presented in a popover.
//- (CGSize)contentSizeForViewInPopoverView {
//    return CGSizeMake(320.0, 500.0);
//}

#pragma mark -
#pragma mark Table view data source

- (NSInteger) numberOfSectionsInTableView: (UITableView *) aTableView
{
    return 1;
}

- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{

    DataManager  *dm = [DataManager sharedInstance];
    NSDictionary *scene = [dm currentSceneContext];

    if (!scene)
        return 0.0f;

    NSDictionary *block = [dm currentBlock: idxPath.row
                                  forScene: scene];

    if (!block)
        return 0.0f;

    float height;

    if ([block objectForKey: @"Height"])
        height = [[block objectForKey: @"Height"] floatValue];
    else if ([dm.masterPlist objectForKey: @"LeftBlockStyle"] &&
             [[dm.masterPlist objectForKey: @"LeftBlockStyle"] isEqual: @"narrow"])
        height = 44.0f;
    else
        height = 100.0f;

    return height;
}

- (NSInteger) tableView: (UITableView *) aTableView
  numberOfRowsInSection: (NSInteger) section
{
    // get it from the environment
    NSDictionary *scene = [[DataManager sharedInstance] currentSceneContext];

    if (!scene)
        return 0;

    return [[scene objectForKey:@"blocks"] count];
}

#define MAINLABEL_TAG   1
#define SECONDLABEL_TAG 2
#define PHOTO_TAG       3
#define WEBVIEW_TAG     4

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    // build multiple reuse queues with different pre-laid out contents
    static NSString *CellIdentifierA = @"CellIdentifierA";
    static NSString *CellIdentifierB = @"CellIdentifierB";
    static NSString *CellIdentifierC = @"CellIdentifierC";
    static NSString *CellIdentifier0 = @"CellIdentifier0";
    static NSString *CellIdentifier1 = @"CellIdentifier1";

    DataManager  *dm = [DataManager sharedInstance];
    NSDictionary *scene = [dm currentSceneContext];

    if (!scene)
        return nil;

    NSDictionary *block = [dm currentBlock: idxPath.row
                                  forScene: scene];

    if (!block)
        return nil;

    NSString *cellstyle = [scene objectForKey: @"cellstyle"];

    if (!cellstyle)
        cellstyle = @"plain";

    NSString *cellIdentifier;
    int       nesting =0;

    if ([block objectForKey: @"WebView"])   //&&[@"webpane" isEqual: [block objectForKey:@"Action"]]) // webview
    {
        // might use different queues based on height of webpane
        cellIdentifier = CellIdentifier1;
    }
    else if ([dm.masterPlist objectForKey: @"LeftBlockStyle"] &&
             [[dm.masterPlist objectForKey: @"LeftBlockStyle"] isEqual: @"narrow"])
    {
        if ([block objectForKey: @"nesting"])
            nesting = [[block objectForKey: @"nesting"] intValue];

        if (nesting > 2)
            nesting = 2;

        if (nesting == 0)
            cellIdentifier = CellIdentifierA;
        else if (nesting ==1)
            cellIdentifier = CellIdentifierB;
        else
            cellIdentifier = CellIdentifierC;
    }
    else
        cellIdentifier = CellIdentifier0;

    // Dequeue or create a cell of the appropriate type.

    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: cellIdentifier];
    UILabel         *mainLabel;
    UILabel         *secondLabel;
    UIWebView       *webView;
    UIImageView     *photo;

    if (!cell)
    {
        cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleDefault
                                       reuseIdentifier: cellIdentifier]
                autorelease];

        if ([block objectForKey: @"WebView"])   //&&[@"webpane" isEqual: [block objectForKey:@"Action"]]) // webview
        {
            mainLabel = [[[UILabel alloc] initWithFrame: CGRectMake (0.0f, 20.0f, 300.0f, 20.0f)]
                         autorelease];

            //mainLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            mainLabel.font = [UIFont systemFontOfSize: 18.0f];
            mainLabel.tag = MAINLABEL_TAG;
            mainLabel.textAlignment = UITextAlignmentLeft;

            [cell.contentView addSubview: mainLabel];

            secondLabel = [[[UILabel alloc] initWithFrame: CGRectMake(0.0f, 2.0f, 300.0f, 16.0f)]
                           autorelease];

            //secondLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            secondLabel.font = [UIFont systemFontOfSize: 12.0f];
            secondLabel.tag = SECONDLABEL_TAG;
            secondLabel.textAlignment = UITextAlignmentLeft;

            [cell.contentView addSubview:secondLabel];

            float height;

            if ([block objectForKey: @"Height"])
                height = [[block objectForKey: @"Height"] floatValue];
            else
                height = 100.0f;

            webView = [[[UIWebView alloc] initWithFrame: CGRectMake(0.0f, 44.0f, 320.0f, height - 44.0f)]
                       autorelease];

            //webView.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            webView.tag = WEBVIEW_TAG;

            [cell.contentView addSubview: webView];
        }
        else if ([dm.masterPlist objectForKey: @"LeftBlockStyle"] &&
                 [[dm.masterPlist objectForKey: @"LeftBlockStyle"] isEqual: @"narrow"])
        {
            float xpos = 10.0f + (15.0f * nesting);

            mainLabel = [[[UILabel alloc] initWithFrame:CGRectMake (xpos, 20.0f, 300.0f, 20.0f)]
                         autorelease];

            //mainLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            mainLabel.font = [UIFont systemFontOfSize: 18.0f];
            mainLabel.tag = MAINLABEL_TAG;
            mainLabel.textAlignment = UITextAlignmentLeft;

            [cell.contentView addSubview: mainLabel];

            //secondLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            secondLabel = [[[UILabel alloc] initWithFrame: CGRectMake (xpos, 2.0f, 300.0f, 16.0f)]
                           autorelease];

            secondLabel.font = [UIFont systemFontOfSize: 12.0f];
            secondLabel.tag = SECONDLABEL_TAG;
            secondLabel.textAlignment = UITextAlignmentLeft;

            [cell.contentView addSubview: secondLabel];
        }
        else
        {
            //  photo plus labels
            mainLabel = [[[UILabel alloc] initWithFrame:CGRectMake (100.0f, 40.0f, 200.0f, 60.0f)]
                         autorelease];

            //mainLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            mainLabel.font = [UIFont systemFontOfSize: 18.0f];
            mainLabel.tag = MAINLABEL_TAG;
            mainLabel.textAlignment = UITextAlignmentRight;

            [cell.contentView addSubview: mainLabel];

            secondLabel = [[[UILabel alloc] initWithFrame:CGRectMake (100.0f, 0.0f, 200.0f, 40.0f)]
                           autorelease];

            //secondLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            secondLabel.font = [UIFont systemFontOfSize: 12.0f];
            secondLabel.tag = SECONDLABEL_TAG;
            secondLabel.textAlignment = UITextAlignmentRight;

            [cell.contentView addSubview: secondLabel];

            photo = [[[UIImageView alloc] initWithFrame: CGRectMake (0.0f, 0.0f, 100.0f, 100.0f)]
                     autorelease];

            //photo.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            photo.tag = PHOTO_TAG;

            [cell.contentView addSubview: photo];
        }
    }
    else
    {
        // reuse an existing cell
        //if ([block objectForKey:@"Action"]&&[@"webpane" isEqual: [block objectForKey:@"Action"]]) // webview has only photo part
        //          photo = (UIImageView *)[cell.contentView viewWithTag:WEBVIEW_TAG];
        //      else {

        mainLabel = (UILabel *) [cell.contentView viewWithTag: MAINLABEL_TAG];
        secondLabel = (UILabel *) [cell.contentView viewWithTag: SECONDLABEL_TAG];

        if ([block objectForKey: @"WebView"])   //&&[@"webpane" isEqual: [block objectForKey:@"Action"]]) // webview has only photo part
            webView = (UIWebView *) [cell.contentView viewWithTag: WEBVIEW_TAG];
        else if (!([dm.masterPlist objectForKey: @"LeftBlockStyle"] &&
                   [[dm.masterPlist objectForKey: @"LeftBlockStyle"] isEqual:@"narrow"]))
            photo = (UIImageView *) [cell.contentView viewWithTag: PHOTO_TAG];
    }

    // Configure the cell.
    if ([block objectForKey: @"Action"] &&
        ([@"ReplaceScene" isEqual: [block objectForKey: @"Action"]] ||
         [@"GotoScene" isEqual: [block objectForKey: @"Action"]]))  // if none specified then assume
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    else
        cell.accessoryType = UITableViewCellAccessoryNone;


    //  if ([block objectForKey:@"Action"]&&[@"webpane" isEqual: [block objectForKey:@"Action"]]) // webview is simple?
    //  {
    //      NSString *url = [[block objectForKey:@"ImageURL"] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    //      if (url)
    //      {
    //          NSData *temp = [[NSData alloc] initWithContentsOfURL:[NSURL URLWithString: url   ]]; //get from remote URL
    //          photo.image  = [[UIImage imageWithData: temp] retain];
    //      }
    //      else photo.image = nil;
    //  }
    //  else
    //{

    // whether its a new or old cell, rewrite the labels
    if ([@"plain" isEqual: cellstyle])
    {
        mainLabel.textColor = [UIColor blackColor];
        mainLabel.backgroundColor = [UIColor whiteColor];

        secondLabel.textColor = [UIColor darkGrayColor];
        secondLabel.backgroundColor = [UIColor whiteColor];
    }
    else
    {
        mainLabel.textColor = [UIColor whiteColor];
        mainLabel.backgroundColor = [UIColor blackColor];

        secondLabel.textColor = [UIColor whiteColor];
        secondLabel.backgroundColor = [UIColor blackColor];
    }

    NSString *main = [[block objectForKey: @"Label"] stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];
    NSString *second = [[block objectForKey: @"Info"] stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];

    if (main)
        mainLabel.text = [NSString stringWithFormat:@"%@   ", main];
    else
        mainLabel.text = nil;

    if (second)
        secondLabel.text = [NSString stringWithFormat:@"%@   ", second];
    else
        secondLabel.text = nil;

    if ([block objectForKey: @"WebView"])   //&&[@"webpane" isEqual: [block objectForKey:@"Action"]]) // webview is simple?
    {
        NSString *url = [[block objectForKey: @"ImageURL"] stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];

        if (url)
        {
            //NSData *temp = [[NSData alloc] initWithContentsOfURL:[NSURL URLWithString: url   ]]; //get from remote URL
            //photo.image  = [[UIImage imageWithData: temp] retain];

            //webView.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
            webView.backgroundColor = [UIColor blueColor];
            //webView.delegate = self;
            webView.scalesPageToFit = YES;

            NSURLRequest *requestObj = [NSURLRequest requestWithURL: [NSURL URLWithString: url]];

            [webView loadRequest: requestObj];
            //
            //              [self.view addSubview:webView];
            //              [webView release];
        }
        //else photo.image = nil;
    }
    else if (!([dm.masterPlist objectForKey: @"LeftBlockStyle"] &&
               [[dm.masterPlist objectForKey: @"LeftBlockStyle"] isEqual: @"narrow"]))
    {
        NSString *url = [[block objectForKey: @"ImageURL"] stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];

        if (url)
        {
            //NSData *temp = [[NSData alloc] initWithContentsOfURL:[NSURL URLWithString: url   ]]; //get from remote URL
            photo.image  = [UIImage imageWithData: [[NSData alloc] initWithContentsOfURL: [NSURL URLWithString: url]]];
        }
        else
            photo.image = nil;
    }
    //}

    return cell;
}

#pragma mark -
#pragma mark Table view delegate

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    DataManager  *dm = [DataManager sharedInstance];
    NSDictionary *scene = [dm currentSceneContext];

    if (!scene)
        return;

    NSDictionary *block = [dm currentBlock: idxPath.row
                                  forScene: scene];

    if (!block)
        return;

    if (![block objectForKey: @"Action"])
    {
        [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                      @"No blocktype found in scene %@ <block/> %d",
                                      [scene objectForKey: @"name"],
                                      idxPath.row]];

        return;
    }

    DetailViewController *dvc = [dm.appDelegate originalDetailViewController];  // JGP hack !!!
    NSString             *blocktype = [block objectForKey: @"Action"];

    /*
     When a row is selected, set the detail view controller's detail item to the item associated with the selected row.
     */
    if (UI_USER_INTERFACE_IDIOM () == UIUserInterfaceIdiomPad)
    {
        if ([@"InvokeMethod" isEqual: blocktype])
        {
            if (![block objectForKey: @"class"] ||
                ![block objectForKey: @"method"])
            {
                [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                              @"Must specify class and method in scene %@ <block/> %d",
                                              [scene objectForKey:@"name"],
                                              idxPath.row]];

                return;
            }

            [dm invokeActionBlockMethod: block];
        }
        else if ([@"ShowDetail" isEqual: blocktype])
        {
            //NSLog (@"Setting detailItem to %d",indexPath.row);

            dvc.detailItem = [NSString stringWithFormat:
                              @"%d",
                              idxPath.row]; // just set as string
        }
        //else if ([@"webpane" isEqual: blocktype])
        //      {
        //          //webpane is same as ShowDetail block, it just has a different picture
        //          detailViewController.detailItem =
        //          [NSString stringWithFormat:@"%d", indexPath.row]; // just set as string
        //      }
        else if ([@"ReplaceScene" isEqual: blocktype])
        {
            // change the scene and refresh the table
            if (![block objectForKey: @"scene"])
            {
                [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                              @"No scene found in ReplaceScene <block/> %d",
                                              idxPath.row]];

                return;
            }

            NSArray  *scenes = [dm allScenes];
            NSString *tscene = [[block objectForKey: @"scene"]
                                stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];

            //NSLog (@"switching to scene %@",tscene);

            int itscene = [tscene intValue];

            if ((itscene < 0) || (itscene >= [scenes count]))
            {
                [dm dieFromMisconfiguration: @"Destination ReplaceScene does not exist"];

                return;
            }

            [dm setScene: itscene];

            self.title = [[scenes objectAtIndex: itscene] objectForKey: @"smalltitle"];

            [self.tableView reloadData];
        }
        else if ([@"GotoScene" isEqual: blocktype])
        {
            // change the scene and refresh the table -- also set the detail item
            if (![block objectForKey:@"scene"])
            {
                [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                              @"No scene found in GotoScene <block/> %d",
                                              idxPath.row]];

                return;
            }

            NSArray  *scenes = [dm allScenes];
            NSString *tscene = [[block objectForKey:@"scene"]
                                stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceAndNewlineCharacterSet]];

            //NSLog (@"switching to scene %@",tscene);

            int itscene = [tscene intValue];

            if ((itscene < 0) || (itscene >= [scenes count]))
            {
                [dm dieFromMisconfiguration: @"Destination GotoScene does not exist"];

                return;
            }

            [dm setScene: itscene];

            self.title = [[scenes objectAtIndex: itscene] objectForKey: @"smalltitle"];

            [self.tableView reloadData];

            dvc.detailItem = 0;
        }
        else    // barf on unknown blocktypes
        {
            [dm dieFromMisconfiguration: [NSString stringWithFormat:
                                          @"Unknown blocktype %@",
                                          blocktype]];

            return;
        }
    }
    else
    {
        // The device is an iPhone or iPod touch. - push a web pane view controller
        [dm dieFromMisconfiguration: @"MedPad is an iPad only Application"];

        return;
    }
}

#pragma mark -
#pragma mark Memory management

- (void) didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];

    // Relinquish ownership any cached data, images, etc. that aren't in use.
}

- (void) viewDidUnload
{
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}


- (void) dealloc
{
    //JGP   [detailViewController release];

    [super dealloc];
}

@end

//
//  MasterViewDataSource.m
//  MCProvider
//
//  Created by J. G. Pusey on 4/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "DataManager.h"
#import "MasterViewDataSource.h"

//
// Assorted view tags:
//
enum
{
    MAIN_LABEL_TAG       = 101,
    DETAIL_LABEL_TAG,
    PHOTO_IMAGE_VIEW_TAG,
    WEB_VIEW_TAG
};

@implementation MasterViewDataSource

- (id) initWithGroup: (BOOL) _withGroup
{
    self = [super init];

    withGroup = _withGroup;

    return self;
}

- (id) init
{
    self = [super init];

    withGroup = NO;

    return self;
}

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return 1;
}

- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
    DataManager  *dm = self.appDelegate.dataManager;
    NSDictionary *scene = [dm currentSceneContext];

    if (!scene)
        return 0.0f;

    NSDictionary *block = [dm currentBlock: idxPath.row
                                  forScene: scene];

    if (!block)
        return 0.0f;

    CGFloat height;

    if ([block objectForKey: @"Height"])
        height = [[block objectForKey: @"Height"] floatValue];
    else if ([dm.masterPlist objectForKey: @"LeftBlockStyle"] &&
             [[dm.masterPlist objectForKey: @"LeftBlockStyle"] isEqual: @"narrow"])
        height = 44.0f;
    else
        height = 100.0f;

    return height;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) sect
{
    NSDictionary *scene = [self.appDelegate.dataManager currentSceneContext];

    if (!scene)
        return 0;

    return [[scene objectForKey: @"blocks"] count];
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    // build multiple reuse queues with different pre-laid out contents
    static NSString *CellIdentifierA = @"CellIdentifierA";
    static NSString *CellIdentifierB = @"CellIdentifierB";
    static NSString *CellIdentifierC = @"CellIdentifierC";
    static NSString *CellIdentifier0 = @"CellIdentifier0";
    static NSString *CellIdentifier1 = @"CellIdentifier1";

    DataManager  *dm = self.appDelegate.dataManager;
    NSDictionary *scene = [dm currentSceneContext];

    if (!scene)
        return nil;

    NSDictionary *block = [dm currentBlock: idxPath.row
                                  forScene: scene];

    if (!block)
        return nil;

    NSString *cellStyle = [scene objectForKey: @"cellstyle"];

    if (!cellStyle)
        cellStyle = @"plain";

    NSString   *cellIdentifier;
    NSUInteger  nesting = 0;

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
        else if (nesting == 1)
            cellIdentifier = CellIdentifierB;
        else
            cellIdentifier = CellIdentifierC;
    }
    else
        cellIdentifier = CellIdentifier0;

    // Dequeue or create a cell of the appropriate type.

    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: cellIdentifier];
    AsyncImageView  *photoImageView = nil;
    UIWebView       *webView = nil;
    UILabel         *detailLabel;
    UILabel         *mainLabel;

    if (!cell)
    {
        cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleDefault
                                       reuseIdentifier: cellIdentifier]
                autorelease];

        if ([block objectForKey: @"WebView"])   //&&[@"webpane" isEqual: [block objectForKey:@"Action"]]) // webview
        {
            mainLabel = [[[UILabel alloc]
                          initWithFrame: CGRectMake (0.0f, 20.0f, 300.0f, 20.0f)]
                         autorelease];

            //mainLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            mainLabel.font = [UIFont systemFontOfSize: 18.0f];
            mainLabel.tag = MAIN_LABEL_TAG;
            mainLabel.textAlignment = UITextAlignmentLeft;

            [cell.contentView addSubview: mainLabel];

            detailLabel = [[[UILabel alloc]
                            initWithFrame: CGRectMake (0.0f, 2.0f, 300.0f, 16.0f)]
                           autorelease];

            //detailLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            detailLabel.font = [UIFont systemFontOfSize: 12.0f];
            detailLabel.tag = DETAIL_LABEL_TAG;
            detailLabel.textAlignment = UITextAlignmentLeft;

            [cell.contentView addSubview: detailLabel];

            CGFloat height;

            if ([block objectForKey: @"Height"])
                height = [[block objectForKey: @"Height"] floatValue];
            else
                height = 100.0f;

            webView = [[[UIWebView alloc]
                        initWithFrame: CGRectMake (0.0f, 44.0f, 320.0f, height - 44.0f)]
                       autorelease];

            //webView.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            webView.tag = WEB_VIEW_TAG;

            [cell.contentView addSubview: webView];
        }
        else if ([dm.masterPlist objectForKey: @"LeftBlockStyle"] &&
                 [[dm.masterPlist objectForKey: @"LeftBlockStyle"] isEqual: @"narrow"])
        {
            CGFloat xpos = 10.0f + (15.0f * nesting);

            mainLabel = [[[UILabel alloc]
                          initWithFrame:CGRectMake (xpos, 20.0f, 300.0f, 20.0f)]
                         autorelease];

            //mainLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            mainLabel.font = [UIFont systemFontOfSize: 18.0f];
            mainLabel.tag = MAIN_LABEL_TAG;
            mainLabel.textAlignment = UITextAlignmentLeft;

            [cell.contentView addSubview: mainLabel];

            //detailLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            detailLabel = [[[UILabel alloc]
                            initWithFrame: CGRectMake (xpos, 2.0f, 300.0f, 16.0f)]
                           autorelease];

            detailLabel.font = [UIFont systemFontOfSize: 12.0f];
            detailLabel.tag = DETAIL_LABEL_TAG;
            detailLabel.textAlignment = UITextAlignmentLeft;

            [cell.contentView addSubview: detailLabel];
        }
        else
        {
            //  photo plus labels
            mainLabel = [[[UILabel alloc]
                          initWithFrame:CGRectMake (100.0f, 40.0f, 200.0f, 60.0f)]
                         autorelease];

            //mainLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            mainLabel.font = [UIFont systemFontOfSize: 18.0f];
            mainLabel.tag = MAIN_LABEL_TAG;
            mainLabel.textAlignment = UITextAlignmentRight;

            [cell.contentView addSubview: mainLabel];

            detailLabel = [[[UILabel alloc]
                            initWithFrame:CGRectMake (100.0f, 0.0f, 200.0f, 40.0f)]
                           autorelease];

            //detailLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            detailLabel.font = [UIFont systemFontOfSize: 12.0f];
            detailLabel.tag = DETAIL_LABEL_TAG;
            detailLabel.textAlignment = UITextAlignmentRight;

            [cell.contentView addSubview: detailLabel];

            photoImageView = [[[AsyncImageView alloc]
                               initWithFrame: CGRectMake (0.0f, 0.0f, 100.0f, 100.0f)]
                              autorelease];

            //photoImageView.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
            photoImageView.tag = PHOTO_IMAGE_VIEW_TAG;

            [cell.contentView addSubview: photoImageView];
        }
    }
    else
    {
        // reuse an existing cell
        //if ([block objectForKey:@"Action"]&&[@"webpane" isEqual: [block objectForKey:@"Action"]]) // webview has only photo part
        //          photo = (UIImageView *)[cell.contentView viewWithTag:WEB_VIEW_TAG];
        //      else {

        mainLabel = (UILabel *) [cell.contentView viewWithTag: MAIN_LABEL_TAG];
        detailLabel = (UILabel *) [cell.contentView viewWithTag: DETAIL_LABEL_TAG];

        if ([block objectForKey: @"WebView"])   //&&[@"webpane" isEqual: [block objectForKey:@"Action"]]) // webview has only photo part
            webView = (UIWebView *) [cell.contentView viewWithTag: WEB_VIEW_TAG];
        else if (!([dm.masterPlist objectForKey: @"LeftBlockStyle"] &&
                   [[dm.masterPlist objectForKey: @"LeftBlockStyle"] isEqual:@"narrow"]))
            photoImageView = (AsyncImageView *) [cell.contentView viewWithTag: PHOTO_IMAGE_VIEW_TAG];
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
    //      NSString *url = [[block objectForKey:@"ImageURL"] stringByTrimmingWhitespace];
    //
    //      if (url)
    //      {
    //          NSData *temp = [[NSData alloc] initWithContentsOfURL:[NSURL URLWithString: url]]; //get from remote URL
    //          photo.image  = [[UIImage imageWithData: temp] retain];
    //      }
    //      else
    //          photo.image = nil;
    //  }
    //  else
    //{

    // whether its a new or old cell, rewrite the labels
    if ([@"plain" isEqual: cellStyle])
    {
        mainLabel.textColor = [UIColor blackColor];
        mainLabel.backgroundColor = [UIColor whiteColor];

        detailLabel.textColor = [UIColor darkGrayColor];
        detailLabel.backgroundColor = [UIColor whiteColor];
    }
    else
    {
        mainLabel.textColor = [UIColor whiteColor];
        mainLabel.backgroundColor = [UIColor blackColor];

        detailLabel.textColor = [UIColor whiteColor];
        detailLabel.backgroundColor = [UIColor blackColor];
    }

    NSString *mainText = [[block objectForKey: @"Label"] stringByTrimmingWhitespace];
    NSString *detailText = [[block objectForKey: @"Info"] stringByTrimmingWhitespace];

    if (mainText)
        mainLabel.text = [NSString stringWithFormat:@"%@   ", mainText];
    else
        mainLabel.text = nil;

    if (detailText)
        detailLabel.text = [NSString stringWithFormat:@"%@   ", detailText];
    else
        detailLabel.text = nil;

    if ([block objectForKey: @"WebView"])   //&&[@"webpane" isEqual: [block objectForKey:@"Action"]]) // webview is simple?
    {
        NSString *url = [[block objectForKey: @"ImageURL"] stringByTrimmingWhitespace];

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
        NSString *url = [[block objectForKey: @"ImageURL"] stringByTrimmingWhitespace];

        if (url)
        {
            //NSData *temp = [[NSData alloc] initWithContentsOfURL:[NSURL URLWithString: url   ]]; //get from remote URL
            // UIImage *temp  = [UIImage imageWithData: [[NSData alloc] initWithContentsOfURL: [NSURL URLWithString: url]]];
            //photo.image =
            [photoImageView loadImageFromURL: [NSURL URLWithString: url]
                               fallbackImage: nil];
        }
        else
            photoImageView.image = nil;
    }
    //}

    return cell;
}

@end

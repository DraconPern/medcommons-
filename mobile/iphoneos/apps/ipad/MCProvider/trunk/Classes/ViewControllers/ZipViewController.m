//
//  ZipViewController.m
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>
#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "DictionaryAdditions.h"
#import "DocumentsManager.h"
#import "MCDocumentTableViewCell.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "WebViewController.h"
#import "ZipViewController.h"
#import "DataStore.h"


#pragma mark -
#pragma mark Public Class ZipViewController
#pragma mark -

#pragma mark Internal Constants

//
// Table sections:
//
enum
{
    DOCUMENTS_SECTION = 0,  // MUST be kept in display order ...

    //
    SECTION_COUNT
};



@interface ZipViewController () <  UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, retain, readwrite) UIBarButtonItem *backButton;
@property (nonatomic, retain, readonly)  NSArray         *documents;



@end

@implementation ZipViewController

@synthesize backButton                 = backButton_;

@dynamic    documents;

#pragma mark Private Instance Methods
- (NSArray *) documents
{

    return self->documents_;
}

#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];

}

- (void) loadView
{
    UITableView *tmpView = [[[UITableView alloc] initWithFrame: self.parentViewController.view.bounds
                                                         style: UITableViewStyleGrouped]
                            autorelease];

    tmpView.dataSource = self;
    tmpView.delegate = self;
    tmpView.separatorColor = [UIColor lightGrayColor];
    tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;

    tmpView.tableHeaderView = nil; // for now [[[InfoHeaderView alloc] initWithFrame: tmpView.frame]
                              // autorelease];


    self.view = tmpView;
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];

    [(UITableView *) self.view flashScrollIndicators];
}

- (void) viewDidLoad
{
    [super viewDidLoad];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];


    UITableView *tabView = (UITableView *) self.view;
    NSIndexPath *idxPath = [tabView indexPathForSelectedRow];

    if (idxPath)
        [tabView deselectRowAtIndexPath: idxPath
                               animated: NO];
}

- (void) viewWillDisappear: (BOOL) animated
{
    [super viewWillDisappear: animated];

}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{

    [self->backButton_ release];
    [self->documents_ release];

    [super dealloc];
}

- (id) initWithBase:(NSString *) str
{
    self = [super init];
    self->base_ = [str copy];

    NSMutableArray          *docs = [[[NSMutableArray alloc] init] autorelease];

    NSString *file;
    NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
                                      enumeratorAtPath: [DataStore pathForExplodedZipFilesForKey:self->base_]];

    // Enumerate all files in the ~/tmp directory
    while ((file = [dirEnum nextObject]))
    {
        // Only check files with jpg extension.
        //if ([[file pathExtension] isEqualToString: @"jpg"])
        {
            NSDictionary *attrs = [dirEnum fileAttributes];

            NSLog (@"file %@ size:%@", file, [attrs valueForKey: @"NSFileSize"]);
            [docs addObject:file];

        }
    }
    self->documents_ =[docs copy];
    return self;
}

#pragma mark Extended UIViewController Methods

- (void) hideMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: nil
                                     animated: YES];
}

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];
}



#pragma mark UITableViewDataSource Methods

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return SECTION_COUNT;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier1 = @"InfoCell1";
    NSUInteger row = idxPath.row;


    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: CellIdentifier1];

    if (!cell)
    {
        switch (idxPath.section)
        {


            case DOCUMENTS_SECTION :
                cell = [[[MCDocumentTableViewCell alloc]
                         initWithReuseIdentifier: CellIdentifier1]
                        autorelease];
                break;

            default :
                break;
        }
    }

    //
    // Reset cell properties to default:
    //
    cell.accessoryType = UITableViewCellAccessoryNone;
    cell.detailTextLabel.text = nil;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.textLabel.text = nil;

    switch (idxPath.section)
    {
        case DOCUMENTS_SECTION :
        {
            if (row < [self.documents count])
            {

                cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
               cell.textLabel.text = [self.documents objectAtIndex: idxPath.row];
            }
            else
                cell = nil;

            break;
        }

        default :
            cell = nil;
            break;
    }

    return cell;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) sect
{

    switch (sect)
    {

        case DOCUMENTS_SECTION :
            return [self.documents count];


        default :
            return 0;
    }
}

- (NSString *) tableView: (UITableView *) tabView
 titleForFooterInSection: (NSInteger) sect
{
    switch (sect)
    {

        case DOCUMENTS_SECTION :
            return NSLocalizedString (@"These docs from zip file", @"");
    }
    return nil;
}

- (NSString *) tableView: (UITableView *) tabView
 titleForHeaderInSection: (NSInteger) sect
{
    switch (sect)
    {

        case DOCUMENTS_SECTION :
        {
            switch ([self.documents count])
            {
                case 0 :
                    return [NSString stringWithFormat: @"%@ - No Documents", self->base_,nil];

                case 1 :
                    return [NSString stringWithFormat: @"%@ - 1 Document",self->base_,nil ];

                default :
                    return [NSString stringWithFormat:
                           @"%@ - %ld Documents",self->base_,[self.documents count],nil ];
            }
        }


        default :
            return nil;
    }
}

#pragma mark UITableViewDelegate Methods

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    switch (idxPath.section)
    {


        case DOCUMENTS_SECTION :
        {
            NSString *s= [NSString stringWithFormat:@"%@/%@",
                          [DataStore pathForExplodedZipFilesForKey:self->base_],
                          [self.documents objectAtIndex: idxPath.row]];
            NSURL *docurl =

            [NSURL fileURLWithPath: s
                       isDirectory: NO];
            //NSLog (@"webview is : %@",s);
            WebViewController *wvc = [[[WebViewController alloc]
                                       initWithURL: docurl]
                                      autorelease];

            wvc.title =
            [NSString stringWithFormat:@"%@:%@",
                                        self->base_,
                                        [self.documents objectAtIndex: idxPath.row]
                                        ];

            [self.navigationController pushViewController: wvc
                                                 animated: YES];
        }


            break;
    }
}

- (void) tableView: (UITableView *) tabView
   willDisplayCell: (UITableViewCell *) cell
 forRowAtIndexPath: (NSIndexPath *) idxPath
{
    //
    // Apple docs say to do this here rather than at cell creation time ...
    //
    cell.backgroundColor = [UIColor whiteColor];
}

@end

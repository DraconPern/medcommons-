//
//  SongsViewController.m
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.
//


#import <MobileCoreServices/MobileCoreServices.h>

#import "DataStore.h"
#import "DataManager.h"
#import	"LoggingViewController.h";
#import "ArchiveInfoController.h"
#import "ModalAlert.h"
#import "ArchivesManager.h"
#import "ArchiveInfo.h"
#import "ArchivesManager.h"

#pragma mark -
#pragma mark Public Class GigStandHomeController
#pragma mark -

#pragma mark Internal Constants

//
// Table sections:
//
enum
{
	INFO_SECTION = 0,  // Right Now Will Have Only "All Songs"
	//    LISTS_SECTION ,     // Will Have All the Lists including Favorites and Recents
	//	ARCHIVES_SECTION ,	// Then Comes All The Archives
	//	ABOUT_SECTION ,		// Whatever else we might want
	
	
    //
    SECTION_COUNT
};

//
// front section rows:
//
enum
{
    INFO_SHORTNAME = 0,  // MUST be kept in display order ...
	INFO_ACTIVATED,
	INFO_SIZE,
	INFO_FILECOUNT,
	INFO_FULLNAME,
	INFO_IMPORTTIME,
	//	INFO_IMAGE,
	
	
    //
    INFO_ROW_COUNT
};



@interface ArchiveInfoController () < UITableViewDataSource, UITableViewDelegate>


- (void) updateNavigationItemAnimated: (BOOL) animated;


- (void) reloadUserInfo;

@end




#pragma mark Private Instance Methods









#pragma mark Public Instance Methods

@implementation ArchiveInfoController
-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}



-(id) initWithArchive: (NSString *)archiv ;
{
	
	
	self = [super init];
	if (self) {
		
		
		self->archive  = [archiv copy];
		
		
		// if attrs is not retained it can crash really hard when scrolling about
		
		self->attrs = [[[NSFileManager defaultManager] attributesOfItemAtPath: [[DataStore pathForSharedDocuments] 
																				stringByAppendingPathComponent: s]
																		error: NULL] retain];
		
		self->mainTableView = nil ; // assigned in loadView
		
	}
	
	return self;
}

-(void) dealloc
{
	[s release]; // this could probably be avoided if initWithIndex didn't retain, but...
	[attrs release];
	[super dealloc];
}



- (void) reloadUserInfo; // redo everything on reload, from rotation
{
	if (self->mainImgView) [self->mainImgView removeFromSuperview];
	if (self->mainWebView) [self->mainWebView removeFromSuperview];
	if (self->mainTableView) [self->mainTableView removeFromSuperview];
	//UIView *temp = self.view;
	[self loadView];
	//[temp release];
	
}

- (void) updateNavigationItemAnimated: (BOOL) animated
{
	self.navigationItem.titleView = [[DataManager allocTitleView:[NSString stringWithFormat:@":%@ info",
																  [ArchivesManager shortName: 
				self->archive ] ]] autorelease];
}


#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
	[super didReceiveMemoryWarning];
	
	// [AsyncImageView clearCache];
}

-(NSString *) infoPageHTMLData
{
	
	ArchiveInfo *ai = [ArchivesManager findArchive:self->archive];
	
	NSString *infopagedata = ai.provenanceHTML;
	if ([infopagedata length]>20) return infopagedata;
	NSString *html = [NSString stringWithFormat:
					  @"<html><head><meta name='viewport' content='width=device-width; maximum-scale=1.2' /> </head><body><center><font size='+1' color='red'>No attribution info was supplied by the publisher of:<br/> &lt;%@&gt;<br/></font><font size='+1' color='black'>To remedy, please make an --info--.html file and insert it into this archive</center></body></html>",
					  archive];
	return html;
}

-(UITableView *) fixupTableFrame:(CGRect )tableFrame ypos:(float) firstline picsize:(float) imgsize;
{
	// the height has already been set here 
	
	
	tableFrame.origin.y = firstline - 10.f;
	tableFrame.size.width -=imgsize +10.f;
	tableFrame.origin.x+= imgsize +10.f;
	
	
	UITableView *tmpView = [[[UITableView alloc] initWithFrame: tableFrame style: UITableViewStyleGrouped] autorelease];
	tmpView.backgroundColor =  [UIColor clearColor]; 
	tmpView.opaque = YES;
	tmpView.backgroundView = nil;
	tmpView.dataSource = self;
	tmpView.delegate = self;
	tmpView.separatorColor = [UIColor lightGrayColor];
	tmpView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	return tmpView;
}
- (void) loadView
{
	
	
	CGRect frame = self.parentViewController.view.bounds;
	UIView *ov = [[UIView alloc] initWithFrame:frame];
	ov.backgroundColor = [UIColor lightGrayColor]; //[DataManager applicationColor];
	
	//BEGIN GEOMETRY CUSTOMIZATON
	
	float firstline = 10.0f+[DataManager navBarHeight];	
	
	float imgsize =  (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?120.f:40.f;
	
	CGRect tableFrame = self.parentViewController.view.bounds;
    tableFrame.size.height = 250.f; // set the height before we go any further
	
	CGRect imgFrame = CGRectMake(10.0f, firstline, imgsize, imgsize);
	float spacing = (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?50.f:90.f;
	
	float deltatablestart  = (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?-20.0f:0.f;
	float deltay = -spacing+[DataManager navBarHeight] + tableFrame.size.height; // was 50 on ipad	
	CGRect webFrame = CGRectMake(10.0f, deltay, self.parentViewController.view.bounds.size.width-20.0f, self.parentViewController.view.bounds.size.height-deltay-10.f);
	
	//END GEOMETRY CUSTOMIZATION
	
	
	UITableView *tmpView = [self fixupTableFrame: tableFrame ypos: firstline+deltatablestart picsize: imgsize];	
	
	//	
	self->mainTableView = tmpView;
	
	[ov addSubview:tmpView]	;
	
	
	UIImageView *imgv = [[[UIImageView alloc] initWithFrame:imgFrame] autorelease];
	imgv.image = [ArchivesManager newArchiveThumbnail: self->archive];
	
	self->mainImgView = imgv;
	
	[ov addSubview:imgv];
	
	// make a webview 
	
	NSString *html = [self infoPageHTMLData];
	
	
	UIWebView *wv = [[[UIWebView alloc] initWithFrame:webFrame] autorelease];
	wv.backgroundColor = [DataManager applicationColor];	
	wv.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
						   UIViewAutoresizingFlexibleWidth);
	wv.dataDetectorTypes = UIDataDetectorTypeLink; // no phone numbers
	wv.delegate =  nil ; //was just self XYZZY
	wv.scalesPageToFit = YES;
	
	self->mainWebView = wv;
	
	[wv loadHTMLString:html baseURL:NULL ];
	
	[ov addSubview:wv];
	
	[self updateNavigationItemAnimated:YES];
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.navigationController.navigationBar.translucent = YES;
	
	
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"back" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(donePressed)] autorelease];
	
	self.view=ov;

}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}
- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
	NSLog (@"AIC didRotateFromInterfaceOrientation");
	[self reloadUserInfo];
}


#pragma mark Overridden NSObject Methods


#pragma mark UITableViewDataSource Methods

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
	
	return SECTION_COUNT;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
		  cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
		static NSString *CellIdentifier0 = @"AInfoCell0";
	
	NSString        *cellIdentifier;
	
	switch (idxPath.section)
	{		
		case INFO_SECTION :
		default:
		{

			{
				
				cellIdentifier = CellIdentifier0; // this section will have not
			}
			
			break;
			
		}
	}
	
	UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: cellIdentifier];
	
	if (!cell)
	{
		switch (idxPath.section)
		{
				
			case INFO_SECTION:

				
				cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleValue1
											   reuseIdentifier: cellIdentifier]
						autorelease];
				
				//	NSLog (@"created new cell %@ with id %@",cell,cellIdentifier);
				
				break;
				
			default :
				break;
		}
	}
	//	else 
	//		NSLog (@"got recycled cell %@ with id %@",cell,cellIdentifier);
	
	//
	// Reset cell properties to default:
	//
	cell.accessoryType = UITableViewCellAccessoryNone;  // off for now	
	//cell.imageView.image =  nil;
	cell.detailTextLabel.text = @"invalid cell";
	cell.selectionStyle = UITableViewCellSelectionStyleNone;
	cell.textLabel.text = @"invalid cell";
	
	if (INFO_SECTION == (idxPath.section))
	{
		double mb = [ArchivesManager fileSize:self->archive];
		NSUInteger filecount  = [ArchivesManager fileCount :self->archive];
		
		switch (idxPath.row)
		{
			case INFO_ACTIVATED :
			{
				
				cell.textLabel.text =@"activated";
				cell.detailTextLabel.text = @"";
			
				
				BOOL b = 	[ArchivesManager isArchiveEnabled:self->archive] ;
				if (b == YES)
					cell.accessoryType = UITableViewCellAccessoryCheckmark;  // ov for now	
				break;
			}	
				
				
			case INFO_SHORTNAME :
			{
				
				cell.textLabel.text =@"archive name";
				cell.detailTextLabel.text= [ArchivesManager shortName:self->archive ]; ;
				break;
			}
			case INFO_SIZE :
			{
				
				cell.textLabel.text =@"archive size";
				cell.detailTextLabel.text=[NSString stringWithFormat:@"%.2fMB",mb];
				break;
			}
			case INFO_FILECOUNT :
			{
				
				cell.textLabel.text =@"files";
				cell.detailTextLabel.text=[NSString stringWithFormat:@"%d",filecount];
				break;
			}
			case INFO_FULLNAME :
			{
				
				cell.textLabel.text =@"full name";
				cell.detailTextLabel.text= self->archive ;
				break;
			}
			case INFO_IMPORTTIME :
			{
				
				cell.textLabel.text =@"iTunes import";
				cell.detailTextLabel.text=[NSString stringWithFormat:@"%@",[self->attrs objectForKey:NSFileCreationDate]];
				break;
			}
				
		}			
		
		
	}
	
	
	return cell;
}

- (NSInteger) tableView: (UITableView *) tabView numberOfRowsInSection: (NSInteger) sect
{
	//   SettingsManager *settings = self.appDelegate.settingsManager;
	
	switch (sect)
	{
		case INFO_SECTION :
			return INFO_ROW_COUNT;
			
			
			
			
		default :
			return 0;
	}
}

#pragma mark footer stuff
- (NSString *) tableView: (UITableView *) tabView titleForFooterInSection: (NSInteger) sect
{
	//	BOOL settingsCanEdit = ![[NSUserDefaults standardUserDefaults] boolForKey:@"SettingsLocked"];
	switch (sect)
	{

		default :
			return nil;
			
	}
}

#pragma mark header stuff

#pragma mark UITableViewDelegate Methods


- (CGFloat) tableView: (UITableView *) tabView heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
	
return  (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?30.f:22.f;

}

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
	if (idxPath.section == INFO_SECTION)
	{
		if (idxPath.row == INFO_ACTIVATED)
			
		{
			
			NSString *shorty = [ArchivesManager shortName: self->archive] ;
			
			BOOL b = 		[ArchivesManager isArchiveEnabled:self->archive] ;

			
			if (b==YES) {
				BOOL yesno = [ModalAlert ask: [NSString stringWithFormat: @"Please confirm you want to make all tunes in %@ invisible",shorty]];
				if (yesno == YES)
				{
					
					[ArchivesManager setArchiveEnabled:NO forArchiveName: self->archive];
					[tabView reloadData];
				}
				
			}
			else {
				BOOL yesno = [ModalAlert ask: [NSString stringWithFormat: @"Please confirm youwant to make all tunes in %@ visible",shorty]];
				if (yesno == YES)
				{
			
					
					[ArchivesManager setArchiveEnabled:YES forArchiveName: self->archive];
					
					[tabView reloadData];
				}
			}
			
			
			
			// should rewrite the archive plis
			
			
		}
		
	}
	return;
}

//- (void) tableView: (UITableView *) tabView
//   willDisplayCell: (UITableViewCell *) cell
// forRowAtIndexPath: (NSIndexPath *) idxPath
//{
//	//
//	// Apple docs say to do this here rather than at cell creation time ...
//	//
//	cell.backgroundColor = [UIColor whiteColor];
//}
#pragma mark UIAlertViewDelegate Methods
-(void) alertView:(UIAlertView *) alertView clickedButtonAtIndex: (int)index

{ 
	if (index!=0) { // not cancelled
		// come here after confirmation
		[ArchivesManager factoryReset];
		[alertView release];
		
		[self reloadUserInfo];
		
	}
}
@end

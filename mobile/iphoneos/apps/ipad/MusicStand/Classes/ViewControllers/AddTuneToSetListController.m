    //
//  AddTuneToSetListController.m
//  MusicStand
//
//  Created by bill donner on 11/9/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AddTuneToSetListController.h"
#import "DataManager.h"
#import "DataStore.h"
#import "TitleNode.h"

#import <UIKit/UIKit.h>


// for general screen
#define kLeftMargin				20.0f
#define kTopMargin				20.0f
#define kRightMargin			20.0f
#define kTweenMargin			10.0f
#define kTextFieldHeight		30.0f

@implementation AddTuneToSetListController

@synthesize currentPicker;
@synthesize myPickerView ,pickerViewArray, label;

-(void) cancelPressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}
-(RefNode *) newRefNode;
{
	NSString *archive = [[DataManager sharedInstance].archives objectAtIndex:0];
	RefNode *rn = [[RefNode alloc] initWithTitle:self->maintitle 
								  andWithArchive:archive ] ;
	return rn;
	
}
-(void) donePressed;
{
		NSMutableArray *items= [[DataManager sharedInstance] allocLoadRefNodeItems:self->chosenList	]	;
		id t = [[self newRefNode] autorelease];
		[items addObject: t];
		[DataManager writeRefNodeItems:items toPropertyList:self->chosenList];	
		[self.parentViewController dismissModalViewControllerAnimated:YES];
	[items release];
}

// return the picker frame based on its size, positioned at the bottom of the page
- (CGRect)pickerFrameWithSize:(CGSize)size
{
	CGRect screenRect = [[UIScreen mainScreen] applicationFrame];
	CGRect pickerRect = CGRectMake(	0.0f,
								   screenRect.size.height - 84.0f - size.height,
								   size.width,
								   size.height);
	return pickerRect;
}
-(NSMutableArray *) newSetlistsScan
{
	NSMutableArray *alllists = [[NSMutableArray alloc] init];
	[alllists addObject:@"favorites"];
	
	NSString *file;
	
	//NSLog (@"scanning for setlists in %@",[DataStore pathForTuneLists]);
	
	NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
									  enumeratorAtPath: [DataStore pathForTuneLists]];
	while ((file = [dirEnum nextObject]))
	{
		NSDictionary *attrs = [dirEnum fileAttributes];
		
		NSString *ftype = [attrs objectForKey:@"NSFileType"];
		if ([ftype isEqualToString:NSFileTypeRegular])
		{
			NSString *shortie = [file stringByDeletingPathExtension];
			if (!([shortie isEqualToString:@"recents"] || [shortie isEqualToString:@"favorites"] ||
				  [shortie isEqualToString:@"alltunes"] ||[shortie isEqualToString:@"archives"]))
				[alllists addObject:shortie];
			//NSLog (@"added setlist %@",shortie);
		}
	}
	return alllists;
}
#pragma mark -

-(id) initWithTitle:(NSString *) titl {
	self = [super init];
	if (self)
	{
	self->maintitle=[titl copy];
	}
	return self;
}
#pragma mark UIPickerView
- (void)createPicker
{
	pickerViewArray =[self newSetlistsScan];
	// note we are using CGRectZero for the dimensions of our picker view,
	// this is because picker views have a built in optimum size,
	// you just need to set the correct origin in your view.
	//
	// position the picker at the bottom
	myPickerView = [[UIPickerView alloc] initWithFrame:CGRectZero];
	CGSize pickerSize = [myPickerView sizeThatFits:CGSizeZero];
	myPickerView.frame = [self pickerFrameWithSize:pickerSize];
	
	myPickerView.autoresizingMask = UIViewAutoresizingFlexibleWidth;
	myPickerView.showsSelectionIndicator = YES;	// note this is default to NO
	myPickerView.backgroundColor = [UIColor clearColor];
	
	// this view controller is the data source and delegate
	myPickerView.delegate = self;
	myPickerView.dataSource = self;
	
	// add this picker to our view controller, initially hidden
	myPickerView.hidden = NO;
	[self.view addSubview:myPickerView];
}




#pragma mark -

- (void)showPicker:(UIView *)picker
{
	// hide the current picker and show the new one
	if (currentPicker)
	{
		currentPicker.hidden = YES;
		label.text = @"";
	}
	picker.hidden = NO;
	
	currentPicker = picker;	// remember the current picker so we can remove it later when another one is chosen
}


-(void) loadView

{
	
	CGRect tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	
	// get it clear so we can see page

	
	UIView *v = [[[UIView alloc] initWithFrame: tmpFrame] autorelease];
	v.backgroundColor = [UIColor clearColor];
	self.view = v;
	
}
- (void)viewDidLoad
{		
	[super viewDidLoad];
	
	self.title = [NSString stringWithFormat:@"Add %@ to Setlist or Favorites",self->maintitle];
	
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithBarButtonSystemItem: UIBarButtonSystemItemCancel 
											  target:self 
											  action:@selector(cancelPressed)] autorelease];
	
	
	self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithBarButtonSystemItem: UIBarButtonSystemItemDone
											  target:self 
											  action:@selector(donePressed)] autorelease];
	
	[self createPicker];	
	
	// label for picker selection output, place it right above the picker
	CGRect labelFrame = CGRectMake(	kLeftMargin,
								   myPickerView.frame.origin.y - kTextFieldHeight,
								   self.view.bounds.size.width - (kRightMargin * 2.0f),
								   kTextFieldHeight);
	self.label = [[[UILabel alloc] initWithFrame:labelFrame] autorelease];
    self.label.font = [UIFont systemFontOfSize: 14];
	self.label.textAlignment = UITextAlignmentCenter;
	self.label.textColor = [UIColor whiteColor];
	self.label.backgroundColor = [UIColor clearColor];
	[self.view addSubview:self.label];
	
}

// called after the view controller's view is released and set to nil.
// For example, a memory warning which causes the view to be purged. Not invoked as a result of -dealloc.
// So release any properties that are loaded in viewDidLoad or can be recreated lazily.
//
- (void)viewDidUnload
{
	[super viewDidUnload];
	
	// release and set out IBOutlets to nil
	
	// release all the other objects
	self.myPickerView = nil;
	self.pickerViewArray = nil;
	
	
	self.label = nil;
//	
//	self.customPickerView = nil;
//	self.customPickerDataSource = nil;
//
}

- (void)dealloc
{
	[pickerViewArray release];
	[myPickerView release];
	[label release];
	
	
	[super dealloc];
}

#pragma mark -
#pragma mark UIPickerViewDelegate

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
	if (pickerView == myPickerView)	// don't show selection for the custom picker
	{
		// report the selection to the UI label
		label.text = [NSString stringWithFormat:@"Click Done to Add This Tune  %@ to %@....",
					  self->maintitle,
					  [pickerViewArray objectAtIndex:[pickerView selectedRowInComponent:0]]
					 ];
		self->chosenList  =  [pickerViewArray objectAtIndex:[pickerView selectedRowInComponent:0]];
	}
}


#pragma mark -
#pragma mark UIPickerViewDataSource

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
	NSString *returnStr = @"";
	
	// note: custom picker doesn't care about titles, it uses custom views
	if (pickerView == myPickerView)
	{
		if (component == 0)
		{
			returnStr = [pickerViewArray objectAtIndex:row];
		}
		else
		{
			returnStr = [[NSNumber numberWithInt:row] stringValue];
		}
	}
	
	return returnStr;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView widthForComponent:(NSInteger)component
{
	CGFloat componentWidth = 768.0f;
	
	return componentWidth;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component
{
	return 60.0f;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
	return [pickerViewArray count];
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
	return 1;
}


#pragma mark -
#pragma mark UIViewController delegate methods

// called after this controller's view was dismissed, covered or otherwise hidden
- (void)viewWillDisappear:(BOOL)animated
{
	currentPicker.hidden = YES;
	
	// restore the nav bar and status bar color to default
	self.navigationController.navigationBar.barStyle = UIBarStyleDefault;
	[UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleDefault;
}

// called after this controller's view will appear
- (void)viewWillAppear:(BOOL)animated
{
	
	// for aesthetic reasons (the background is black), make the nav bar black for this particular page
	self.navigationController.navigationBar.barStyle = UIBarStyleBlackOpaque;
	
	// match the status bar with the nav bar
	[UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleBlackOpaque;
}

@end


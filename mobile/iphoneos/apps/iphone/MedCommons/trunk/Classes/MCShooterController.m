//
//  MCShooterController.m
//  MedCommons
//
//  Created by bill donner on 1/22/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

#import "MedCommons.h"
#import "MCUploadController.h"
#import "PatientStore.h"
#import "CustomViews.h"
#import "MCPanningController.h"
#import "MCShooterController.h"
#import "VideoDevice.h"
#import "CameraDevice.h"
#import "MCSlideSorter.h"
#import "GPSDevice.h"
#import "DataManager.h"

@implementation MCShooterController

// none of this works if we try to shoot directly from self, not sure why this is need

-(void) shootVideoFromCamera
{
	VideoDevice *vc = [[VideoDevice alloc] initWithCamera: YES   ];
	
	[[DataManager sharedInstance].ffOuterController presentModalViewController:vc animated:YES];
	
}
-(void) shootVideoFromRoll
{
	
	VideoDevice *vc = [[VideoDevice alloc] initWithCamera: NO   ];
	
	[[DataManager sharedInstance].ffOuterController presentModalViewController:vc animated:YES];
	
}

-(void) shootSubjectPhotoFromCamera
{
	CameraDevice *cc = [[CameraDevice alloc] initWithSubject:YES wantsCamera: YES wantsRoll: NO ];
	
	[[DataManager sharedInstance].ffOuterController presentModalViewController:cc animated:YES];
	
}
-(void) shootSubjectPhotoFromAlbum
{
	CameraDevice *cc = [[CameraDevice alloc] initWithSubject:YES wantsCamera: NO wantsRoll: NO ] ;
	[[DataManager sharedInstance].ffOuterController presentModalViewController:cc animated:YES];
	
}
-(void) shootSubjectPhotoFromRoll
{
	CameraDevice *cc = [[CameraDevice alloc] initWithSubject:YES wantsCamera: NO wantsRoll: YES ];
	[[DataManager sharedInstance].ffOuterController presentModalViewController:cc animated:YES];
	
}
-(void) shootPartPhotoFromCamera
{
	CameraDevice *cc = [[CameraDevice alloc] initWithSubject:NO wantsCamera: YES wantsRoll: NO ]; 
	[[DataManager sharedInstance].ffOuterController presentModalViewController:cc animated:YES];
}
-(void) shootPartPhotoFromAlbum
{
	CameraDevice *cc = [[CameraDevice alloc] initWithSubject:NO wantsCamera: NO wantsRoll: NO ];
	[[DataManager sharedInstance].ffOuterController presentModalViewController:cc animated:YES];
	
}
-(void) shootPartPhotoFromRoll
{
	CameraDevice *cc = [[CameraDevice alloc] initWithSubject:NO wantsCamera: NO wantsRoll: YES ];
	[	[DataManager sharedInstance].ffOuterController presentModalViewController:cc  animated:YES];
	
	
}

-(void) actionSheet: (UIActionSheet *) alertView clickedButtonAtIndex:(NSInteger) buttonIndex
{
	
	//	if( (alert_state ==2)&&(buttonIndex == 0)) [self reset ];	
	//	if( (alert_state ==2)&&(buttonIndex == 1)) [self newSeries ];
	
	//	if( (alert_state ==21)&&(buttonIndex == 0)) { [self reset ];	}
	//	if( (alert_state ==21)&&(buttonIndex == 1)) { [self newSeries ];  }	
	if( (alert_state ==3)&&(buttonIndex == 0))  [self shootSubjectPhotoFromAlbum];	
	
	if( (alert_state ==31)&&(buttonIndex == 0)) [self shootSubjectPhotoFromAlbum];	
	if( (alert_state ==31)&&(buttonIndex == 1)) [self shootSubjectPhotoFromCamera];
	
	
	if( (alert_state ==4)&&(buttonIndex == 0))
	{  if (![patientStore haveSubjectPhoto] ) [self shootSubjectPhotoFromAlbum]; else 
		[self shootPartPhotoFromAlbum];
	}
	if( (alert_state ==5)&&(buttonIndex == 0)) 
	{
		if (![patientStore haveSubjectPhoto] ) [self shootSubjectPhotoFromAlbum]; else 
			[self shootPartPhotoFromAlbum];
	}
	if( (alert_state ==5)&&(buttonIndex == 1)) {
		if (![patientStore haveSubjectPhoto] ) [self shootSubjectPhotoFromCamera]; else 
			[self shootPartPhotoFromCamera];
	}
	if( (alert_state ==6)&&(buttonIndex == 0))
	{
		if (![patientStore haveSubjectPhoto] ) [self shootSubjectPhotoFromAlbum]; else 
			[self shootPartPhotoFromAlbum];
	}
	if( (alert_state ==6)&&(buttonIndex == 1)) 
	{
		if (![patientStore haveSubjectPhoto] ) [self shootSubjectPhotoFromCamera]; else 
			[self shootPartPhotoFromCamera];
	}
	if( (alert_state ==6)&&(buttonIndex == 2)) 
	{
		if (![patientStore haveSubjectPhoto] ) [self shootSubjectPhotoFromCamera]; else 
			[self shootVideoFromRoll];	
	}
	if( (alert_state ==6)&&(buttonIndex == 3)) 
	{
		if (![patientStore haveSubjectPhoto] ) [self shootSubjectPhotoFromCamera];
		else [self shootVideoFromCamera];
	}
	
	[actionSheet release];
	
	
}



-(void) confirmResetAfterUpload
{
	alert_state = 21;
	
	
	
	actionSheet = [[UIActionSheet alloc] initWithTitle:@"You Have Successfully Uploaded this Photo Series and Can Open a New One" 
											  delegate:self cancelButtonTitle: @"Cancel"
								destructiveButtonTitle: nil
									 otherButtonTitles:@"New Subject", @"New Series for Subject",nil];
	//[self showActionSheet];
	
}

-(void)setTabInt:(int) num
{   
	if (num== 0)
		[UIApplication sharedApplication].applicationIconBadgeNumber = 0; // show however many are lingering
	else
		[UIApplication sharedApplication].applicationIconBadgeNumber = num; // show however many are lingering
	
}
-(void) showActionSheet
{
	
	[actionSheet showFromTabBar:(UITabBar *) self.view];
}

- (BOOL) isVideoRecordingAvailable
{
	if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) return NO;
	return [[UIImagePickerController availableMediaTypesForSourceType:UIImagePickerControllerSourceTypeCamera] containsObject:@"public.movie"];
}
- (BOOL) isPhotoShootingAvailable
{
	return [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera] ;
}
- (void) CameraDone: (id) obj
{
	// called when camera is disappearing
	
	//[outerViewController screenUpdate:nil]; // reapint and go back 
	int numpics = [patientStore countPartPics];
	[self setTabInt:numpics];
	//[patientStore dumpPatientStore];
	return;
	
}
-(void) flipit
{
	
	//MY_ASSERT(flipsideController!=nil);
	//[self.navigationController pushViewController:(MainInfoController *)  flipsideController 	animated:YES];
	
}
- (void) switchToFilpside
{
	//let the actionsheet unwind before flipping
	[self performSelector: @selector (flipit) withObject: nil	afterDelay:0.0f];
	
}

-(void) confirmShootSubjectChoices
{
	
	//	UIAlertView *baseAlert;
	if ([self isPhotoShootingAvailable])
	{
		alert_state = 31;
		if (![patientStore haveSubjectPhoto])
		{
			
			actionSheet= [[UIActionSheet alloc] initWithTitle:@"Shoot the Subject Photo" 
													 delegate:self cancelButtonTitle: @"Cancel" destructiveButtonTitle:nil
											otherButtonTitles:@"Photo Album",@"Still Photo", nil];	
			
		} 
		else
		{
			
			actionSheet= [[UIActionSheet alloc] initWithTitle:@"Re-shoot the Subject Photo" 
													 delegate:self cancelButtonTitle: @"Cancel" destructiveButtonTitle:nil
											otherButtonTitles:@"Photo Album",@"Still Photo", nil];
		}
	}
	else
	{
		alert_state = 3;
		if (![patientStore haveSubjectPhoto])
		{
			
			actionSheet= [[UIActionSheet alloc] initWithTitle:@"Shoot the Subject Photo" 
													 delegate:self cancelButtonTitle: @"Cancel" destructiveButtonTitle:nil
											otherButtonTitles:@"Photo Album",nil];	
			
		} 
		else
		{
			
			actionSheet= [[UIActionSheet alloc] initWithTitle:@"Re-shoot the Subject Photo" 
													 delegate:self cancelButtonTitle: @"Cancel" destructiveButtonTitle:nil
											otherButtonTitles:@"Photo Album",nil];
		}
	}
	
	
	[self showActionSheet];
	
}
-(void) confirmShootPartChoices
{
	if (![patientStore haveSubjectPhoto] )  [self confirmShootSubjectChoices]; // if no subject photo then force it
	else
	{
		
		
		alert_state = 4;
		
		actionSheet= [[UIActionSheet alloc] initWithTitle:@"Shoot Parts" 		 
												 delegate:self cancelButtonTitle: @"Cancel"	destructiveButtonTitle:nil				
										otherButtonTitles:@"Photo Album",nil];
		if ([self isPhotoShootingAvailable])
		{
			alert_state = 5;
			actionSheet= [[UIActionSheet alloc] initWithTitle:@"Shoot Parts" 		 
													 delegate:self cancelButtonTitle: @"Cancel"	destructiveButtonTitle:nil				
											otherButtonTitles:@"Photo Album",@"Still Camera",nil];
			
		}
		if ([self isVideoRecordingAvailable])
		{
			alert_state = 6;
			actionSheet= [[UIActionSheet alloc] initWithTitle:@"Shoot Parts" 		 
													 delegate:self cancelButtonTitle: @"Cancel"	destructiveButtonTitle:nil				
											otherButtonTitles:@"Photo Album",@"Still Camera",
#if defined(ENABLE_BIGAPP_PLACES)	
#else
						  @"Video Roll",@"Video Camera",
#endif						  
						  
						  nil];
		}
		[self showActionSheet];
	}
}

- (void)viewWillAppear:(BOOL)animated
{	
	
	[self subjectScreenUpdate: nil]; // reapint screen
	[super viewWillAppear:animated];
	
}
- (void) dealloc
{
	
	[[DataManager sharedInstance].ffGPSDevice gpsDisable]; // get the turned off
	[tinyPics release];
	BREADCRUMBS_POP;
	[super dealloc];
	
}

-(MCShooterController *) init
{
	self = [super init];
	///self = [super initWithRootViewController: _mvc]; // make ourself a nav controller
	patientStore = [[DataManager sharedInstance] ffPatientStore];
	customViews = [[DataManager sharedInstance] ffCustomViews]; 	
	disableTouchesSemaphore = 0; // semaphore to disable touch recognition
	validFieldEntered = NO;
	[[DataManager sharedInstance].ffGPSDevice gpsEnable]; // get the gps warmed up
	BREADCRUMBS_PUSH;
	return self;
}


-(void) uploadit
{
	if (disableTouchesSemaphore==0)
		//[mainViewController confirmUpload]; // here when button was hit
	{
		NSString *msg1 = @"";	NSString *msg2 = @"";
		if ([patientStore haveSubjectPhoto]) msg1 = @"a new Patient Photo";
		if ([patientStore countPartPics] >0) msg2 = [NSString stringWithFormat:@"- %d target photos",[patientStore countPartPics]];
		NSString *middle = [NSString stringWithFormat:@"Uploading %@ %@",msg1,msg2];
		
		MCUploadController *aneController = [(MCUploadController *)[MCUploadController alloc] initWithTitle:@"Photo Uploading"
																								 andWithTop:@"hitting upload will store photos to the patient's medcommons account"
																							  andWithMiddle:middle 
																							 andWithSuccess:@"Silhouette.png"
																							 andWithFailure:@"logoGray.png"
											 ];
		
		[self.navigationController pushViewController:aneController animated:YES];
		[aneController release];
		
	}
	
}
-(void) slidesorter: (id) obje
{
	MCSlideSorter *horizontalController = (MCSlideSorter *)[[MCSlideSorter alloc]  init ];
	MY_ASSERT (horizontalController!=nil);
	[self.navigationController pushViewController:(UIViewController *)horizontalController 	animated:YES];
	[horizontalController release];
}

-(void) wideviewit
{
	MCPanningController *horizontalController = (MCPanningController *)[[MCPanningController alloc] init ];
	MY_ASSERT (horizontalController!=nil);
	[self.navigationController pushViewController:(UIViewController *)horizontalController 	animated:YES];
	[horizontalController release];
}
- (void) switchToWideview :(int) partnum
{
	//let the actionsheet unwind before flipping -- need to set initial page
	//	[horizontalController loadScrollViewWithPage: partnum];
	//	[horizontalController changePage:nil];
	[self performSelector: @selector (wideviewit) withObject:nil afterDelay:0.0f];
	
}


#pragma mark touch handling

// Handles the start of a touch
-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
	if (disableTouchesSemaphore==0)
	{ 
		NSUInteger numTaps = [[touches anyObject] tapCount];
		
		if(numTaps >= 2) //???
		{
			
		}
		
	}
	
}
// Checks to see which view, or views,  the point is in and then calls a method to perform the closing animation,
// which is to return the piece to its original size, as if it is being put down by the user.
-(BOOL)dispatchTouchEndEvent:(UIView *)theView toPosition:(CGPoint)position
{   
	// Check to see which view, or views,  the point is in and then animate to that position.
	if ((CGRectContainsPoint(toggleFrame, position)))
	{
		[self slidesorter:NULL];
		return YES;
	}
	
	// Check to see which view, or views,  the point is in and then animate to that position.
	for (int j=0; j<[tinyPics count]-1; j++)
	{
		
		if (CGRectContainsPoint([[tinyPics objectAtIndex:j] frame], position))
		{
			if (j==0)
			{
				if([patientStore haveSubjectPhoto]) 
					
					[self switchToWideview : j];
				else 
					[self confirmShootSubjectChoices];
				
			}
			else {
				if([patientStore havePhotoAtIndex: j-1]) 
					
					[self switchToWideview : j];
				else 
					[self confirmShootPartChoices];
			}
			return YES;
			
		}
	}
	
	return NO;
}
// Handles one end of a touch event.
-(void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
	if (disableTouchesSemaphore==0)
	{ 
		// Enumerates through all touch object
		for (UITouch *touch in touches) {
			// Sends to the dispatch method, which will make sure the appropriate subview is acted upon
			if ([self dispatchTouchEndEvent:[touch view] toPosition:[touch locationInView:self.view]] == YES) return;
		}
	}
}

#pragma mark standard workflow
-(void) subjectViewMainLayout
{
	UIImageView* oneImageView = [tinyPics objectAtIndex:0];	
	//if([patientStore haveRemoteSubjectPhoto])
	//	oneImageView.image  = [patientStore remoteSubjectPhoto];
	
	//else 
	if(![patientStore haveSubjectPhoto]) 
		oneImageView.image = [UIImage imageNamed:PLEASE_SHOOT_PATIENT_IMAGE];	else
			oneImageView.image = [UIImage imageWithContentsOfFile:[patientStore fullSubjectPhotoSpec]];
}
- (void) subjectViewFooter
{

	for (int i=1; i<=10; i++)
	{
		; // get the next possible file
		UIImageView* oneImageView = [tinyPics objectAtIndex:i]; //compensate for photo pic
		if([patientStore havePhotoAtIndex:i-1]) 
			oneImageView .image = [UIImage imageWithContentsOfFile:	[patientStore fullPhotoSpecAtIndex:i-1]]; 
		else 
			oneImageView .image = [UIImage imageNamed:PLEASE_SHOOT_PATIENT_IMAGE];
	}	
}

-(void) prepareNavBar
{
PatientStore *ps = [[DataManager sharedInstance] ffPatientStore];
	NSInteger count = [ps countPartPics];
if ([ps haveSubjectPhoto]) count++;
	if (count==0)	self.navigationItem.rightBarButtonItem = nil; else
	self.navigationItem.rightBarButtonItem =[ [[UIBarButtonItem alloc] initWithTitle:
											   @"Upload"
																			   style:UIBarButtonItemStylePlain
																			  target:self	action: @selector (uploadit)] autorelease];
    self.navigationItem.title =@"Camera";
	self.navigationItem.backBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Back" 
																			  style:UIBarButtonItemStylePlain
																			 target:nil action:nil] autorelease];
}
- (void) subjectScreenUpdate: (NSTimer *) Timer
{
	[customViews customMainPortraitControllerFieldReload]; //reload from prefs incase was updated from dashboard
	[self subjectViewMainLayout];
	[self subjectViewFooter];	
	[self prepareNavBar];
}
- (void) loadView
{
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  
	appFrame.origin.y = appFrame.origin.y + 44.0f;
	appFrame.size.height = appFrame.size.height - 44.0f ;
	
	outerView = [[UIView alloc] initWithFrame:appFrame];  
	outerView.backgroundColor = [UIColor whiteColor];  
	outerView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;  
	self.view = outerView;  
	
	//*******************   LAYOUT OF Subject Demographics Standard View
	float pics_row_1_start = -4.0f+327.0f-44;
	float pics_row_2_start = -4.0f +392.0f-44;
	
	touchFrame = CGRectMake(0.0f,pics_row_1_start - 5.0f, 320.0f, pics_row_2_start+80.0f); // defined at outer level
	
	pictureframe =  CGRectMake(4.0f+4.0f,-4.0f+56.0f, 80.0f, 80.0f);
	
	toggleFrame =  CGRectMake(17.0f,198.0f, 60.0f, 60.0f);
	iconFrame = CGRectMake(18.0f,145.0f, 57.0f, 57.0f);
	//	NSLog (@"This specific app %@ is calling remote server with appkey %@",
	//[[[NSBundle mainBundle] infoDictionary]   objectForKey:@"CFBundleName"],appkeyCFBundleIconFile	}
	UIImageView *iconView = [[UIImageView alloc] initWithFrame:iconFrame];
	//iconView.image = [UIImage imageNamed:[[[NSBundle mainBundle] infoDictionary]   
	//										  objectForKey:@"CFBundleIconFile"]];	
	
	iconView.image = [UIImage imageNamed:[[DataManager sharedInstance] ffAppLogoImage]];
	[outerView addSubview:iconView] ;
	[iconView release];
	
	UIButton *cbutton = [UIButton buttonWithType: UIButtonTypeInfoDark];
	[cbutton setFrame: toggleFrame];
	//[cbutton addTarget: self action: @selector (slidesorter:) forControlEvents:UIControlEventTouchUpInside];
	[outerView addSubview:cbutton];
	//****************   End of Layout
	// at the very bottom there will be an array of up to 10 photos
	tinyPics  = [[NSMutableArray alloc] init];
	//make the Subject be the zeroth entry
	UIImageView *oneImageView = [[UIImageView alloc] initWithFrame:pictureframe];
	//	if ([patientStore haveRemoteSubjectPhoto])
	//		oneImageView.image  = [patientStore remoteSubjectPhoto];
	//	else
//	{
	//	if(![patientStore haveSubjectPhoto]) 
//			oneImageView.image = [UIImage imageNamed:PLEASE_SHOOT_PATIENT_IMAGE];	else
//				oneImageView.image = [UIImage imageWithContentsOfFile:[patientStore fullSubjectPhotoSpec]];	//??
//	}
	[tinyPics addObject: oneImageView]; // remember this, we need it to show the uploads
	[outerView addSubview: oneImageView ];
	[oneImageView  release];
	
	///
	for (int j=0;j<5;j++)
	{
		int xpos = 4.0f+j*63.0f;
		CGRect tf = CGRectMake(xpos, pics_row_1_start, 60.0f, 60.0f);
		oneImageView = [[UIImageView alloc] initWithFrame:tf];
		[tinyPics addObject: oneImageView]; // remember this, we need it to show the uploads
		[outerView addSubview: oneImageView ];
		[oneImageView  release];
	}
	for (int j=5;j<10;j++)
	{
		int xpos = 4.0f+(j-5)*63.0f;
		CGRect tf = CGRectMake(xpos, pics_row_2_start, 60.0f, 60.0f);
		oneImageView = [[UIImageView alloc] initWithFrame:tf];
		[tinyPics addObject: oneImageView]; // remember this, we need it to show the uploads
		[outerView addSubview: oneImageView ];
		[oneImageView  release];
	}
	
	UIView *cView = [customViews customMainPortaitControllerLoadView:self];
	[outerView addSubview: cView ];
	[cView release];
	[outerView release];	
	// prepare the navbar
	[self prepareNavBar];
	//[self subjectScreenUpdate:nil];
	// Set up the window
	TRY_RECOVERY;
}

#pragma mark UITextFieldDelegates
- (void)textFieldDidBeginEditing:(UITextField *)textField
{
	//	NSLog (@"textFieldDidBeginEditing tag %d text %@",[textField tag],[textField text]);
	disableTouchesSemaphore ++; // bump semaphore, will prevent us from recognizing touches while editing from keyboard
	validFieldEntered = NO;
}

- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField
{
	//	NSLog (@"textFieldShouldBeginEditing tag %d text %@",[textField tag],[textField text]);
	return YES;
}
- (BOOL)textFieldShouldEndEditing:(UITextField *)textField
{
	return YES;
}
- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
	[textField resignFirstResponder]; // this gets rid of the keyboard	
	return YES;
}
- (BOOL)textFieldShouldClear:(UITextField *)textField
{
	//	NSLog (@"textFieldShouldClear tag %d text %@",[textField tag],[textField text]);
	return YES;
}

- (void)textFieldDidEndEditing:(UITextField *)textField
{    
	validFieldEntered = YES;	
	disableTouchesSemaphore --;
	if (disableTouchesSemaphore <0) NSLog (@"text edit semaphore failure");
	[customViews	customMainPortaitControllerStoreTextData: textField];
	[textField resignFirstResponder]; // this gets rid of the keyboard
	//[outerViewController screenUpdate:nil]; 
}
#pragma mark -
#pragma mark UITextViewDelegate

- (void)textViewDidBeginEditing:(UITextView *)textView
{
	// provide my own Save button to dismiss the keyboard
	UIBarButtonItem* saveItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone
																			  target:self action:@selector(saveAction:)];
	// provide my own Cancel button to dismiss the keyboard
	UIBarButtonItem* cancelItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel
																				target:self action:@selector(cancelAction:)];
	self.navigationItem.rightBarButtonItem = saveItem;
	self.navigationItem.leftBarButtonItem = cancelItem;
	savingTextView = textView; // keep track of this text view
	[saveItem release];
}

- (void)saveAction:(id)sender
{

	
	//[prefs setObject:savingTextView.text forKey:@"comment"]; 
	// finish typing text/dismiss the keyboard by removing it as the first responder
	//
	[savingTextView resignFirstResponder];		
	[self prepareNavBar];
}

- (void)cancelAction:(id)sender
{
	// finish typing text/dismiss the keyboard by removing it as the first responder
	//
	[savingTextView resignFirstResponder];
	[self prepareNavBar];
}

@end

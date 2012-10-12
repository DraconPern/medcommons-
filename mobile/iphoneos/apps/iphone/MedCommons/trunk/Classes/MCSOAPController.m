//
//  MCSOAPController.m
//  MedCommons
//
//  Created by bill donner on 1/20/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//
#import "MedCommons.h"
#import "MCUploadController.h"
#import "MCSOAPController.h"
#import "DashboardPatient.h"
#import "DataManager.h"
#import	"PatientStore.h"

@implementation MCSOAPController

-(void) showActionSheet
{
	
}
-(void) done  : (NSObject *)  id
{
	[self.navigationController popViewControllerAnimated:YES];
	
}
- (void) upload:(NSObject *)  id
{
	MCUploadController *aneController = [(MCUploadController *)[MCUploadController alloc]  initWithTitle:[wrapper nameForTitle]
																							  andWithTop:@"hitting upload will store SOAP Notes the patient's medcommons account"
																						   andWithMiddle:@"<for the moment this is in a separate server>"
										 
																						  andWithSuccess:@"Silhouette.png"
																						  andWithFailure:@"mcImage-3.png"	
										 ];
	
	
	[self.navigationController pushViewController:aneController animated:YES];
	[aneController release];
}

-(void) Q : (NSObject *)  id s: (NSString *)sx
{
	NSDate *date = [NSDate date];
	NSString *s = [[NSString stringWithFormat:@"%@",date] substringToIndex:16] ;
	labelNotes.text = [NSString stringWithFormat:@" %@ %@",s, sx];
	self.navigationItem.title = [wrapper nameForTitle];
	
}


-(void) A  : (NSObject *)  id
{
	
	[savingTextView removeFromSuperview]; 
	[outerView addSubview: textFieldNotesA];
	[self Q:id s:@"Admissions Note"];
}
-(void) P : (NSObject *)  id
{
	
	[savingTextView removeFromSuperview]; 
	[outerView addSubview: textFieldNotesP];
	[self Q:id s:@"Progress Note"];
}
-(void) S : (NSObject *)  id
{
	[savingTextView removeFromSuperview]; 
	[outerView addSubview: textFieldNotesS];
	[self Q:id s:@"Sign Out Note"];
}
-(void) D   : (NSObject *)  id
{
	[savingTextView removeFromSuperview]; 
	[outerView addSubview: textFieldNotesD];
	[self Q:id s:@"Discharge Note"];
}
-(void) C  : (NSObject *)  id
{
	[savingTextView removeFromSuperview]; 
	[outerView addSubview: textFieldNotesC];
	[self Q:id s:@"Consultation Note"];
}

-(MCSOAPController *) init
{
	self = [super init];
	theurl = [[NSString stringWithFormat:@"http://%@/%@/?auth=%@",
						  [DataManager sharedInstance].ffMCappliance, 
						  [[DataManager sharedInstance].ffPatientWrapper patientID],
						  [DataManager sharedInstance].ffMCauth] retain];
	
	wrapper = [[DataManager sharedInstance] ffPatientWrapper];
	panetitle = [wrapper name] ;
	BREADCRUMBS_PUSH;

	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	if (![prefs objectForKey:@"blogEntryA"]) [prefs setValue:@"" forKey:@"blogEntryA"];	
	if (![prefs objectForKey:@"blogEntryP"]) [prefs setValue:@"" forKey:@"blogEntryP"];
	if (![prefs objectForKey:@"blogEntryS"]) [prefs setValue:@"" forKey:@"blogEntryS"];
	if (![prefs objectForKey:@"blogEntryD"]) [prefs setValue:@"" forKey:@"blogEntryD"];
	if (![prefs objectForKey:@"blogEntryC"]) [prefs setValue:@"" forKey:@"blogEntryC"];
	return self;
}
-(void) dealloc 
{
	BREADCRUMBS_POP;
	[textFieldNotesA release];
	[textFieldNotesP release];
	[textFieldNotesS release];
	[textFieldNotesD release];
	[textFieldNotesC release];
//	[savingTextView release];
	[labelNotes release];
	[theurl release];
	//[panetitle release];
	[outerView release];// causes crash on return
	[super dealloc];
}
-(UITextView *) makeFieldNote:(int)str forKey:(NSString*)key  withText:(NSString *)text  
{
	
	CGRect notesFrame =				CGRectMake(0.0f,83.0f, 320.0f, 182.0f);
	

	
	
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;

	
	UITextView *textFieldNotes = [[UITextView alloc] initWithFrame:notesFrame];
	if ([prefs objectForKey:key])
	textFieldNotes.text	= [prefs objectForKey:key]; else
	textFieldNotes.text = text ;
	textFieldNotes.textColor = [UIColor darkTextColor];
	textFieldNotes.backgroundColor = [UIColor colorWithRed:.92f green:.92f blue:.92f alpha:1.0];
	textFieldNotes.font = [UIFont systemFontOfSize:14.0];
	textFieldNotes.tag = str;		// tag this control 
	textFieldNotes.delegate = self;	// let us be the delegate so we know when the keyboard's "Done" button is pressed
	textFieldNotes.returnKeyType = UIReturnKeyDefault;
	textFieldNotes.keyboardType = UIKeyboardTypeDefault;	// use the default type input method (entire keyboard)
	textFieldNotes.scrollEnabled = YES;
	return textFieldNotes;
}
-(void) prepareNavBar
{	

	
	NSInteger count=0;
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	if ([prefs objectForKey:@"blogEntryA"]&&([[prefs objectForKey:@"blogEntryA"] length]>0)) count++;	
	
	if ([prefs objectForKey:@"blogEntryP"]&&([[prefs objectForKey:@"blogEntryP"] length]>0)) count++;	
	
	if ([prefs objectForKey:@"blogEntryS"]&&([[prefs objectForKey:@"blogEntryS"] length]>0)) count++;	
	
	if ([prefs objectForKey:@"blogEntryD"]&&([[prefs objectForKey:@"blogEntryD"] length]>0)) count++;	
	
	if ([prefs objectForKey:@"blogEntryC"]&&([[prefs objectForKey:@"blogEntryC"] length]>0)) count++;	
	
	if (count>0)
		   self.navigationItem.rightBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:@"Upload" style:UIBarButtonItemStyleBordered  target:self action:@selector(upload:)];
	
	
	self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:@"Done" style:UIBarButtonItemStyleBordered  target:self action:@selector(done:)];

	
	self.navigationItem.hidesBackButton = NO;
}

// put buttons towards the bottom
-(void) makebottombuttons
{
	// I have tried to build a loop to make this happen, but the parameterization of @selector escapes me
	
	//CGRect bframe = CGRectMake(30, 30, 320.0, 40.f);
	CGRect buttonframe = CGRectMake(8, 404, 47.0f, 47.0f); // button is 30 wide for now
	
	//CGRect uploadbuttonframe = CGRectMake(250, 300, 60.0f, 40.0f); // button is 30 wide for now
	float deltaX = 63.0f;
	
	
	
	
	UIButton *cButton;
	//UIView *bview = [[[UIView alloc] initWithFrame:	bframe ] retain];
	

	
	cButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];	
	[cButton setTitle:@"A" forState:UIControlStateNormal];
	[cButton setFrame:buttonframe];
	[cButton addTarget:self action:@selector(A:) forControlEvents:UIControlEventTouchUpInside];
	[outerView addSubview:cButton];
	
	buttonframe.origin.x += deltaX; 
	
	
	cButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];	
	[cButton setTitle:@"P" forState:UIControlStateNormal];
	[cButton setFrame:buttonframe];
	[cButton addTarget:self action:@selector(P:) forControlEvents:UIControlEventTouchUpInside];
	[outerView addSubview:cButton];

	buttonframe.origin.x += deltaX; 
	
	
	cButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];	
	[cButton setTitle:@"S" forState:UIControlStateNormal];
	[cButton setFrame:buttonframe];
	[cButton addTarget:self action:@selector(S:) forControlEvents:UIControlEventTouchUpInside];
	[outerView addSubview:cButton];

	buttonframe.origin.x += deltaX; 
	
	
	
	cButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];	
	[cButton setTitle:@"D" forState:UIControlStateNormal];
	[cButton setFrame:buttonframe];
	[cButton addTarget:self action:@selector(D:) forControlEvents:UIControlEventTouchUpInside];
[outerView addSubview:cButton];

	buttonframe.origin.x += deltaX; 
	
	cButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];	
	[cButton setTitle:@"C" forState:UIControlStateNormal];
	[cButton setFrame:buttonframe];
	[cButton addTarget:self action:@selector(C:) forControlEvents:UIControlEventTouchUpInside];
	[outerView addSubview:cButton];
	buttonframe.origin.x += deltaX; 
	
	
}	


- (void) loadView
{	
	
	
	

	
	CGRect labelnotesFrame =			CGRectMake(0,45, 320.0, 38.f);

	
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  

	outerView = [[UIView alloc] initWithFrame:appFrame];  
	outerView.backgroundColor = [UIColor colorWithRed:.96f green:.96f blue:.96f alpha:1.0]; 
	
	labelNotes = [[UILabel alloc] initWithFrame:labelnotesFrame] ;
	labelNotes.textColor = [UIColor grayColor];
	
	labelNotes.textAlignment = UITextAlignmentLeft;
	labelNotes.font = [UIFont fontWithName:@"Arial" size:14];
	labelNotes.backgroundColor = [UIColor whiteColor];
	labelNotes.text = @"<SOAP>";
	[outerView addSubview:labelNotes];

	
		[self makebottombuttons];

	textFieldNotesA = [self makeFieldNote:111 forKey:@"blogEntryA" withText:@"Brought to you by the letter A"];
	textFieldNotesP = [self makeFieldNote:112 forKey:@"blogEntryP" withText:@"Brought to you by the letter P"];
	textFieldNotesS = [self makeFieldNote:113 forKey:@"blogEntryS" withText:@"Brought to you by the letter S"];
	textFieldNotesD = [self makeFieldNote:114 forKey:@"blogEntryD" withText:@"Brought to you by the letter D"];
	
	textFieldNotesC = [self makeFieldNote:115 forKey:@"blogEntryC" withText:@"Brought to you by the letter C"];
	


	// start us out on Soap controller
	[outerView addSubview: textFieldNotesP];
	[self P:NULL];
	[self prepareNavBar];

	self.view = outerView;  
	TRY_RECOVERY;

}

#pragma mark -
#pragma mark UITextViewDelegate


- (void)saveAction:(id)sender
{
	
	//
	// OK STASH THE TEXT
	//
		NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	
	if (savingTextView.tag ==111)
	[prefs setObject:savingTextView.text forKey:@"blogEntryA"]; 
	
	if (savingTextView.tag ==112)
		[prefs setObject:savingTextView.text forKey:@"blogEntryP"]; 
	
	if (savingTextView.tag ==113)
		[prefs setObject:savingTextView.text forKey:@"blogEntryS"]; 
	
	if (savingTextView.tag ==114)
		[prefs setObject:savingTextView.text forKey:@"blogEntryD"]; 
	
	
	if (savingTextView.tag ==115)
		[prefs setObject:savingTextView.text forKey:@"blogEntryC"]; 
	// finish typing text/dismiss the keyboard by removing it as the first responder
	//
	[[[DataManager sharedInstance] ffPatientStore] writePatientStore]; // take snapshot <+++++++++++++++
	[savingTextView resignFirstResponder];		
	[self prepareNavBar];
}

- (void)cancelAction:(id)sender
{
	// finish typing text/dismiss the keyboard by removing it as the first responder
	//
	savingTextView.text = @"";
	[savingTextView resignFirstResponder];
	[self prepareNavBar];
}
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
	self.navigationItem.title = [wrapper nameForTitle];
	savingTextView = textView; // keep track of this text view
	[saveItem release];
	[cancelItem release];
}

@end


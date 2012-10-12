//
//  MCCustomView.m
//  MedCommons
//
//  Created by bill donner on 11/11/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//
#import "MedCommons.h"
#import "MCCustom+View.h"
#import "PatientStore.h"
#import "DataManager.h"

#define kStatusBarHeight 20.0f
#define kTextFieldHeight 24.0f
@implementation MCCustomViews
-(NSString *) customMainPanningTitle
{
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	return	[NSString stringWithFormat:@"%@ %@",
			 [prefs objectForKey:@"firstname"],
			 [prefs objectForKey:@"lastname"]
			 ]; 
}
-(UIView *)customHistoryFullPageControllerLabelView
{
	return nil;
}
-(UIImageView *)customHistoryFullPageControllerPhotoView
{
	return nil;
}
-(NSDictionary *)customEmailVoucherBodyWithoutPIN :(NSString *)title andName:(NSString *)name andID:(NSString *)voucherid
										   andPath:(NSString *)path

{   // path is the pickupurl for now
	NSLog (@"Emailing %@",name);
	
	NSMutableDictionary *dict = [[NSMutableDictionary alloc]init];	
	[dict setObject:[NSString stringWithFormat:@"Forensic Media Set from %@",title, nil]
			 forKey: @"subject"];
	[dict setObject:[NSString stringWithFormat:@"<p>I saved a forensic media set re %@ using %@</p><p>You can view this series with</p><p> <code>Voucher ID: %@<br/></code><br/>but first please contact me for the additional PIN Code<br/>					 <a href='%@' >go there now</a></p>",
					 name,title,
					 voucherid,
					 path] forKey: @"body"];
	[dict setObject:MAIN_LOGO forKey:@"name"];
	[dict setObject:@"image/gif" forKey:@"mimetype"];
	[dict setObject:path forKey:@"path"];
	[dict setObject:[NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:MAIN_LOGO  ofType:MAIN_LOGO_TYPE]] 
			 forKey:@"picdata"];	
	return dict;	
}
-(NSDictionary *)customEmailVoucherBodyWithPIN :(NSString *)title andName:(NSString *)name andID:(NSString *)voucherid andPin:(NSString *)pin andPath:(NSString *)path

{   // path is the pickupurl for now
	NSLog (@"Emailing %@",name);
	
	NSMutableDictionary *dict = [[NSMutableDictionary alloc]init];	
	[dict setObject:[NSString stringWithFormat:@"Forensic Media Set from %@",title, nil]
			 forKey: @"subject"];
	[dict setObject:[NSString stringWithFormat:@"<p>I saved a forensic media set re %@ using %@</p><p>You can view this series with -</p><p> <code>Voucher ID: %@<br/>PIN: %@</code><br/><br/><a href='%@' >go there now</a></p>",
					 name,title,
					 voucherid,
					 pin, path] forKey: @"body"];
	[dict setObject:MAIN_LOGO forKey:@"name"];
	[dict setObject:@"image/gif" forKey:@"mimetype"];
	[dict setObject:path forKey:@"path"];
	[dict setObject:[NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:MAIN_LOGO  ofType:MAIN_LOGO_TYPE]] 
			 forKey:@"picdata"];	
	return dict;	
}
-(void) customMainFullPageScreenUpdate
{
	
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	NSString *name = [NSString stringWithFormat:@"%@ %@ %@",
					  [prefs objectForKey:@"firstname"],
					  [prefs objectForKey:@"lastname"],
					  [prefs objectForKey:@"dob"]];
	if (pageNumber==0)
		pageNumberLabel.text = [NSString stringWithFormat:@"%@ - Photo",name]; else
			pageNumberLabel.text = [NSString stringWithFormat:@"%@ - Part %d", name,pageNumber ];
}
-(UIView *)customMainFullPageLabelView
{
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  
	appFrame.origin.y = 0;//statusheight;
	UIView *view = [[UIView alloc] initWithFrame:appFrame];
	CGRect pageNumberLabelFrame = CGRectMake(10,53,300, 16);
	pageNumberLabel = [[UILabel alloc] initWithFrame:pageNumberLabelFrame] ;
	pageNumberLabel.textColor = [UIColor blackColor];		
	pageNumberLabel.textAlignment = UITextAlignmentCenter;
	pageNumberLabel.font = [UIFont fontWithName:@"Arial" size:16];
	pageNumberLabel.backgroundColor = [UIColor lightGrayColor];
	//	pageNumberLabel.text = [NSString stringWithFormat:@"%@ - Part %d", name,pageNumber ];
	[self customMainFullPageScreenUpdate];
	[view addSubview:pageNumberLabel];
	return view;
}
-(UIImageView *)customMainFullPagePhotoView
{
	
	CGRect photoframe = CGRectMake(0,42,320,320);
	return [[[UIImageView alloc] initWithFrame:photoframe] retain]; //keep this hanging around now
}

- (void) customMainLandscapeLayout
{
		NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	
	labelseries.text = 
	[NSString stringWithFormat:@"series - %@", [prefs objectForKey:@"series"]];	
	labelcomment.text = [NSString stringWithFormat:@"comment - %@", [prefs objectForKey:@"comment"]];	
	labelsender.text = [NSString stringWithFormat:@"sender - %@", [prefs objectForKey:@"sender"]];	
	labelsubjectLongName.text = [NSString stringWithFormat:@"%@ %@ %@",
								 [prefs objectForKey:@"firstname"],
								 [prefs objectForKey:@"lastname"],
								 [prefs objectForKey:@"dob"]];
}
- (UIView *) customMainLandscapeLoadView
{
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  
	appFrame.origin.y = appFrame.origin.y - kStatusBarHeight;
	appFrame.size.height = appFrame.size.height + kStatusBarHeight;
	
	UIView *view  = [[UIView alloc] initWithFrame:appFrame]; 
	
	//*******************   LAYOUT OF Landscape custom
		NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	
	CGRect labelsubjectLongNameFrame =	CGRectMake(110.0f, 24.0f, 265.0f, 17.0f);
	CGRect labelsenderFrame =	CGRectMake(110.0f, 44.0f, 265.0f, 17.0f);
	CGRect labelcommentFrame =	CGRectMake(110.0f, 64.0f, 265.0f, 17.0f);
	CGRect labelseriesFrame =	CGRectMake(110.0f, 84.0f, 265.0f, 17.0f);
	
	labelsubjectLongName = [[UILabel alloc] initWithFrame:labelsubjectLongNameFrame] ;
	labelsubjectLongName.textAlignment = UITextAlignmentLeft;
	labelsubjectLongName.font = [UIFont fontWithName:@"Arial" size:16];
	labelsubjectLongName.textColor = [UIColor whiteColor];
	labelsubjectLongName.backgroundColor = [UIColor lightGrayColor];
	labelsubjectLongName.text = [NSString stringWithFormat:@"%@ %@ %@",
								 [prefs objectForKey:@"firstname"],
								 [prefs objectForKey:@"lastname"],
								 [prefs objectForKey:@"dob"]];
	
	labelsender  = [[UILabel alloc] initWithFrame:labelsenderFrame] ;
	labelsender.textAlignment = UITextAlignmentLeft;
	labelsender.font = [UIFont fontWithName:@"Arial" size:16];
	labelsender.textColor = [UIColor whiteColor];
	labelsender.backgroundColor = [UIColor lightGrayColor];
	labelsender.text = [NSString stringWithFormat:@"sender - %@", [prefs objectForKey:@"sender"]];
	
    labelcomment  = [[UILabel alloc] initWithFrame:labelcommentFrame] ;
	labelcomment.textAlignment = UITextAlignmentLeft;
	labelcomment.font = [UIFont fontWithName:@"Arial" size:16];
	labelcomment.textColor = [UIColor whiteColor];
	labelcomment.backgroundColor = [UIColor lightGrayColor];
	labelcomment.text = [NSString stringWithFormat:@"comment - %@", [prefs objectForKey:@"comment"]];
	
	
	labelseries  = [[UILabel alloc] initWithFrame:labelseriesFrame] ;
	labelseries.textAlignment = UITextAlignmentLeft;
	labelseries.font = [UIFont fontWithName:@"Arial" size:16];
	labelseries.textColor = [UIColor whiteColor];
	labelseries.backgroundColor = [UIColor lightGrayColor];
	labelseries.text = 
	[NSString stringWithFormat:@"series - %@", [prefs objectForKey:@"series"]];
	
	
	
	[view addSubview:labelsubjectLongName];
	[view addSubview:labelsender];
	[view addSubview:labelcomment];
	[view addSubview:labelseries];
	
	return view;
}


-(void) customMainPortaitControllerStoreTextData:(UITextField *)textField
{
	//********** must match down below
	NSInteger tagin = [textField tag];
	
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	
	
	if (((NSInteger) 111) == tagin)	
		[prefs setObject:textFieldSubjectFirstName.text forKey:@"firstname"]; else
			
			if (((NSInteger) 112) == tagin)	
				[prefs setObject:textFieldSubjectLastName.text forKey:@"lastname"]; else
					
					if (((NSInteger) 113) == tagin)	
						[prefs setObject:textFieldSubjectDOB.text forKey:@"dob"]; else
							
							if (((NSInteger) 114) == tagin)	
								[prefs setObject:textFieldSender.text forKey:@"sender"]; 	else
									if (((NSInteger) 115) == tagin)	
										[prefs setObject:textFieldComment.text forKey:@"comment"]; else
											
											if (((NSInteger) 116) == tagin)	
												[prefs setObject:textFieldSeries.text forKey:@"series"];
	[[DataManager sharedInstance].ffPatientStore writePatientStore];// checkpoint
}
-(void) customMainPortraitControllerFieldReload
{
	
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	textFieldSubjectFirstName.text =	[prefs objectForKey:@"firstname"];
	textFieldSubjectLastName.text = [prefs objectForKey:@"lastname"];
	textFieldSubjectDOB.text =[prefs objectForKey:@"dob"];	
	textFieldSender.text =	[prefs objectForKey:@"sender"];
	textFieldComment.text = [prefs objectForKey:@"comment"];
	textFieldSeries.text =[prefs objectForKey:@"series"];	
}
//customMainPortaitControllerLoadView
- (UIView *) customMainPortaitControllerLoadView:(id)delegate
{
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  
	appFrame.origin.y = appFrame.origin.y - kStatusBarHeight;
	appFrame.size.height = appFrame.size.height +kStatusBarHeight;
	
	UIView *view  = [[UIView alloc] initWithFrame:appFrame]; 
	
	//*******************   LAYOUT OF Standard Components for Every App Version
	
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	CGRect labelsubjectFirstNameFrame = CGRectMake(93.0f, -28.0f+4.0f+80.0f, 35.0f, 17.0f);
	CGRect subjectFirstNameFrame =		CGRectMake(133.0f,-28.0f+4.0f+79.0f, 320.0f-2.0f*4.0f -130.0f, kTextFieldHeight);
	
	CGRect labelsubjectLastNameFrame =	CGRectMake(93.0f, -28.0f+4.0f+113.0f, 35.0f, 17.0f);
	CGRect subjectLastNameFrame =		CGRectMake(133.0f,-28.0f+4.0f+112.0f, 320.0f-2.0f*4.0f -130.0f, kTextFieldHeight);
	
	CGRect labelsubjectDOBFrame =		CGRectMake(93.0f, -28.0f+4.0f+146.0f, 35.0f, 17.0f);
	CGRect subjectDOBFrame =			CGRectMake(133.0f, -28.0f+4.0f+145.0f, 320.0f-2.0f*4.0f -130.0f, kTextFieldHeight);
	
	CGRect labelsenderFrame =			CGRectMake(93.0f, -28.0f+4.0f+179.0f, 35.0f, 17.0f);	
	CGRect senderFrame =				CGRectMake(133.0f, -28.0f+4.0f+178.0f, 320.0f-2.0f*4.0f -130.0f, kTextFieldHeight);
	
	CGRect labelcommentFrame =			CGRectMake(83.0f, -28.0f+4.0f+212.0f, 45.0f, 17.0f);
	CGRect commentFrame =				CGRectMake(133.0f,-28.0f+ 4.0f+211.0f, 320.0f-2.0f*4.0f -130.0f, kTextFieldHeight);
	
	CGRect labelseriesFrame =			CGRectMake(93.0f, -28.0f+4.0f+245.0f, 35.0f, 17.0f);
	CGRect seriesFrame =				CGRectMake(133.0f,-28.0f+ 4.0f+244.0f, 320.0f-2.0f*4.0f -130.0f, kTextFieldHeight);
	
	
	labelSubjectFirstName = [[UILabel alloc] initWithFrame:labelsubjectFirstNameFrame] ;
	labelSubjectFirstName.textColor = [UIColor grayColor];
	labelSubjectFirstName.textAlignment = UITextAlignmentRight;
	labelSubjectFirstName.font = [UIFont fontWithName:@"Arial" size:10];
	labelSubjectFirstName.backgroundColor = [UIColor whiteColor];
	//labelSubjectFirstName.editable =YES;
	labelSubjectFirstName.text = @"First";
	[view  addSubview:labelSubjectFirstName];
	[labelSubjectFirstName release]; 
	
	
	textFieldSubjectFirstName = [[UITextField alloc] initWithFrame:subjectFirstNameFrame];
	
	textFieldSubjectFirstName.borderStyle = UITextBorderStyleRoundedRect;
	textFieldSubjectFirstName.textColor = [UIColor darkTextColor];
	textFieldSubjectFirstName.font = [UIFont systemFontOfSize:14.0];	
	
	textFieldSubjectFirstName.placeholder = @"<enter given name>";
	textFieldSubjectFirstName.backgroundColor = [UIColor groupTableViewBackgroundColor];
	textFieldSubjectFirstName.autocorrectionType = UITextAutocorrectionTypeNo;	// no auto correction support
	textFieldSubjectFirstName.keyboardType = UIKeyboardTypeDefault;	// use the default type input method (entire keyboard)
	textFieldSubjectFirstName.returnKeyType = UIReturnKeyDone;
	textFieldSubjectFirstName.clearButtonMode = UITextFieldViewModeWhileEditing;	// has a clear 'x' button to the right
	
	//textFieldSubjectFirstName.clearsOnBeginEditing = YES;	// has a clear 'x' button to the right
	textFieldSubjectFirstName.enabled = YES;
	textFieldSubjectFirstName.tag = 111;		// tag this control so we can remove it later for recycled cells
	textFieldSubjectFirstName.delegate = delegate;	// let us be the delegate so we know when the keyboard's "Done" button is pressed
	[view addSubview: textFieldSubjectFirstName];
	[textFieldSubjectFirstName release];
	
	labelSubjectLastName = [[UILabel alloc] initWithFrame:labelsubjectLastNameFrame] ;
	labelSubjectLastName.textColor = [UIColor grayColor];
	labelSubjectLastName.textAlignment = UITextAlignmentRight;
	labelSubjectLastName.font = [UIFont fontWithName:@"Arial" size:10];
	labelSubjectLastName.backgroundColor = [UIColor whiteColor];
	//labelSubjectLastName.editable = NO;
	labelSubjectLastName.text = @"Last";
	[view addSubview:labelSubjectLastName];
	[labelSubjectLastName release]; 
	
	textFieldSubjectLastName = [[UITextField alloc] initWithFrame:subjectLastNameFrame];
	
	textFieldSubjectLastName.borderStyle = UITextBorderStyleRoundedRect;
	textFieldSubjectLastName.textColor = [UIColor darkTextColor];
	textFieldSubjectLastName.font = [UIFont systemFontOfSize:14.0];
	textFieldSubjectLastName.placeholder = @"<enter family name>";
	textFieldSubjectLastName.backgroundColor = [UIColor groupTableViewBackgroundColor];
	textFieldSubjectLastName.autocorrectionType = UITextAutocorrectionTypeNo;	// no auto correction support
	textFieldSubjectLastName.keyboardType = UIKeyboardTypeDefault;	// use the default type input method (entire keyboard)
	textFieldSubjectLastName.returnKeyType = UIReturnKeyDone;
	textFieldSubjectLastName.clearButtonMode = UITextFieldViewModeWhileEditing;	// has a clear 'x' button to the right
	textFieldSubjectLastName.enabled = YES;
	textFieldSubjectLastName.tag = 112;		// tag this control so we can remove it later for recycled cells
	textFieldSubjectLastName.delegate = delegate;	// let us be the delegate so we know when the keyboard's "Done" button is pressed
	[view addSubview: textFieldSubjectLastName];
	[textFieldSubjectLastName release];
	
	labelSubjectDOB = [[UILabel alloc] initWithFrame:labelsubjectDOBFrame] ;
	labelSubjectDOB.textColor = [UIColor grayColor];
	
	labelSubjectDOB.textAlignment = UITextAlignmentRight;
	labelSubjectDOB.font = [UIFont fontWithName:@"Arial" size:10];
	labelSubjectDOB.backgroundColor = [UIColor whiteColor];
	//labelSubjectDOB.editable = NO;
	labelSubjectDOB.text = @"DOB";
	[view addSubview:labelSubjectDOB];
	[labelSubjectDOB	 release]; 
	
	
	textFieldSubjectDOB = [[UITextField alloc] initWithFrame:subjectDOBFrame];
	
	textFieldSubjectDOB.borderStyle = UITextBorderStyleRoundedRect;
	textFieldSubjectDOB.textColor = [UIColor darkTextColor];
	textFieldSubjectDOB.font = [UIFont systemFontOfSize:14.0];
	textFieldSubjectDOB.placeholder = @"<mm/dd/yy>";
	textFieldSubjectDOB.backgroundColor = [UIColor groupTableViewBackgroundColor];
	textFieldSubjectDOB.autocorrectionType = UITextAutocorrectionTypeNo;	// no auto correction support
	textFieldSubjectDOB.keyboardType = UIKeyboardTypeDefault;	// use the default type input method (entire keyboard)
	textFieldSubjectDOB.returnKeyType = UIReturnKeyDone;
	textFieldSubjectDOB.clearButtonMode = UITextFieldViewModeWhileEditing;	// has a clear 'x' button to the right
	textFieldSubjectDOB.enabled = YES;
	textFieldSubjectDOB.tag = 113;		// tag this control so we can remove it later for recycled cells
	textFieldSubjectDOB.delegate = delegate;	// let us be the delegate so we know when the keyboard's "Done" button is pressed
	[view addSubview: textFieldSubjectDOB];
	[textFieldSubjectDOB release];
	
	labelSender = [[UILabel alloc] initWithFrame:labelsenderFrame] ;
	labelSender.textColor = [UIColor grayColor];
	
	labelSender.textAlignment = UITextAlignmentRight;
	
	labelSender.font = [UIFont fontWithName:@"Arial" size:10];
	labelSender.backgroundColor = [UIColor whiteColor];
	//labelSender.editable = NO;
	labelSender.text = @"Sender";
	[view addSubview:labelSender];
	[labelSender release];
	
	
	textFieldSender = [[UITextField alloc] initWithFrame:senderFrame];
	textFieldSender.text = [prefs objectForKey:@"sender"];
	if ([@"" isEqualToString:textFieldSender.text])	
		textFieldSender.text = [[UIDevice currentDevice] name];//[prefs objectForKey:@"sender"];
	
	textFieldSender.borderStyle = UITextBorderStyleRoundedRect;
	textFieldSender.textColor = [UIColor darkTextColor];
	textFieldSender.font = [UIFont systemFontOfSize:14.0];
	textFieldSender.placeholder = @"<eg Dr Jones>";
	textFieldSender.backgroundColor = [UIColor groupTableViewBackgroundColor];
	textFieldSender.autocorrectionType = UITextAutocorrectionTypeNo;	// no auto correction support
	textFieldSender.keyboardType = UIKeyboardTypeDefault;	// use the default type input method (entire keyboard)
	textFieldSender.returnKeyType = UIReturnKeyDone;
	textFieldSender.clearButtonMode = UITextFieldViewModeWhileEditing;	// has a clear 'x' button to the right
	textFieldSender.enabled = YES;
	textFieldSender.tag = 114;		// tag this control so we can remove it later for recycled cells
	textFieldSender.delegate = delegate;	// let us be the delegate so we know when the keyboard's "Done" button is pressed
	[view addSubview: textFieldSender];
	[textFieldSender release];
	
	labelComment = [[UILabel alloc] initWithFrame:labelcommentFrame] ;
	labelComment.textColor = [UIColor grayColor];
	
	labelComment.textAlignment = UITextAlignmentRight;
	labelComment.font = [UIFont fontWithName:@"Arial" size:10];
	labelComment.backgroundColor = [UIColor whiteColor];
	labelComment.text = @"Comment";
	[view addSubview:labelComment];
	[labelComment release]; 
	
	textFieldComment = [[UITextField alloc] initWithFrame:commentFrame];
	textFieldComment.text = [prefs objectForKey:@"comment"];
	if ([@"" isEqualToString:textFieldComment.text])	
	{
		
		NSString *myphone = [[NSUserDefaults standardUserDefaults] objectForKey:@"SBFormattedPhoneNumber"];
		if (myphone==nil) myphone =[NSString stringWithFormat:@" - on the simulator"];			
		textFieldComment.text = [NSString stringWithFormat:@"call me - %@", myphone];
	}
	textFieldComment.borderStyle = UITextBorderStyleRoundedRect;
	textFieldComment.textColor = [UIColor darkTextColor];
	textFieldComment.font = [UIFont systemFontOfSize:14.0];
	textFieldComment.placeholder = @"<shoot subject photo>";
	textFieldComment.backgroundColor = [UIColor groupTableViewBackgroundColor];
	textFieldComment.autocorrectionType = UITextAutocorrectionTypeNo;	// no auto correction support
	textFieldComment.keyboardType = UIKeyboardTypeDefault;	// use the default type input method (entire keyboard)
	textFieldComment.returnKeyType = UIReturnKeyDone;
	textFieldComment.clearButtonMode = UITextFieldViewModeWhileEditing;	// has a clear 'x' button to the right
	textFieldComment.enabled = YES;
	textFieldComment.tag = 115;		// tag this control so we can remove it later for recycled cells
	textFieldComment.delegate = delegate;	// let us be the delegate so we know when the keyboard's "Done" button is pressed
	[view addSubview: textFieldComment];
	[textFieldComment release];
	
	labelSeries = [[UILabel alloc] initWithFrame:labelseriesFrame] ;
	labelSeries.textColor = [UIColor grayColor];
	labelSeries.textAlignment = UITextAlignmentRight;
	labelSeries.font = [UIFont fontWithName:@"Arial" size:10];
	labelSeries.backgroundColor = [UIColor whiteColor];
	//labelSeries.editable = NO;
	labelSeries.text = @"Series";
	[view addSubview:labelSeries];
	[labelSeries release];
	
	textFieldSeries = [[UITextField alloc] initWithFrame:seriesFrame];
	textFieldSeries.text = 	[prefs objectForKey:@"series"];
	//if ([@"" isEqualToString:textFieldSeries.text])	
	//	textFieldSeries.text = [NSString stringWithFormat:@"%d parts", [patientStore countPartPics]];
	
	textFieldSeries.borderStyle = UITextBorderStyleRoundedRect;
	textFieldSeries.textColor = [UIColor darkTextColor];
	textFieldSeries.font = [UIFont systemFontOfSize:14.0];
	textFieldSeries.placeholder = @"<shoot subject photo>";
	textFieldSeries.backgroundColor = [UIColor groupTableViewBackgroundColor];
	textFieldSeries.autocorrectionType = UITextAutocorrectionTypeNo;	// no auto correction support
	textFieldSeries.keyboardType = UIKeyboardTypeDefault;	// use the default type input method (entire keyboard)
	textFieldSeries.returnKeyType = UIReturnKeyDone;
	textFieldSeries.clearButtonMode = UITextFieldViewModeWhileEditing;	// has a clear 'x' button to the right
	textFieldSeries.enabled = YES;
	textFieldSeries.tag = 116;		// tag this control so we can remove it later for recycled cells
	textFieldSeries.delegate = delegate;	// let us be the delegate so we know when the keyboard's "Done" button is pressed
	[view addSubview: textFieldSeries];
	[textFieldSeries release];
	[self customMainPortraitControllerFieldReload];
	return view;
}
- (NSString *)customMainViewUploadMetaString: (NSTimeInterval ) today
{
	NSString *unique = [[UIDevice currentDevice] uniqueIdentifier]; //get the iphones unique identifier
	
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	NSString *request = [NSString stringWithFormat:@"&uid=%@&fn=%@&ln=%@&dob=%@&sender=%@&comment=%@&series=%@&iphone_time=%f&mc=%@",	unique,
						 [prefs objectForKey:@"firstname"],[prefs objectForKey:@"lastname"], [prefs objectForKey:@"dob"],
						 [prefs objectForKey:@"sender"],[prefs objectForKey:@"comment"],[prefs objectForKey:@"series"],today,
						 [[DataManager sharedInstance] ffMCmcid]]; //<========
	
	return request;
	
}
#pragma mark needs adjustment


-(void) customMainViewConfirmUpload:	(UIAlertView *) baseAlert  delegate:(id)delegate
{
	// we are doing the messaging and testing here, but the action continues back in the delgate which is always the guy who called us
	
	NSString *message;
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	
	if (([prefs objectForKey:@"firstname"]==@"") && (@""==[prefs objectForKey:@"lastname"]) ) 
	{
		message = @"Please Enter the Subject Name"; //[self promptForName];
		[baseAlert initWithTitle:@"You can't upload without identifying the subject" 
						 message:message
						delegate:delegate cancelButtonTitle: @"OK"
			   otherButtonTitles:nil];
	}
	else
		if ((![[[DataManager sharedInstance] ffPatientStore] haveSubjectPhoto] ) &&([[[DataManager sharedInstance] ffPatientStore] countPartPics]==0))
		{
			message = @"Please Shoot a Photo"; //[self promptForName];
			[baseAlert initWithTitle:@"You can't upload without any photos or Videos" 
							 message:message
							delegate:delegate cancelButtonTitle: @"OK"
				   otherButtonTitles:nil];
		}
		else
		{
			if ([prefs objectForKey:@"dob"]==@"")  
				message=@"Preferably you should enter Date of Birth"; 
			else
				if (![[[DataManager sharedInstance] ffPatientStore] havePhotoAtIndex:0])
					message=@"You only have a subject photo"; 
			    else  message= @"Your photo series is complete" ;
			
			[baseAlert initWithTitle:@"Do you really want to upload?" 
							 message:message
							delegate:delegate cancelButtonTitle: @"Cancel"
				   otherButtonTitles:@"Upload Now",nil];
			
		}
	
	
}

-(void)customMainViewResetPrefs

	{	
		NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	textFieldSubjectFirstName.text =textFieldSubjectLastName.text =textFieldSubjectDOB.text =textFieldSender.text = textFieldComment.text =textFieldSeries.text =@"";
	
	// saving an NSString
	[prefs setObject: @"" forKey:@"firstname"];	
	[prefs setObject: @"" forKey:@"lastname"];	
	[prefs setObject: @"" forKey:@"dob"];	
	[prefs setObject: @"" forKey:@"sender"];	
	[prefs setObject: @"" forKey:@"comment"];	
	[prefs setObject: @"" forKey:@"series"];
	[prefs setObject: @"" forKey:@"working-mcid"];
	
									  
	
}

@end

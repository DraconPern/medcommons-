//
//  NewSetListView.m
//  MusicStand
//
//  Created by bill donner on 11/10/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "NewSetListView.h"
#import "DataStore.h"


@implementation NewSetListView

-(void) signalError:(NSString *)errorstring;
{
	self->errorLabelText.hidden = NO;
	self->errorLabelText.text = errorstring;
	self->setListNameField.text = @"";
}
-(void) clear;
{
	self->errorLabelText.hidden = YES;
	self->setListNameField.text = @"";
}
-(void) reload ;
{
	[self->setListPickerView reloadAllComponents];
}
-(NSString *) allocNewSetListNameFieldFromText;
{
	return [[NSString alloc  ] initWithString:self->setListNameField.text];
}
-(void) dealloc
{
	[errorLabelText release];
	[setListNameField release];
	[setListPickerView release];
	[super dealloc];
}

-(id) initWithDelegateController: (CreateNewSetListController *) dg;
{
	self = [super init];
	if (self)
	{
		
	    self->delegateController = dg;
		// this came from a nib via nib2objc utility
		// the bits that need to be messed with from outside have been promoted to @properties and are not released until dealloc
		


		// start of nib2objc gen code
//		UILabel *label9 = [[UILabel alloc] initWithFrame:CGRectMake(20.0f, 38.0f, 740.0f, 21.0f)];
//		label9.frame = CGRectMake(20.0f, 38.0f, 740.0f, 21.0f);
//		label9.adjustsFontSizeToFitWidth = YES;
//		label9.alpha = 1.000f;
//		label9.autoresizesSubviews = YES;
//		label9.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
//		label9.backgroundColor = [UIColor colorWithWhite:0.000f alpha:0.000f];
//		label9.baselineAdjustment = UIBaselineAdjustmentAlignCenters;
//		label9.clearsContextBeforeDrawing = YES;
//		label9.clipsToBounds = YES;
//		label9.contentMode = UIViewContentModeLeft;
//		label9.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
//		label9.enabled = YES;
//		label9.font = [UIFont fontWithName:@"Helvetica" size:17.000f];
//		label9.hidden = NO;
//		label9.highlightedTextColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
//		label9.lineBreakMode = UILineBreakModeTailTruncation;
//		label9.minimumFontSize = 10.000f;
//		label9.multipleTouchEnabled = NO;
//		label9.numberOfLines = 1;
//		label9.opaque = NO;
//		label9.shadowOffset = CGSizeMake(0.0f, -1.0f);
//		label9.tag = 0;
//		label9.text = @"Enter a unique name of your list. ";
//		label9.textAlignment = UITextAlignmentLeft;
//		label9.textColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
//		label9.userInteractionEnabled = NO;
		
		UIButton *button25 = [UIButton buttonWithType:UIButtonTypeRoundedRect];
		button25.frame = CGRectMake(133.0f, 262.0f, 212.0f, 37.0f);
		button25.adjustsImageWhenDisabled = YES;
		button25.adjustsImageWhenHighlighted = YES;
		button25.alpha = 1.000f;
		button25.autoresizesSubviews = YES;
		button25.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
		button25.clearsContextBeforeDrawing = YES;
		button25.clipsToBounds = NO;
		button25.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
		button25.contentMode = UIViewContentModeScaleToFill;
		button25.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
		button25.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
		button25.enabled = YES;
		button25.hidden = NO;
		button25.highlighted = NO;
		button25.multipleTouchEnabled = NO;
		button25.opaque = NO;
		button25.reversesTitleShadowWhenHighlighted = NO;
		button25.selected = NO;
		button25.showsTouchWhenHighlighted = NO;
		button25.tag = 0;
		button25.titleLabel.font = [UIFont fontWithName:@"Helvetica-Bold" size:15.000f];
		button25.titleLabel.lineBreakMode = UILineBreakModeMiddleTruncation;
		button25.titleLabel.shadowOffset = CGSizeMake(0.0f, 0.0f);
		button25.userInteractionEnabled = YES;
		[button25 setTitle:@"+ Add New List" forState:UIControlStateNormal];
		[button25 setTitleColor:[UIColor colorWithRed:0.196f green:0.310f blue:0.522f alpha:1.000f] forState:UIControlStateNormal];
		[button25 setTitleColor:[UIColor colorWithWhite:1.000f alpha:1.000f] forState:UIControlStateHighlighted];
		[button25 setTitleShadowColor:[UIColor colorWithWhite:0.500f alpha:1.000f] forState:UIControlStateNormal];
		
		UILabel *label21 = [[UILabel alloc] initWithFrame:CGRectMake(20.0f, 50.0f, 400.0f, 21.0f)];
		label21.frame = CGRectMake(20.0f, 40.0f, 400.f, 21.0f);
		label21.adjustsFontSizeToFitWidth = YES;
		label21.alpha = 1.000f;
		label21.autoresizesSubviews = YES;
		label21.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
		label21.backgroundColor = [UIColor colorWithWhite:0.000f alpha:0.000f];
		label21.baselineAdjustment = UIBaselineAdjustmentAlignCenters;
		label21.clearsContextBeforeDrawing = YES;
		label21.clipsToBounds = YES;
		label21.contentMode = UIViewContentModeLeft;
		label21.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
		label21.enabled = YES;
		label21.font = [UIFont fontWithName:@"Helvetica" size:17.000f];
		label21.hidden = YES;
		label21.highlightedTextColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
		label21.lineBreakMode = UILineBreakModeTailTruncation;
		label21.minimumFontSize = 10.000f;
		label21.multipleTouchEnabled = NO;
		label21.numberOfLines = 1;
		label21.opaque = NO;
		label21.shadowOffset = CGSizeMake(0.0f, -1.0f);
		label21.tag = 0;
		label21.text = @"A hidden line for error messages goes here";
		label21.textAlignment = UITextAlignmentLeft;
		label21.textColor = [UIColor colorWithRed:0.886f green:0.107f blue:0.076f alpha:1.000f];
		label21.userInteractionEnabled = NO;
		
		UIView *view2 = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 768.0f, 960.0f)];
		view2.frame = CGRectMake(0.0f, 0.0f, 768.0f, 960.0f);
		view2.alpha = 1.000f;
		view2.autoresizesSubviews = YES;
		view2.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
		view2.backgroundColor = [UIColor colorWithRed:0.000f green:0.000f blue:0.000f alpha:1.000f];
		view2.clearsContextBeforeDrawing = NO;
		view2.clipsToBounds = NO;
		view2.contentMode = UIViewContentModeScaleToFill;
		view2.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
		view2.hidden = NO;
		view2.multipleTouchEnabled = NO;
		view2.opaque = YES;
		view2.tag = 0;
		view2.userInteractionEnabled = YES;
		
		UILabel *label26 = [[UILabel alloc] initWithFrame:CGRectMake(20.0f, 210.0f, 256.0f, 21.0f)];
		label26.frame = CGRectMake(20.0f, 210.0f, 256.0f, 21.0f);
		label26.adjustsFontSizeToFitWidth = YES;
		label26.alpha = 1.000f;
		label26.autoresizesSubviews = YES;
		label26.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
		label26.backgroundColor = [UIColor colorWithWhite:0.000f alpha:0.000f];
		label26.baselineAdjustment = UIBaselineAdjustmentAlignCenters;
		label26.clearsContextBeforeDrawing = YES;
		label26.clipsToBounds = YES;
		label26.contentMode = UIViewContentModeLeft;
		label26.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
		label26.enabled = YES;
		label26.font = [UIFont fontWithName:@"Helvetica" size:17.000f];
		label26.hidden = NO;
		label26.highlightedTextColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
		label26.lineBreakMode = UILineBreakModeTailTruncation;
		label26.minimumFontSize = 10.000f;
		label26.multipleTouchEnabled = NO;
		label26.numberOfLines = 1;
		label26.opaque = NO;
		label26.shadowOffset = CGSizeMake(0.0f, -1.0f);
		label26.tag = 0;
		label26.text = @"When you are ready click to add:";
		label26.textAlignment = UITextAlignmentLeft;
		label26.textColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
		label26.userInteractionEnabled = NO;
		
		UILabel *label11 = [[UILabel alloc] initWithFrame:CGRectMake(20.0f, 369.0f, 300.0f, 27.0f)];

		label11.adjustsFontSizeToFitWidth = YES;
		label11.alpha = 1.000f;
		label11.autoresizesSubviews = YES;
		label11.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
		label11.backgroundColor = [UIColor colorWithWhite:0.000f alpha:0.000f];
		label11.baselineAdjustment = UIBaselineAdjustmentAlignCenters;
		label11.clearsContextBeforeDrawing = YES;
		label11.clipsToBounds = YES;
		label11.contentMode = UIViewContentModeLeft;
		label11.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
		label11.enabled = YES;
		label11.font = [UIFont fontWithName:@"Helvetica" size:14.000f];
		label11.hidden = NO;
		label11.highlightedTextColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
		label11.lineBreakMode = UILineBreakModeTailTruncation;
		label11.minimumFontSize = 10.000f;
		label11.multipleTouchEnabled = NO;
		label11.numberOfLines = 1;
		label11.opaque = NO;
		label11.shadowOffset = CGSizeMake(0.0f, -1.0f);
		label11.tag = 0;
		label11.text = @"Your current lists:";
		label11.textAlignment = UITextAlignmentLeft;
		label11.textColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
		label11.userInteractionEnabled = NO;
		
		UIPickerView *pickerview10 = [[UIPickerView alloc] initWithFrame:CGRectMake(0.0f, 424.0f, 768.0f, 216.0f)];
		pickerview10.frame = CGRectMake(0.0f, 424.0f, 768.0f, 216.0f);
		pickerview10.alpha = 1.000f;
		pickerview10.autoresizesSubviews = YES;
		pickerview10.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleBottomMargin;
		pickerview10.clearsContextBeforeDrawing = YES;
		pickerview10.clipsToBounds = NO;
		pickerview10.contentMode = UIViewContentModeScaleToFill;
		pickerview10.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
		pickerview10.hidden = NO;
		pickerview10.multipleTouchEnabled = NO;
		pickerview10.opaque = YES;
		pickerview10.showsSelectionIndicator = YES;
		pickerview10.tag = 0;
		pickerview10.userInteractionEnabled = YES;
		
		UITextField *textfield4 = [[UITextField alloc] initWithFrame:CGRectMake(133.0f, 125.0f, 320.0f, 31.0f)];
		//textfield4.frame = CGRectMake(133.0f, 125.0f, 600.0f, 31.0f);
		textfield4.adjustsFontSizeToFitWidth = YES;
		textfield4.alpha = 1.000f;
		textfield4.autocapitalizationType = UITextAutocapitalizationTypeNone;
		textfield4.autocorrectionType = UITextAutocorrectionTypeDefault;
		textfield4.autoresizesSubviews = YES;
		textfield4.autoresizingMask =  UIViewAutoresizingFlexibleBottomMargin;
		textfield4.borderStyle = UITextBorderStyleRoundedRect;
		textfield4.clearButtonMode = UITextFieldViewModeWhileEditing;
		textfield4.clearsContextBeforeDrawing = YES;
		textfield4.clearsOnBeginEditing = NO;
		textfield4.clipsToBounds = YES;
		textfield4.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
		textfield4.contentMode = UIViewContentModeScaleToFill;
		textfield4.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
		textfield4.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
		textfield4.enabled = YES;
		textfield4.enablesReturnKeyAutomatically = NO;
		textfield4.font = [UIFont fontWithName:@"Helvetica" size:17.000f];
		textfield4.hidden = NO;
		textfield4.highlighted = NO;
		textfield4.keyboardAppearance = UIKeyboardAppearanceDefault;
		textfield4.keyboardType = UIKeyboardTypeDefault;
		textfield4.minimumFontSize = 17.000f;
		textfield4.multipleTouchEnabled = NO;
		textfield4.opaque = NO;
		textfield4.placeholder = @"enter your list name here";
		textfield4.returnKeyType = UIReturnKeyDefault;
		textfield4.secureTextEntry = NO;
		textfield4.selected = NO;
		textfield4.tag = 0;
		textfield4.text = @"";
		textfield4.textAlignment = UITextAlignmentLeft;
		textfield4.textColor = [UIColor colorWithWhite:0.000f alpha:1.000f];
		textfield4.userInteractionEnabled = YES;
		
		UIImageView *imageview27 = [[UIImageView alloc] initWithFrame:CGRectMake(290.0f, 690.0f, 200.0f, 200.0f)];
		imageview27.frame = CGRectMake(290.0f, 690.0f, 200.0f, 200.0f);
		imageview27.alpha = 0.162f;
		imageview27.autoresizesSubviews = YES;
		imageview27.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleBottomMargin;
		imageview27.clearsContextBeforeDrawing = YES;
		imageview27.clipsToBounds = NO;
		imageview27.contentMode = UIViewContentModeCenter;
		imageview27.contentStretch = CGRectFromString(@"{{0f, 0}, {0f, 0}}");
		imageview27.hidden = NO;
		imageview27.highlighted = NO;
		imageview27.image = nil;
		imageview27.multipleTouchEnabled = NO;
		imageview27.opaque = NO;
		imageview27.tag = 0;
		imageview27.userInteractionEnabled = NO;
		
		UILabel *label6 = [[UILabel alloc] initWithFrame:CGRectMake(20.0f, 125.0f, 96.0f, 26.0f)];
		label6.frame = CGRectMake(20.0f, 125.0f, 96.0f, 26.0f);
		label6.adjustsFontSizeToFitWidth = YES;
		label6.alpha = 1.000f;
		label6.autoresizesSubviews = YES;
		label6.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
		label6.backgroundColor = [UIColor colorWithWhite:0.000f alpha:0.000f];
		label6.baselineAdjustment = UIBaselineAdjustmentAlignCenters;
		label6.clearsContextBeforeDrawing = YES;
		label6.clipsToBounds = YES;
		label6.contentMode = UIViewContentModeLeft;
		label6.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
		label6.enabled = YES;
		label6.font = [UIFont fontWithName:@"Helvetica" size:21.000f];
		label6.hidden = NO;
		label6.highlightedTextColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
		label6.lineBreakMode = UILineBreakModeTailTruncation;
		label6.minimumFontSize = 10.000f;
		label6.multipleTouchEnabled = NO;
		label6.numberOfLines = 1;
		label6.opaque = NO;
		label6.shadowOffset = CGSizeMake(0.0f, -1.0f);
		label6.tag = 0;
		label6.text = @"List Name";
		label6.textAlignment = UITextAlignmentLeft;
		label6.textColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
		label6.userInteractionEnabled = NO;
		
		UILabel *label28 = [[UILabel alloc] initWithFrame:CGRectMake(20.0f, 78.0f, 740.0f, 21.0f)];
		label28.frame = CGRectMake(20.0f, 78.0f, 740.0f, 21.0f);
		label28.adjustsFontSizeToFitWidth = YES;
		label28.alpha = 1.000f;
		label28.autoresizesSubviews = YES;
		label28.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
		label28.backgroundColor = [UIColor colorWithWhite:0.000f alpha:0.000f];
		label28.baselineAdjustment = UIBaselineAdjustmentAlignCenters;
		label28.clearsContextBeforeDrawing = YES;
		label28.clipsToBounds = YES;
		label28.contentMode = UIViewContentModeLeft;
		label28.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
		label28.enabled = YES;
		label28.font = [UIFont fontWithName:@"Helvetica" size:17.000f];
		label28.hidden = NO;
		label28.highlightedTextColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
		label28.lineBreakMode = UILineBreakModeTailTruncation;
		label28.minimumFontSize = 10.000f;
		label28.multipleTouchEnabled = NO;
		label28.numberOfLines = 1;
		label28.opaque = NO;
		label28.shadowOffset = CGSizeMake(0.0f, -1.0f);
		label28.tag = 0;
		label28.text = @"Enter a unique name for your list, preferrably with no spaces";
		label28.textAlignment = UITextAlignmentLeft;
		label28.textColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
		label28.userInteractionEnabled = NO;
		
		UIButton *button20 = [UIButton buttonWithType:UIButtonTypeRoundedRect];
		button20.frame = CGRectMake(653.0f, 174.0f, 72.0f, 37.0f);
		button20.adjustsImageWhenDisabled = YES;
		button20.adjustsImageWhenHighlighted = YES;
		button20.alpha = 1.000f;
		button20.autoresizesSubviews = YES;
		button20.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
		button20.clearsContextBeforeDrawing = YES;
		button20.clipsToBounds = NO;
		button20.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
		button20.contentMode = UIViewContentModeScaleToFill;
		button20.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
		button20.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
		button20.enabled = YES;
		button20.hidden = YES;
		button20.highlighted = NO;
		button20.multipleTouchEnabled = NO;
		button20.opaque = NO;
		button20.reversesTitleShadowWhenHighlighted = NO;
		button20.selected = NO;
		button20.showsTouchWhenHighlighted = NO;
		button20.tag = 0;
		button20.titleLabel.font = [UIFont fontWithName:@"Helvetica-Bold" size:15.000f];
		button20.titleLabel.lineBreakMode = UILineBreakModeMiddleTruncation;
		button20.titleLabel.shadowOffset = CGSizeMake(0.0f, 0.0f);
		button20.userInteractionEnabled = YES;
		[button20 setTitle:@"+ Image" forState:UIControlStateNormal];
		[button20 setTitleColor:[UIColor colorWithRed:0.196f green:0.310f blue:0.522f alpha:1.000f] forState:UIControlStateNormal];
		[button20 setTitleColor:[UIColor colorWithWhite:1.000f alpha:1.000f] forState:UIControlStateHighlighted];
		[button20 setTitleShadowColor:[UIColor colorWithWhite:0.500f alpha:1.000f] forState:UIControlStateNormal];
		
	//	UILabel *label19 = [[UILabel alloc] initWithFrame:CGRectMake(20.0f, 181.0f, 740.0f, 21.0f)];
//		label19.frame = CGRectMake(20.0f, 181.0f, 740.0f, 21.0f);
//		label19.adjustsFontSizeToFitWidth = YES;
//		label19.alpha = 1.000f;
//		label19.autoresizesSubviews = YES;
//		label19.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
//		label19.backgroundColor = [UIColor colorWithWhite:0.000f alpha:0.000f];
//		label19.baselineAdjustment = UIBaselineAdjustmentAlignCenters;
//		label19.clearsContextBeforeDrawing = YES;
//		label19.clipsToBounds = YES;
//		label19.contentMode = UIViewContentModeLeft;
//		label19.contentStretch = CGRectFromString(@"{{0f, 0}, {1, 1}}");
//		label19.enabled = YES;
//		label19.font = [UIFont fontWithName:@"Helvetica" size:17.000f];
//		label19.hidden = YES;
//		label19.highlightedTextColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
//		label19.lineBreakMode = UILineBreakModeTailTruncation;
//		label19.minimumFontSize = 10.000f;
//		label19.multipleTouchEnabled = NO;
//		label19.numberOfLines = 1;
//		label19.opaque = NO;
//		label19.shadowOffset = CGSizeMake(0.0f, -1.0f);
//		label19.tag = 0;
//		label19.text = @"You can also chose an image to associate with your list or take a live shot now.";
//		label19.textAlignment = UITextAlignmentLeft;
//		label19.textColor = [UIColor colorWithWhite:1.000f alpha:1.000f];
//		label19.userInteractionEnabled = NO;
		
	//	[view2 addSubview:label19];
		[view2 addSubview:button20];
		[view2 addSubview:imageview27];
		[view2 addSubview:pickerview10];
		[view2 addSubview:label11];
		[view2 addSubview:label6];
		[view2 addSubview:textfield4];
		[view2 addSubview:label28];
		//[view2 addSubview:label9];
		[view2 addSubview:label21];
		[view2 addSubview:label26];
		[view2 addSubview:button25];
		
		
		
// end of untouched nib2objc outpu
		
		imageview27.image = [UIImage imageNamed: @"MusicStand_512x512.png"];
		
		imageview27.autoresizesSubviews = NO;

		
		[button25 addTarget:self->delegateController action:@selector(newSetListButtonTouched) forControlEvents:UIControlEventTouchUpInside];
		
		//[button20 addTarget:self->delegateController action:@selector(newImageButtonTouched) forControlEvents:UIControlEventTouchUpInside];
 
 
 pickerview10.dataSource = self->delegateController;
pickerview10.delegate = self->delegateController;
textfield4.delegate = self->delegateController; 
[self addSubview:view2];
		self->errorLabelText = [label21 retain];
		self->setListNameField = [textfield4 retain];
		self->setListPickerView = [pickerview10 retain];

		// button 20 doesnt need release
		[label11 release]; [pickerview10 release]; [label6 release]; [textfield4 release]; //[label9 release]; [label19 release];   
		[label26 release]; 
		[imageview27 release];[label28 release];

		
	}
	
	return self;
	
}

@end

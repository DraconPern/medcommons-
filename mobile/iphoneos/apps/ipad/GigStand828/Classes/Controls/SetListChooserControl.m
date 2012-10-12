//
//  SetListChooserControl.m
//  GigStand
//
//  Created by bill donner on 1/31/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "SetListChooserControl.h"
#import "DataManager.h"
#import "SetListsManager.h"


@implementation SetListChooserControl
-(void) pickerSetListChosen;
{
	
	[myPickerView removeFromSuperview];
	[myPickerView release];
}

- (void)createPicker :(CGRect) frame  forTune:(NSString *)tunex ;
{	
	
	if (pickerViewArray) [pickerViewArray release];
	if (myPickerView) 
	{ 
		[myPickerView removeFromSuperview];
		[myPickerView release];
	}
	
	
	// put this in an action sheet
	NSString *title = UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation)?
	[NSString stringWithFormat:@"\n\n\n\n\n\n\n\n\n\n\nSelect List then Tap to Add \n%@",tunex]:
	
	[NSString stringWithFormat:@"\n\n\n\n\n\n\n\n\n\n\n\n\n\nSelect List then Tap to Add \n%@",tunex];
	
	pickerActionSheet = [[UIActionSheet alloc] initWithTitle: title
													delegate:self
										   cancelButtonTitle:@"Cancel"
									  destructiveButtonTitle:nil
										   otherButtonTitles:[NSString stringWithFormat:@"Add To List",nil],nil];
	pickerActionSheet.tag = 2;
	[pickerActionSheet showInView:self->viewController.view];								  
	pickerViewArray = [SetListsManager newSetlistsScanNoRecents];
	

	
	myPickerView = [[UIPickerView alloc] initWithFrame:frame];
	myPickerView.tag = 2;
	
	
	//myPickerView.autoresizingMask = UIViewAutoresizingFlexibleWidth;
	myPickerView.showsSelectionIndicator = YES;	// note this is default to NO
	myPickerView.backgroundColor = [UIColor clearColor];
	
	// this view controller is the data source and delegate
	myPickerView.delegate = self;
	myPickerView.dataSource = self;
	[myPickerView selectRow:0 inComponent:0 animated:YES];
	self->chosenList = @"favorites";
	[pickerActionSheet addSubview:myPickerView];
	
}

-(void) dealloc {
	// release all the other objects
	
	[pickerViewArray release];
	[myPickerView release];
	
	
	//self->pickerActionSheet.delegate=nil;
	[self->pickerActionSheet dismissWithClickedButtonIndex:-1 animated:NO];
	[self->pickerActionSheet release];
	[super dealloc];
}

- (id) initWithTune:(NSString *)tunex
		   andAction: (SEL) action andController: (UIViewController *) controller;
{
	
	CGRect frame = UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation)?
	CGRectMake(0, 0, 480, 162.0f):
	CGRectMake(0, 0, 320, 216.0f);
	
	
	self=[super initWithFrame:frame];
	if (self)
	{
		self->completionAction = action;
		self->viewController = controller;
		self->tune = tunex;
		self->pickerViewArray = [SetListsManager newSetlistsScanNoRecents];
		[self createPicker:frame forTune: tunex];		
	}
	return self;
}
#pragma mark -
#pragma mark UIPickerViewDelegate

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
	self->chosenList  =  [pickerViewArray objectAtIndex:[pickerView selectedRowInComponent:0]];
}



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
	CGFloat componentWidth = pickerView.bounds.size.width; //self.parentViewController.view.bounds.size.width;
	
	return componentWidth;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component
{
	return [DataManager standardRowSize];
}


#pragma mark -
#pragma mark UIPickerViewDataSource

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
	return [pickerViewArray count];
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
	return 1;
}
#pragma mark UIActionSheet delegate

- (void)actionSheet:(UIActionSheet *)aSheet didDismissWithButtonIndex:(NSInteger)buttonIndex 
{ 

				if (aSheet.tag == 2) // this is invoked from the big plus sign
				{
					{
						if (buttonIndex >=  0)
						{
							NSDictionary *dict = [NSDictionary dictionaryWithObjectsAndKeys:
												  self->tune,@"tune",self->chosenList,@"list",nil];
					
							[self->viewController performSelector:self->completionAction withObject:dict]; 
						}
					}
				}
	
	
}
@end

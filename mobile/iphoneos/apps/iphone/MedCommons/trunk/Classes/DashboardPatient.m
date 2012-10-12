//
//  DashboardPatient.m
//  MedCommons
//
//  Created by bill donner on 12/27/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//

#import "DashboardPatient.h"
#import "MedCommons.h"




static NSString *today;
static NSString *tomorrow;
static NSString *yesterday;

static UIImage *q1Image;
static UIImage *q2Image;
static UIImage *q3Image;
static UIImage *q4Image;


@implementation DashboardPatient
@synthesize firstName;
@synthesize lastName;
@synthesize patientID;
@synthesize patientSex;
@synthesize patientDOB;
@synthesize patientDateTime;
@synthesize patientPurpose;

@synthesize patientStatus;
@synthesize photoURL;
@synthesize  image;
+ (void)initialize {
	// Unlikely to have any subclasses, but check class nevertheless
	if (self == [DashboardPatient class]) {
		today = [NSLocalizedString(@"Today", "Today") retain];
		tomorrow = [NSLocalizedString(@"Tomorrow", "Tomorrow") retain];
		yesterday = [NSLocalizedString(@"Yesterday", "Yesterday") retain];
		
		q1Image = [[UIImage imageNamed:@"12-6AM.png"] retain];
		q2Image = [[UIImage imageNamed:@"6-12AM.png"] retain];
		q3Image = [[UIImage imageNamed:@"12-6PM.png"] retain];
		q4Image = [[UIImage imageNamed:@"6-12PM.png"] retain];	
	}
}
-(NSString *) summary
{	NSString  *s = @"";
	if (![@"" isEqual: self.patientStatus]) s = [s stringByAppendingFormat:@"Status: %@\n",self.patientStatus];
	
	if (![@"" isEqual: self.patientDOB]) s = [s stringByAppendingFormat:@"DOB: %@\n",self.patientDOB];
	
	if (![@"" isEqual: self.patientSex]) s = [s stringByAppendingFormat:@"Sex: %@\n",self.patientSex];
	
	if (![@"" isEqual: self.patientPurpose]) s = [s stringByAppendingFormat:@"Purpose: %@\n",self.patientPurpose];
	
	return s;
}
-(NSString *) nameForTitle 
{	NSString  *s = @"";
	NSString *spacer;
	BOOL hasName = NO;
	
	if (![@"" isEqual: self.firstName]	)  { 
		hasName = YES; 
//		NSString * encodedString1 = (NSString *)CFURLCreateStringByAddingPercentEscapes(
//																						NULL,
//																						(CFStringRef)self.firstName,
//																						NULL,
//																						(CFStringRef)@"!*'();:@&=+$,/?%#[]",
//																						kCFStringEncodingUTF8 );
//		
//		s = encodedString1;
		s=self.firstName;
		spacer = @" "; 
	} 
	else spacer = @"";
	
	if (![@"" isEqual: self.lastName])  { 
		hasName = YES;  
	//	NSString * encodedString2 = (NSString *)CFURLCreateStringByAddingPercentEscapes(
//																						NULL,
//																						(CFStringRef)self.lastName,
//																						NULL,
//																						(CFStringRef)@"!*'();:@&=+$,/?%#[]",
//																						kCFStringEncodingUTF8 );
//		
//		s = [s stringByAppendingFormat:@"%@%@",spacer,encodedString2]; 
//		
		s = [s stringByAppendingFormat:@"%@%@",spacer,self.lastName]; 
		
	}
	if (hasName ==NO ) s=@"<unnamed>";
	
	return s;
}
-(NSString *) name 
{	NSString  *s = @"";
	NSString *spacer;
	BOOL hasName = NO;
	
	if (![@"" isEqual: self.firstName]	)  { 
		hasName = YES; 
			NSString * encodedString1 = (NSString *)CFURLCreateStringByAddingPercentEscapes(
																								NULL,
																								(CFStringRef)self.firstName,
																								NULL,
																								(CFStringRef)@"!*'();:@&=+$,/?%#[]",
																								kCFStringEncodingUTF8 );
			
		s = encodedString1;
		//s=self.firstName;
		spacer = @"%20"; 
	} 
	else spacer = @"";
	
	if (![@"" isEqual: self.lastName])  { 
		hasName = YES;  
			NSString * encodedString2 = (NSString *)CFURLCreateStringByAddingPercentEscapes(
																								NULL,
																							(CFStringRef)self.lastName,
																							NULL,
																							(CFStringRef)@"!*'();:@&=+$,/?%#[]",
																							kCFStringEncodingUTF8 );
			
				s = [s stringByAppendingFormat:@"%@%@",spacer,encodedString2]; 
			
		//s = [s stringByAppendingFormat:@"%@%@",spacer,self.lastName]; 
		
	}
	if (hasName ==NO ) s=@"<unnamed>";
	
	return s;
}
-(DashboardPatient *) initWithFirstName:(NSString *)_name lastName:(NSString *)_lastName  patientID:(NSString *) _patientID  
							 patientSex:(NSString *) _patientSex patientDOB:(NSString *) _patientDOB 
						  patientStatus:(NSString *) _patientStatus 
						patientDateTime:(NSString *) _patientDateTime 
						 patientPurpose:(NSString *) _patientPurpose 
							  photoURL:(NSString *) _photoURL 
								 danger:(BOOL) _danger
{
	//wrapper.photoURL
	if (self = [super init]) {
		firstName = [_name retain];
		lastName = [_lastName retain];		
		patientID =[ _patientID retain];		
		patientSex = [_patientSex retain];
		patientDOB = [_patientDOB retain];
		patientStatus =@"";// [_patientStatus retain];////////////////// must see why this crashes us in some cases
		
		patientDateTime = [_patientDateTime retain];
		
		patientPurpose = [_patientPurpose retain];
		photoURL = [_photoURL retain];
		danger = _danger;
		image = q2Image;
		//CONSOLE_LOG (@"dashboard entry %@ %@ %@ %@ %@ %@",firstName,lastName,patientSex,patientDOB,patientID,patientStatus);
		
	}
	return self;
}
-(BOOL) dangerdanger 
{
	return danger;
}

- (void)dealloc { 
	[ firstName release];
	[ lastName release];
	[ patientID release];
	[ patientSex release];
	[ patientDOB release];
	[ patientStatus release];
	//	[  image release];	
	[super dealloc];
}


@end

//
//  DashboardPatient.h
//  MedCommons
//
//  Created by bill donner on 12/27/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//


@interface DashboardPatient : NSObject {

	NSString *firstName;
	NSString *lastName;
	NSString *patientID;
	NSString *patientSex;
	NSString *patientDOB;
	NSString *patientStatus;	
	NSString *patientDateTime;	
	NSString *patientPurpose;
	NSString *photoURL;
	UIImage *image;
	BOOL danger;
}
@property (nonatomic, retain) NSString *firstName;
@property (nonatomic, retain) NSString *lastName;
@property (nonatomic, retain) NSString *patientID;
@property (nonatomic, retain) NSString *patientSex;
@property (nonatomic, retain) NSString *patientDOB;
@property (nonatomic, retain) NSString *patientStatus;
@property (nonatomic, retain) NSString *patientDateTime;
@property (nonatomic, retain) NSString *patientPurpose;
@property (nonatomic, retain) UIImage *image;
@property (nonatomic, retain) NSString *photoURL;

-(BOOL) dangerdanger ;
-(NSString *) summary;
-(NSString *) name ;

-(NSString *) nameForTitle ;
-(DashboardPatient *) initWithFirstName:(NSString *)_name lastName:(NSString *)_lastName  patientID:(NSString *) _patientID  
							 patientSex:(NSString *) _patientSex patientDOB:(NSString *) _patientDOB 
						  patientStatus:(NSString *) _patientStatus 
						patientDateTime:(NSString *) _patientDateTime 
						 patientPurpose:(NSString *) _patientPurpose 
							photoURL:(NSString *) _photoURL
								 danger:(BOOL) _danger;
;

@end

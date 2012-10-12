/*
 
 File: Person.h
 Abstract: The model class that stores the information about an Patient.
 

 
 Copyright (C) 2008 Apple Inc. All Rights Reserved.
 
 */

#import <Foundation/Foundation.h>
#import "mcUrlConnection.h"

@interface Person : NSObject <mcUrlConnectionDelegate> {
	
@private    

    NSString *_webLink;         // Holds the URL to the USGS web page of the Patient.
	NSString *_firstname;       // Holds the elders first name
	NSString *_lastname;        // Holds the elders last name
	NSString *_mcid;            // Holds the medcommons id
	NSString *_sex;              // 
	NSString *_sponsorfbid;      // 
	NSString *_familyfbid;       // who opened up this team and is manager
	NSString *_oauth_token;      // 
	NSString *_oauth_secret;     //
	NSString *_applianceurl;    // 
	NSString *_photoUrl;        // 
	NSString *_gw_modified_date_time; // should be nicely formatted
	NSString *_alertlevel;      // shows implicit status by coloring the display
	
	NSString *_hurl;      // shows implicit status by coloring the display
	NSString *_photoFileSpec;  // 
	NSString *_photoState;      // 
}


@property (nonatomic, retain) NSString *webLink;
@property (nonatomic, retain) NSString  *firstname;
@property (nonatomic, retain) NSString  *lastname;
@property (nonatomic, retain) NSString  *mcid;
@property (nonatomic, retain) NSString  *sex; // 
@property (nonatomic, retain) NSString  *sponsorfbid; // 
@property (nonatomic, retain) NSString  *familyfbid; // who opened up this team and is manager
@property (nonatomic, retain) NSString  *oauth_token; // 
@property (nonatomic, retain) NSString  *oauth_secret; //
@property (nonatomic, retain) NSString  *applianceurl; // 
@property (nonatomic, retain) NSString  *photoUrl; // 
@property (nonatomic, retain) NSString  *gw_modified_date_time; // should be nicely formatted
@property (nonatomic, retain) NSString  *alertlevel; // shows implicit status by coloring the display

@property (nonatomic, retain) NSString  *hurl; // shows implicit status by coloring the display
@property (nonatomic, retain) NSString *photoFileSpec; // shows implicit status by coloring the display
@property (nonatomic, retain) NSString *photoState; // shows implicit status by coloring the display
@end


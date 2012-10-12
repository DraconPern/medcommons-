//
//  GPSDevice.m
//  MedCommons
//
//  Created by bill donner on 1/30/10.
//  Copyright 2010 Apple Inc. All rights reserved.
//
#import "MedCommons.h"
#import "GPSDevice.h"
#import "DataManager.h"
#import <CoreLocation/CLLocation.h>
#import <CoreLocation/CLLocationManagerDelegate.h>
#import <CoreLocation/CLLocationManager.h>


@implementation GPSDevice
@synthesize lastMeasuredLatitude;
@synthesize lastMeasuredLongitude;
@synthesize lastMeasuredVerticalAccuracy;
@synthesize lastMeasuredHorizontalAccuracy;
@synthesize lastMeasuredTime;

@synthesize locmanager;


-(void) gpsEnable
{
	GPSDevice *pgs = [DataManager sharedInstance].ffGPSDevice;
	[pgs.locmanager startUpdatingLocation]; 	isLocating =YES;
}
-(void) gpsDisable
{
	GPSDevice *pgs = [DataManager sharedInstance].ffGPSDevice;
	[pgs.locmanager  stopUpdatingLocation]; 	isLocating =NO;
}

-(void) dealloc 
{
	// turn off the GPS
	if (isLocating == YES) [self gpsDisable];
	[locmanager release];
	[super dealloc];
}
	
-(NSDictionary *) currentLoc
{
	NSDictionary *dict = [NSDictionary dictionaryWithObjectsAndKeys:
						  lastMeasuredLatitude, @"latitude", lastMeasuredLongitude, @"longitude", nil];
	[dict retain];
	return dict;
}
- (BOOL)locationManagerShouldDisplayHeadingCalibration:(CLLocationManager *)manager
{
	return YES;
}

- (void)locationManager:(CLLocationManager *)manager didUpdateHeading:(CLHeading *)newHeading
{
	NSLog(@"location search update heading");
}


- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
	NSLog(@"location search fail...");
	isLocating = NO;
}
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation
{
	GPSDevice *gps = [[DataManager sharedInstance] ffGPSDevice];
	// just keep this going as plain strings so they are easily moved into the dictionaries
	CLLocationCoordinate2D loc = [newLocation coordinate];
	gps.lastMeasuredHorizontalAccuracy = [[NSString stringWithFormat:@"%f", [newLocation horizontalAccuracy]] retain];
	gps.lastMeasuredVerticalAccuracy = [[NSString stringWithFormat:@"%f", [newLocation verticalAccuracy]] retain];
	gps.lastMeasuredLatitude = [[NSString stringWithFormat:@"%f", loc.latitude] retain];
	gps.lastMeasuredLongitude = [[NSString stringWithFormat:@"%f", loc.longitude] retain];
	
	CONSOLE_LOG (@"New gps report %@",gps);
	
}

-(GPSDevice *) init
{
	self = [super init];
		
	//GPS get the GPS setup but not going going when this is allocated by the camera controller, not before

	locmanager = [[CLLocationManager alloc] init];
	[locmanager	setDelegate:self];
	[locmanager setDesiredAccuracy:kCLLocationAccuracyBest];
	[locmanager retain];
	
	if (!locmanager.locationServicesEnabled) NSLog(@"Location Services Not Enabled");
	
	
	lastMeasuredHorizontalAccuracy = @"<unknown>";
	lastMeasuredVerticalAccuracy = @"<unknown>";
	lastMeasuredLatitude = @"<unknown>";
	lastMeasuredLongitude = @"<unknown>";
	lastMeasuredTime = [NSDate date];
	return self;
}


@end

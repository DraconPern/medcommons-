//
//  GPSDevice.h
//  MedCommons
//
//  Created by bill donner on 1/30/10.
//  Copyright 2010 Apple Inc. All rights reserved.
//


#import <CoreLocation/CLLocationManagerDelegate.h>


@interface GPSDevice : NSObject <CLLocationManagerDelegate> {
	CLLocationManager *locmanager;
	NSString *lastMeasuredLatitude;
	NSString *lastMeasuredLongitude;
	NSString *lastMeasuredVerticalAccuracy;
	NSString *lastMeasuredHorizontalAccuracy;
	NSDate *lastMeasuredTime;
	BOOL isLocating;
}


@property (nonatomic, retain)  NSString *lastMeasuredLatitude;
@property (nonatomic, retain)  NSString *lastMeasuredLongitude;
@property (nonatomic, retain)  NSString *lastMeasuredVerticalAccuracy;
@property (nonatomic, retain)  NSString *lastMeasuredHorizontalAccuracy;
@property (nonatomic, retain)  NSDate *lastMeasuredTime;

@property (nonatomic, retain)  	CLLocationManager *locmanager;

-(void) gpsEnable;

-(void) gpsDisable;

-(NSDictionary *) currentLoc;
- (BOOL)locationManagerShouldDisplayHeadingCalibration:(CLLocationManager *)manager;
- (void)locationManager:(CLLocationManager *)manager didUpdateHeading:(CLHeading *)newHeading;
- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error;
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation;
-(GPSDevice *) init;
@end

//
//  CLLocation+MCToolbox.h
//  MCToolbox
//
//  Created by J. G. Pusey on 5/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <CoreLocation/CoreLocation.h>

@interface CLLocation (MCToolbox)

@property (nonatomic, assign, readonly) CLLocationDegrees latitude;
@property (nonatomic, assign, readonly) CLLocationDegrees longitude;

+ (CLLocation *) currentLocation;

+ (CLLocation *) currentLocationWithTimeout: (NSTimeInterval) timeout
                                   accuracy: (CLLocationAccuracy) accuracy;

+ (CLLocation *) lastKnownLocation;

+ (CLLocationManager *) sharedLocationManager;

@end

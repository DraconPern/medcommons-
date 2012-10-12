//
//  CLLocation+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 5/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CLLocation+MCToolbox.h"

#pragma mark -
#pragma mark Private Class MCLocationProvider
#pragma mark -

// ??? Break this out into separate MCBackgroundLocationManager class ???
//      or just stick with MCLocationProvider name ???
//
// Should run on background thread -- real location manager should receive
// location updates as infrequently as possible; background thread should
// silently stop if no requests for certain time interval
//
// Perhaps make use of NSOperation ...
//
// request for location should specify desired accuracy, staleness tolerance,
// and timeout values

@interface MCLocationProvider : NSObject
{
@private

    CLLocation        *lastKnownLocation_;
    CLLocationManager *locationManager_;
}

@property (nonatomic, retain, readonly) CLLocation        *lastKnownLocation;
@property (nonatomic, retain, readonly) CLLocationManager *locationManager;

+ (MCLocationProvider *) sharedInstance;

- (CLLocation *) currentLocationWithTimeout: (NSTimeInterval) timeout
                                   accuracy: (CLLocationAccuracy) accuracy;

@end

@interface MCLocationProvider () <CLLocationManagerDelegate>

@property (nonatomic, retain, readwrite) CLLocation *lastKnownLocation;

- (void) stopUpdatingLocation;

@end

@implementation MCLocationProvider

@synthesize lastKnownLocation = lastKnownLocation_;
@synthesize locationManager   = locationManager_;

#pragma mark Public Class Methods

+ (MCLocationProvider *) sharedInstance
{
    static MCLocationProvider *SharedInstance;

    if (!SharedInstance)
        SharedInstance = [[MCLocationProvider alloc] init];

    return SharedInstance;
}

#pragma mark Public Instance Methods

- (CLLocation *) currentLocationWithTimeout: (NSTimeInterval) timeout
                                   accuracy: (CLLocationAccuracy) accuracy
{
    self.locationManager.delegate = self;
    self.locationManager.desiredAccuracy = accuracy;

    [self.locationManager startUpdatingHeading];

    [self performSelector: @selector (stopUpdatingLocation)
               withObject: nil
               afterDelay: timeout];

    return self.lastKnownLocation;  // for now ...
}

#pragma mark Private Instance Methods

- (void) stopUpdatingLocation
{
    [self.locationManager stopUpdatingLocation];

    self.locationManager.delegate = nil;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->lastKnownLocation_ release];
    [self->locationManager_ release];

    [super dealloc];
}

- (id) init
{
    self = [super init];

    if (self)
        self->locationManager_ = [[CLLocationManager alloc] init];

    return self;
}

#pragma mark CLLocationManagerDelegate Methods

- (void) locationManager: (CLLocationManager *) manager
        didFailWithError: (NSError *) error
{
}

- (void) locationManager: (CLLocationManager *) manager
     didUpdateToLocation: (CLLocation *) newLocation
            fromLocation: (CLLocation *) oldLocation
{
    //
    // See if new measurement is too old:
    //
    NSTimeInterval locationAge = -[newLocation.timestamp timeIntervalSinceNow];

    if (locationAge > 5.0f) // MAX_LOCATION_AGE
        return;

    //
    // See if new measurement is valid:
    //
    if (newLocation.horizontalAccuracy < 0.0f)
        return;

    //
    // See if new measurement is more accurate than last known measurement:
    //
    if (!self.lastKnownLocation ||
        (self.lastKnownLocation.horizontalAccuracy > newLocation.horizontalAccuracy))
    {
        [self.lastKnownLocation release];

        self.lastKnownLocation = [newLocation copy];

        //
        // See if new measurement meets desired accuracy:
        //
        // IMPORTANT!!! kCLLocationAccuracyBest should not be used for
        // comparison with location coordinate or altitude accuracy because
        // it is a negative value. Instead, compare against some predetermined
        // "real" measure of acceptable accuracy, or depend on the timeout to
        // stop updating. This sample depends on the timeout.
        //
        if (newLocation.horizontalAccuracy <= manager.desiredAccuracy)
        {
            //
            // Stop location updates ASAP:
            //
            [self stopUpdatingLocation];

            //
            // Also cancel previous perform request:
            //
            [NSObject cancelPreviousPerformRequestsWithTarget: self
                                                     selector: @selector (stopUpdatingLocation)
                                                       object: nil];
        }
    }
}

@end

#pragma mark -
#pragma mark Public Class CLLocation Additions
#pragma mark -

#pragma mark Internal Constants

#define DEFAULT_ACCURACY kCLLocationAccuracyKilometer
#define DEFAULT_TIMEOUT  30.0f

@implementation CLLocation (MCToolbox)

@dynamic latitude;
@dynamic longitude;

#pragma mark Public Class Methods

+ (CLLocation *) currentLocation
{
    return [CLLocation currentLocationWithTimeout: DEFAULT_TIMEOUT
                                         accuracy: DEFAULT_ACCURACY];
}

+ (CLLocation *) currentLocationWithTimeout: (NSTimeInterval) timeout
                                   accuracy: (CLLocationAccuracy) accuracy
{
    return [[MCLocationProvider sharedInstance] currentLocationWithTimeout: timeout
                                                                  accuracy: accuracy];
}

+ (CLLocation *) lastKnownLocation
{
    return [MCLocationProvider sharedInstance].lastKnownLocation;
}

+ (CLLocationManager *) sharedLocationManager
{
    return [MCLocationProvider sharedInstance].locationManager;
}

#pragma mark Public Instane Methods

- (CLLocationDegrees) latitude
{
    return self.coordinate.latitude;
}

- (CLLocationDegrees) longitude
{
    return self.coordinate.longitude;
}

@end

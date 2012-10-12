//
//  CLLocation+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 5/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//  * Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of MedCommons, Inc. nor the names of its contributors
//    may be used to endorse or promote products derived from this software
//    without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

#pragma mark Public Instance Methods

- (CLLocationDegrees) latitude
{
    return self.coordinate.latitude;
}

- (CLLocationDegrees) longitude
{
    return self.coordinate.longitude;
}

@end

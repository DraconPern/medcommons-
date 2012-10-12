//
//  Photo.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "DictionaryAdditions.h"
#import "Photo.h"

#pragma mark -
#pragma mark Public Class Photo
#pragma mark -

#pragma mark Internal Constants

#define HORIZONTAL_ACCURACY_KEY @"HorizontalAccuracy"
#define LATITUDE_KEY            @"Latitude"
#define LONGITUDE_KEY           @"Longitude"
#define MEDIA_ATTRS_KEY         @"MediaAttrs"
#define MEDIA_PATH_KEY          @"MediaPath"
#define VERTICAL_ACCURACY_KEY   @"VerticalAccuracy"

@interface Photo ()

+ (void) addLocationAttributes: (NSMutableDictionary *) attrs;

@end

@implementation Photo

@synthesize attributes = attributes_;
@synthesize path       = path_;

#pragma mark Public Class Methods

+ (id) photoWithPath: (NSString *) path
          attributes: (NSDictionary *) attributes
{
    return [self photoWithPath: path
                    attributes: attributes
                   addLocation: YES];
}

+ (id) photoWithPath: (NSString *) path
          attributes: (NSDictionary *) attributes
         addLocation: (BOOL) addLocation
{
    return [[[self alloc] initWithPath: path
                            attributes: attributes
                           addLocation: addLocation]
            autorelease];
}

+ (id) photoWithPropertyList: (NSDictionary *) plist
{
    return [[[self alloc] initWithPropertyList: plist]
            autorelease];
}

#pragma mark Private Class Methods

+ (void) addLocationAttributes: (NSMutableDictionary *) attrs
{
    CLLocation *location = [CLLocation currentLocation];

    if (location)
    {
        [attrs setObject: [NSString stringWithFormat:
                           @"%f",
                           location.horizontalAccuracy]
                  forKey: HORIZONTAL_ACCURACY_KEY];

        [attrs setObject: [NSString stringWithFormat:
                           @"%f",
                           location.latitude]
                  forKey: LATITUDE_KEY];

        [attrs setObject: [NSString stringWithFormat:
                           @"%f",
                           location.longitude]
                  forKey: LONGITUDE_KEY];

        [attrs setObject: [NSString stringWithFormat:
                           @"%f",
                           location.verticalAccuracy]
                  forKey: VERTICAL_ACCURACY_KEY];
    }
}
#pragma mark Public Instance Methods

- (id) initWithPath: (NSString *) path
         attributes: (NSDictionary *) attributes
{
    return [self initWithPath: path
                   attributes: attributes
                  addLocation: YES];
}

- (id) initWithPath: (NSString *) path
         attributes: (NSDictionary *) attributes
        addLocation: (BOOL) addLocation
{
    self = [super init];

    if (self)
    {
        NSString *tmpPath = (path ? [path stringByTrimmingWhitespace] : @"");

        if ([tmpPath length] == 0)
            [NSException raise: NSInvalidArgumentException
                        format: @"Empty path"];

        self->path_ = [tmpPath copy];

        if (addLocation)
        {
            NSMutableDictionary *extAttrs = (attributes ?
                                             [NSMutableDictionary dictionaryWithDictionary: attributes] :
                                             [NSMutableDictionary dictionary]);

            [[self class] addLocationAttributes: extAttrs];

            self->attributes_ = [extAttrs copy];
        }
        else
            self->attributes_ = [attributes copy];
    }

    return self;
}

- (id) initWithPropertyList: (NSDictionary *) plist
{
    NSDictionary *tmpAttrs = [plist dictionaryForKey: MEDIA_ATTRS_KEY];
    NSString     *tmpPath = [plist stringForKey: MEDIA_PATH_KEY];

    if (tmpPath && tmpAttrs)
        return [self initWithPath: tmpPath
                       attributes: tmpAttrs];

    [self release];

    return nil;
}

- (NSDictionary *) propertyList
{
    NSDictionary *tmpAttrs = (self.attributes ?
                              self.attributes :
                              [NSDictionary dictionary]);

    return [NSDictionary dictionaryWithObjectsAndKeys:
            tmpAttrs,  MEDIA_ATTRS_KEY,
            self.path, MEDIA_PATH_KEY,
            nil];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->attributes_ release];
    [self->path_ release];

    [super dealloc];
}

@end

//
//  MapAnnotation.h
//  MedCommons
//
//  Created by bill donner on 10/22/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//


#import <Foundation/Foundation.h>
#import <MapKit/MKAnnotation.h>

@interface MapAnnotation : NSObject <MKAnnotation> 
{
	CLLocationCoordinate2D coordinate;
	NSString *title;
	NSString *subtitle;
}

@property (nonatomic, assign) CLLocationCoordinate2D coordinate;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *subtitle;

@end

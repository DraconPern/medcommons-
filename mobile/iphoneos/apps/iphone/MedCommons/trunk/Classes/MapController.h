//
//  MapController.h
//  MedCommons
//
//  Created by bill donner on 10/22/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//
#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@interface MapController : UIViewController <MKMapViewDelegate>{	
	NSDictionary *gpsconfig;	
	//UIView *outerView;
	MKMapView *mapView;
}
-(MapController *) initWithConfig:(NSDictionary *)config;
@end
//
//  MapController.m
//  MedCommons
//
//  Created by bill donner on 10/22/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//

#import "MedCommons.h"
#import "PatientStore.h"
#import "MapController.h"
#import "MapAnnotation.h"
#import "DataManager.h"

#define kStatusBarHeight 20.0f
@implementation MapController
-(MapController *) initWithConfig:(NSDictionary *)config
{
	self = [super init];
	gpsconfig = config;
		BREADCRUMBS_PUSH;
		
	return self;
}
-(void) dealloc 
{
	BREADCRUMBS_POP;
	[super dealloc];
}
// override to allow orientations other than the default portrait orientation
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait); // support only portrait
}
- (void)viewWillDisappear:(BOOL)animated
{	
	[self dismissModalViewControllerAnimated:YES];	
}
- (void)loadView
{
	double latitude = [[gpsconfig objectForKey:@"latitude"] doubleValue];
	double longitude = [[gpsconfig objectForKey:@"longitude"] doubleValue];
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  
	appFrame.origin.y = appFrame.origin.y  - kStatusBarHeight;
	appFrame.size.height = appFrame.size.height +kStatusBarHeight;
	mapView = [[MKMapView alloc] initWithFrame:appFrame];  
	mapView.backgroundColor = [UIColor redColor];  
	mapView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;  
	
	[mapView setMapType:MKMapTypeStandard];
	[mapView setZoomEnabled:YES];
	[mapView setScrollEnabled:YES];
	
	MKCoordinateRegion region = { {0.0, 0.0 }, { 0.0, 0.0 } };
	region.center.latitude = latitude; //41.902245099708516;
	region.center.longitude =  longitude; //12.457906007766724;
	region.span.longitudeDelta = 0.01f;
	region.span.latitudeDelta = 0.01f;	
	[mapView setRegion:region animated:YES];
	
	[mapView setDelegate:self];
	
	MapAnnotation *ann = [[MapAnnotation alloc] init];
	ann.title = @"Rome";
	ann.subtitle = @"San Peter";
	ann.coordinate = region.center;
	[mapView addAnnotation:ann];
	
	self.view = mapView;  
	[ann release];
	[mapView release];
    self.navigationItem.title = @"Series Geo Info";
}

- (MKAnnotationView *)mapView:(MKMapView *)mV viewForAnnotation:(id <MKAnnotation>)annotation
{
	MKPinAnnotationView *pinView = nil;
	if(annotation != mapView.userLocation) 
	{
		static NSString *defaultPinID = @"com.medcommons.pin";
		pinView = (MKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:defaultPinID];
		if ( pinView == nil )
			pinView = [[[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:defaultPinID] autorelease];
		
		pinView.pinColor = MKPinAnnotationColorPurple;
		pinView.canShowCallout = YES;
		pinView.animatesDrop = YES;
	}
	else
	{
		[mapView.userLocation setTitle:@"I am here"];
	}
	
    return pinView;
}
@end
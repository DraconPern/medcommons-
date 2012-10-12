//
//  MapMeViewController.m
//  MapMe
//
//  Created by jeff on 11/4/09.
//  Copyright Jeff LaMarche 2009. All rights reserved.
//

#import "MapMeViewController.h"
#import "MapLocation.h"

@implementation MapMeViewController
@synthesize mapView;
@synthesize progressBar;
@synthesize progressLabel;
@synthesize goButton;

@dynamic hidesMasterViewInLandscape;

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}
#pragma mark -
- (IBAction)findMe {
    if (!lm) {
        lm = [[CLLocationManager alloc] init];
    lm.delegate = self;
    lm.desiredAccuracy = kCLLocationAccuracyBest;
    [lm startUpdatingLocation];
    }

    progressBar.hidden = NO;
    progressBar.progress = 0.0f;
    progressLabel.text = NSLocalizedString(@"Determining Current Location", @"Determining Current Location");

    goButton.hidden = YES;
}
- (void)openCallout:(id<MKAnnotation>)annotation {
    progressBar.progress = 1.0f;
    progressLabel.text = NSLocalizedString(@"Showing Annotation",@"Showing Annotation");
    [mapView selectAnnotation:annotation animated:YES];
}
#pragma mark -
- (void)viewDidLoad {
    mapView.mapType = MKMapTypeStandard;
//    mapView.mapType = MKMapTypeSatellite;
  //  mapView.mapType = MKMapTypeHybrid;
}
- (void)viewDidUnload {
    self.mapView = nil;
    self.progressBar = nil;
    self.progressLabel = nil;
    self.goButton = nil;
}
- (void)dealloc {
    if (lm) [lm release];
    [mapView release];
    [progressBar release];
    [progressLabel release];
    [goButton release];
    [super dealloc];
}

-(void) loadView {
   progressLabel = [[UILabel alloc] initWithFrame:CGRectMake(20.0f, 425.0f, 280.0f, 21.0f)];
    progressLabel.frame = CGRectMake(20.0f, 425.0f, 280.0f, 21.0f);
    progressLabel.adjustsFontSizeToFitWidth = YES;
    progressLabel.alpha = 1.000f;
    progressLabel.autoresizesSubviews = YES;
    progressLabel.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
    progressLabel.baselineAdjustment = UIBaselineAdjustmentAlignCenters;
    progressLabel.clearsContextBeforeDrawing = YES;
    progressLabel.clipsToBounds = YES;
    progressLabel.contentMode = UIViewContentModeScaleToFill;
    progressLabel.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
    progressLabel.enabled = YES;
    progressLabel.font = [UIFont fontWithName:@"Helvetica" size:13.000f];
    progressLabel.hidden = NO;
    progressLabel.lineBreakMode = UILineBreakModeTailTruncation;
    progressLabel.minimumFontSize = 10.000f;
    progressLabel.multipleTouchEnabled = NO;
    progressLabel.numberOfLines = 1;
    progressLabel.opaque = NO;
    progressLabel.shadowOffset = CGSizeMake(0.0f, -1.0f);
    progressLabel.tag = 0;
    progressLabel.text = @"";
    progressLabel.textAlignment = UITextAlignmentCenter;
    progressLabel.textColor = [UIColor colorWithRed:0.000f green:0.000f blue:0.000f alpha:1.000f];
    progressLabel.userInteractionEnabled = NO;

     mapView = [[MKMapView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 320.0f, 380.0f)];
    mapView.frame = CGRectMake(0.0f, 0.0f, 320.0f, 380.0f);
    mapView.alpha = 1.000f;
    mapView.autoresizesSubviews = YES;
    mapView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    mapView.clearsContextBeforeDrawing = YES;
    mapView.clipsToBounds = YES;
    mapView.contentMode = UIViewContentModeCenter;
    mapView.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
    mapView.hidden = NO;
    mapView.mapType = MKMapTypeStandard;
    mapView.multipleTouchEnabled = YES;
    mapView.opaque = NO;
    mapView.scrollEnabled = YES;
    mapView.showsUserLocation = NO;
    mapView.tag = 0;
    mapView.userInteractionEnabled = YES;
    mapView.zoomEnabled = YES;


    UIView *mainView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 20.0f, 320.0f, 460.0f)];
    mainView.frame = CGRectMake(0.0f, 20.0f, 320.0f, 460.0f);
    mainView.alpha = 1.000f;
    mainView.autoresizesSubviews = YES;
    mainView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    mainView.backgroundColor = [UIColor colorWithWhite:0.750f alpha:1.000f];
    mainView.clearsContextBeforeDrawing = NO;
    mainView.clipsToBounds = NO;
    mainView.contentMode = UIViewContentModeScaleToFill;
    mainView.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
    mainView.hidden = NO;
    mainView.multipleTouchEnabled = NO;
    mainView.opaque = YES;
    mainView.tag = 0;
    mainView.userInteractionEnabled = YES;

    goButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    goButton.frame = CGRectMake(228.0f, 384.0f, 72.0f, 37.0f);
    goButton.adjustsImageWhenDisabled = YES;
    goButton.adjustsImageWhenHighlighted = YES;
    goButton.alpha = 1.000f;
    goButton.autoresizesSubviews = YES;
    goButton.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
    goButton.clearsContextBeforeDrawing = NO;
    goButton.clipsToBounds = NO;
    goButton.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    goButton.contentMode = UIViewContentModeScaleToFill;
    goButton.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
    goButton.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
    goButton.enabled = YES;
    goButton.hidden = NO;
    goButton.highlighted = NO;
    goButton.multipleTouchEnabled = NO;
    goButton.opaque = NO;
    goButton.reversesTitleShadowWhenHighlighted = NO;
    goButton.selected = NO;
    goButton.showsTouchWhenHighlighted = NO;
    goButton.tag = 0;
    goButton.titleLabel.font = [UIFont fontWithName:@"Helvetica-Bold" size:15.000f];
    goButton.titleLabel.lineBreakMode = UILineBreakModeMiddleTruncation;
    goButton.titleLabel.shadowOffset = CGSizeMake(0.0f, 0.0f);
    goButton.userInteractionEnabled = YES;
    [goButton setTitle:@"Go" forState:UIControlStateNormal];
    [goButton setTitleColor:[UIColor colorWithRed:0.196f green:0.310f blue:0.522f alpha:1.000f] forState:UIControlStateNormal];
    [goButton setTitleColor:[UIColor colorWithWhite:1.000f alpha:1.000f] forState:UIControlStateHighlighted];
    [goButton setTitleShadowColor:[UIColor colorWithWhite:0.500f alpha:1.000f] forState:UIControlStateNormal];

 progressBar = [[UIProgressView alloc] initWithProgressViewStyle:UIProgressViewStyleDefault];
    progressBar.frame = CGRectMake(20.0f, 408.0f, 280.0f, 9.0f);
    progressBar.alpha = 1.000f;
    progressBar.autoresizesSubviews = YES;
    progressBar.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
    progressBar.clearsContextBeforeDrawing = YES;
    progressBar.clipsToBounds = YES;
    progressBar.contentMode = UIViewContentModeScaleToFill;
    progressBar.contentStretch = CGRectFromString(@"{{0, 0}, {1, 1}}");
    progressBar.hidden = YES;
    progressBar.multipleTouchEnabled = YES;
    progressBar.opaque = NO;
    progressBar.progress = 0.500f;
    progressBar.progressViewStyle = UIProgressViewStyleDefault;
    progressBar.tag = 0;
    progressBar.userInteractionEnabled = YES;

    [mainView addSubview:mapView];
    [mainView addSubview:goButton];
    [mainView addSubview:progressBar];
    [mainView addSubview:progressLabel];
    self.view= mainView;

}
#pragma mark -
#pragma mark CLLocationManagerDelegate Methods
- (void)locationManager:(CLLocationManager *)manager
    didUpdateToLocation:(CLLocation *)newLocation
           fromLocation:(CLLocation *)oldLocation {

    if ([newLocation.timestamp timeIntervalSince1970] < [NSDate timeIntervalSinceReferenceDate] - 60)
        return;

    MKCoordinateRegion viewRegion = MKCoordinateRegionMakeWithDistance(newLocation.coordinate, 2000, 2000);
    MKCoordinateRegion adjustedRegion = [mapView regionThatFits:viewRegion];
    [mapView setRegion:adjustedRegion animated:YES];

    manager.delegate = nil;
    [manager stopUpdatingLocation];
    [manager autorelease];

    progressBar.progress = .25f;
    progressLabel.text = NSLocalizedString(@"Reverse Geocoding Location", @"Reverse Geocoding Location");
    if (geocoder) [geocoder release];
   geocoder = [[MKReverseGeocoder alloc] initWithCoordinate:newLocation.coordinate];
    geocoder.delegate = self;
    [geocoder start];
}
- (void)locationManager:(CLLocationManager *)manager
       didFailWithError:(NSError *)error {

    NSString *errorType = (error.code == kCLErrorDenied) ?
    NSLocalizedString(@"Access Denied", @"Access Denied") :
    NSLocalizedString(@"Unknown Error", @"Unknown Error");

    UIAlertView *alert = [[UIAlertView alloc]
                          initWithTitle:NSLocalizedString(@"Error getting Location", @"Error getting Location")
                          message:errorType
                          delegate:self
                          cancelButtonTitle:NSLocalizedString(@"Okay", @"Okay")
                          otherButtonTitles:nil];
    [alert show];
    [alert release];
    [manager release];
}
#pragma mark -
#pragma mark Alert View Delegate Methods
- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex {
    progressBar.hidden = YES;
    progressLabel.text = @"";
}
#pragma mark -
#pragma mark Reverse Geocoder Delegate Methods
- (void)reverseGeocoder:(MKReverseGeocoder *)geocoder_ didFailWithError:(NSError *)error {
    UIAlertView *alert = [[UIAlertView alloc]
                          initWithTitle:NSLocalizedString(@"Error translating coordinates into location", @"Error translating coordinates into location")
                          message:NSLocalizedString(@"Geocoder did not recognize coordinates", @"Geocoder did not recognize coordinates")
                          delegate:self
                          cancelButtonTitle:NSLocalizedString(@"Okay", @"Okay")
                          otherButtonTitles:nil];
    [alert show];
    [alert release];

    geocoder_.delegate = nil;
    [geocoder_ autorelease];
}
- (void)reverseGeocoder:(MKReverseGeocoder *)geocoder_ didFindPlacemark:(MKPlacemark *)placemark {
    progressBar.progress = 0.5f;
    progressLabel.text = NSLocalizedString(@"Location Determined", @"Location Determined");

    MapLocation *annotation = [[MapLocation alloc] init];
    annotation.streetAddress = placemark.thoroughfare;
    annotation.city = placemark.locality;
    annotation.state = placemark.administrativeArea;
    annotation.zip = placemark.postalCode;
    annotation.coordinate = geocoder_.coordinate;

    [mapView addAnnotation:annotation];


    [annotation release];

    geocoder_.delegate = nil;
    [geocoder_ autorelease];
}
#pragma mark -
#pragma mark Map View Delegate Methods
- (MKAnnotationView *) mapView:(MKMapView *)theMapView viewForAnnotation:(id <MKAnnotation>) annotation {
    static NSString *placemarkIdentifier = @"Map Location Identifier";
    if ([annotation isKindOfClass:[MapLocation class]]) {
        MKPinAnnotationView *annotationView = (MKPinAnnotationView *)[theMapView dequeueReusableAnnotationViewWithIdentifier:placemarkIdentifier];
        if (annotationView == nil)  {
            annotationView = [[[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:placemarkIdentifier] autorelease]; // added by donner
        }
        else
            annotationView.annotation = annotation;

        annotationView.enabled = YES;
        annotationView.animatesDrop = YES;
        annotationView.pinColor = MKPinAnnotationColorPurple;
        annotationView.canShowCallout = YES;
        [self performSelector:@selector(openCallout:) withObject:annotation afterDelay:0.5];

        progressBar.progress = 0.75f;
        progressLabel.text = NSLocalizedString(@"Creating Annotation",@"Creating Annotation");

        return annotationView;
    }
    return nil;
}
- (void)mapViewDidFailLoadingMap:(MKMapView *)theMapView withError:(NSError *)error {
    UIAlertView *alert = [[UIAlertView alloc]
                          initWithTitle:NSLocalizedString(@"Error loading map", @"Error loading map")
                          message:[error localizedDescription]
                          delegate:nil
                          cancelButtonTitle:NSLocalizedString(@"Okay", @"Okay")
                          otherButtonTitles:nil];
    [alert show];
    [alert release];
}
@end

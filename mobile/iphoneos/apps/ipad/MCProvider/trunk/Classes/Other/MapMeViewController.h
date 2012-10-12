#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import <CoreLocation/CoreLocation.h>

@interface MapMeViewController : UIViewController
    <CLLocationManagerDelegate, MKReverseGeocoderDelegate, MKMapViewDelegate> {
        CLLocationManager *lm;
        MKReverseGeocoder *geocoder;
}
@property (nonatomic, retain) IBOutlet MKMapView *mapView;
@property (nonatomic, retain) IBOutlet UIProgressView *progressBar;
@property (nonatomic, retain) IBOutlet UILabel *progressLabel;
@property (nonatomic, retain) IBOutlet UIButton *goButton;

@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;

- (IBAction)findMe;
@end

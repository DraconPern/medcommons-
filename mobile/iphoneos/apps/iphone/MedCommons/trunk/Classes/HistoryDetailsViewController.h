//#import <MessageUI/MessageUI.h>
@class HistoryCase,PatientStore;


@interface HistoryDetailsViewController : UIViewController 
{
	
//	UIStatusBarStyle oldStatusBarStyle;
	UILabel *tLabel;	UILabel *aLabel;UILabel *bLabel;UILabel *nLabel;	UILabel *cLabel;	UIButton *aButton ;	UIButton *bButton ;
	HistoryCase *hcase;
	PatientStore *patientStore;
	NSMutableArray *tinyPics ;
	
	NSMutableDictionary *imageCache;
	

}

-(id)  initWithCase:(HistoryCase *)_hcase ;

// alternate intializer to show details for "Last"
-(id)  init ;
@end
//
//  AllTunesViewController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//

#import <UIKit/UIKit.h>

@interface AllTunesViewController : UIViewController
{
@private
	NSString *navTitle; // the top nav title
	NSArray *allTitleNames;
	UILocalizedIndexedCollation *collation;
	NSMutableArray *allTitleNodeWrappers;
	NSMutableArray *titleNodeWrappersByLetter;
                       // has outer level for A-Z and #, each has own array of particular titles for the letter
}


@property (nonatomic, retain) NSMutableArray *allTitleNodeWrappers;

-(id) initWithArray:(NSArray *) a andTitle:(NSString *)titl;
@end

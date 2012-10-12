//
//  AllTunesViewController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//

#import <UIKit/UIKit.h>


@class MainTableView, SearchResultsTableView;

@interface SearchableTunesViewController : UIViewController<UISearchBarDelegate>
{
@private
	NSString *navTitle; // the top nav title
	NSArray *allTitleNames;
	//UILocalizedIndexedCollation *collation;
	NSMutableArray *tunesByLetter; // has outer level for A-Z and #, each has own array of particular titles for the letter
	MainTableView *tableview;
	UISearchBar *searchbarview;
    UIView *searchResultsOverlayView;
	SearchResultsTableView *searchresultstableview;
	NSArray *searchResults; // these get written into overlay table
	NSString *archive;
	//NSArray *sectionIndexTitles;
}


@property(retain) UIView *searchResultsOverlayView;

@property (nonatomic, retain) NSArray *allTitleNames; // these get written into overlay table


@property (nonatomic, retain) NSArray *searchResults; // these get written into overlay table
//@property (nonatomic, retain) UILocalizedIndexedCollation *collation;
@property (nonatomic, retain) NSMutableArray *tunesByLetter;
- (void)searchBar:(UISearchBar *)searchBar activate:(BOOL) active;

- (void)configureSections;

-(id) initWithArray:(NSArray *) a andTitle:(NSString *)title andArchive:(NSString *)archive;
@end

@interface MainTableView: UITableView <UITableViewDelegate,UITableViewDataSource>
{
	SearchableTunesViewController *thisController;
}
-(id) initWithFrame:(CGRect)oframe style:(UITableViewStyle)ostyle controller:(SearchableTunesViewController *)ocontroller;
@end

@interface SearchResultsTableView: UITableView <UITableViewDelegate,UITableViewDataSource>
{
	
	SearchableTunesViewController *thisController;
}
-(id) initWithFrame:(CGRect)oframe style:(UITableViewStyle)ostyle controller:(SearchableTunesViewController *)ocontroller;
@end

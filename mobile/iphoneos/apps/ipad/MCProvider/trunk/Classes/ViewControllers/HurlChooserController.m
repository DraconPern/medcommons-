
/*
 ipad only for now
 */

#import "AppDelegate.h"
#import "HurlChooserController.h"
#import "HurlWebViewController.h"
#import "Member.h"
#import "MemberStore.h"
#import "RecentSearchesController.h"
#import "Session.h"
#import "SessionManager.h"

@interface HurlChooserController ()

- (void) finishSearchWithString: (NSString *) searchString;

@end

@implementation HurlChooserController

@synthesize progressLabel;
@synthesize recentSearchesController;
@synthesize recentSearchesPopoverController;
@synthesize searchBar;
@synthesize toolbar;

#pragma mark -
#pragma mark Create and manage the search results controller

- (void) doSearchAndDisplay: (NSString *) searchString
{
    SessionManager *sm = self.appDelegate.sessionManager;
    Session        *session = sm.loginSession;

    if (session.memberInFocus)
        NSLog (@"Search switching from member %@",
               session.memberInFocus.identifier);

    session.memberInFocus = [Member memberWithIdentifier: searchString
                                               givenName: @"unknown"
                                              familyName: @"member"
                                                dateTime: @""];

    HurlWebViewController *hwvc = [[[HurlWebViewController alloc]
                                    initWithURL: [NSURL URLWithString: searchString]]
                                   autorelease];

    hwvc.title = session.memberInFocus.name;

    [self.navigationController pushViewController: hwvc
                                         animated: NO];
}

- (id)init
{
    // Override initWithStyle: if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
    self = [super init];
    pushIntoSurl=nil;
    return self;
}
-(id) initWithSoloURL: (NSString *) s
{
    self = [super init];
    pushIntoSurl= [s copy];
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.title = @"Search";

    self.navigationItem.hidesBackButton = NO;
    self.navigationController.toolbarHidden = YES;

    // Create and configure a search bar.
    searchBar = [[UISearchBar alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 400.0f, 0.0f)];
    searchBar.delegate = self;

    UITextView *l = [[UITextView alloc] initWithFrame:CGRectMake(10.0f, 10.0f, 300.0f, 300.0f)];
    l.text = @"enter a full health URL in the search box\ne.g. https://portal.medcommons.net/1234567890123456\n\nor paste directly from MedCommons email";
    l.font = [UIFont systemFontOfSize: 18.0f];;
    l.textColor = [UIColor lightGrayColor];
    l.backgroundColor = [UIColor clearColor];
    //l.lineBreakMode = UILineBreakModeCharacterWrap;

    [self.view addSubview:l];
    [l release];

    // Create a bar button item using the search bar as its view.
    UIBarButtonItem *searchItem = [[UIBarButtonItem alloc] initWithCustomView:searchBar];
    // Create a space item and set it and the search bar as the items for the toolbar.
    UIBarButtonItem *spaceItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:NULL];
    toolbar.items = [NSArray arrayWithObjects:spaceItem, searchItem, nil];
    [spaceItem release];
    [searchItem release];

    self.navigationItem.rightBarButtonItem = searchItem; // was toolbar; //! was searchItem;

    // Create and configure the recent searches controller.
    RecentSearchesController *aRecentsController = [[RecentSearchesController alloc] initWithStyle:UITableViewStylePlain];
    self.recentSearchesController = aRecentsController;
    recentSearchesController.delegate = self;

    // Create a navigation controller to contain the recent searches controller, and create the popover controller to contain the navigation controller.
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:recentSearchesController];

    UIPopoverController *popover = [[UIPopoverController alloc] initWithContentViewController:navigationController];
    self.recentSearchesPopoverController = popover;
    recentSearchesPopoverController.delegate = self;

    // Ensure the popover is not dismissed if the user taps in the search bar.
    popover.passthroughViews = [NSArray arrayWithObject:searchBar];

    [navigationController release];
    [aRecentsController release];
    [popover release];


    if (pushIntoSurl)
        // start right at healthurl if remotely invoked
    {
        searchBar.text = pushIntoSurl;
        [recentSearchesController addToRecentSearches:pushIntoSurl];
        [self doSearchAndDisplay:pushIntoSurl];
    }

    // otherwise, we'll start normally
}


#pragma mark -
#pragma mark Search results controller delegate method

- (void)recentSearchesController:(RecentSearchesController *)controller didSelectString:(NSString *)searchString {

    /*
     The user selected a row in the recent searches list.
     Set the text in the search bar to the search string, and conduct the search.
     */
    searchBar.text = searchString;
    [self finishSearchWithString:searchString];
}


#pragma mark -
#pragma mark Search bar delegate methods

- (void)searchBarTextDidBeginEditing:(UISearchBar *)aSearchBar {

    // Display the search results controller popover.
    [recentSearchesPopoverController presentPopoverFromRect:[searchBar bounds] inView:searchBar permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
}


- (void)searchBarTextDidEndEditing:(UISearchBar *)aSearchBar {

    // If the user finishes editing text in the search bar by, for example tapping away rather than selecting from the recents list, then just dismiss the popover.
    [recentSearchesPopoverController dismissPopoverAnimated:YES];
    [aSearchBar resignFirstResponder];
}


- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText {

    // When the search string changes, filter the recents list accordingly.
    [recentSearchesController filterResultsUsingString:searchText];
}


- (void)searchBarSearchButtonClicked:(UISearchBar *)aSearchBar {

    // When the search button is tapped, add the search term to recents and conduct the search.
    NSString *searchString = [searchBar text];
    [recentSearchesController addToRecentSearches:searchString];
    [self finishSearchWithString:searchString];
}



- (void)finishSearchWithString:(NSString *)searchString {
    // Conduct the search. In this case, simply report the search term used.
    [recentSearchesPopoverController dismissPopoverAnimated:YES];
    [self doSearchAndDisplay:searchString];
    [searchBar resignFirstResponder];
}


- (void)popoverControllerDidDismissPopover:(UIPopoverController *)popoverController {

    // Remove focus from the search bar without committing the search.
    progressLabel.text = @"Canceled a search.";
    [searchBar resignFirstResponder];
}

#pragma mark -
#pragma mark View lifecycle

// Override to allow orientations other than the default portrait orientation.
- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) interfaceOrientation
{
    return YES;
}

- (void) viewDidUnload
{
    self.recentSearchesController = nil;
    self.recentSearchesPopoverController = nil;
}

#pragma mark -
#pragma mark Memory management

- (void) dealloc
{
    [searchBar release];
    [toolbar release];
    [recentSearchesController release];
    [recentSearchesPopoverController release];

    [super dealloc];
}

@end

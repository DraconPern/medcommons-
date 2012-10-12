/*


*/

#import "RootViewController.h"
#import "MyFamilyCareTeamAppDelegate.h"
#import "TableViewCell.h"

@implementation RootViewController


- (id)initWithStyle:(UITableViewStyle)style {
	if (self = [super initWithStyle:style]) {
		self.title = NSLocalizedString(@"Family Care Team", @"RootViewController title");
        self.tableView.delegate = self;
        self.tableView.dataSource = self;
        self.tableView.rowHeight = 57.0;
        self.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
        self.tableView.sectionHeaderHeight = 0;
	}
	return self;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
	return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
	MyFamilyCareTeamAppDelegate *appDelegate = (MyFamilyCareTeamAppDelegate *)[[UIApplication sharedApplication] delegate];
    NSUInteger count = [appDelegate countOfList];
	// If no earthquakes were parsed because the RSS feed was not available,
    // return a count of 1 so that the data source method tableView:cellForRowAtIndexPath: is called.
    // It also calls -[FamilyCareTeamXMLAppDelegate isDataSourceAvailable] to determine what to show in the table.
    if ([appDelegate isDataSourceAvailable] == NO) {
        return 1;
    }
    return count;
}

- (NSIndexPath *)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{	
//	MyFamilyCareTeamAppDelegate *appDelegate = (MyFamilyCareTeamAppDelegate *)[[UIApplication sharedApplication] delegate];
//	[appDelegate showPersonInfo:[(TableViewCell *)[tableView cellForRowAtIndexPath:indexPath] person]];
	return nil;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    
	static NSString *MyIdentifier = @"MyIdentifier";
    
  	TableViewCell *cell = (TableViewCell *)[tableView dequeueReusableCellWithIdentifier:MyIdentifier];
	if (cell == nil) {
		cell = [[[TableViewCell alloc] initWithFrame:CGRectZero reuseIdentifier:MyIdentifier] autorelease];
	}
    
    // Set up the cell.
	MyFamilyCareTeamAppDelegate *appDelegate = (MyFamilyCareTeamAppDelegate *)[[UIApplication sharedApplication] delegate];
    
    // If the RSS feed isn't accessible (which could happen if the network isn't available), show an informative
    // message in the first row of the table.
	if ([appDelegate isDataSourceAvailable] == NO) {
        cell = [[[UITableViewCell alloc] initWithFrame:CGRectZero reuseIdentifier:@"DefaultTableViewCell"] autorelease];
        cell.text = NSLocalizedString(@"RSS Host Not Available", @"RSS Host Not Available message");
		cell.textColor = [UIColor colorWithWhite:0.5 alpha:0.5];
		cell.accessoryType = UITableViewCellAccessoryNone;
		return cell;
	}
	
	Person *personForRow = [appDelegate objectInListAtIndex:indexPath.row];
    [cell setPerson:personForRow];
	return cell;
}


- (void)dealloc {
	[super dealloc];
}


- (void)loadView {
	[super loadView];
}

- (void)viewWillAppear:(BOOL)animated {
	[super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated {
	[super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
}

- (void)viewDidDisappear:(BOOL)animated {
}

- (void)didReceiveMemoryWarning {
	[super didReceiveMemoryWarning];
}

@end


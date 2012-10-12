

#import "TitleNodeWrapper.h"
#import "DataManager.h"


@implementation TitleNodeWrapper

@synthesize titleName , titleNode;

- (id)initWithTitle:(NSString *) name;
{
	
	self = [super init];
		
	if (self	) {
		
		

	//	NSLog (@"Title node mode for %@",name);
		
		titleName = [name retain];
		
		titleNode = [[DataManager sharedInstance].titlesDictionary objectForKey:titleName];
		
	}
	return self;
}


- (void)dealloc {
	[titleName	release];
	//[titleNode release];
	
	[super dealloc];
}


@end

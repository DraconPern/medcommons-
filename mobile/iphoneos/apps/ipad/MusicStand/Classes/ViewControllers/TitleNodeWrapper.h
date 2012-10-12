

#import "TitleNode.h"


@interface TitleNodeWrapper : NSObject {
	NSString *titleName;
	TitleNode  *titleNode;
}

@property (nonatomic, copy) NSString *titleName;
@property (nonatomic, retain) TitleNode *titleNode;

- (id)initWithTitle:(NSString *) name;

@end

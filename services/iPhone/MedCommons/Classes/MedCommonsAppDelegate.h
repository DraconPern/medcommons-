//
//  MedCommonsAppDelegate.h
//  MedCommons
//
//  Created by bill donner on 5/7/09.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//



#import <UIKit/UIKit.h>
#import "mcUrlConnection.h"

@interface MedCommonsAppDelegate : NSObject <UIApplicationDelegate> {
	
	IBOutlet UIWindow *window;
	UINavigationController *navigationController;
	
	NSMutableArray *list;
    
    BOOL _isDataSourceAvailable;
	NSString *filePath;
	NSString *dataPath;
	NSError *error;
	
}

@property (nonatomic, retain) UIWindow *window;
@property (nonatomic, retain) UINavigationController *navigationController;
@property (nonatomic, retain) NSMutableArray *list;
@property (nonatomic, retain) NSString *filePath;
@property (nonatomic, retain) NSString *dataPath;



- (BOOL)isDataSourceAvailable;

- (NSUInteger)countOfList;
- (id)objectInListAtIndex:(NSUInteger)theIndex;
- (void)getList:(id *)objsPtr range:(NSRange)range;

@end

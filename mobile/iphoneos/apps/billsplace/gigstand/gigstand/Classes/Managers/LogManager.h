//
//  LogManager.h
//  GigStand
//
//  Created by bill donner on 3/17/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreData/CoreData.h>

@interface LogManager : NSObject {
	NSInteger dummy;	
	NSMutableArray *logfilespecs;
}

@property (nonatomic, retain) NSMutableArray *logfilespecs;
@property (nonatomic) NSInteger dummy;
+ (LogManager *) sharedInstance;
+(void) setup;
-(id) init;
+(NSString *) pathForCurrentLog;
+(NSString *) pathForPreviousLog;
+(void) clearCurrentLog;
+(void) rotateLogs;
@end

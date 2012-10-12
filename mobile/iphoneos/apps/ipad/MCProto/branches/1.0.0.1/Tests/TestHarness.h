//
//  TestHarness.h
//  MedPad
//
//  Created by bill donner on 4/3/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

#define TOP_LOG(format,...)  [self toplog:[NSString stringWithFormat:[NSString stringWithString:format],## __VA_ARGS__]];

#define BOTTOM_LOG(format,...) [self bottomlog:[NSString stringWithFormat:[NSString stringWithString:format],## __VA_ARGS__]];


@interface TestHarness : NSObject {

}
-(void) toplog: (NSString *) s;
-(void) bottomlog: (NSString *) s;
@end

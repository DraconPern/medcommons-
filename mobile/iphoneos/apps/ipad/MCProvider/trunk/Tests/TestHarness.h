//
//  TestHarness.h
//  MCProvider
//
//  Created by Bill Donner on 4/3/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TestHarness : NSObject {

}
-(void) toplog: (NSString *) s;
-(void) bottomlog: (NSString *) s;
-(void) topinfo: (NSString *) s;
-(void) bottominfo: (NSString *) s;
@end

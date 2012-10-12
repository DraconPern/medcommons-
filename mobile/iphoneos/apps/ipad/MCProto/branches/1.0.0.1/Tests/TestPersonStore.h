//
//  TestPersonStore.h
//  MedPad
//
//  Created by bill donner on 4/5/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

// these Method Names are matched up with the .plist for MedPad
#import "TestHarness.h"
@class PersonStore;

@interface TestPersonStore : TestHarness {
    PersonStore *ps;
}

-(void) TESTinitWithMcid; // reads from disk or creates at docs/mcid-xxx
-(void) TESTwritePersonStore; // writes back
-(void) TESTreadPersonStore;
-(void) TESTcleanupPersonStore;
-(void) TESTdumpPersonStore;
-(void) TESTspecFor;
-(void) TESTremoveFromStore;


@end

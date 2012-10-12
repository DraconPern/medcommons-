//
//  TestPersonStore.m
//  MedPad
//
//  Created by bill donner on 4/5/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "TestPersonStore.h"
#import "PersonStore.h"

@implementation TestPersonStore
-(PersonStore *) getps
{
    if (!ps)
        ps =
        [[PersonStore alloc] initWithMcid:@"1234567890123456"];

    return ps;
}

-(void) TESTinitWithMcid // reads from disk or creates at docs/mcid-xxx
{

    TOP_LOG (@"Hit TESTinitWithMcid");
    if (ps) [ps release]; // clean out whatever is lying about
    ps = [self getps];
    id ret = [ps initWithMcid:@"1234567890123456"];
    BOTTOM_LOG (@"TESTinitWithMcid: %@",ret);
}
-(void) TESTwritePersonStore // writes back
{
    TOP_LOG (@"Hit TESTwritePersonStore");
    ps = [self getps];
    [ps writePersonStore];
    BOTTOM_LOG (@"TESTwritePersonStore");
}
-(void) TESTreadPersonStore
{
    TOP_LOG (@"Hit TESTreadPersonStore");
    ps = [self getps];
    BOOL yesno = [ps readPersonStore];
    BOTTOM_LOG (@"TESTreadPersonStore: %d",yesno);
}
-(void) TESTcleanupPersonStore
{
    TOP_LOG (@"Hit TESTcleanupPersonStore");
    ps = [self getps];
    [ps cleanupPersonStore];
    BOTTOM_LOG (@"TESTcleanupPersonStore");
}
-(void) TESTdumpPersonStore
{
    TOP_LOG (@"Hit TESTdumpPersonStore");
    ps = [self getps];
    [ps dumpPersonStore];
    BOTTOM_LOG (@"TESTdumpPersonStore");
}
-(void) TESTspecFor
{
    TOP_LOG (@"Hit TESTspecFor:AAAAAA.BBB");
    ps = [self getps];
    id ret = [ps specFor:@"AAAAAA.BBB"];
    BOTTOM_LOG (@"TESTspecFor: %@",ret);
}
-(void) TESTremoveFromStore
{
    TOP_LOG (@"Hit TESTremoveFromStore:AAAAAA.BBB");
    ps = [self getps];
    [ps removeFromStore:@"AAAAAA.BBB"];
    BOTTOM_LOG (@"TESTremoveFromStore");
}
@end

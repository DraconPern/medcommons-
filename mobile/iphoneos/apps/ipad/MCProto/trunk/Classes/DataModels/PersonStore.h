//
//  PersonStore.h
//  MedCommons
//
//  Created by bill donner on 4/5/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

#define kHowManySlots  11

#define DOCSFOLDER [NSHomeDirectory() stringByAppendingPathComponent:@"Documents"]
#define ROOTFOLDER NSHomeDirectory()

@interface PersonStore : NSObject
{
    NSString *mcid;  // this is usually interpreted as the key
    NSMutableArray *videospecs;  //NSStrings
    NSMutableArray *photospecs; //the main subject photo is kept as ther zeroth element of these arrays
    NSMutableArray *attrdicts; // array of NSDictionaries
    NSMutableDictionary *prefs; // fields, SOAP, etc
}

@property (nonatomic, retain)  NSString *mcid;
@property (nonatomic, retain)  NSMutableArray *videospecs;
@property (nonatomic, retain)  NSMutableArray *photospecs;
@property (nonatomic, retain)  NSMutableArray *attrdicts;
@property (nonatomic, retain)  NSMutableDictionary *prefs;

-(PersonStore *) initWithMcid:(NSString *)mcid; // reads from disk or creates at docs/mcid-xxx
-(void) writePersonStore; // writes back
-(BOOL) readPersonStore;
-(void) cleanupPersonStore;
-(void) dumpPersonStore;
-(NSString *) specFor:(NSString *) s;
-(void) removeFromStore:(NSString *) s;

@end

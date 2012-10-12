//
//  PatientStore.h
//  ForensicFoto
//
//  Created by bill donner on 9/12/09.
//  Copyright 2009 MedCommons,Inc. . All rights reserved.
//

@interface PatientStore : NSObject 
{
	NSString *mcid;
	NSMutableArray *videospecs;  //NSStrings
	NSMutableArray *photospecs; //the main subject photo is kept as ther zeroth element of these arrays
	NSMutableArray *attrdicts; // array of NSDictionaries	
	NSMutableDictionary *prefs; // fields, SOAP, etc

}
@property (nonatomic, retain)  NSMutableArray *videospecs;
@property (nonatomic, retain)  NSMutableArray *photospecs;
@property (nonatomic, retain)  NSMutableArray *attrdicts;
@property (nonatomic, retain)  NSMutableDictionary *prefs;

-(PatientStore *) initWithMcid:(NSString *)mcid; // reads from disk or creates at docs/mcid-xxx
-(void) writePatientStore; // writes back 

-(void) addAttrs:(NSDictionary *) attrs atIndex: (int) slot;
-(NSDictionary *) attrsAtIndex: (int) slot;
-(BOOL) haveSubjectPhoto;
-(BOOL) havePhotoAtIndex: (int )i;
-(BOOL) haveVideoAtIndex: (int )i;
-(int) countPartPics;
-(int) countVideos;

-(NSString *)  findFreePatientStorePath;
-(NSString *)  newSubjectPhotoSpec;
-(int)  nextFreeIndex;
-(void) cleanup;

-(NSString *) subjectPhotoSpec;
-(NSString *) photoSpecAtIndex: (int) i;
-(NSString *) videoSpecAtIndex: (int) i;

-(void) setSubjectPhotoSpec: (NSString *) url withPhotoAttrs: (NSDictionary *) attrs;
-(void) setPhotoSpec: (NSString *) url atIndex: (int) i withPhotoAttrs: (NSDictionary *) attrs;
-(void) setVideoSpec: (NSString *) url atIndex: (int) i withVideoAttrs: (NSDictionary *) attrs;

-(void) swapPhotoPart:(int)p withPart:(int)q;
-(void) swapSubjectPhotoWithPart: (int)p;
-(void) trashPhotoPart:(int)p;
-(void) trashSubjectPhoto;

-(NSString *) fullSubjectPhotoSpec;
-(NSString *) fullPhotoSpecAtIndex: (int) i;
-(NSString *) fullVideoSpecAtIndex: (int) i;

-(void) dumpPatientStore;

@end

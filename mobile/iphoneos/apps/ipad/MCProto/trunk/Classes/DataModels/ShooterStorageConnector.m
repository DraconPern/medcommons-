//
//  ShooterStorage.m
//  MedPad
//
//  Created by bill donner on 4/11/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "ShooterStorageConnector.h"
#import "GPSDevice.h"
#import "DataManager.h"



@implementation ShooterStorageConnector

#pragma mark Current case
-(BOOL) isEmptySpec :(NSString *) spec
{
    return ([@"" isEqualToString:spec]);
}

- (int) countPartPics
{
    int numpics =0;

    for (int i=1; i<kHowManySlots; i++)
    {
        if(![self isEmptySpec:[photospecs objectAtIndex:i]])
            ++numpics;
    }
    return numpics;
}
- (int) countVideos
{
    int numpics =0;

    for (int i=1; i<kHowManySlots; i++)
    {
        if(![self isEmptySpec:[videospecs objectAtIndex:i]])
            ++numpics;
    }
    return numpics;
}
-(void) dumpPatientStore
{
    for (int j=0; j<kHowManySlots; j++)
    {

        if(![self isEmptySpec:[photospecs objectAtIndex:j]])
            NSLog (@"part photo %d:  %@",j,[photospecs objectAtIndex:j]);
        if(![self isEmptySpec:[videospecs objectAtIndex:j]])
            NSLog (@"video %d:  %@",j,[videospecs objectAtIndex:j]);
        if([attrdicts objectAtIndex:j])
            NSLog (@"attrs %d:  %@",j,[attrdicts objectAtIndex:j]);

    }

}
-(void) swapPhotoPart:(int)j withPart:(int)k
{
    id temp = [photospecs objectAtIndex:k+1];
    id temv = [videospecs objectAtIndex:k+1];
    id tema = [attrdicts objectAtIndex:k+1];

    [photospecs replaceObjectAtIndex:k+1 withObject:[photospecs objectAtIndex:j+1]];
    [photospecs replaceObjectAtIndex:j+1 withObject:temp];

    [videospecs replaceObjectAtIndex:k+1 withObject:[videospecs objectAtIndex:j+1]];
    [videospecs replaceObjectAtIndex:j+1 withObject:temv];

    [attrdicts replaceObjectAtIndex:k+1 withObject:[attrdicts objectAtIndex:j+1]];
    [attrdicts replaceObjectAtIndex:j+1 withObject:tema];

}
-(void) swapSubjectPhotoWithPart: (int)j
{
    id temp = [photospecs objectAtIndex:0];
    [photospecs replaceObjectAtIndex:0 withObject:[photospecs objectAtIndex:j+1]];
    [photospecs replaceObjectAtIndex:j+1 withObject:temp];
}
-(void) trashPhotoPart:(int)j
{
    NSError *error;
    [[NSFileManager defaultManager] removeItemAtPath:[DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
                                                                                                 [photospecs objectAtIndex:j+1]]]   error:&error];
    [photospecs   removeObjectAtIndex:  j+1  ];
    [photospecs   addObject:@""   ];

    [[NSFileManager defaultManager] removeItemAtPath:[DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
                                                                                                 [videospecs objectAtIndex:j+1]]]   error:&error];
    [videospecs   removeObjectAtIndex:  j +1 ];
    [videospecs   addObject:@""   ];

    [attrdicts   removeObjectAtIndex:   j +1 ];
    [attrdicts addObject:[[NSDictionary alloc]init]];


}
-(void) trashSubjectPhoto
{
    NSError *error;
    [[NSFileManager defaultManager] removeItemAtPath:[DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
                                                                                                 [photospecs objectAtIndex:0]]] error:&error];




    [photospecs replaceObjectAtIndex:0 withObject:[NSString stringWithString:@""]];
    [attrdicts   removeObjectAtIndex:   0];
}


-(BOOL) hasSubjectPhoto
{
    return (![self isEmptySpec:[photospecs objectAtIndex:0]]);
    //return ([[NSFileManager defaultManager] fileExistsAtPath:FIRSTIMAGEPATH]) ;
}

-(void) setSubjectPhotoSpec: (NSString *) url withPhotoAttrs: (NSDictionary *) attrs
{
    GPSDevice *gps =[[DataManager sharedInstance] ffGPSDevice];

    // this is where the last gps reading gets saved with the pictures attributes
    [photospecs  replaceObjectAtIndex:0 withObject:[url copy]]; // make a copy
    NSMutableDictionary *dict = [attrdicts objectAtIndex:0];

    //NSLog(@"--- %d PatientStore setSubjectPhotoSpec on slot %d ",[[attrdicts objectAtIndex:0] retainCount], 0);
    [dict addEntriesFromDictionary: attrs];
    [dict setObject:gps.lastMeasuredVerticalAccuracy forKey:@"vertical-accuracy"];
    [dict setObject:gps.lastMeasuredHorizontalAccuracy forKey:@"horizontal-accuracy"];
    [dict setObject:gps.lastMeasuredLatitude forKey:@"latitude"];
    [dict setObject:gps.lastMeasuredLongitude   forKey:@"longitude"];
    //NSLog(@"After setting subject photo dumping image store");
    //[self dumpPatientStore];
}


- (NSString *) subjectPhotoSpec
{
    return [photospecs objectAtIndex:0];
}


-(NSString *) photoSpecAtIndex: (int) i
{
    return [photospecs objectAtIndex:i+1];
}

-(NSString *) videoSpecAtIndex: (int) i
{
    return [videospecs objectAtIndex:i+1];
}


- (NSString *) fullSubjectPhotoSpec
{
    NSString *plistPath = [ROOTFOLDER stringByAppendingPathComponent:[photospecs objectAtIndex:0]];
    //NSLog (@"subject photo is %@",plistPath);
    return plistPath;
}


-(NSString *) fullPhotoSpecAtIndex: (int) i
{
    NSString *plistPath = [ROOTFOLDER stringByAppendingPathComponent:[photospecs objectAtIndex:i+1]];
    //  NSLog (@"parts photo is %@",plistPath);
    return plistPath;
}
-(NSString *) fullVideoSpecAtIndex: (int) i
{

    NSString *plistPath = [ROOTFOLDER stringByAppendingPathComponent:[videospecs objectAtIndex:i+1]];
    return plistPath;
}



-(BOOL) hasPhotoAtIndex: (int )i
{
    return (![self isEmptySpec: [photospecs objectAtIndex:i+1]]);
}
-(void) setPhotoSpec: (NSString *) url atIndex: (int) i withPhotoAttrs: (NSDictionary *) attrs
{
    //
    // this is where we will absorb the gps info and att to the attrs, it is right at the point the picture is taken
    //
    NSLog (@"writing %@ to index %d",url,i);

    [photospecs  replaceObjectAtIndex:  i+1 withObject:url ];
    NSMutableDictionary *dict = [attrdicts objectAtIndex:i+1];

    ////NSLog(@"--- %d PatientStore setPhotoSpec on slot %d ",[[attrdicts objectAtIndex:i+1] retainCount], i+1);
    [dict addEntriesFromDictionary: attrs];

    [[[DataManager sharedInstance] ffPatientStore] writePersonStore];// snap it to disk

}
-(void) addAttrs:(NSDictionary *) attrs atIndex: (int) slot
{
    NSMutableDictionary *dict = [attrdicts objectAtIndex:slot];

    ////NSLog(@"--- %d PatientStore addAttrs on slot %d ",[[attrdicts objectAtIndex:slot] retainCount], slot);
    [dict addEntriesFromDictionary: attrs];
}
-(NSDictionary *) attrsAtIndex: (int) slot
{
    NSDictionary *dict = [attrdicts objectAtIndex:slot]; // return it as read only
    return dict;

}
-(BOOL) hasVideoAtIndex: (int)i
{
    return (![self isEmptySpec: [videospecs objectAtIndex:i+1]]);
}
-(void) setVideoSpec: (NSString *) url atIndex: (int) i withVideoAttrs: (NSDictionary *) attrs
{
    [videospecs  replaceObjectAtIndex:  i+1 withObject:url ];

    [[[DataManager sharedInstance] ffPatientStore].prefs setObject:videospecs forKey:@"videospecs"];

    [[[DataManager sharedInstance] ffPatientStore] writePersonStore];// snap it to disk
    NSMutableDictionary *dict = [attrdicts objectAtIndex:i+1];

    NSLog(@"--- %d PatientStore setVideoSpec on slot %d ",[[attrdicts objectAtIndex:i+1] retainCount], i+1);
    [dict addEntriesFromDictionary: attrs];
    //NSLog(@"After setting video dumping image store");
    //[//self dumpPatientStore];
}

-(NSString *)  newSubjectPhotoSpec
{
    NSInteger i = [[[DataManager sharedInstance] ffNextFileIndex] intValue]; // step ahead to next
    [DataManager sharedInstance].ffNextFileIndex = [NSNumber numberWithInt:i+1];


    NSString *x =  [NSString stringWithFormat:@"Documents/%@-%@.%@",  BASE_PATH, [DataManager sharedInstance].ffNextFileIndex, BASE_TYPE,nil ] ;
    //CAM_LOG(@"------------New subject photo spec is %@",ss);
    return x;
}
-(NSString *)  findFreePatientStorePath
{
    NSInteger i = [[[DataManager sharedInstance] ffNextFileIndex] intValue]; // step ahead to next
    [DataManager sharedInstance].ffNextFileIndex = [NSNumber numberWithInt:i+1];

    NSString *x =  [NSString stringWithFormat:@"Documents/%@-%@.%@", BASE_PATH, [DataManager sharedInstance].ffNextFileIndex, BASE_TYPE,nil ] ;
    //  CAM_LOG(@"------------New body part spec is %@",ss);
    return x;

}

-(int)  nextFreeIndex
{
    // back up the return value by one so when it is passed back into the store function it moves to the right spot -- yuck
    for (int j=1; j<kHowManySlots; j++)
        if([self isEmptySpec:[photospecs objectAtIndex:j]])
        { /////NSLog (@"nextFreeIndex returns", j-1);
            return j-1;
        }


    return -1;
}


//
//-(void) cleanup
//{
//  //********************* LOOP THRU Files ***********************
//
//  NSError *error;
//
//  for (int i = 0; i<kHowManySlots; i++)
//  { //do this agressively
//      if (![[NSFileManager defaultManager] fileExistsAtPath:
//            [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
//                                                        [photospecs objectAtIndex:i]]]])
//          [[NSFileManager defaultManager] removeItemAtPath:
//           [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
//                                                       [photospecs objectAtIndex:i]]] error:&error] ;
//      [photospecs   replaceObjectAtIndex: i withObject:@""   ];
//      // clean out the video references, should we delete these files?
//      [videospecs   replaceObjectAtIndex: i withObject:@""   ];
//      [attrdicts     replaceObjectAtIndex: i  withObject: [[NSMutableDictionary alloc] init]]; // probably leaking
//  }
//  //*********************Remove ***********************
//  [[[DataManager sharedInstance] ffPatientStore] writePatientStore];// snap it to disk
//
//}

@end

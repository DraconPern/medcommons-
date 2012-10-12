//
//  PersonStore.m
//  MedCommons
//
//  Created by bill donner on 4/5/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "PersonStore.h"



@implementation PersonStore



@synthesize videospecs;
@synthesize photospecs;
@synthesize attrdicts;
@synthesize prefs;
@synthesize mcid;

-(void) dealloc
{
    [videospecs release];
    [photospecs release];
    [attrdicts release];
    [prefs  release];
    [mcid release];
    [super dealloc];
}
-(NSString *) specFor:(NSString *) s
{
    return [ROOTFOLDER stringByAppendingPathComponent:s];
}
-(void) removeFromStore:(NSString *) s
{
    NSError *error;
    [[NSFileManager defaultManager] removeItemAtPath:[DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
                                                                                                 s]]    error:&error];
}

-(PersonStore *) initWithMcid:(NSString *)_mcid
{

    self = [super init];
    mcid = [_mcid retain];

    // create it afresh
    prefs = [[NSMutableDictionary alloc] init];



    //
    // now read whatever we have
    //

    NSLog (@"reading record store for %@", mcid);
    NSString *errorDesc = nil;
    NSPropertyListFormat format;

    NSString *plistPath = [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcid-%@.plist",mcid]];
    if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
        NSLog(@"No plist for this patient %@",mcid);
        // ok, no plist, so lets just initialize
        self.photospecs = [[NSMutableArray alloc]init]; // place to store NSURLs for onphone photos
        for (int j=0; j<kHowManySlots; j++)
        {
            [self.photospecs addObject:@""]; // set to nothing for now, will overwrite

        }

        self.videospecs = [[NSMutableArray alloc]init]; // place to store NSURLs for onphone videos
        for (int j=0; j<kHowManySlots; j++)
        {
            [self.videospecs addObject:@""]; // set to nothing for now, will overwrite

        }

        attrdicts = [[NSMutableArray alloc]init]; // place to store NSURLs for onphone videos
        for (int j=0; j<kHowManySlots; j++)
        {
            [self.attrdicts addObject:[[NSMutableDictionary alloc] init]] ; // set to nothing for now, will overwrite

        }


        for (int j=0; j<kHowManySlots; j++)
        {
            // make sure all of the files exist and cleanupPersonStore if the system has removed them since the last time we ran
            if (![@"" isEqualToString:[self.photospecs objectAtIndex:j]])
            {
                if (![[NSFileManager defaultManager] fileExistsAtPath:
                      [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
                                                                  [self.photospecs objectAtIndex:j]]]])
                {
                    [self.photospecs   replaceObjectAtIndex:    j withObject:@""   ];
                    [self.videospecs   replaceObjectAtIndex:    j withObject:@""   ];
                }
            }
        }
        // end of no existing plist
    }
    else
    {
        // we have the plist, lets read it and fix things up
        NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
        NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
                                              propertyListFromData:plistXML
                                              mutabilityOption:NSPropertyListMutableContainersAndLeaves
                                              format:&format
                                              errorDescription:&errorDesc];
        if (!temp) {
            NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);
        }

        self.photospecs = [[NSMutableArray arrayWithArray:[temp objectForKey:@"photospecs"]] retain];

        self.videospecs = [[NSMutableArray arrayWithArray:[temp objectForKey:@"videospecs"]] retain];

        self.attrdicts = [[NSMutableArray arrayWithArray:[temp objectForKey:@"attrdicts"]] retain];

        self.prefs = [[NSMutableDictionary dictionaryWithDictionary:[temp objectForKey:@"prefs"]] retain];

        NSLog(@"Did not restore",nil);

    }
    // OK at this point we are ok either way

    return self;
}


-(BOOL) readPersonStore // gets saved
{

    NSString *errorDesc = nil;
    NSPropertyListFormat format;

    NSString *plistPath = [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcid-%@.plist",mcid]];
    if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
        //plistPath = [[NSBundle mainBundle] pathForResource:@"Data" ofType:@"plist"];
        NSLog(@"No plist for this record %@",mcid);
        return NO;
    }
    NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
    NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
                                          propertyListFromData:plistXML
                                          mutabilityOption:NSPropertyListMutableContainersAndLeaves
                                          format:&format
                                          errorDescription:&errorDesc];
    if (!temp) {
        NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);
    }

    self.photospecs = [[NSMutableArray arrayWithArray:[temp objectForKey:@"photospecs"]] retain];

    self.videospecs = [[NSMutableArray arrayWithArray:[temp objectForKey:@"videospecs"]] retain];

    self.attrdicts = [[NSMutableArray arrayWithArray:[temp objectForKey:@"attrdicts"]] retain];

    self.prefs = [[NSMutableDictionary dictionaryWithDictionary:[temp objectForKey:@"prefs"]] retain];

    return YES;
}

-(void) writePersonStore // writes back
{
    NSLog (@"write record store for %@", mcid);

    //[NSKeyedArchiver archiveRootObject:self toFile:PERSONPATH]; // write entire tree in one go

    NSString *error;
    //  NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *plistPath = [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcid-%@.plist",mcid]];
    NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:
                               [NSArray arrayWithObjects: photospecs,videospecs,attrdicts,prefs, nil]
                                                          forKeys:[NSArray arrayWithObjects: @"photospecs", @"videospecs", @"attrdicts", @"prefs" ,nil]];
    NSData *plistData = [NSPropertyListSerialization dataFromPropertyList:plistDict
                                                                   format:NSPropertyListXMLFormat_v1_0
                                                         errorDescription:&error];
    if(plistData) {
        [plistData writeToFile:plistPath atomically:YES];
    }
    else {
        NSLog(@"error %@", error);
        [error release];
    }
}


-(void) cleanupPersonStore
{
    //********************* LOOP THRU Files ***********************

    NSError *error;

    for (int i = 0; i<kHowManySlots; i++)
    { //do this agressively
        if (![[NSFileManager defaultManager] fileExistsAtPath:
              [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
                                                          [self.photospecs objectAtIndex:i]]]])
            [[NSFileManager defaultManager] removeItemAtPath:
             [DOCSFOLDER stringByAppendingPathComponent:[NSString stringWithFormat:@"mcImage-%@.png",
                                                         [self.photospecs objectAtIndex:i]]] error:&error] ;
        [self.photospecs   replaceObjectAtIndex:    i withObject:@""   ];
        // clean out the video references, should we delete these files?
        [self.videospecs   replaceObjectAtIndex:    i withObject:@""   ];
        [self.attrdicts    replaceObjectAtIndex: i  withObject: [[NSMutableDictionary alloc] init]]; // probably leaking
    }
    //*********************Remove ***********************
    [self writePersonStore];// snap it to disk

}

-(void) dumpPersonStore
{
    for (int j=0; j<kHowManySlots; j++)
    {

        if([self.photospecs objectAtIndex:j])
            NSLog (@"part photo %d:  %@",j,[self.photospecs objectAtIndex:j]);
        if([self.videospecs objectAtIndex:j])
            NSLog (@"video %d:  %@",j,[self.videospecs objectAtIndex:j]);
        if([self.attrdicts objectAtIndex:j])
            NSLog (@"attrs %d:  %@",j,[self.attrdicts objectAtIndex:j]);

    }

}

@end

//
//  DocumentsManager.m
//  MCProvider
//
//  Created by J. G. Pusey on 6/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "DataStore.h"           // for now ...
#import "DocumentsManager.h"
#import "DataManager.h"

#import "TitleNode.h"
#import "SectionLabelWrapper.h"

#pragma mark -
#pragma mark Public Class DocumentsManager
#pragma mark -

@interface DocumentsManager ()

@property (nonatomic, copy,   readonly)  NSString      *iTunesInboxPath;
@property (nonatomic, retain, readwrite) NSArray       *documents;
@property (nonatomic, retain, readonly)  NSFileManager *fileManager;

@end

@implementation DocumentsManager

@dynamic    iTunesInboxPath;
@synthesize documents   = documents_;
@synthesize fileManager = fileManager_;

#pragma mark Dynamic Properties Methods



- (NSArray *) documents     // for now ...
{
    if (!self->documents_)
        self->documents_ = [[NSArray array] retain];
	
    return self->documents_;
}

#pragma mark Public Class Methods

+ (DocumentsManager *) sharedInstance
{
    static DocumentsManager *SharedInstance;
	
    if (!SharedInstance)
        SharedInstance = [[DocumentsManager alloc] init];
	
    return SharedInstance;
}

#pragma mark Public Instance Methods

- (NSArray *) documentsConformingToUTIs: (NSArray *) UTIs
{
    NSMutableArray *docs = [NSMutableArray arrayWithCapacity: [self.documents count]];
	
    for (MCDocument *doc in self.documents)
    {
        for (NSString *UTI in UTIs)
        {
            // just say YES:            if ([doc.UTI conformsToUTI: UTI])
            {
                [docs addObject: doc];
				
                break;
            }
        }
    }
	
    return docs;
}


#pragma mark once only startup file processing



#pragma mark Private Instance Methods



#pragma mark Overridden NSObject Methods

- (void) dealloc
{

	[self->documents_ release];
	
	[super dealloc];
}

- (id) init
{
	self = [super init];
	
	
	return self;
}

@end

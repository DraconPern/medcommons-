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
#import "ZipArchive.h"

#pragma mark -
#pragma mark Public Class DocumentsManager
#pragma mark -

@interface DocumentsManager ()

@property (nonatomic, copy,   readonly)  NSString      *docsDirPath;
@property (nonatomic, retain, readwrite) NSArray       *documents;
@property (nonatomic, retain, readonly)  NSFileManager *fileManager;

- (NSArray *) scanForDocuments;

@end

@implementation DocumentsManager

@dynamic    docsDirPath;
@synthesize documents   = documents_;
@synthesize fileManager = fileManager_;

#pragma mark Dynamic Properties Methods

- (NSString *) docsDirPath  // replace DataStore call with inline ???
{
    if (!self->docsDirPath_)
        self->docsDirPath_ = [[DataStore pathForSharedDocuments] retain];

    return self->docsDirPath_;
}

- (NSArray *) documents     // for now ...
{
    if (!self->documents_)
        self->documents_ = [[self scanForDocuments] retain];

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

#pragma mark Private Instance Methods

- (NSArray *) scanForDocuments
{
    NSArray *paths = [self.fileManager contentsOfDirectoryAtPath: self.docsDirPath
                                                           error: NULL];

    if (paths)
    {
        NSMutableArray *docs = [NSMutableArray arrayWithCapacity: [paths count]];
                for (NSString *path in paths)
       if (![path isEqualToString:@".DS_Store"])
       {
            NSString     *fullPath = [self.docsDirPath stringByAppendingPathComponent: path];
            NSDictionary *attrs = [self.fileManager attributesOfItemAtPath: fullPath
                                                                     error: NULL];

            if (attrs && [[attrs fileType] isEqualToString: NSFileTypeRegular])
            {

                [docs addObject: [MCDocument documentWithPath: fullPath
                                                   attributes: attrs]];

                // if it's a zip file then unpack it

                NSString *zip = @".zip";

                NSRange range = [fullPath rangeOfString:zip
                                                options:(NSCaseInsensitiveSearch)];

                if (range.location != NSNotFound)

                {

                    NSLog (@" zip file fullpath %@ attrs %@", fullPath, attrs);
                    NSString *doctags = @"/Documents/";

                    NSRange range2 = [fullPath rangeOfString:doctags
                                                     options:(NSCaseInsensitiveSearch)];

                    if (range2.location != NSNotFound)
                    {
                        int len = range.location - range2.location - 11;
                        NSRange xrange= NSMakeRange(range2.location+11,len);
                        NSString *s = [fullPath substringWithRange:xrange];

                        ZipArchive *za = [[ZipArchive alloc] init];
                        if ([za UnzipOpenFile: fullPath]) {
                            BOOL ret = [za UnzipFileTo: [DataStore pathForExplodedZipFilesForKey:s] overWrite: YES];
                            if (NO == ret){} [za UnzipCloseFile];
                        }
                        [za release];
                    }
                }
            }
        }
        return docs;
    }

    return [NSArray array];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->docsDirPath_ release];
    [self->documents_ release];
    [self->fileManager_ release];

    [super dealloc];
}

- (id) init
{
    self = [super init];

    if (self)
        self->fileManager_ = [[NSFileManager alloc] init];

    return self;
}

@end

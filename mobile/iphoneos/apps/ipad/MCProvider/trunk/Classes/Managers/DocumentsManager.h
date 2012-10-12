//
//  DocumentsManager.h
//  MCProvider
//
//  Created by J. G. Pusey on 6/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DocumentsManager : NSObject
{
@private

    NSString      *docsDirPath_;
    NSArray       *documents_;
    NSFileManager *fileManager_;
}

+ (DocumentsManager *) sharedInstance;

- (NSArray *) documentsConformingToUTIs: (NSArray *) UTIs;

@end

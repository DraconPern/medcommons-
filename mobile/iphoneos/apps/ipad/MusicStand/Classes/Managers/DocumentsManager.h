//
//  DocumentsManager.h
//  MCProvider
//
//  Created by J. G. Pusey on 6/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DocumentsManager : NSObject
{
@private

    NSArray       *documents_;
	UIAlertView *zipalert;

}

+ (DocumentsManager *) sharedInstance;

- (NSArray *) documentsConformingToUTIs: (NSArray *) UTIs;



@end

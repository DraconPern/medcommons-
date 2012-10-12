//
//  MusicStandController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ZipViewController : UIViewController
{
@private

    UIBarButtonItem *backButton_;
    NSString        *base_;
 //   NSArray         *documents_;
	NSString 		*alphabetIndex_;
	
	NSArray *sectionsArray;// has outer level for A-Z and #, each has own array of particular MCDocs
	
	UILocalizedIndexedCollation *collation;
	
	
}

@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;

@property (nonatomic, retain) NSArray *sectionsArray;
@property (nonatomic, retain) UILocalizedIndexedCollation *collation;

- (id) initWithBase: (NSString *) str;

@end

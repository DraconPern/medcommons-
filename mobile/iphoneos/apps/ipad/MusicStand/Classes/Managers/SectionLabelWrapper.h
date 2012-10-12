//
//  SectionLabelWrapper.h
//  MusicStand
//
//  Created by bill donner on 10/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>




@interface SectionLabelWrapper : NSObject {
	NSString *localeName;
	NSString *sectionLabel;
}

@property (nonatomic, copy) NSString *localeName;
@property (nonatomic, retain) NSString *sectionLabel;

- (id)initWithSectionLabel:(NSString *)aLabel nameComponents:(NSArray *)nameComponents ;

@end

//
//  SectionLabelWrapper.m
//  MusicStand
//
//  Created by bill donner on 10/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "SectionLabelWrapper.h"



@implementation SectionLabelWrapper

@synthesize localeName, sectionLabel ;

- (id)initWithSectionLabel:(NSString *)aLabel nameComponents:(NSArray *)nameComponents {
	
	if ((self = [super init])) {
		
		sectionLabel = [aLabel retain];
		
		NSString *name = nil;
		name = [nameComponents objectAtIndex:0];
		
		localeName = [[name stringByReplacingOccurrencesOfString:@"_" withString:@" "] retain];
	}
	return self;
}


- (void)dealloc {
	[localeName release];
	[sectionLabel release];
	
	[super dealloc];
}


@end



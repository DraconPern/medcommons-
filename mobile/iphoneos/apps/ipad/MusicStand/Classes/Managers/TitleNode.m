//
//  TitleNode.m
//  MusicStand
//
//  Created by bill donner on 12/21/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
#import "TitleNode.h"


@implementation RefNode 
@synthesize archive,title ;

-(RefNode *)  initWithTitle:(NSString *)titl andWithArchive:(NSString *) arch;

{
	self = [super init];
	if (self)
	{
		archive  = [arch copy];
		title = [titl copy];
	}
	return self;
}
+(RefNode *) inArray:(NSMutableArray *)array findTitle:(NSString *)titl
{
	for (RefNode *s in array)
	{
		if ([titl isEqualToString:s.title]) return s;
	}
	return nil;
}
@end


@implementation TitleNode
@synthesize title, variants,lastvariantsegmentindex;

-(TitleNode *) initWithTitle:(NSString *) titl
{
	self = [super init];
	if (self)
	{
		title = [titl copy];
		variants = [[NSMutableDictionary alloc] init];
		lastvariantsegmentindex = -1;
	}
	return self;
}
-(BOOL) addSourceDocument:(NSString *)name segment:(NSUInteger)segmentindex resetLast:(BOOL) resetLast;
{
	if ([self->variants objectForKey:name]) // if already there
	{
		//NSLog (@"path %@ name %@ dupe", pat, name);
		return NO;
	}
	else {
		//not there so add it
		[self->variants setObject:[NSNumber numberWithInt:segmentindex ] forKey:name];
		if (resetLast) lastvariantsegmentindex	= segmentindex;
		return YES;
	}
}



- (void) dealloc
{
	[title release];
	[variants release];
	[super dealloc];
}

@end

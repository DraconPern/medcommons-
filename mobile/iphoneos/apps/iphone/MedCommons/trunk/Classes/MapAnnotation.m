//
//  MapAnnotation.m
//  MedCommons
//
//  Created by bill donner on 10/22/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//

#import "MapAnnotation.h"


@implementation MapAnnotation

@synthesize coordinate, title, subtitle;

-(void)dealloc 
{
	[title release];
	[subtitle release];
	[super dealloc];
}

@end


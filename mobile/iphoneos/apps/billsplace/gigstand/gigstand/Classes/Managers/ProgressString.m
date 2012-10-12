//
//  ProgressString.m
//  GigStand
//
//  Created by bill donner on 1/1/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "ProgressString.h"

@implementation ProgressString

#pragma mark ProgressString


-(void) text:(NSString *)s;
{
	if (!self->internalString) 
	{
		self->internalString = [[NSString alloc] initWithString:s];
	//	NSLog (@"ProgressStringA: %@", self->internalString);
	}
	else 
	{// dont refresh if identical
		if (![self->internalString isEqualToString:s])
		{  
			[self->internalString release];
			self->internalString = [[NSString alloc] initWithString:s];
	//		NSLog (@"ProgressStringB: %@", self->internalString);
		}
	}
}
	
	-(void) dealloc
	{
		
		if (self->internalString) [self->internalString release];
		[super dealloc];
	}
	
	-(NSString *) pretty
	{
		return [NSString stringWithFormat:@"%@",self->internalString];
	}
	
	@end

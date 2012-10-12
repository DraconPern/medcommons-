//
//  ProgressString.h
//  GigStand
//
//  Created by bill donner on 1/1/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ProgressString : NSObject {
	

	NSString *internalString;

}

-(NSString *) pretty;
-(void) text:(NSString *)s;


@end

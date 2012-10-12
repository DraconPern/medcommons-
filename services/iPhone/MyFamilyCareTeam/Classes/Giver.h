//
//  Giver.h
//  MyFamilyCareTeam
//
//  Created by bill donner on 5/5/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//


#import <Foundation/Foundation.h>
#import "Person.h"

@interface Giver : Person {
	
@private    
    NSString *_phone;       // Derived from the title property.
    NSString *_email;        // Derived from the title property.
    NSString *_fbid;           // Holds location and magnitude.
	NSString *_twitter;
	
}

@property (nonatomic, retain) NSString *phone;
@property (nonatomic, retain) NSString *email;
@property (nonatomic, retain) NSString *fbid;
@property (nonatomic, retain) NSString *twitter;

@end


//
//  BonJourManager.h
//  GigStand
//
//  Created by bill donner on 2/15/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface BonJourManager : NSObject< NSNetServiceDelegate>
{
	
	NSNetService *netService;
	
	NSDictionary *addresses;	
}

+ (BonJourManager *) sharedInstance;
- (BonJourManager *) init;
- (void)localhostAdressesResolved:(NSNotification *) notification;


+(void) starting;

+(void) writeTXTData ;
+(NSDictionary *) buildTXTDictionary;

-(void) publishTXTFromLastTitle;
@end

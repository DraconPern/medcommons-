//
//  InnerREST.h
//  MedCommons
//
//  Created by bill donner on 4/1/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

//
// this module now has NO instance variables and these could probably not be a class but just some routines
//
//

#if (!ENABLE_LLC_LOGGING)
#define LLC_LOG(format,...)
#else
#define LLC_LOG(format,...)  CONSOLE_LOG(format ,## __VA_ARGS__)
#endif

@interface InnerREST : NSObject {

}
-(id) init;
-(NSString *) postIt:(NSString *)request whichService:(NSString  *)whichservice   withBitsFromPath:(NSString *)uniquePath;
-(NSDictionary *) postGenericRequestWithJSONResponse:(NSString *)request toService:(NSString *)service ;

@end

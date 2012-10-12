//
//  RESTComms.h
//  ForensicFoto
//
//  Created by bill donner on 9/9/09.
//  Copyright 2009 MEDCOMMONS, INC.. All rights reserved.
//

#if (!ENABLE_LLC_LOGGING)
#define LLC_LOG(format,...) 
#else
#define LLC_LOG(format,...)  CONSOLE_LOG(format ,## __VA_ARGS__)
#endif
@class PatientStore,MCUploadController;
@interface RESTComms: NSObject 
{
	NSString *lastpostresponseline;
	
//	PatientStore *patientStore ;

	NSDictionary *photoAttrs;
	NSString *appKey;
	
}

-(NSString *) postIt:(NSString *)request whichService:(NSString  *)whichservice   withBitsFromPath:(NSString *)uniquePath;
-(NSDictionary *) postGenericRequestWithJSONResponse:(NSString *)request toService:(NSString *)service ;

- (void) doPosts:  (NSString *)finalRequest withId:(NSString *)unique andTimeStamp:(double)dtoday andGeneralAttrs:(NSDictionary *) genAttrs andMasterController: (MCUploadController *) masterController;
-(RESTComms *) init;
@end

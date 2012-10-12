//
//  HistoryCase.m
//  MedCommons
//
//  Created by bill donner on 10/15/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//

#import "HistoryCase.h"
@implementation HistoryCase

-(void) dumpCase
{
	NSLog(@"dumpCase %@ %@ %@ %@ %@ %@ %@ %@",xslot,xtimestamp,xname,xsha1,xvoucherid,xpin,xattributes,xattrdicts);
}
-(NSString *)sha1
{
	return xsha1;
}
-(NSArray *)attrdicts
{
	return xattrdicts;
}
-(NSDictionary *) attributes
{
	return xattributes;
}
//-(NSString *) viewerurl
//{
//	return xviewerurl;
//}
-(NSString *) pickupurl
{
	return xpickupurl;
}
-(NSString *) voucherid
{
	return xvoucherid;
}
-(NSString *) pin
{
	return xpin;
}
-(NSString *) timestamp
{
	return xtimestamp;
}
-(NSString *) name
{
	return xname;
}
-(NSString *) slot
{
	return xslot;
}
-(NSString *) thumbnail
{
	return xthumbnail;
}
-(HistoryCase *)	initWithName: (NSString *)_name andSlot: (NSString *)_slot
					andWithGeneralAttributes:(NSDictionary *) _attributes
					andWithPhotoAttributes: (NSArray *) _attrdicts
{
	
	self = [super init];
	xslot = [_slot copy ];//[NSString stringWithFormat:@"%d", _slot]; //stash as string, otherwise the encode/decode is wierd
	xattributes = [_attributes copy ];
	//
	//NSLog (@"--- %d Dictionary in HistoryCase ",[xattributes retainCount]);
	xattrdicts = [_attrdicts copy];

	
	//NSLog (@"--- %d Array in HistoryCase ",[xattrdicts retainCount]);
	xname = [_name copy];	
    xtimestamp = [[_attributes objectForKey:@"remotetimestamp"] copy];		
	xpickupurl = [ [_attributes objectForKey:@"pickupurl"]copy];
	//xviewerurl = [ [_attributes objectForKey:@"viewerurl"]copy];
    xpin  = [[_attributes objectForKey:@"pin"] copy];
	xvoucherid= [ [_attributes objectForKey:@"voucherid"] copy];	
	xsha1= [ [_attributes objectForKey:@"remoteseriessha1"] copy];	
	NSDictionary *dict0 = [_attrdicts  objectAtIndex: 0]; //
	
	xthumbnail = [[dict0 objectForKey:@"remoteurl"] copy];
	return self;
}


#pragma mark delegate methods
- (void) encodeWithCoder: (NSCoder *) coder 
{
    [coder encodeObject: xname
				 forKey: kHistoryCaseNameKey];
    [coder encodeObject: xtimestamp
				 forKey: kHistoryCaseTimeStampKey];
    [coder encodeObject: xvoucherid
				 forKey: kHistoryCaseVoucherIdKey];
    [coder encodeObject: xpin
				 forKey: kHistoryCasePINKey];
	[coder encodeObject: xslot
				 forKey: kHistoryCaseSlotKey];
	[coder encodeObject: xpickupurl
				 forKey: kHistoryCasePickupURLKey];	
//	[coder encodeObject: xviewerurl
//				 forKey: kHistoryCaseViewerURLKey];
	[coder encodeObject: xattributes
				 forKey: kHistoryCaseAttributesKey];
	[coder encodeObject: xattrdicts
				 forKey: kHistoryCaseAttrDictsKey];
	[coder encodeObject: xsha1
				 forKey: kHistoryCaseSha1Key];
	[coder encodeObject: xthumbnail
				 forKey: kHistoryCaseThumbnailKey];
	
} // encodeWithCoder

- (id) initWithCoder: (NSCoder *) decoder {
    if (self = [super init]) {
		xname = [[decoder decodeObjectForKey: kHistoryCaseNameKey] retain];       
		xtimestamp = [[decoder decodeObjectForKey: kHistoryCaseTimeStampKey] retain]; 
	    xvoucherid = [[decoder decodeObjectForKey: kHistoryCaseVoucherIdKey] retain]; 
		xpin = [[decoder decodeObjectForKey: kHistoryCasePINKey] retain];
		xslot = [[decoder decodeObjectForKey: kHistoryCaseSlotKey] retain];
		xpickupurl = [[decoder decodeObjectForKey: kHistoryCasePickupURLKey] retain];		
	//	xviewerurl = [[decoder decodeObjectForKey: kHistoryCaseViewerURLKey] retain];		
		xattributes = [[decoder decodeObjectForKey: kHistoryCaseAttributesKey] retain];		
		xattrdicts = [[decoder decodeObjectForKey: kHistoryCaseAttrDictsKey] retain];		
		xsha1 = [[decoder decodeObjectForKey: kHistoryCaseSha1Key] retain];
		xthumbnail = [[decoder decodeObjectForKey: kHistoryCaseThumbnailKey] retain];
	}
    return (self);
} // initWithCoder	
@end

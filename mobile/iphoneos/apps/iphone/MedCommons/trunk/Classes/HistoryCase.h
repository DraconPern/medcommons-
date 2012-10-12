//
//  HistoryCase.h
//  MedCommons
//
//  Created by bill donner on 10/15/09.
//  Copyright 2009 MedCommons, Inc.. All rights reserved.
//

#define kHistoryCaseNameKey @"1"
#define kHistoryCaseTimeStampKey @"2"
#define kHistoryCaseVoucherIdKey @"3"
#define kHistoryCasePINKey @"4"
#define kHistoryCasePickupURLKey  @"5"
#define kHistoryCaseViewerURLKey @"6"
#define kHistoryCaseAttrDictsKey @"7"
#define kHistoryCaseAttributesKey @"8"
#define kHistoryCaseSlotKey @"9"
#define kHistoryCaseSha1Key  @"10"
#define kHistoryCaseThumbnailKey  @"11"

@interface HistoryCase : NSObject <NSCoding> {
	NSString *xtimestamp;
	NSString *xname;
	NSString *xvoucherid;
	NSString *xpin;
	NSString *xpickupurl;
//	NSString *xviewerurl;
	NSDictionary *xattributes;
	NSString *xslot;
	NSString *xsha1;
	NSString *xthumbnail;
	NSMutableArray *xattrdicts; // array of NSDictionaries	
}
-(void) dumpCase;
-(NSString *) sha1;
-(NSString *) timestamp;
-(NSString *) name;
-(NSString *) voucherid;
-(NSString *) pin;
-(NSString *) pickupurl;
//-(NSString *) viewerurl;
-(NSDictionary *) attributes;
-(NSString *) slot;
-(NSString *) thumbnail;
-(NSArray *)attrdicts;
-(HistoryCase *)	initWithName: (NSString *)_name andSlot: (NSString *)_slot
	 andWithGeneralAttributes:(NSDictionary *) _attributes
	   andWithPhotoAttributes: (NSArray *) _attrdicts;


@end


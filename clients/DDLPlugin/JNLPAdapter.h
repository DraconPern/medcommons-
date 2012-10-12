//
//  JNLPAdapter.h
//  DDLPlugin
//
//  Created by Donald Way on 5/28/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

struct Remote {
	NSString *defaultDICOMRemoteAETitle;
	NSString *defaultDICOMRemoteDicomPort;
	NSString *defaultDICOMRemoteHost;
	};

struct Local {
	NSString *defaultDICOMLocalAETitle;
	NSString *defaultDICOMLocalDicomPort;
	};

@interface JNLPAdapter:NSObject {
	NSXMLDocument *doc;
}
-(id)initUsingResource:(NSString *)resource ofType:(NSString *)type;
-(id)initUsingURL:(NSURL *)URL;
-(struct Local)exchange: (struct Remote)input;
-(NSData *)instantiate;
@end

//
//  JNLPAdapter.m
//  DDLPlugin
//
//  Created by Donald Way on 5/28/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

#import "JNLPAdapter.h"

@implementation JNLPAdapter

-(id)initUsingResource:(NSString *)resource ofType:(NSString *)type {
	NSBundle *bundle = [NSBundle bundleForClass:[self class]];
	NSString *path = [bundle pathForResource:resource ofType:type];
	NSData *data = [NSData dataWithContentsOfFile: path];
	NSError *error;
	doc = [[NSXMLDocument alloc] initWithData:data options:NSXMLDocumentTidyXML error:&error];
	return self;
}

-(id)initUsingURL:(NSURL *)URL {
	NSData *data = [NSData dataWithContentsOfURL: URL];
	NSError *error;
	doc = [[NSXMLDocument alloc] initWithData:data options:NSXMLDocumentTidyXML error:&error];
	return self;
}

- (struct Local) exchange:(struct Remote)input {
	struct Local output;
	NSError *error;
	NSArray *nodes = [doc nodesForXPath:@".//resources/property" error:&error];
	unsigned i, n = [nodes count];
	for (i = 0; i < n; i++) {
		NSXMLElement *elem = [nodes objectAtIndex:i];
		NSString *name = [[elem attributeForName:@"name"] stringValue];
		NSXMLNode *value = [elem attributeForName:@"value"];
		if ([name compare:@"defaultDICOMRemoteAETitle"] == NSOrderedSame) {
			[value setStringValue:input.defaultDICOMRemoteAETitle];
		}
		if ([name compare:@"defaultDICOMRemoteDicomPort"] == NSOrderedSame) {
			[value setStringValue:input.defaultDICOMRemoteDicomPort];
		}
		if ([name compare:@"defaultDICOMRemoteHost"] == NSOrderedSame) {
			[value setStringValue:input.defaultDICOMRemoteHost];
		}
		if ([name compare:@"defaultDICOMLocalAETitle"] == NSOrderedSame) {
			output.defaultDICOMLocalAETitle = [value stringValue];
		}
		if ([name compare:@"defaultDICOMLocalDicomPort"] == NSOrderedSame) {
			output.defaultDICOMLocalDicomPort = [value stringValue];
		}
	}
 	return output;
}

- (NSData *) instantiate {
	NSData *data = [doc XMLData];
	return data;
}
@end


//
//  DDLFilter.m
//  DDL
//
//  Copyright (c) 2008 MedCommons. All rights reserved.
//

#import "DDLFilter.h"
#import "JNLPAdapter.h"

@implementation DDLFilter

NSString *fetchDDL = @"http://stego.myhealthespace.com/DDL/app/ddl.jnlp";
NSString *statusDDL = @"http://localhost:16092/localDDL/status.html";

- (struct Remote) getRemote {
	struct Remote remote;
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	remote.defaultDICOMRemoteAETitle = [defaults objectForKey: @"AETITLE"];
	remote.defaultDICOMRemoteDicomPort = [defaults objectForKey: @"AEPORT"];
	remote.defaultDICOMRemoteHost = @"127.0.0.1";
	return remote;
}

- (void) addLocal:(struct Local)local {
	NSArray	*array = [NSArray arrayWithContentsOfFile: [[NSBundle bundleForClass:[self class]] pathForResource:@"ddl" ofType:@"plist"]];
	NSMutableDictionary *dict = [[array objectAtIndex: 0] mutableCopy];
	[dict setValue:local.defaultDICOMLocalAETitle forKey:@"AETitle"];
	[dict setValue:local.defaultDICOMLocalDicomPort forKey:@"Port"];
	NSArray	*servers = [[NSUserDefaults standardUserDefaults] arrayForKey: @"SERVERS"];
	int i, n = [servers count];
	for (i = 0; i < n; i++) {
		NSDictionary *obj = [servers objectAtIndex: i];
		if ([[obj valueForKey:@"AETitle"] isEqualToString: local.defaultDICOMLocalAETitle])
			break;
	}
	if (i == n)
		[[NSUserDefaults standardUserDefaults] setObject: [servers arrayByAddingObject: [dict copy]] forKey: @"SERVERS"];
	[dict release];
}

- (void) initPlugin {
	controller = nil;

//	NSRunAlertPanel(@"DDL", @"initPlugin1", nil, nil, nil);

	JNLPAdapter *adapter = [[JNLPAdapter alloc] initUsingURL:[NSURL URLWithString:fetchDDL]];

	[self addLocal: [adapter exchange:[self getRemote]]];

	NSData *data = [adapter instantiate];
	NSString *path = [[NSBundle bundleForClass:[self class]] pathForResource:@"ddl" ofType:@"jnlp"];
	[data writeToFile: path atomically: NO];

//	[[NSWorkspace sharedWorkspace] openURL: [NSURL fileURLWithPath: [path stringByExpandingTildeInPath]]];
	[[NSWorkspace sharedWorkspace] openURLs:[NSArray arrayWithObject:[NSURL fileURLWithPath: [path stringByExpandingTildeInPath]]]
		withAppBundleIdentifier:@"com.apple.JavaWebStart"
		options:NSWorkspaceLaunchDefault
		additionalEventParamDescriptor:nil
		launchIdentifiers:nil];
}

- (long) filterImage:(NSString*) menuName {
//	NSRunAlertPanel(@"DDL", @"filterImage", nil, nil, nil);
	if (! controller)
		controller = [[DDLWebController alloc] initWithURL: [NSURL URLWithString: statusDDL]];

	[controller showWindow: nil];
	return 0;
}

@end

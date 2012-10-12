//
//  main.m
//  MedCommons
//
//  Created by bill donner on 2/4/10.
//  Copyright Apple Inc 2010. All rights reserved.
//
//

int main(int argc, char *argv[])
{
	NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];
	int retVal = UIApplicationMain(argc, argv, nil, @"MedCommonsAppDelegate");
	[pool release];
	return retVal;
}

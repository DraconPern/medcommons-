//
//  main.m
//
//  Copyright 2010 Copyright 2011 Bill Donner and GigStand.Net, Inc. All rights reserved.
//

//http://www.nytimes.com/2006/08/21/technology/21ecom.html?_r=1

#import <UIKit/UIKit.h>

int main (
          int argc,
          char *argv []
          )
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    int retVal = UIApplicationMain (argc,
                                    argv,
                                    nil,
                                    @"GigStandAppDelegate");
    [pool release];
    return retVal;
}

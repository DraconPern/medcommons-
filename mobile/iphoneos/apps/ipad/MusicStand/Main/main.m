//
//  main.m
//  MCProvider
//
//  Created by Bill Donner on 3/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

//http://www.nytimes.com/2006/08/21/technology/21ecom.html?_r=1

#import <UIKit/UIKit.h>

int main (int argc,
          char *argv [])
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    NSString          *delegateClassName = nil;

    switch (UI_USER_INTERFACE_IDIOM ())
    {
        case UIUserInterfaceIdiomPad :
            delegateClassName = @"AppDelegate_iPad";
            break;

        case UIUserInterfaceIdiomPhone :
            delegateClassName = @"AppDelegate_iPhone";
            break;

        default :
            NSCAssert1 (NO,
                        @"Unknown user interface idiom: %d",
                        UI_USER_INTERFACE_IDIOM ());
            break;
    }

    int retVal = UIApplicationMain (argc,
                                    argv,
                                    nil,
                                    delegateClassName);

    [pool release];

    return retVal;
}

//
//  DDLFilter.h
//  DDL
//
//  Copyright (c) 2008 MedCommons. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PluginFilter.h"

#import "DDLWebController.h"

@interface DDLFilter : PluginFilter {
	DDLWebController *controller;
}

- (long) filterImage:(NSString*) menuName;

@end

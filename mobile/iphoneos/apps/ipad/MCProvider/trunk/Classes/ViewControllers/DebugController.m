//
//  DebugController.m
//  MCProvider
//
//  Created by Bill Donner on 4/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "DataManager.h"
#import "DebugController.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"

#define TOP_LOG(format, ...)     [self traceLogTopDetail: [NSString stringWithFormat: format, ## __VA_ARGS__]];

#define BOTTOM_LOG(format, ...)  [self traceLogBottomDetail: [NSString stringWithFormat: format, ## __VA_ARGS__]];

#define TOP_INFO(format, ...)    [self traceLogTopInfo: [NSString stringWithFormat: format, ## __VA_ARGS__]];

#define BOTTOM_INFO(format, ...) [self traceLogBottomInfo: [NSString stringWithFormat: format, ## __VA_ARGS__]];

@implementation DebugController

@dynamic hidesMasterViewInLandscape;

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

#pragma mark traceLog Support

- (void) traceLogTopDetail: (NSString *) s
{
    topTextView_.text = s;       // set top part
}

- (void ) traceLogBottomDetail: (NSString *) s
{
    bottomTextView_.text = s;    // set bottom part
}

- (void) traceLogTopInfo: (NSString *) s
{
    topTextView_.text = s;       // set top part
}

- (void) traceLogBottomInfo: (NSString *) s
{
    bottomTextView_.text = s;    // set bottom part
}


- (void) loadView
{
    StyleManager *styles = self.appDelegate.styleManager;

    // text window with 2 frames

    CGRect thisFrame = [[UIScreen mainScreen] applicationFrame];
    thisFrame.size.height = thisFrame.size.height - 44.0f;
    thisFrame.origin.y  = thisFrame.origin.y + 44.0f;
    thisFrame.size.width = 768.0f;

    traceView_ = [[UIView alloc] initWithFrame:thisFrame];
    traceView_.backgroundColor = styles.backgroundColorLighter;

    CGRect topTextLabelFrame = CGRectMake(10.0f, 10.0f, 748.0f, 18.0f);
    CGRect topTextFrame = CGRectMake(10.f,30.f,748.0f,80.f);

    CGRect bottomTextLabelFrame = CGRectMake(10.0f, 140.0f, 748.0f, 18.0f);
    CGRect bottomTextFrame = CGRectMake(10.0f, 160.0f, 748.0f, 800.0f);

    topLabel_ = [[UILabel alloc] initWithFrame:topTextLabelFrame];
    topLabel_.text = @"Local:";
    topTextView_ = [[UITextView alloc] initWithFrame:topTextFrame];
    topTextView_.text = @"top text not set";
    topTextView_.backgroundColor = [UIColor lightGrayColor];
    topTextView_.editable = NO;

    bottomLabel_ = [[UILabel alloc] initWithFrame:bottomTextLabelFrame];
    bottomLabel_.text = @"STDERR";
    bottomTextView_ = [[UITextView alloc] initWithFrame:bottomTextFrame];
    bottomTextView_.text = @"bottom text not set";
    bottomTextView_.editable = NO;
    bottomTextView_.backgroundColor = styles.backgroundColorLight;
	
	
#define STDERR_OUT [NSHomeDirectory() stringByAppendingPathComponent:@"tmp/stderr.txt"]
	

		NSError *error;
		NSStringEncoding encoding;
		NSString *contents = [NSString stringWithContentsOfFile:STDERR_OUT usedEncoding:&encoding error: &error];

		[bottomTextView_ setText:contents];

    [traceView_ addSubview:topLabel_];
    [traceView_ addSubview:topTextView_];
    [traceView_ addSubview:bottomLabel_];
    [traceView_ addSubview:bottomTextView_];

    self.view = traceView_;


}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (id) init
{
    self = [super init];
    return self;
}

- (void) dealloc
{
    [traceView_ release];
    [topTextView_ release];
    [bottomTextView_ release];
    [topLabel_ release];
    [bottomLabel_ release];

    [super dealloc];
}





@end

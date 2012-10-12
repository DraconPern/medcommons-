//
//  CCRViewController.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/19/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "CCRActionController.h"
#import "CCRActionItem.h"
#import "CCRButton.h"
#import "CCREpisodeActionItem.h"
#import "CCRHeaderView.h"
#import "CCRMainView.h"
#import "CCRScrubberView.h"
#import "CCRShareActionController.h"
#import "CCRToolCell.h"
#import "CCRToolListView.h"
#import "CCRThumbCell.h"
#import "CCRThumbListView.h"
#import "CCRView.h"
#import "CCRViewController.h"
#import "DictionaryAdditions.h"
#import "Member.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "WebViewController.h"

#pragma mark Internal Constants

#define TRACE_JS_ACTIONS       1
#define TRACE_JS_CALLBACKS    1

#define COMPONENT_EPISODES     @"episodes"
#define COMPONENT_HEADER       @"head"
#define COMPONENT_MAIN         @"main"
#define COMPONENT_MENU         @"menu"
#define COMPONENT_SCRUBBER     @"scrub"
#define COMPONENT_THUMB        @"thumb"
#define COMPONENT_THUMBS       @"thumbs"
#define COMPONENT_TOOL         @"tool"
#define COMPONENT_TOOLS        @"tools"
#define COMPONENT_OVERLAY      @"overlay"

#define HEADER_ID_DOCUMENTS    @"documents"
#define HEADER_ID_EPISODES     @"episodes"
#define HEADER_ID_SUBTITLE     @"subtitle"
#define HEADER_ID_TITLE        @"title"

#define KEY_COMPONENT          @"comp"
#define KEY_CURRENT_VALUE      @"curval"
#define KEY_DOCUMENT_LIST      @"doclist"
#define KEY_IDENTIFIER         @"id"
#define KEY_IMAGE              @"img"
#define KEY_JS_EVENT           @"jsevt"
#define KEY_JS_PARAMETER       @"jsprm"
#define KEY_MAXIMUM_VALUE      @"maxval"
#define KEY_MINIMUM_VALUE      @"minval"
#define KEY_OPERATION          @"op"
#define KEY_SELECT             @"sel"
#define KEY_SUBTITLE           @"subtitle"
#define KEY_TITLE              @"title"
#define KEY_URL                @"url"
#define KEY_VISIBLE            @"vis"
#define KEY_ZOOM               @"zoom"

#define OPERATION_ADD          @"add"
#define OPERATION_DELETE       @"del"
#define OPERATION_MODIFY       @"mod"

#define VISIBLE_HIDE           @"hide"
#define VISIBLE_SHOW           @"show"

#define ZOOM_MAXIMUM           @"max"
#define ZOOM_MINIMUM           @"min"
#define ZOOM_NORMAL            @"norm"

#define JS_ACTION_FORMAT       @"doit('%@','%@');"
#define JS_ACTION_FORMAT2      @"doit('%@', %f);"
#define JS_CALLBACK_URL_PREFIX @"mc://viewer"

#define CLASS_RESOURCE_NAME    @"CCRViewController"
#define CLASS_RESOURCE_TYPE    @"plist"

enum
{
    MENU_SEGMENT_INDEX     = 0,
    EPISODES_SEGMENT_INDEX
};

//
// From AQGridViewController.m:
//
@interface AQGridView (AQGridViewPrivate)

- (void) viewDidRotate;

- (void) viewWillRotateToInterfaceOrientation: (UIInterfaceOrientation) toOrient;

@end

#pragma mark -
#pragma mark Public Class CCRViewController
#pragma mark -

@interface CCRViewController () <UIWebViewDelegate>

@property (nonatomic, retain, readwrite) UIBarButtonItem          *backButton;
@property (nonatomic, retain, readonly)  CCRActionController      *episodesActionController;
@property (nonatomic, retain, readonly)  CCRHeaderView            *headerView;
@property (nonatomic, retain, readonly)  CCRMainView              *mainView;
@property (nonatomic, retain, readonly)  CCRShareActionController *menuActionController;
@property (nonatomic, retain, readonly)  CCRScrubberView          *scrubberView;
@property (nonatomic, retain, readwrite) UIBarButtonItem          *segmentedButton;
@property (nonatomic, retain, readwrite) UISegmentedControl       *segmentedControl;
@property (nonatomic, retain, readonly)  CCRThumbListView         *thumbListView;
@property (nonatomic, retain, readonly)  CCRToolListView          *toolListView;
@property (nonatomic, copy,   readonly)  NSURL                    *URL;

- (void) addEpisodesComponent: (NSDictionary *) params;

- (void) addMenuComponent: (NSDictionary *) params;

- (void) addThumbComponent: (NSDictionary *) params;

- (void) addToolComponent: (NSDictionary *) params;

- (void) changeTheme: (NSString *) theme;

- (void) chooseAction: (id) sender;

- (void) chooseEpisodesAction;

- (void) chooseMenuAction;

- (void) clickedButton: (CCRButton *) button;

- (void) clickedEpisodesItem: (CCRActionItem *) item;

- (void) clickedMenuItem: (CCRActionItem *) item;

- (void) clickedThumbListCell: (CCRThumbCell *) cell;

- (void) deleteEpisodesComponent: (NSDictionary *) params;

- (void) deleteMenuComponent: (NSDictionary *) params;

- (void) deleteThumbComponent: (NSDictionary *) params;

- (void) deleteToolComponent: (NSDictionary *) params;

- (NSString *) injectJavaScript: (NSString *) jsString;

- (void) modifyEpisodesComponent: (NSDictionary *) params;

- (void) modifyHeaderComponent: (NSDictionary *) params;

- (void) modifyMainComponent: (NSDictionary *) params;

- (void) modifyMenuComponent: (NSDictionary *) params;

- (void) modifyScrubberComponent: (NSDictionary *) params;

- (void) modifyThumbComponent: (NSDictionary *) params;

- (void) modifyThumbsComponent: (NSDictionary *) params;

- (void) modifyToolComponent: (NSDictionary *) params;

- (void) modifyToolsComponent: (NSDictionary *) params;

- (void) movedScrubber;

- (NSDictionary *) parseJavaScriptCallbackURL: (NSURL *) URL;

- (void) performJavaScriptCallbackWithParameters: (NSDictionary *) params;

- (void) resetComponents;

- (void) showEpisodesActionController;

- (void) showMenuActionController;

- (void) updateNavigationItemAnimated: (BOOL) animated;

@end

@implementation CCRViewController

static NSDictionary *ImageNameDictionary;

@synthesize backButton               = backButton_;
@synthesize episodesActionController = episodesActionController_;
@dynamic    headerView;
@dynamic    mainView;
@synthesize menuActionController     = menuActionController_;
@dynamic    scrubberView;
@synthesize segmentedButton          = segmentedButton_;
@synthesize segmentedControl         = segmentedControl_;
@dynamic    thumbListView;
@dynamic    toolListView;
@synthesize URL                      = URL_;

#pragma mark Public Instance Methods

- (id) initWithURL: (NSURL *) URL
{
    self = [super init];
	
    if (self)
    {
        self->URL_ = [URL copy];
		
        self->episodesActionController_ = [[CCRActionController alloc]
                                           initWithTitle: NSLocalizedString (@"Episodes", @"")];
		
        self->menuActionController_ = [[CCRShareActionController alloc] initWithViewController: self
                                                                                       webView: self.mainView];
    }
	
    return self;
}

#pragma mark Private Instance Methods

- (void) addEpisodesComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.addEpisodesComponent: %@", params);
	
    //
    // Required parameters: id, title
    // Optional parameters: doclist, jsevt, jsprm
    //
    NSString *ident = [params stringForKey: KEY_IDENTIFIER];
    NSString *title = [params stringForKey: KEY_TITLE];
	
    if (ident &&
        title &&
        ([self.episodesActionController itemWithIdentifier: ident] == nil))
    {
        CCREpisodeActionItem *item = [[[CCREpisodeActionItem alloc] initWithIdentifier: ident]
                                      autorelease];
		
        item.action = @selector (clickedEpisodesItem:);
        item.jsEvent = [params stringForKey: KEY_JS_EVENT];
        item.jsParameter = [params stringForKey: KEY_JS_PARAMETER];
        item.target = self;
        item.title = title;
		
        NSString *tmpString = [params stringForKey: KEY_DOCUMENT_LIST];
		
        if (tmpString)
            item.docList = [tmpString componentsSeparatedByString: @","];
		
        [self.episodesActionController addItem: item];
    }
}

- (void) addMenuComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.addMenuComponent: %@", params);
	
    //
    // Required parameters: id, title
    // Optional parameters: jsevt, jsprm
    //
    NSString *ident = [params stringForKey: KEY_IDENTIFIER];
    NSString *title = [params stringForKey: KEY_TITLE];
	
    if (ident &&
        title &&
        ([self.menuActionController itemWithIdentifier: ident] == nil))
    {
        CCRActionItem *item = [[[CCRActionItem alloc] initWithIdentifier: ident]
                               autorelease];
		
        item.action = @selector (clickedMenuItem:);
        item.jsEvent = [params stringForKey: KEY_JS_EVENT];
        item.jsParameter = [params stringForKey: KEY_JS_PARAMETER];
        item.target = self;
        item.title = title;
		
        [self.menuActionController addItem: item];
    }
}
- (void) addOverlayComponent: (NSDictionary *) params
{
	//Presents A UIWEBVIEW AS A NAV CONTROLLER
	//  the controller will be dismissed by the end user and is out of reach of the JS once initiated
    //NSLog (@"*** CCRViewController.addOverlayComponent: %@", params);
	
    //
    // Required parameters: id, url
    // Optional parameters: title,  jsevt, jsprm
    //
    NSString *ident = [params stringForKey: KEY_IDENTIFIER];
    NSURL    *overlayURL = [params URLForKey: KEY_URL];
	NSString *ptitle = [params stringForKey:KEY_TITLE];
	
    if (ident &&
        overlayURL)
    {
//		CGRect tmpFrame;
//		
//		//
//		// Background view:
//		//
//		tmpFrame = CGRectStandardize (self.parentViewController.view.bounds);
//		
//		UIView *backgroundView = [[[UIView alloc] initWithFrame: tmpFrame]
//								  autorelease];
//		
//		backgroundView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
//										   UIViewAutoresizingFlexibleWidth);
//		backgroundView.backgroundColor = [UIColor lightGrayColor];
//		
//		//
//		// Content view:
//		//
//		tmpFrame = CGRectStandardize (backgroundView.bounds);
//		
//		CGFloat edgeInset = 5.0f;
//		
//		//
//		// If desired, inset content view frame a bit to help in debugging:
//		//
//		if (edgeInset > 0.0f)
//			tmpFrame = UIEdgeInsetsInsetRect (tmpFrame,
//											  UIEdgeInsetsMake (edgeInset,
//																edgeInset,
//																edgeInset,
//																edgeInset));
//		
//		wv = [[UIWebView alloc] initWithFrame: tmpFrame];
//		
//		wv.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
//							   UIViewAutoresizingFlexibleWidth);
//		wv.backgroundColor = [UIColor blueColor];
//		wv.dataDetectorTypes = UIDataDetectorTypeAll; // no phone numbers
//		wv.delegate = nil;
//		wv.scalesPageToFit = YES;
//		
//		wv.title = [params stringForKey: KEY_TITLE];
//		[backgroundView addSubview: wv];
//		
//		
//		
//		self.view = backgroundView;
		//
//		//
//		// Kick off initial load of content view:
//		//
//		[wv loadRequest: [NSURLRequest requestWithURL: overlayURL]];
		
		WebViewController *wv = [[WebViewController alloc] initWithURL: overlayURL];
		if (ptitle) wv.title = ptitle;
		[self.navigationController pushViewController:wv animated:YES];
		
		[wv release];
    }
}
- (void) addThumbComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.addThumbComponent: %@", params);
	
    //
    // Required parameters: id, url
    // Optional parameters: title, subtitle, jsevt, jsprm
    //
    NSString *ident = [params stringForKey: KEY_IDENTIFIER];
    NSURL    *thumbURL = [params URLForKey: KEY_URL];
	
    if (ident &&
        thumbURL &&
        ([self.thumbListView cellWithIdentifier: ident] == nil))
    {
        CCRThumbCell *cell = [[[CCRThumbCell alloc]
                               initWithIdentifier: ident]
                              autorelease];
		
        cell.action = @selector (clickedThumbListCell:);
        cell.jsEvent = [params stringForKey: KEY_JS_EVENT];
        cell.jsParameter = [params stringForKey: KEY_JS_PARAMETER];
        cell.subtitle = [params stringForKey: KEY_SUBTITLE];
        cell.target = self;
        cell.title = [params stringForKey: KEY_TITLE];
		
        [cell loadImageFromURL: thumbURL];
		
        [self.thumbListView addCell: cell];
		
        [(CCRView *) self.view forceRecomputeSubviews];
    }
}

- (void) addToolComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.addToolComponent: %@", params);
	
    //
    // Required parameters: id, img
    // Optional parameters: jsevt, jsprm
    //
    NSString *ident = [params stringForKey: KEY_IDENTIFIER];
    NSString *imageName = [params stringForKey: KEY_IMAGE];
	
    if (imageName)
        imageName = [ImageNameDictionary stringForKey: imageName];
	
    if (ident &&
        imageName &&
        ([self.toolListView cellWithIdentifier: ident] == nil))
    {
        CCRToolCell *cell = [[[CCRToolCell alloc]
                              initWithIdentifier: ident]
                             autorelease];
		
        cell.action =  @selector (clickedButton:);
        cell.image = [UIImage imageNamed: imageName];
        cell.jsEvent = [params stringForKey: KEY_JS_EVENT];
        cell.jsParameter = [params stringForKey: KEY_JS_PARAMETER];
        cell.target = self;
		
        [self.toolListView addCell: cell];
		
        [(CCRView *) self.view forceRecomputeSubviews];
    }
}

- (void) changeTheme: (NSString *) theme
{
    if (!theme)
        theme = @"Pitch Black";
	
    UIColor *parentColor = nil;
    UIColor *thumbsColor = nil;
    UIColor *scrubColor = nil;
    UIColor *toolsColor = nil;
    UIColor *mainColor = nil;
	
    if ([theme isEqualToString: @"Dark Gray"])
    {
        parentColor = [UIColor colorWithWhite: 0.125f
                                        alpha: 1.0f];
    }
    else if ([theme isEqualToString: @"Dark Texture"])
    {
        parentColor = [UIColor viewFlipsideBackgroundColor];
    }
    else if ([theme isEqualToString: @"Exploding Clown"])
    {
        parentColor = [UIColor yellowColor];
        mainColor = [UIColor redColor];
        scrubColor = [UIColor greenColor];
        thumbsColor = [UIColor orangeColor];
        toolsColor = [UIColor blueColor];
    }
    else if ([theme isEqualToString: @"Light Texture"])
    {
        parentColor = [UIColor scrollViewTexturedBackgroundColor];
    }
    else if ([theme isEqualToString: @"Pitch Black"])
    {
        parentColor = [UIColor blackColor];
    }
	
    self.view.backgroundColor = parentColor;
	
    self.mainView.backgroundColor = (mainColor ? mainColor : parentColor);
    self.scrubberView.backgroundColor = (scrubColor ? scrubColor : parentColor);
    self.thumbListView.backgroundColor = (thumbsColor ? thumbsColor : parentColor);
    self.toolListView.backgroundColor = (toolsColor ? toolsColor : parentColor);
	
    [self.thumbListView reloadData];
    [self.toolListView reloadData];
}

- (void) chooseAction: (id) sender
{
    //NSLog (@"*** CCRViewController.chooseAction: %@", sender);
	
    switch (self.segmentedControl.selectedSegmentIndex)
    {
        case EPISODES_SEGMENT_INDEX :
            [self chooseEpisodesAction];
            break;
			
        case MENU_SEGMENT_INDEX :
            [self chooseMenuAction];
            break;
			
        default :
            break;
    }
}

- (void) chooseEpisodesAction
{
    if (self.menuActionController.isVisible)
        [self.menuActionController dismissWithCancelButtonAnimated: YES];
	
    if (!self.episodesActionController.isVisible)
        [self showEpisodesActionController];
}

- (void) chooseMenuAction
{
    if (self.episodesActionController.isVisible)
        [self.episodesActionController dismissWithCancelButtonAnimated: YES];
	
    if (!self.menuActionController.isVisible)
        [self showMenuActionController];
}

- (void) clickedButton: (CCRButton *) button
{
    //NSLog (@"*** CCRViewController.clickedButton: %@", button);
	
    [self injectJavaScript: [NSString stringWithFormat:
                             JS_ACTION_FORMAT,
                             button.jsEvent,
                             button.jsParameter]];
}

- (void) clickedEpisodesItem: (CCRActionItem *) item
{
    //NSLog (@"*** CCRViewController.clickedEpisodesItem: %@", item);
	
    //
    // Episode is about to change; reset header, scrubber, thumbs, and tools
    // components to initial values:
    //
    [self resetComponents];
	
    [self injectJavaScript: [NSString stringWithFormat:
                             JS_ACTION_FORMAT,
                             item.jsEvent,
                             item.jsParameter]];
}

- (void) clickedMenuItem: (CCRActionItem *) item
{
    //NSLog (@"*** CCRViewController.clickedMenuItem: %@", item);
	
    [self injectJavaScript: [NSString stringWithFormat:
                             JS_ACTION_FORMAT,
                             item.jsEvent,
                             item.jsParameter]];
}

- (void) clickedThumbListCell: (CCRThumbCell *) cell
{
    //NSLog (@"*** CCRViewController.clickedThumbListCell: %@", cell);
	
    [self injectJavaScript: [NSString stringWithFormat:
                             JS_ACTION_FORMAT,
                             cell.jsEvent,
                             cell.jsParameter]];
}

- (void) deleteEpisodesComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.deleteEpisodesComponent: %@", params);
	
    //
    // Required parameters: id
    // Optional parameters: (none)
    //
    [self.episodesActionController removeItemWithIdentifier: [params stringForKey: KEY_IDENTIFIER]];
}

- (void) deleteMenuComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.deleteMenuComponent: %@", params);
	
    //
    // Required parameters: id
    // Optional parameters: (none)
    //
    [self.menuActionController removeItemWithIdentifier: [params stringForKey: KEY_IDENTIFIER]];
}

- (void) deleteThumbComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.deleteThumbComponent: %@", params);
	
    //
    // Required parameters: id
    // Optional parameters: (none)
    //
    [self.thumbListView removeCellWithIdentifier: [params stringForKey: KEY_IDENTIFIER]];
	
    [(CCRView *) self.view forceRecomputeSubviews];
}

- (void) deleteToolComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.deleteToolComponent: %@", params);
	
    //
    // Required parameters: id
    // Optional parameters: (none)
    //
    [self.toolListView removeCellWithIdentifier: [params stringForKey: KEY_IDENTIFIER]];
	
    [(CCRView *) self.view forceRecomputeSubviews];
}

- (CCRHeaderView *) headerView
{
    return ((CCRView *) self.view).headerView;
}

- (NSString *) injectJavaScript: (NSString *) jsString
{
#if TRACE_JS_ACTIONS
    NSLog (@"> %@", jsString);
#endif
	
    return [self.mainView stringByEvaluatingJavaScriptFromString: jsString];
}

- (CCRMainView *) mainView
{
    return ((CCRView *) self.view).mainView;
}

- (void) modifyEpisodesComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.modifyEpisodesComponent: %@", params);
	
    //
    // Required parameters: id
    // Optional parameters: title, jsevt, jsprm
    //
    CCREpisodeActionItem *item = (CCREpisodeActionItem *) [self.episodesActionController itemWithIdentifier:
                                                           [params stringForKey: KEY_IDENTIFIER]];
	
    if (item)
    {
        NSString *tmpString = [params stringForKey: KEY_TITLE];
		
        if (tmpString)
            item.title = tmpString;
		
        tmpString = [params stringForKey: KEY_JS_EVENT];
		
        if (tmpString)
            item.jsEvent = tmpString;
		
        tmpString = [params stringForKey: KEY_JS_PARAMETER];
		
        if (tmpString)
            item.jsParameter = tmpString;
		
        tmpString = [params stringForKey: KEY_DOCUMENT_LIST];
		
        if (tmpString)
            item.docList = [tmpString componentsSeparatedByString: @","];
		
    }
}- (void) addHeaderComponentAKAlog: (NSDictionary *) params
{
    NSLog (@"*** CCRViewController.addHeaderComponentAKAlog: %@", params);
	
    // Just plunk into the log file , same syntax as header
    // Required parameters: id
    // Optional parameters: title, jsevt, jsprm
    //
    NSString  *ident = [params stringForKey: KEY_IDENTIFIER];
	
    if (!ident)
        return;
	
    NSString *tmpString;
	
    if ([ident isEqualToString: HEADER_ID_SUBTITLE])
    {
        tmpString = [params stringForKey: KEY_TITLE];
		
        if (tmpString)
        {
			
			NSLog (@"sb - %@",tmpString);
			
        }
		
        return;
    }
	
	
    if ([ident isEqualToString: HEADER_ID_TITLE])
    {
        tmpString = [params stringForKey: KEY_TITLE];
		if (tmpString)
        {
			
			NSLog (@"ti - %@",tmpString);
			
        }
		
        return;
		
    }
	
}
- (void) modifyHeaderComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.modifyHeaderComponent: %@", params);
	
    //
    // Required parameters: id
    // Optional parameters: title, jsevt, jsprm
    //
    NSString  *ident = [params stringForKey: KEY_IDENTIFIER];
	
    if (!ident)
        return;
	
    NSString *tmpString;
	
    if ([ident isEqualToString: HEADER_ID_SUBTITLE])
    {
        tmpString = [params stringForKey: KEY_TITLE];
		
        if (tmpString)
        {
            //            self.headerView.subtitleLabel.text = tmpString;
            //
            //            [self.headerView setNeedsLayout];
			
            self.headerView.subtitleText = tmpString;
			
            self.navigationItem.title = [NSString stringWithFormat:
                                         @"%@ - %@",
                                         self.headerView.titleText,
                                         self.headerView.subtitleText];
        }
		
        return;
    }
	
    //    CCRButton *button = nil;
    //
    //    if ([ident isEqualToString: HEADER_ID_DOCUMENTS])
    //        button = self.headerView.documentsButton;
    //    else if ([ident isEqualToString: HEADER_ID_EPISODES])
    //        button = self.headerView.episodesButton;
    //    else
    if ([ident isEqualToString: HEADER_ID_TITLE])
    {
        //        button = self.headerView.titleButton;
        tmpString = [params stringForKey: KEY_TITLE];
		
        self.headerView.titleText = tmpString;
		
        self.navigationItem.title = [NSString stringWithFormat:
                                     @"%@ - %@",
                                     self.headerView.titleText,
                                     self.headerView.subtitleText];
    }
	
    //    if (button)
    //    {
    //        button.jsEvent = [params stringForKey: KEY_JS_EVENT];
    //        button.jsParameter = [params stringForKey: KEY_JS_PARAMETER];
    //
    //        tmpString = [params stringForKey: KEY_TITLE];
    //
    //        if (tmpString)
    //        {
    //            [button setTitle: tmpString
    //                    forState: UIControlStateNormal];
    //
    //            [self.headerView setNeedsLayout];
    //        }
    //    }
}

- (void) modifyMainComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.modifyMainComponent: %@", params);
	
    //
    // Required parameters: (none)
    // Optional parameters: zoom
    //
    NSString *tmpString = [params stringForKey: KEY_ZOOM];
	
    if ([tmpString isEqualToString: ZOOM_MAXIMUM])
        [(CCRView *) self.view maximizeMainViewAnimated: YES];
    else if ([tmpString isEqualToString: ZOOM_NORMAL])
        [(CCRView *) self.view normalizeMainViewAnimated: YES];
}

- (void) modifyMenuComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.modifyMenuComponent: %@", params);
	
    //
    // Required parameters: id
    // Optional parameters: title, jsevt, jsprm
    //
    CCRActionItem *item = [self.menuActionController itemWithIdentifier:
                           [params stringForKey: KEY_IDENTIFIER]];
	
    if (item)
    {
        NSString *tmpString = [params stringForKey: KEY_TITLE];
		
        if (tmpString)
            item.title = tmpString;
		
        tmpString = [params stringForKey: KEY_JS_EVENT];
		
        if (tmpString)
            item.jsEvent = tmpString;
		
        tmpString = [params stringForKey: KEY_JS_PARAMETER];
		
        if (tmpString)
            item.jsParameter = tmpString;
    }
}

- (void) modifyScrubberComponent: (NSDictionary *) params
{
    NSLog (@"*** CCRViewController.modifyScrubberComponent: %@", params);
	
    //
    // Required parameters: (none)
    // Optional parameters: curval, minval, maxval, jsevt, vis
    //
    if ([params objectForKey: KEY_CURRENT_VALUE])
        self.scrubberView.currentValue = [params floatForKey: KEY_CURRENT_VALUE];
	
    if ([params objectForKey: KEY_MINIMUM_VALUE])
        self.scrubberView.minimumValue = [params floatForKey: KEY_MINIMUM_VALUE];
	
    if ([params objectForKey: KEY_MAXIMUM_VALUE])
        self.scrubberView.maximumValue = [params floatForKey: KEY_MAXIMUM_VALUE];
	
    NSString *tmpString;
	
    tmpString = [params stringForKey: KEY_JS_EVENT];
	
    if (tmpString)
        self.scrubberView.userInfo = tmpString;
	
    tmpString = [params stringForKey: KEY_VISIBLE];
	
    if ([tmpString isEqualToString: VISIBLE_HIDE])
        [(CCRView *) self.view hideScrubberViewAnimated: YES];
    else if ([tmpString isEqualToString: VISIBLE_SHOW])
        [(CCRView *) self.view showScrubberViewAnimated: YES];
	
    [self.scrubberView setNeedsLayout];
}

- (void) modifyThumbComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.modifyThumbComponent: %@", params);
	
    //
    // Required parameters: id
    // Optional parameters: url, title, subtitle, jsevt, jsprm, sel
    //
    CCRThumbCell *cell = [self.thumbListView cellWithIdentifier: [params stringForKey: KEY_IDENTIFIER]];
	
    if (cell)
    {
        NSURL *tmpURL = [params URLForKey: KEY_URL];
		
        if (tmpURL)
            [cell loadImageFromURL: tmpURL];
		
        NSString *tmpString = [params stringForKey: KEY_TITLE];
		
        if (tmpString)
            cell.title = tmpString;
		
        tmpString = [params stringForKey: KEY_SUBTITLE];
		
        if (tmpString)
            cell.subtitle = tmpString;
		
        tmpString = [params stringForKey: KEY_JS_EVENT];
		
        if (tmpString)
            cell.jsEvent = tmpString;
		
        tmpString = [params stringForKey: KEY_JS_PARAMETER];
		
        if (tmpString)
            cell.jsParameter = tmpString;
		
        tmpString = [params stringForKey: KEY_SELECT];
		
        if (tmpString)  // has parameter actually been set?
        {
            if ([params boolForKey: KEY_SELECT])
                self.thumbListView.activeCell = cell;
            else if (self.thumbListView.activeCell == cell)
                self.thumbListView.activeCell = nil;
        }
    }
}

- (void) modifyThumbsComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.modifyThumbsComponent: %@", params);
	
    //
    // Required parameters: (none)
    // Optional parameters: vis, zoom
    //
    NSString *tmpString = [params stringForKey: KEY_VISIBLE];
	
    if ([tmpString isEqualToString: VISIBLE_HIDE])
        [(CCRView *) self.view hideThumbListViewAnimated: YES];
    else if ([tmpString isEqualToString: VISIBLE_SHOW])
        [(CCRView *) self.view showThumbListViewAnimated: YES];
	
    tmpString = [params stringForKey: KEY_ZOOM];
	
    if ([tmpString isEqualToString: ZOOM_MAXIMUM])
        [(CCRView *) self.view maximizeThumbListViewAnimated: YES];
    else if ([tmpString isEqualToString: ZOOM_NORMAL])
        [(CCRView *) self.view normalizeThumbListViewAnimated: YES];
}

- (void) modifyToolComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.modifyToolComponent: %@", params);
	
    //
    // Required parameters: id
    // Optional parameters: img, jsevt, jsprm
    //
    CCRToolCell *cell = [self.toolListView cellWithIdentifier: [params stringForKey: KEY_IDENTIFIER]];
	
    if (cell)
    {
        NSString *tmpString = [params stringForKey: KEY_IMAGE];
		
        if (tmpString)
            tmpString = [ImageNameDictionary stringForKey: tmpString];
		
        if (tmpString)
            cell.image = [UIImage imageNamed: tmpString];
		
        tmpString = [params stringForKey: KEY_JS_EVENT];
		
        if (tmpString)
            cell.jsEvent = tmpString;
		
        tmpString = [params stringForKey: KEY_JS_PARAMETER];
		
        if (tmpString)
            cell.jsParameter = tmpString;
    }
}

- (void) modifyToolsComponent: (NSDictionary *) params
{
    //NSLog (@"*** CCRViewController.modifyToolsComponent: %@", params);
	
    //
    // Required parameters: (none)
    // Optional parameters: vis
    //
    NSString *tmpString = [params stringForKey: KEY_VISIBLE];
	
    if ([tmpString isEqualToString: VISIBLE_HIDE])
        [(CCRView *) self.view hideToolListViewAnimated: YES];
    else if ([tmpString isEqualToString: VISIBLE_SHOW])
        [(CCRView *) self.view showToolListViewAnimated: YES];
}
-(void) handle_scrub_timer

{
    if (YES==self->scrubTimerSet)
    {
        self->lastScrubTime = CFAbsoluteTimeGetCurrent();
        self->scrubTimerSet = NO;
		
		
        NSLog (@" %@ value %4.2f from timer",self->lastScrubJS,self.scrubberView.currentValue);
        [self injectJavaScript: [NSString stringWithFormat:
                                 JS_ACTION_FORMAT2,
                                 self->lastScrubJS,
                                 self.scrubberView.currentValue]];
    }
	
}


- (void) movedScrubber
{
    // don't send more than 1 scrubber message every .1 sec
    CFTimeInterval timenow = CFAbsoluteTimeGetCurrent();
    CFTimeInterval difference = timenow-self->lastScrubTime;
	
    if (difference <0.30f)
		
    {
        // if only a short time has passed, don't inject it, just save it
        // this might get redone several times before the time finally fires
		
        if (NO==self->scrubTimerSet)
        {
			
            self->lastScrubValue = self.scrubberView.currentValue; // save this value
            self->lastScrubJS =  (NSString *) self.scrubberView.userInfo;
			
			
			
			
            self->scrubTimer = [NSTimer scheduledTimerWithTimeInterval: .2f
                                                                target: self
                                                              selector: @selector (handle_scrub_timer)
                                                              userInfo: nil
                                                               repeats: NO];
			
            self->scrubTimerSet = YES;
			
            return; // don't do anything more it will get handled by the timer
			
        }
		
        return;
		
    }
	
    else
    {
        // inject javascript now
		
        NSString *jsEvent = (NSString *) self.scrubberView.userInfo;
        NSLog (@" %@ value %4.2f elapsed %2.3f ms",jsEvent,self.scrubberView.currentValue, difference);
        self->lastScrubTime = timenow;
        [self injectJavaScript: [NSString stringWithFormat:
                                 JS_ACTION_FORMAT2,
                                 jsEvent,
                                 self.scrubberView.currentValue]];
		
    }
}

- (NSDictionary *) parseJavaScriptCallbackURL: (NSURL *) URL
{
    //NSLog (@"*** CCRViewController.parseJavaScriptCallbackURL: %@", URL);
	
    //
    // Expect URL with syntax: "mc://viewer?key1=val1&key2=val2&...":
    //
    NSMutableDictionary *params = [NSMutableDictionary dictionary];
	
    for (NSString *queryPair in [[URL query] componentsSeparatedByString: @"&"])
    {
        NSArray *query = [queryPair componentsSeparatedByString: @"="];
		
        if ([query count] == 2)
        {
            NSString *key = [[query objectAtIndex: 0] stringByURLDecoding];
            NSString *obj = [[query objectAtIndex: 1] stringByURLDecoding];
			
            if (key && obj)
                [params setObject: obj
                           forKey: key];
        }
    }
	
    return params;
}

- (void) performJavaScriptCallbackWithParameters: (NSDictionary *) params
{
	NSString *comp = [params stringForKey: KEY_COMPONENT];
    NSString *op = [params stringForKey: KEY_OPERATION];
	
	if ([comp isEqualToString: COMPONENT_THUMB]) // the most frequent
    {
        if ([op isEqualToString: OPERATION_ADD])
            [self addThumbComponent: params];
        else if ([op isEqualToString: OPERATION_DELETE])
            [self deleteThumbComponent: params];
        else if ([op isEqualToString: OPERATION_MODIFY])
            [self modifyThumbComponent: params];
    }
	else if ([comp isEqualToString: COMPONENT_TOOL])
    {
        if ([op isEqualToString: OPERATION_ADD])
            [self addToolComponent: params];
        else if ([op isEqualToString: OPERATION_DELETE])
            [self deleteToolComponent: params];
        else if ([op isEqualToString: OPERATION_MODIFY])
            [self modifyToolComponent: params];
    }
    else if ([comp isEqualToString: COMPONENT_TOOLS])
    {
        if ([op isEqualToString: OPERATION_MODIFY])
            [self modifyToolsComponent: params];
    }
	else if ([comp isEqualToString: COMPONENT_EPISODES])
    {
        if ([op isEqualToString: OPERATION_ADD])
            [self addEpisodesComponent: params];
        else if ([op isEqualToString: OPERATION_DELETE])
            [self deleteEpisodesComponent: params];
        else if ([op isEqualToString: OPERATION_MODIFY])
            [self modifyEpisodesComponent: params];
    }
    else if ([comp isEqualToString: COMPONENT_HEADER])
    {
        if ([op isEqualToString: OPERATION_MODIFY])
            [self modifyHeaderComponent: params];
		if ([op isEqualToString: OPERATION_ADD])
            [self addHeaderComponentAKAlog: params];
    }
    else if ([comp isEqualToString: COMPONENT_MAIN])
    {
        if ([op isEqualToString: OPERATION_MODIFY])
            [self modifyMainComponent: params];
    }
    else if ([comp isEqualToString: COMPONENT_MENU])
    {
        if ([op isEqualToString: OPERATION_ADD])
            [self addMenuComponent: params];
        else if ([op isEqualToString: OPERATION_DELETE])
            [self deleteMenuComponent: params];
        else if ([op isEqualToString: OPERATION_MODIFY])
            [self modifyMenuComponent: params];
    }
    else if ([comp isEqualToString: COMPONENT_SCRUBBER])
    {
        if ([op isEqualToString: OPERATION_MODIFY])
            [self modifyScrubberComponent: params];
    }
	
    else if ([comp isEqualToString: COMPONENT_THUMBS])
    {
        if ([op isEqualToString: OPERATION_MODIFY])
            [self modifyThumbsComponent: params];
    }
	
	else if ([comp isEqualToString: COMPONENT_OVERLAY])
    {
        if ([op isEqualToString: OPERATION_ADD])
            [self addOverlayComponent: params];
    }
	
}

- (void) resetComponents
{
    //    self.headerView.subtitleLabel.text = nil;
    self.headerView.subtitleText = nil;
    self.headerView.titleText = nil;
	
    //    [self.headerView.documentsButton setTitle: nil
    //                                     forState: UIControlStateNormal];
    //    [self.headerView.episodesButton setTitle: nil
    //                                    forState: UIControlStateNormal];
    //    [self.headerView.titleButton setTitle: nil
    //                                 forState: UIControlStateNormal];
	
    [self.scrubberView resetValues];
    [self.thumbListView removeAllCells];
    //[self.toolListView removeAllCells];
	
    [self.episodesActionController removeAllItems];
    [self.menuActionController removeAllItems];
}

- (CCRScrubberView *) scrubberView
{
    return ((CCRView *) self.view).scrubberView;
}

- (void) showEpisodesActionController
{
    if (self.appDelegate.targetIdiom == UIUserInterfaceIdiomPad)
    {
        CGRect rect = [self.segmentedControl rectForSegmentAtIndex: EPISODES_SEGMENT_INDEX];
		
        [self.episodesActionController showFromRect: rect
                                             inView: self.segmentedControl
                                           animated: YES];
    }
    else
        [self.episodesActionController showInView: self.view];
}

- (void) showMenuActionController
{
    if (self.appDelegate.targetIdiom == UIUserInterfaceIdiomPad)
    {
        CGRect rect = [self.segmentedControl rectForSegmentAtIndex: MENU_SEGMENT_INDEX];
		
        [self.menuActionController showFromRect: rect
                                         inView: self.segmentedControl
                                       animated: YES];
    }
    else
        [self.menuActionController showInView: self.view];
}

- (CCRThumbListView *) thumbListView
{
    return ((CCRView *) self.view).thumbListView;
}

- (CCRToolListView *) toolListView
{
    return ((CCRView *) self.view).toolListView;
}

- (void) updateNavigationItemAnimated: (BOOL) animated
{
    if (!self.backButton)
        self.backButton = [[[UIBarButtonItem alloc] initWithTitle: NSLocalizedString (@"CCR", @"")
                                                            style: UIBarButtonItemStylePlain
                                                           target: nil
                                                           action: NULL]
                           autorelease];
	
    if (!self.segmentedButton)
    {
        if (!self.segmentedControl)
        {
            self.segmentedControl = [[[UISegmentedControl alloc]
                                      initWithItems: [NSArray arrayWithObjects:
                                                      NSLocalizedString (@"Menu", @""),
                                                      NSLocalizedString (@"Episodes", @""),
                                                      nil]]
                                     autorelease];
			
            self.segmentedControl.momentary = YES;
            self.segmentedControl.segmentedControlStyle = UISegmentedControlStyleBar;
			
            [self.segmentedControl addTarget: self
                                      action: @selector (chooseAction:)
                            forControlEvents: UIControlEventValueChanged];
        }
		
        self.segmentedButton = [[[UIBarButtonItem alloc]
                                 initWithCustomView: self.segmentedControl]
                                autorelease];
    }
	
    AppDelegate    *appDel = self.appDelegate;
    SessionManager *sm = appDel.sessionManager;
    Member         *member = sm.loginSession.memberInFocus;
	
    self.navigationItem.title = member.name;
	
    self.navigationItem.backBarButtonItem = self.backButton;
	
    [self.navigationItem setRightBarButtonItem: self.segmentedButton
                                      animated: animated];
}

#pragma mark -
#pragma mark Responding to gestures

- (CCRButton *) findButton: (NSString *) imageName
{
    return [self.toolListView cellWithImage:
            [UIImage imageNamed: [ImageNameDictionary stringForKey: imageName]]].button;
}

- (void) tool_button_pressed:(NSString *) imageName
{
    // thanks to JGP help
    CCRButton *button = [self findButton: imageName];
	
    //NSLog(@"Button for %@ is %@", imageName, button);
	
    [self injectJavaScript: [NSString stringWithFormat:
                             JS_ACTION_FORMAT,
                             button.jsEvent,
                             button.jsParameter]];
}



- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch {
	
    return YES;
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer
shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return YES;
}
//
//- (void)handlePinchGestureFrom:(UIPinchGestureRecognizer *)recognizer {
//    CGFloat factor = [(UIPinchGestureRecognizer *)recognizer scale];
//
//    if (recognizer.state == UIGestureRecognizerStateBegan)
//    {
//        pinchStart = factor;
//
//    }
//    else
//        if (recognizer.state == UIGestureRecognizerStateEnded)
//        {
//            if (factor > pinchStart)
//            {
//                NSLog (@" pinched out = magnify_active start %f factor %f", pinchStart, factor);
//
//                [self tool_button_pressed:@"magnify_active"];
//            }
//            else
//            {
//                NSLog (@" pinched in ==> magnify start %f factor %f", pinchStart, factor);
//
//                [self tool_button_pressed:@"magnify"];
//            }
//
//        }
//
//}
////
//- (void)handleTapFrom:(UITapGestureRecognizer *)recognizer {
//
//    //  CGPoint location = [recognizer locationInView:self.mainView];
//
//    NSLog (@" single tap ==> arrow");
//
//    [self tool_button_pressed:@"arrow"];
//
//}
//- (void)handleDublTapFrom:(UITapGestureRecognizer *)recognizer {
//
//    //  CGPoint location = [recognizer locationInView:self.mainView];
//    NSLog (@" double tap ==> overlay");
//    [self tool_button_pressed:@"overlay"];
//}
//- (void)handleTwoFingerTapFrom:(UITapGestureRecognizer *)recognizer {
//
//
//    NSLog (@" two finger single tap ==> pause");
//
//    [self tool_button_pressed:@"pause"];
//
//}
//
//- (void)handlePanFrom:(UIPanGestureRecognizer *)recognizer {
//
//    CGPoint translate = [recognizer translationInView:self.mainView];
//
//    if (recognizer.state == UIGestureRecognizerStateBegan)
//    {
//        panDeltaX = 0.0f;
//        panDeltaY = 0.0f;
//
//        //  NSLog (@"handlePanFrom %@ UIGestureRecognizerStateBegan x %f y %f", recognizer, translate.x, translate.y);
//
//    }
//
//    if (recognizer.state == UIGestureRecognizerStateEnded)
//    {
//        if (sqrt(panDeltaX*panDeltaX + panDeltaY*panDeltaY)>100.0f)
//        {
//            NSLog (@" single finger drag ==> wl_active deltaX %f deltaY %f", panDeltaX, panDeltaY);
//            [self tool_button_pressed:@"wl_active"];
//        }
//    }
//
//
//    else { // accumulate the delta
//        panDeltaY += translate.y;
//        panDeltaX += translate.x;
//    }
//
//
//}
//- (void)handleTwoFingerPanFrom:(UIPanGestureRecognizer *)recognizer {
//
//
//    CGPoint translate = [recognizer translationInView:self.mainView];
//
//    if (recognizer.state == UIGestureRecognizerStateBegan)
//    {
//        panDeltaX = 0.0f;
//        panDeltaY = 0.0f;
//
//        //  NSLog (@"handlePanFrom %@ UIGestureRecognizerStateBegan x %f y %f", recognizer, translate.x, translate.y);
//
//    }
//
//    if (recognizer.state == UIGestureRecognizerStateEnded)
//    {
//
//        float absx = abs(panDeltaX);
//        float absy = abs(panDeltaY);
//
//        if (sqrt(panDeltaX*panDeltaX + panDeltaY*panDeltaY)>100.0f)
//        {
//
//            NSLog (@" dubl finger drag ==> wl_active deltaX %f deltaY %f", panDeltaX, panDeltaY);
//
//            if (absx > absy)
//            {
//                if (panDeltaX<0.0f)
//                {
//                    NSLog(@" left to right pan two finger ==> Next Image");
//
//                    [self tool_button_pressed:@"next_image"];
//                }
//                else {
//                    NSLog (@" right to left pan two finger ==> Prev Image");
//
//                    [self tool_button_pressed:@"prev_image"];
//                }
//            }
//            else
//            {
//                if (panDeltaY<0.0f) {
//                    NSLog(@" top down pan two finger ==> Next Series");
//
//                    [self tool_button_pressed:@"next_series"];
//
//                }
//                else
//                {
//                    NSLog (@" bottom up pan two finger ==> Prev Series");
//
//                    [self tool_button_pressed:@"prev_series"];
//                }
//            }
//        }
//
//    }
//
//
//    else { // accumulate the delta
//        panDeltaY += translate.y;
//        panDeltaX += translate.x;
//    }
//
//
//}


- (void)handleSwipeFrom:(UISwipeGestureRecognizer *)recognizer {
	
	//  CGPoint location = [recognizer locationInView:self.view];
	
	
    if (recognizer.direction == UISwipeGestureRecognizerDirectionLeft) {
        NSLog (@"UISwipeGestureRecognizerDirectionLeft mapped to next_image");
		[self tool_button_pressed:@"next_image"];
    }
    else {
        NSLog (@"UISwipeGestureRecognizerDirectionRight mapped to prev_image");
		[self tool_button_pressed:@"prev_image"];
    }
}

- (void)handleTripleTapFrom:(UITapGestureRecognizer *)recognizer {
	
    //  CGPoint location = [recognizer locationInView:self.mainView];
    NSLog (@" UITapGestureRecognizer triple tap gesture mapped to arrow");
    [self tool_button_pressed:@"arrow"];
}


#pragma mark Overridden UIViewController Methods

- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
    //NSLog (@"*** CCRViewController.didRotateFromInterfaceOrientation");
	
    [self.thumbListView viewDidRotate];
    [self.toolListView viewDidRotate];
	
    [(CCRView *) self.view showSubviewsAfterRotation];
}

- (void) loadView
{
    self.view = [[[CCRView alloc]
                  initWithFrame: CGRectStandardize (self.parentViewController.view.bounds)]
                 autorelease];
	
	
    self.mainView.delegate = self;
	
    self.scrubberView.action = @selector (movedScrubber);
    self.scrubberView.target = self;
	
    [(CCRView *) self.view hideThumbListViewAnimated: NO];
    [(CCRView *) self.view hideScrubberViewAnimated: NO];
    [(CCRView *) self.view hideToolListViewAnimated: NO];
	
    /*
     Create and configure the recognizers. The order is important
     */
	
	
	//
	//
	//
	//    // pan gesture recognizers for 1 and multiple fingers
	//
	//    UIPanGestureRecognizer *oecognizer = [[[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handleTwoFingerPanFrom:)] autorelease];
	//    oecognizer.delegate = self;
	//    oecognizer.minimumNumberOfTouches = 2;
	//
	//    [self.mainView addGestureRecognizer:oecognizer];
	//
	//
	//    UIPanGestureRecognizer *ooecognizer = [[[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePanFrom:)] autorelease];
	//    ooecognizer.delegate = self;
	//    ooecognizer.minimumNumberOfTouches = 1;
	//    ooecognizer.maximumNumberOfTouches = 1;
	//    [ooecognizer requireGestureRecognizerToFail: oecognizer];
	//
	//    [self.mainView addGestureRecognizer:ooecognizer];
	//
	//
	//
	//
	//
	//    /*
	//     Create a dubl tap recognizer and add it to the view.
	//     */
	//    UITapGestureRecognizer *tecognizer = [[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleDublTapFrom:)] autorelease];
	//    tecognizer.delegate = self;
	//
	//    tecognizer.numberOfTouchesRequired = 1;
	//    tecognizer.numberOfTapsRequired = 2;
	//
	//    [tecognizer requireGestureRecognizerToFail: tttecognizer];
	//
	//    [self.mainView addGestureRecognizer:tecognizer];
	//
	//
	//
	//    /*
	//     Create a  single tap recognizer and add it to the view. It should work only if no double tap
	//     */
	//    UITapGestureRecognizer *ttecognizer = [[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapFrom:)] autorelease];
	//    ttecognizer.delegate = self;
	//
	//    ttecognizer.numberOfTouchesRequired = 1;
	//    ttecognizer.numberOfTapsRequired = 1;
	//    [ttecognizer requireGestureRecognizerToFail: tecognizer];
	//    [ttecognizer requireGestureRecognizerToFail: tttecognizer];
	//
	//    [self.mainView addGestureRecognizer:ttecognizer];
	//
	//    /*
	//     Two finger tap
	//     */
	//    UITapGestureRecognizer *tfecognizer = [[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTwoFingerTapFrom:)] autorelease];
	//    tfecognizer.delegate = self;
	//    tfecognizer.numberOfTouchesRequired = 2;
	//    tfecognizer.numberOfTapsRequired = 1;
	//
	//    [self.mainView addGestureRecognizer:tfecognizer];
	//
	//
	//    UIPinchGestureRecognizer *pinchGesture = [[[UIPinchGestureRecognizer alloc]
	//                                               initWithTarget:self action:@selector(handlePinchGestureFrom:)] autorelease];
	//    pinchGesture.delegate =self;
	//
	//    [pinchGesture requireGestureRecognizerToFail: ttecognizer];
	//    [pinchGesture requireGestureRecognizerToFail: tfecognizer];
	//    [pinchGesture requireGestureRecognizerToFail: ooecognizer];
	//    [self.mainView addGestureRecognizer:pinchGesture];
	
	
    /*
     Create a swipe gesture recognizer to recognize right swipes (the default).
     */
	UISwipeGestureRecognizer  *recognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeFrom:)];
    recognizer.delegate = self;
    [self.mainView addGestureRecognizer:recognizer];
    [recognizer release];
	
    /*
     Create a swipe gesture recognizer to recognize left swipes.
     */
    UISwipeGestureRecognizer *srecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeFrom:)];
    srecognizer.delegate = self;
    srecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
	
    [self.mainView addGestureRecognizer:srecognizer];
    [srecognizer release];
	
	
	/*
	 //     Create a tripl tap recognizer and add it to the view.
	 //     */
	UITapGestureRecognizer *tttecognizer = [[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTripleTapFrom:)] autorelease];
	tttecognizer.delegate = self;
	
	tttecognizer.numberOfTouchesRequired = 1;
	tttecognizer.numberOfTapsRequired = 3;
	[self.mainView addGestureRecognizer:tttecognizer];
	
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];
	
    [self.thumbListView flashScrollIndicators];
}

- (void) viewDidLoad
{
    [super viewDidLoad];
	
    NSURLRequest *URLRequest = [NSURLRequest requestWithURL: self.URL
                                                cachePolicy: NSURLRequestReloadIgnoringLocalCacheData
                                            timeoutInterval: 30];
	
    [self.mainView loadRequest: URLRequest];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];
	
    [self updateNavigationItemAnimated: animated];
	
    NSUInteger idx = [self.thumbListView indexOfSelectedItem];
	
    if (idx != NSNotFound)
        [self.thumbListView deselectItemAtIndex: idx
                                       animated: NO];
	
    [self changeTheme: self.appDelegate.settingsManager.theme];
}

- (void) viewWillDisappear: (BOOL) animated
{
    [super viewWillDisappear: animated];
	
    if (self.episodesActionController.isVisible)
        [self.episodesActionController dismissWithCancelButtonAnimated: YES];
	
    if (self.menuActionController.isVisible)
        [self.menuActionController dismissWithCancelButtonAnimated: YES];
}

- (void) willAnimateRotationToInterfaceOrientation: (UIInterfaceOrientation) toOrient
                                          duration: (NSTimeInterval) duration
{
    //NSLog (@"*** CCRViewController.willAnimateRotationToInterfaceOrientation");
	
    [(CCRView *) self.view rotateSubviews];
}

- (void) willRotateToInterfaceOrientation: (UIInterfaceOrientation) toOrient
                                 duration: (NSTimeInterval) duration
{
    //NSLog (@"*** CCRViewController.willRotateToInterfaceOrientation");
	
    [(CCRView *) self.view hideSubviewsBeforeRotation];
	
    [self.thumbListView viewWillRotateToInterfaceOrientation: toOrient];
    [self.toolListView viewWillRotateToInterfaceOrientation: toOrient];
}

#pragma mark Overridden NSObject Methods

+ (void) initialize
{
    NSString     *cvcPath = [[NSBundle mainBundle] pathForResource: CLASS_RESOURCE_NAME
                                                            ofType: CLASS_RESOURCE_TYPE];
    NSDictionary *cvcDict = [NSDictionary dictionaryWithContentsOfFile: cvcPath];
	
    ImageNameDictionary = [[cvcDict dictionaryForKey: @"ImageNames"]
                           copy];
}

- (void) dealloc
{
    self.mainView.delegate = nil;   // Apple docs say this is required
	
    [self->backButton_ release];
    [self->episodesActionController_ release];
    [self->menuActionController_ release];
    [self->segmentedButton_ release];
    [self->segmentedControl_ release];
    [self->URL_ release];
	
    [super dealloc];
}

#pragma mark Extended UIViewController Methods

- (void) hideMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: nil
                                     animated: YES];
}

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];
}

#pragma mark UIWebViewDelegate Methods

- (void) webView: (UIWebView *) webView
didFailLoadWithError: (NSError *) error
{
    NSLog (@"*** CCRViewController.webView: %@ didFailLoadWithError: %@",
           webView,
           error);
	
    [self.mainView updateActivityIndicator];
}

- (BOOL) webView: (UIWebView *) webView
shouldStartLoadWithRequest: (NSURLRequest *) request
  navigationType: (UIWebViewNavigationType) navType
{
    //NSLog (@"*** CCRViewController.webView: %@ shouldStartLoadWithRequest: %@ navigationType: %d",
    //       webView,
    //       request,
    //       navType);
	
    [self.mainView updateActivityIndicator];
	
    if ([request isKindOfClass: [NSMutableURLRequest class]])
        [(NSMutableURLRequest *) request setCachePolicy: NSURLRequestReloadIgnoringLocalCacheData];
	
    if ([[[request URL] absoluteString] hasPrefix: JS_CALLBACK_URL_PREFIX])
    {
#if TRACE_JS_CALLBACKS
        NSLog (@"< %@", [request URL]);
#endif
		
        NSDictionary *params = [self parseJavaScriptCallbackURL: [request URL]];
		
        if (params)
        {
            [self performJavaScriptCallbackWithParameters: params];
			
            return NO;  // request handled internally
        }
    }
	
    return YES;
}

- (void) webViewDidFinishLoad: (UIWebView *) webView
{
    //NSLog (@"*** CCRViewController.webViewDidFinishLoad: %@", webView);
	
    [self.mainView updateActivityIndicator];
}

- (void) webViewDidStartLoad: (UIWebView *) webView
{
    //NSLog (@"*** CCRViewController.webViewDidStartLoad: %@", webView);
	
    [self.mainView updateActivityIndicator];
}

@end

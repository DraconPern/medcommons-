//
//  ShareActionController.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/27/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "CCRView.h"
#import "CCRViewController.h"
#import "Group.h"
#import "Member.h"
#import "NoteListViewController.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "ShareActionController.h"
#import "ShooterController.h"
#import "WebViewController.h"
#import "AddressBarWebViewController.h"

#pragma mark -
#pragma mark Public Class ShareActionController
#pragma mark -

@interface ShareActionController ()
-(void) portraitAction;

- (void) docsAction;

- (void) faxAction;

- (void) notesAction;

- (void) photosAction;

- (void) shareAction;

@end

@implementation ShareActionController

@synthesize viewController = viewController_;
@synthesize webView        = webView_;

#pragma mark Public Instance Methods

- (id) initWithViewController: (UIViewController *) viewController
                      webView: (UIWebView *) webView
{
    self = [super initWithTitle: NSLocalizedString (@"Health URL Actions", @"")];

    if (self)
    {
        self->viewController_ = [viewController retain];
        self->webView_ = [webView retain];

        AppDelegate     *appDel = self.appDelegate;
        SettingsManager *settings = appDel.settingsManager;

        if (appDel.targetIdiom != UIUserInterfaceIdiomPad)
            [self addCancelButtonWithTitle: NSLocalizedString (@"Cancel", @"")
                                    target: nil
                                    action: NULL
                                  userInfo: nil];

        if ([self.viewController isKindOfClass: [CCRViewController class]])
            [self addOtherButtonWithTitle: NSLocalizedString (@"All Documents", @"")
                                   target: self
                                   action: @selector (docsAction)
                                 userInfo: nil];
		
		if ([self.viewController isKindOfClass: [CCRViewController class]])
            [self addOtherButtonWithTitle: NSLocalizedString (@"Patient Portrait", @"")
                                   target: self
                                   action: @selector (portraitAction)
                                 userInfo: nil];

        if (settings.featureLevel >= 1)
        {
            if (settings.useNotes)
                [self addOtherButtonWithTitle: NSLocalizedString (@"Notes", @"")
                                       target: self
                                       action: @selector (notesAction)
                                     userInfo: nil];
        }

        if (settings.featureLevel >= 2)
            [self addOtherButtonWithTitle: NSLocalizedString (@"Fax", @"")
                                   target: self
                                   action: @selector (faxAction)
                                 userInfo: nil];

        if (settings.featureLevel >= 3)
        {
            [self addOtherButtonWithTitle: NSLocalizedString (@"Share", @"")
                                   target: self
                                   action: @selector (shareAction)
                                 userInfo:nil];

            if (settings.useCamera)
                [self addOtherButtonWithTitle: NSLocalizedString (@"Photos", @"")
                                       target: self
                                       action: @selector (photosAction)
                                     userInfo: nil];
        }
    }

    return self;
}

#pragma mark Private Instance Methods

-(void) portraitAction
{
	
	
    SessionManager *sm = self.appDelegate.sessionManager;
    Session        *session = sm.loginSession;
    Member         *member = session.memberInFocus;
	
	AddressBarWebViewController *wvc = [[[AddressBarWebViewController alloc]
										 initWithMcid:member.identifier] 
										autorelease];
	
	// make a decent looking title 
	wvc.title = [NSString stringWithFormat:@"%@ Choosing Patient Portrait for %@ %@",member.identifier,member.givenName,member.familyName];
	
	
	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
	
	
	//[nav setModalPresentationStyle:UIModalPresentationFormSheet];
	
	[self.viewController presentModalViewController:nav animated: YES];
	
}

- (void) docsAction
{
    [(CCRView *) self.viewController.view maximizeThumbListViewAnimated: YES];
}

- (void) faxAction
{
    SessionManager *sm = self.appDelegate.sessionManager;
    Session        *session = sm.loginSession;
    Member         *member = session.memberInFocus;
    Group          *group = session.groupInFocus;

    //
    // Replace following with:
    //
    //  NSURL *fwvcURL = [self.appDelegate.sessionManager URLForFaxCoverSheet];
    //
    //      or:
    //
    //  NSURL *fwvcURL = [self.appDelegate.sessionManager URLOfFaxCoverSheetForMember: member];
    //
    //      or some such ...
    //

    NSString *urlString = [@"https://" stringByAppendingFormat:@"%@/acct/phaxCover.php?scale=%@",
                           session.appliance,
                           ((self.appDelegate.targetIdiom == UIUserInterfaceIdiomPad) ?
                            @"1.2" : @"0.5"), nil];
    NSString *temp1 = [urlString stringByAppendingFormat:
                       @"&name=%@&mcid=%@&groupid=%@",
                       member.name,
                       member.identifier,
                       group.identifier ];
    NSString *temp2 = [temp1 stringByTrimmingWhitespace];
    NSString *fullURLString = [temp2 stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];

    WebViewController *wvc = [[[WebViewController alloc]
                               initWithURL: [NSURL URLWithString: fullURLString]]
                              autorelease];

    wvc.title = self.viewController.title;

    [self.viewController.navigationController pushViewController: wvc
                                                        animated: YES];
}

- (void) notesAction
{
    SessionManager *sm = self.appDelegate.sessionManager;
    Session        *session = sm.loginSession;
    Member         *member = session.memberInFocus;

    NoteListViewController *nlvc = [[[NoteListViewController alloc]
                                     initWithMember: member]
                                    autorelease];

    [self.viewController.navigationController pushViewController: nlvc
                                                        animated: YES];
}

- (void) photosAction
{
    ShooterController *sc = [[[ShooterController alloc] init]
                             autorelease];

    [self.viewController.navigationController pushViewController: sc
                                                        animated: YES];
}

- (void) shareAction
{
    [self.webView stringByEvaluatingJavaScriptFromString: @"showSharingDialog({});"];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->viewController_ release];
    [self->webView_ release];

    [super dealloc];
}

@end

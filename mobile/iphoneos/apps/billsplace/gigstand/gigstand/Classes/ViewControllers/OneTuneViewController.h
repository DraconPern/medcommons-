//
//  OneTuneViewController.h
//  MusicStand
//
//  Created by bill donner on 10/8/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//

#import <MediaPlayer/MediaPlayer.h>

#import <MessageUI/MFMailComposeViewController.h>
@class VariantChooserControl;
@class SetListChooserControl;
@class MediaViewController;
@class InstanceInfo;

@interface OneTuneViewController : UIViewController <
MPMediaPickerControllerDelegate,
UIWebViewDelegate,
UIActionSheetDelegate,
MFMailComposeViewControllerDelegate,
UIPrintInteractionControllerDelegate, 
UITextViewDelegate>
{
    @private	
    NSURL                   *contentURL;
    UIWebView               *contentView;
	NSString				*html;	
	UIView					*backgroundView;	
	CGRect					contentFrame;
	
	
    UIActivityIndicatorView *activityIndicator;
    NSUInteger               depth;
    BOOL                     sharedDocument;
    NSTimeInterval           startTime;
    
	NSString				*currentTuneTitle;
	NSInteger				currentListPosition;
	NSUInteger				currentVariantPosition;
	
    UIToolbar				*footerToolbar;
    UIView					*footerView;
    UIView                  *snapshotView;
    VariantChooserControl  
    *variantChooser;
	SetListChooserControl   *setListChooserControl;
	MediaViewController		*mediaViewController;
	NSString *variantChooserText ;
	NSArray *variantItems;
	
	//
    // Flags:
    //
	BOOL					first;
	BOOL					canPlay;
	BOOL					fullScreen;
	BOOL					contentIsOpaque;
	MFMailComposeViewController	
    *controller;
    
    NSTimer              *snapshotTimer;

	UIActionSheet *toass;
	NSArray *listItems;
	NSString *archivePathForFile;
    
}



-(OneTuneViewController *) initWithURL:(NSURL *)URL andWithTitle:(NSString *) titlex 
						  andWithItems:(NSArray *)items andPath:(NSString *)path;

@end

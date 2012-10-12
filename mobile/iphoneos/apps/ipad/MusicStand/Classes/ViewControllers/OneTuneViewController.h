//
//  OneTuneViewController.h
//  MusicStand
//
//  Created by bill donner on 10/8/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <MediaPlayer/MediaPlayer.h>

#import <MessageUI/MFMailComposeViewController.h>

@interface OneTuneViewController : UIViewController <MPMediaPickerControllerDelegate,UIWebViewDelegate,UIActionSheetDelegate,MFMailComposeViewControllerDelegate,
UIPickerViewDelegate,UIPrintInteractionControllerDelegate,
UIPickerViewDataSource, UITextViewDelegate>
{
	MPMusicPlayerController *appMusicPlayer;
    UIActivityIndicatorView *activityIndicator_;
    NSURL                   *contentURL_;
    UIWebView               *contentView_;
	NSString				*html_;
    NSUInteger               depth_;
    BOOL                     sharedDocument_;
    NSTimeInterval           startTime_;
	
	NSString			*mainTitle;
    UIToolbar          *footerToolbar_;
    UIView             *footerView_;
	UIView				*backgroundView_;
    UISegmentedControl *segmentedControl_;
	CGFloat             toolbarHeight_;
	CGRect				contentFrame_;
	//
    // Flags:
    //
    BOOL                isShowingToolbar_;
	BOOL				first;
	BOOL				canPlay;
	
	
	UIPickerView		*myPickerView;
	NSMutableArray				*pickerViewArray;
	
	NSString		*chosenList;
	
	UIView				*currentPicker;
	MFMailComposeViewController* controller;
	
	
	
@private	
	UIActionSheet *toass;
	UIActionSheet *pickerActionSheet;
	NSString *path;
	UIViewController *pc;
	NSString *backLabel;
	NSArray *listItems;
	BOOL canViewSource;
	
	
	
}


@property (nonatomic, retain) NSString			*mainTitle;

@property (nonatomic, retain) UIPickerView *myPickerView;
@property (nonatomic, retain) NSArray *pickerViewArray;

@property (nonatomic, retain) UILabel *pickerLabel;

@property (nonatomic, retain) UIView *currentPicker;


@property (nonatomic, retain, readonly) UIToolbar          *footerToolbar;
@property (nonatomic, retain, readonly) UIView             *footerView;

@property (nonatomic, retain, readonly) UIView             *backgroundView;
@property (nonatomic, assign, readonly) BOOL                needsFooterToolbar;
@property (nonatomic, assign, readonly) CGFloat             toolbarHeight;
@property (nonatomic, retain, readonly) UIWebView *contentView;

- (void) refreshWithURL : (NSURL *) URL andWithShortPath: (NSString *) shortpath;

- (void) refreshWithHTML : (NSString *) html andWithShortPath: (NSString *) shortpath;

-(OneTuneViewController *) initWithURL:(NSURL *)URL andWithTitle:(NSString *) titlex andWithShortPath: (NSString *) shortpath andWithBackLabel:(NSString *) labelx;

-(OneTuneViewController *) initWithURL:(NSURL *)URL andWithTitle:(NSString *) titlex 
					  andWithShortPath: (NSString *) shortpath andWithBackLabel:(NSString *) labelx andWithItems:(NSArray *)items;

@end

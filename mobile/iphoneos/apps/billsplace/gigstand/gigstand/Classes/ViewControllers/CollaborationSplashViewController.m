//
//  CollaborationSplashViewController.m
//  gigstand
//
//  Created by bill donner on 4/3/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//
#import "SettingsManager.h"
#import "CollaborationSplashViewController.h"
#import "ModalAlert.h"

// subclass of splashview
//
// puts up simple local web page to explain collaboration and to accept payment


@implementation CollaborationSplashViewController

-(void) donePressed:(id) obj
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}

-(void) rightPressed:(id) obj
{
    // NSLog (@"right button pressed in collaboration controller");
    
    BOOL yesno = [ModalAlert ask:@"Do you want to enable Collaboration Features? This will cost a bit in a future version"];
    if (yesno)
    {
        [SettingsManager sharedInstance].collabFeatures = YES;
        [ModalAlert say:@"Collaboration has been enabled"];
        
    }
    
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}

- (void)dealloc
{
    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle



- (id) init;

{
	BOOL collabfeatures = [[SettingsManager sharedInstance] collabFeatures];
    NSString *details  = @"<h2>Archives</h2><p>Load individual .zip files with thousands of tunes as extra archives</p><h2>Web Server</h2><p>Play tunes with friends who don't have iPads</p><h2>BonJour</h2><p>Follow your bandmate's tune selections</p>";
    
    if (!collabfeatures)
    {
        NSString *body =@"<h1>Collaboration Features Add-On Pack</h1>";
        NSString *html =  [NSString stringWithFormat:@"<html><div style='font-family:Helvetica'><font size='+2' color='black'>%@%@</font></div></html>",body,details];
        
        self = [super initWithVerticalHTML:html horitzontalHTML:html title:@"Enable Collaboration"
                           leftButtonOrNil:@"no thanks" rightButtonOrNil:@"yes please" target:self leftaction:@selector(donePressed:) rightaction:@selector(rightPressed:)];
        
    }
        
  
    else
        
    {
        NSString *body =@"<h1>You've Already Enabled Collaboration</h1>";
        NSString *html =  [NSString stringWithFormat:@"<html><div style='font-family:Helvetica'><font size='+2' color='black'>%@%@</font></div></html>",body,details];
        
        
        self = [super initWithVerticalHTML:html horitzontalHTML:html title:@"Collaboration Enabled" leftButtonOrNil:@"ok" rightButtonOrNil:nil target:self leftaction:@selector(donePressed:) rightaction:@selector(donePressed:)];
        
    }
    
    
    if (self)
    {
        
    }
    return self;
    
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    
    [super viewDidLoad];
}


- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


@end

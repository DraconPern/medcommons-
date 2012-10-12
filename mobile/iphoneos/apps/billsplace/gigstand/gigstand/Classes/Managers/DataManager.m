//
//  DataManager.m
//  MCProvider //
//  Created by Bill Donner on 4/11/10.
//  Copyright 2011 Bill Donner and GigStand.Net All rights reserved.
//
#import "SettingsManager.h"
#import "DataManager.h"
#import "DataStore.h"
#import "ZipArchive.h"
#import "ProgressString.h"
#import "ArchivesManager.h"
#import	"WebViewController.h"
#import "MediaViewController.h"
#import "TunesManager.h"
#import "OneTuneViewController.h"
#import <QuartzCore/QuartzCore.h>
#import "InstanceInfo.h"
#import "HTTPServer.h"
#import "MyHTTPConnection.h"



#pragma mark -
#pragma mark Public Class DataManager
#pragma mark -


@implementation DataManager

@synthesize starttime;
@synthesize applicationName;
@synthesize applicationVersion;
@synthesize myLocalIP;
@synthesize myLocalPort;
@synthesize docInteractionController;
@synthesize appMusicPlayer;
@synthesize progressString;
@synthesize inSim;


-(void) setTVScreenBounds:(CGRect) rect window:(UIWindow *) winx;
{
	self->tVFrame = rect;
	self->tVWindow = winx;
}

-(void)setupTVScreen:(UIView *) viewx 
{
	// this needs to change the external window 
	self->tVView = viewx;
	[self->tVWindow addSubview:viewx];	
	[self->tVWindow makeKeyAndVisible];
	
}
-(CGRect) frameForTVScreen
{
	return self->tVFrame;
}


#pragma mark Public Class Methods
- (DataManager *) init
{
	self = [super init];
	if (self) 
	{
		self->applicationName = [NSString stringWithString:[[[NSBundle mainBundle] infoDictionary]   objectForKey:@"CFBundleName"]];
		self->applicationVersion = [NSString stringWithString:[[[NSBundle mainBundle] infoDictionary]   objectForKey:@"CFBundleVersion"]];
		self->progressString = [[ProgressString alloc] init];
		self->docInteractionController = nil; 
		self->starttime = [[NSDate date] retain];
		//NSLog (@"DataManager started at %@",self->starttime);
	}
	return self;	
}
#pragma mark Function or Subroutine Based Methods



+(float) statusBarHeight;
{
	return STATUS_BAR_HEIGHT;
}
+(float) standardThumbSize;
{
	return (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?LARGER_THUMB_SIZE:STANDARD_THUMB_SIZE;
}

+(float) standardRowSize;
{
	return (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?LARGER_ROW_SIZE:STANDARD_ROW_SIZE;
}
+(float) infoRowSize;
{
	return (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?STANDARD_ROW_SIZE:INFO_ROW_SIZE;
}
+(float) navBarHeight;
{
	return (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?NAV_BAR_HEIGHT:
	((UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation))? 32.0f:NAV_BAR_HEIGHT);
}
+(float) toolBarHeight;
{
	return (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?NORMAL_TOOLBAR_HEIGHT:
	((UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation))? NARROW_TOOLBAR_HEIGHT:NORMAL_TOOLBAR_HEIGHT);
}
+(float) searchBarHeight;
{
	return (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?SEARCH_BAR_HEIGHT:
	((UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation))? 32.0f:SEARCH_BAR_HEIGHT);
}
+(float) titleViewWidth;
{
	return (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)? 
	((UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation))? 700.f:400.f)
	:
	((UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation))? 300.f:200.f)
	;
}


+ (BOOL) modalPopOversEnabled
{
    return ((UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) && 
     MODAL_POPOVER == YES);
}


+(float) topFudgeFactor;

{
	
	return [DataManager navBarHeight];
}
+(CGRect) busyOverlayFrame:(CGRect)frame;
{
	return frame;
}

+ (void) worldViewPulse
{
    NSLog (@"worldViewPulse called");
   // [[NSNotificationCenter defaultCenter] postNotificationName:GigStandWorldViewHasChanged object:nil];
}
    

+ (BOOL) isPDF:(NSString *)filespec;
{
	BOOL pdf = NO;
	NSRange range=[filespec rangeOfString:@".pdf" options:0 ];
	if(range.location<[filespec length])
	{
		pdf = YES;
	}
	NSRange range2=[filespec rangeOfString:@".PDF" options:0 ];
	if(range2.location<[filespec length])
	{
		pdf = YES;
	}
	NSLog (@"isPDF:%@ is %d",filespec,pdf);
	return pdf;
}
+(BOOL) isOpaque:(NSString *)filename;
{
	// the only files we are going to mess with are htms and txt files
	// regular html will be treated like docs and pdf because some html is very fussy
	//
	// to get GigStand to manipulate html, start with an 'htm' extension
	
	NSString *ext = [filename pathExtension];
	
	if 
		([ext isEqualToString:@"htm"] || [ext isEqualToString:@"txt"] )
		return NO;
	
	return YES;
}
	

+ (UIColor *) applicationColor
{
	BOOL settingsCanEdit = [SettingsManager sharedInstance].normalMode;
	return (settingsCanEdit)? 
	[UIColor colorWithRed:(CGFloat).467f green:(CGFloat).125f blue:(CGFloat).129 alpha:(CGFloat).7f]:	
	[UIColor colorWithRed:110.f/256.f green:123.f/256.f blue:139.f/256.f alpha:.7f]; 
}

+(BOOL) docMenuForURL: (NSURL *) fileURL barButton: (UIBarButtonItem *) vew;
{
	if ([DataManager sharedInstance].docInteractionController == nil)
	{
		[DataManager sharedInstance].docInteractionController = [UIDocumentInteractionController interactionControllerWithURL:fileURL];
		[DataManager sharedInstance].docInteractionController.delegate = nil; // no thanks
	}	
	else 
	{
		// just keep reusing the one we already have
		[DataManager sharedInstance].docInteractionController.URL = fileURL;
	}
	
	BOOL didPaint = [[DataManager sharedInstance].docInteractionController presentOptionsMenuFromBarButtonItem:vew
																									  animated:YES];
	return didPaint;
}

+(BOOL) docMenuForURL: (NSURL *) fileURL inView: (UIView *) vew;
{
	if ([DataManager sharedInstance].docInteractionController == nil)
	{
		[DataManager sharedInstance].docInteractionController = [UIDocumentInteractionController interactionControllerWithURL:fileURL];
		[DataManager sharedInstance].docInteractionController.delegate = nil; // no thanks
	}
	
	else 
	{
		// just keep reusing the one we already have
		[DataManager sharedInstance].docInteractionController.URL = fileURL;
	}
	
	BOOL didPaint = [[DataManager sharedInstance].docInteractionController presentOptionsMenuFromRect:vew.frame inView:vew 
																							 animated:YES];
	return didPaint;
}

-(void) httpServerGo
{ 
	if (!httpServer)
	{
		//NSLog(@"/////////starting internal http server");
		// optionally start the webserver
		NSString *root = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,NSUserDomainMask,YES) objectAtIndex:0];
		httpServer = [HTTPServer new];
		[httpServer setType:@"_http._tcp."];
		[httpServer setDomain:@""]; // must set something
		//	[httpServer setName:[[UIDevice currentDevice] name]];
		[httpServer setConnectionClass:[MyHTTPConnection class]];
		[httpServer setDocumentRoot:[NSURL fileURLWithPath:root]];
		
		
		// start the webserver
		
		NSError *error;
		if(![httpServer start:&error])
		{
			NSLog(@"Error starting HTTP Server: %@", error);
		}
		else 
		{
			//NSLog(@"///////restarting http server");
		}
		
	}
	else {
		// kick bonjour in the butt a bit
		[self->httpServer kickstart];
		
	}
	
	
} 

+(UIImage *)captureView:(UIView *)view scale: (float) scalefactor;

{ 
	CGRect tmpFrame = [[UIScreen mainScreen] bounds];
	
	CGRect screenRect = CGRectMake(0,0,tmpFrame.size.width,(tmpFrame.size.height-STATUS_BAR_HEIGHT-STANDARD_NAV_HEIGHT-TOP_TOOBAR_HEIGHT));
    
    CGRect newRect = CGRectMake(0,0,screenRect.size.width*scalefactor, screenRect.size.height*scalefactor); //, <#CGFloat y#>, <#CGFloat width#>, <#CGFloat height#>)
	
	UIGraphicsBeginImageContextWithOptions(newRect.size, NO, 0);//(screenRect.size);
	
	CGContextRef ctx = UIGraphicsGetCurrentContext(); 
    
    [[UIColor blackColor] set]; 
    
    CGContextFillRect(ctx, screenRect);
	
	[view.layer renderInContext:ctx];
	
//	UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    
    
	UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();

	
	UIGraphicsEndImageContext();
	
	//xNSLog (@"captureView returning %@ x %f y %f w %f h %f",newImage,screenRect.origin.x,screenRect.origin.y,screenRect.size.width,screenRect.size.height);
	return newImage; 
}

+(UIImage*)xcaptureView:(UIView *)view  
{
    // Create a graphics context with the target size
    // On iOS 4 and later, use UIGraphicsBeginImageContextWithOptions to take the scale into consideration
    // On iOS prior to 4, fall back to use UIGraphicsBeginImageContext
    
    
	CGRect tmpFrame = [[UIScreen mainScreen] bounds];
	
	CGRect screenRect = CGRectMake(0,0,FACTOR*tmpFrame.size.width,FACTOR*(tmpFrame.size.height-STATUS_BAR_HEIGHT-STANDARD_NAV_HEIGHT-ADDRESS_BAR_HEIGHT));
	
	UIGraphicsBeginImageContextWithOptions(screenRect.size, NO, 0);//(screenRect.size);
    
    
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    // Iterate over every window from back to front
    for (UIWindow *window in [[UIApplication sharedApplication] windows]) 
    {
        if ((![window respondsToSelector:@selector(screen)] || [window screen] == [UIScreen mainScreen])
            )// && 
            
            // if (
            //    ([window layer] == [view layer])) // only capture the actual webview
        {
            
            // -renderInContext: renders in the coordinate space of the layer,
            // so we must first apply the layer's geometry to the graphics context
            CGContextSaveGState(context);
            // Center the context around the window's anchor point
            CGContextTranslateCTM(context, [window center].x, [window center].y);
            // Apply the window's transform about the anchor point
            CGContextConcatCTM(context, [window transform]);
            // Offset by the portion of the bounds left of and above the anchor point
            
    
            
            // Render the layer hierarchy to the current context
            [[window layer] renderInContext:context];
            
            // Restore the context
            CGContextRestoreGState(context);
        }
    }
    
    // Retrieve the screenshot image
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    
    UIGraphicsEndImageContext();
    
    return image;
}
+(NSString *) saveImageToSnapshotsGallery:  (UIImage *) image tune: (NSString *) tune;
{	
	NSDate *datenow = [NSDate date];
	NSString *dateString = [datenow description];
	// this **dateString** string will have **"yyyy-MM-dd HH:mm:ss +0530"**
	NSArray *arr = [dateString componentsSeparatedByString:@" "];
	NSString *date = [arr objectAtIndex:0];
	NSString *time = [arr objectAtIndex:1];
	
	NSString *hh = [time substringToIndex:2];
	NSString *mm = [[time substringFromIndex:3]substringToIndex:2];
	NSString *ss = [[time substringFromIndex:6]substringToIndex:2];
	
	// arr will have [0] -> yyyy-MM-dd, [1] -> HH:mm:ss, [2] -> +0530 (time zone)
	
	NSString *filepath = [NSString stringWithFormat:  @"%@%@%@-snapshotthumb-%@-%@.png", hh,mm,ss,date,tune];
	NSString *topath = [[DataStore pathForThumbnails] 
						stringByAppendingPathComponent:filepath];
	
	NSData *imageData = UIImagePNGRepresentation(image);
	if (imageData != nil) {
		
		[imageData writeToFile:topath atomically:YES];		
//
	}
	return topath;
}
#pragma mark MPMediaQuery to play Audio from iPod
+ (void) playMusic:(NSString *)title;
{
	
	if ([[SettingsManager sharedInstance] disableMediaPlayer]==NO && [DataManager sharedInstance].inSim == NO)
	{
		
		/// PLAY MUSIC ONLY ON REAL DEVICE
		
		MPMediaQuery *iTunesQuery = [[[MPMediaQuery alloc] init] autorelease];	
		MPMediaPropertyPredicate *p = [MPMediaPropertyPredicate predicateWithValue:title forProperty:MPMediaItemPropertyTitle];
		[iTunesQuery addFilterPredicate:p];
		if  ( [ [iTunesQuery items] count] > 0) 
		{
			
			[DataManager sharedInstance].appMusicPlayer =[MPMusicPlayerController applicationMusicPlayer];
			[[DataManager sharedInstance].appMusicPlayer setShuffleMode: MPMusicShuffleModeOff];
			[[DataManager sharedInstance].appMusicPlayer setRepeatMode: MPMusicRepeatModeNone];
			[[DataManager sharedInstance].appMusicPlayer setQueueWithQuery:iTunesQuery];
			[[DataManager sharedInstance].appMusicPlayer play];
		}
	}
	
}
+(BOOL) canPlayTune:(NSString *)s;
{
	
	if ([[SettingsManager sharedInstance] disableMediaPlayer]==YES || [DataManager sharedInstance].inSim == YES) return NO;
	
	MPMediaQuery *iTunesQuery = [[[MPMediaQuery alloc] init] autorelease];	
	MPMediaPropertyPredicate *p = [MPMediaPropertyPredicate predicateWithValue:s forProperty:MPMediaItemPropertyTitle];
	[iTunesQuery addFilterPredicate:p];
	return  ( [ [iTunesQuery items] count] > 0) ;
}

+(NSString *) deriveLongPath:(NSString *)filePath forArchive:(NSString *)archive;
{
    // this is autoreleased, i hope
	NSString *s = [[NSString alloc] initWithFormat:@"%@/%@",archive,filePath];
    return [s autorelease];
}

+(UIViewController *) makeOneTuneViewController:(NSString *)longPath title:(NSString *)title items:(NSArray *)items;
{
	NSString *urlp = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],longPath];

	
	NSURL    *docURL = [NSURL fileURLWithPath: urlp isDirectory: NO];
	
	OneTuneViewController *wvc = [[[OneTuneViewController alloc]
								   initWithURL: docURL andWithTitle:title andWithItems: items andPath: longPath] autorelease] ;
	
    NSLog (@"created OTV retaincount = %d",[wvc retainCount]);
//	
//	UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController: wvc];
//       // if we release nav we crash when popping back to the home controller
//    
//    NSLog (@"created OTV retaincount = %d, nav = %d",[wvc retainCount],[nav retainCount]);
	//return [nav autorelease];
    return wvc;
    
	//}
}
+(UIView *)makeTitleView:(NSString *)titletext;
{
	// this will appear as the title in the navigation bar//
	CGRect frame = CGRectMake(0, 0, [DataManager titleViewWidth], [DataManager navBarHeight]);
	UILabel *label = [[UILabel alloc] initWithFrame:frame];
	label.backgroundColor = [UIColor clearColor];
	label.font = [UIFont boldSystemFontOfSize:20.0f];
	label.shadowColor = [UIColor colorWithWhite:0.0f alpha:0.5f];
	label.textAlignment = UITextAlignmentCenter;
	label.textColor = [UIColor whiteColor];
	label.text = NSLocalizedString(titletext, @"");
    
	return [label autorelease];//042311
}
+(UIView *)makeAppTitleView:(NSString *)titletext;
{
	// this will first prepend the app name appear as the title in the navigation bar//
	CGRect frame = CGRectMake(0, 0, [DataManager titleViewWidth], [DataManager navBarHeight]);
	UILabel *label = [[UILabel alloc] initWithFrame:frame];
	label.backgroundColor = [UIColor clearColor];
	label.font = [UIFont boldSystemFontOfSize:20.0f];
	label.shadowColor = [UIColor colorWithWhite:0.0f alpha:0.5f];
	label.textAlignment = UITextAlignmentCenter;
	label.textColor = [UIColor whiteColor];
	label.text = [NSString stringWithFormat:@"%@: %@", [DataManager sharedInstance].applicationName, titletext];
	
	return [label autorelease];//042311
}



+(UIImage *)makeThumbInternal:(NSString *) fullPathToMainImage  size: (float)length  path:(NSString *)path; 
{
	
	NSString *fullPathToThumbImage =[NSString stringWithFormat:@"%@/%@-thumbnail.png",[DataStore pathForSharedDocuments] ,path];
	
	NSFileManager *fileManager = [NSFileManager defaultManager];
	
	if ([fileManager fileExistsAtPath:fullPathToThumbImage] == NO) 
	{
		//		//couldn’t find a previously created thumb image so create one first…
		UIImage *mainImage;
		if ([fileManager fileExistsAtPath:fullPathToMainImage] == NO) 
			mainImage = [UIImage imageNamed:fullPathToMainImage];
		else 
			mainImage = [UIImage imageWithContentsOfFile:fullPathToMainImage];
		
		UIImageView *mainImageView = [[[UIImageView alloc] initWithImage:mainImage] autorelease];
		BOOL widthGreaterThanHeight = (mainImage.size.width > mainImage.size.height);
		float sideFull = (widthGreaterThanHeight) ? mainImage.size.height : mainImage.size.width;
		CGRect clippedRect = CGRectMake(0, 0, sideFull, sideFull);
		//creating a square context the size of the final image which we will then
		// manipulate and transform before drawing in the original image
		UIGraphicsBeginImageContext(CGSizeMake(length, length));
		CGContextRef currentContext = UIGraphicsGetCurrentContext();
		CGContextClipToRect( currentContext, clippedRect);
		CGFloat scaleFactor = length/sideFull;
		if (widthGreaterThanHeight) {
			//a landscape image – make context shift the original image to the left when drawn into the context
			CGContextTranslateCTM(currentContext, -((mainImage.size.width - sideFull) / 2) * scaleFactor, 0);
		}
		else {
			//a portfolio image – make context shift the original image upwards when drawn into the context
			CGContextTranslateCTM(currentContext, 0, -((mainImage.size.height - sideFull) / 2) * scaleFactor);
		}
		//this will automatically scale any CGImage down/up to the required thumbnail side (length) when the CGImage gets drawn into the context on the next line of code
		CGContextScaleCTM(currentContext, scaleFactor, scaleFactor);
		[mainImageView.layer renderInContext:currentContext];
		UIImage *tempthumbnail = UIGraphicsGetImageFromCurrentImageContext();
		UIGraphicsEndImageContext();
		NSData *imageData = UIImagePNGRepresentation(tempthumbnail);
		[imageData writeToFile:fullPathToThumbImage atomically:YES];
		//NSLog (@"allocSquareThumb %@",fullPathToThumbImage);
		//
		
	}
	UIImage *thumbnail = [UIImage imageWithContentsOfFile:fullPathToThumbImage];//042311 removed retain here 
	return thumbnail;
}
+(UIImage *)makeThumbFS:(NSString *) path  size: (float)length; 
{//not sure why there's no /
	NSString *fullPathToMainImage = [NSString stringWithFormat: @"%@/%@",[DataStore pathForSharedDocuments ] ,path] ;
	return [DataManager makeThumbInternal:fullPathToMainImage size: length path:path];
}
+(UIImage *)makeThumbRS:(NSString *) path  size: (float)length; 
{
	return [DataManager makeThumbInternal:path size: length path:[NSString stringWithFormat:@"/_thumbs/%@",path]];
}

+(NSString *) noHungarian:(NSString *)htitle
{
	const char *foo = [htitle UTF8String];
	char obuf[1000];
	NSUInteger opos = 0;
	NSUInteger len = strlen(foo);
	if (len>1000) len=1000;
	char tab = '\t';
	BOOL spaces = NO;
	BOOL firstx = YES; // 
	for (NSUInteger index = 0; index<len; index++)
	{
		char o = foo[index];
		if(! ((o=='"')||(o=='\'')) )
		{
			if ((o==' ')||(o==tab)||(o=='_')) // space or tab? just note it
			{	
				spaces = YES;
			}
			else 
			{
				
				// anything else gets copied
				if (YES==spaces)
				{	
					
					obuf[opos]=' '; // insert a space
					opos++;
					
				}
				else //if (NO==spaces) // can get changed in above conditional
				{
					// not coming off string of spaces
					if ((o>='A')&&(o<='Z')) // make each cap generate a new word
					{	
						if(NO==firstx)
						{
							obuf[opos]=' '; // insert a space
							opos++;
						}
					}
					firstx = NO;
					
				}
				
				spaces = NO; // not in a space anymore
				// in all cases if not a space then copy it over
				obuf[opos]=o;
				opos++;
			}
		}
	}
	obuf[opos]='\0';
	// squeeze out beginning ending and extra blanks
	//	
	NSString* newStringq = [[[NSString stringWithUTF8String:obuf] 
							 stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]] 
							capitalizedString];
	
	return newStringq;
}

+(BOOL) isFileTypeProcessedByUS :(NSString *)stype
{
	NSString *type = [stype lowercaseString];
	if(
	   ([@"zip" isEqualToString:type]) ||
	   ([@"stl" isEqualToString:type]) ||
	   ([@"pdf" isEqualToString:type]) ||	   
	   ([@"txt" isEqualToString:type]) ||
	   ([@"htm" isEqualToString:type]) ||
	   ([@"html" isEqualToString:type]) ||
	   ([@"doc" isEqualToString:type]) ||
	   ([@"rtf" isEqualToString:type]) ||
	   ([@"mp3" isEqualToString:type]) ||
	   ([@"m4v" isEqualToString:type]) ||
	   ([@"jpg" isEqualToString:type]) ||
	   ([@"jpeg" isEqualToString:type]) ||
	   ([@"gif" isEqualToString:type]) ||
	   ([@"png" isEqualToString:type]) )
		return YES;
	return NO;
}
+(BOOL) isFileMusic :(NSString *)stype;
{
	NSString *type = [stype lowercaseString];
	if(([@"mp3" isEqualToString:type])) 

		return YES;
	return NO;
}
+(BOOL) isFileVideo :(NSString *)stype;
{
	NSString *type = [stype lowercaseString];
	if
	   (
	   ([@"m4v" isEqualToString:type]) 
	   )
		return YES;
	return NO;
}
+(NSUInteger) incomingInboxDocsCount;
{
	//BOOL any = NO;
	NSUInteger zipcount=0;
	NSArray *paths = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:[DataStore pathForItunesInbox]  error: NULL];
	if (paths){
		for (NSString *path in paths)
			if (![path isEqualToString:@".DS_Store"])
			{
				NSString *fpath = [[DataStore pathForItunesInbox] stringByAppendingPathComponent: path];
				NSDictionary *attrs = [[NSFileManager defaultManager] attributesOfItemAtPath:fpath 
																					   error: NULL];
				if (attrs && [[attrs fileType] isEqualToString: NSFileTypeRegular])
				{
					
					NSString *ext = [fpath pathExtension]; //
					if ([self isFileTypeProcessedByUS:ext]==YES) zipcount++;
					
				}
			}
	}
	return zipcount;
}

+(NSMutableArray *) newInboxDocumentsList;
{
	//BOOL any = NO;
	//	NSUInteger zipcount=0;
	NSMutableArray *results = [[NSMutableArray alloc] init];
	NSArray *paths = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:[DataStore pathForItunesInbox]  error: NULL];
	if (paths){
		for (NSString *path in paths)
			if (![path isEqualToString:@".DS_Store"])
			{
				NSDictionary *attrs = [[NSFileManager defaultManager] attributesOfItemAtPath: [[DataStore pathForItunesInbox] 
																							   stringByAppendingPathComponent: path]
																					   error: NULL];
				if (attrs && [[attrs fileType] isEqualToString: NSFileTypeRegular])
				{
					[results addObject:path];
				}
				
				
			}
	}
	return results;
}

+ (DataManager *) sharedInstance
{
	static DataManager *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[DataManager alloc] init];
	}
	
	return SharedInstance;
}

+(float)getTotalDiskSpaceInBytes;
{
    float totalSpace = 0.0f;
    NSError *error = nil;
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSDictionary *dictionary = [[NSFileManager defaultManager] attributesOfFileSystemForPath:[paths lastObject] error: &error];
	
    if (dictionary) {
        NSNumber *fileSystemSizeInBytes = [dictionary objectForKey: NSFileSystemFreeSize];
        totalSpace = [fileSystemSizeInBytes floatValue];
    } else {
        NSLog(@"Error Obtaining File System Info: Domain = %@, Code = %@", [error domain], [error code]);
    }
	
    return totalSpace;
} 
+ (NSString *) makeBlurb:(NSString *)title;
{
	NSArray *variants = [TunesManager allVariantsFromTitle:title] ;
	
	NSMutableString *result = [NSMutableString stringWithString:@""];	/// removed a retain here    042111
	NSUInteger varsCount = [variants count];
	NSMutableArray *blurbs = [[[NSMutableArray alloc] initWithCapacity:varsCount] autorelease];
	int blurbCounters [varsCount];
	NSUInteger blurbCount = 0; // must always be less
	NSUInteger musicCount = 0;
	NSUInteger plainCount = 0;
	NSUInteger videoCount = 0;
	for (NSUInteger i=0; i<varsCount; i++) blurbCounters[i]=0;
	for (InstanceInfo *ii in variants) 
	{
		NSString *ext = [ii.filePath pathExtension];
		
		if ([DataManager isFileMusic:ext]) musicCount++;
		else 
			
			if ([DataManager isFileVideo:ext]) videoCount++;
			else plainCount++;
		
		
		//	NSLog(@"--matching %@",sn);
		NSUInteger matched = NSNotFound;
		NSUInteger matchcount = 0;
		
		//up to the first slash to get the archive part of the filepath;
		NSString *s = [ii.archive stringByDeletingPathExtension];

		
		for (NSString *foo in blurbs) 
		{
			//NSLog(@"--isequal-- %@ -- %@",foo,s);
			if ([foo isEqualToString:s]) 
			{ 
				matched = matchcount; break;
			} 
			else matchcount++;
		}
		
		if (matched==NSNotFound)
		{
			[blurbs addObject:s]; // full names here
			blurbCounters[blurbCount]=1;
			blurbCount++;
			//NSLog (@"makeBlurb %@ count %d",s,blurbCount);
		}
		else 
		{
			//int jj = 
			blurbCounters[matched]++;
			//NSLog(@"--matched  %@ %d %d",
			//				  sn,matched, jj);
		}
	}
	
	if(videoCount>0) [result appendString:[NSString stringWithFormat:@"%C ",0x02AD]] ;
	if(musicCount>0) [result appendString:[NSString stringWithFormat:@"%C ",0x266C]] ;
	if(plainCount>0) [result appendString:[NSString stringWithFormat:@"%C ",0x0298]] ;
	
	for (NSUInteger i=0; i<blurbCount; i++)
	{	
		NSString	*sn = [ArchivesManager shortName:[blurbs objectAtIndex:i]];		
		NSUInteger bc = blurbCounters[i];
		
		if(bc<=1)
			
			[ result appendString:[NSString stringWithFormat: @" %@ ",sn]];
		
		else 
			
			[ result appendString: [NSString stringWithFormat:@" %@(%d) ",sn,bc ]];
		
	}
	
	return result; // some viewcontrollers fail if this [result autorelease];
}
+(NSArray *) list:(NSArray *) list bringToTop:(NSArray *) special;
{
	// build a new list from list while bringing the specials to the top
	NSMutableArray *newlist = [[[NSMutableArray alloc] init] autorelease];
	for (NSString *s in list)
	{
		BOOL found=NO;
		
		for (NSString *t in special)
		{
			if ([t isEqualToString:s]) 
			{
				found = YES;
				break;
			}
		}
		
		if (found == NO)
		{
			[newlist addObject:s];
		}
	}
	// sort the mutable arry
	[newlist sortUsingSelector:@selector(compare:)];
	
	// now produce final merge with special items at the front
	
	NSMutableArray *final = [[[NSMutableArray alloc] init] autorelease];
	
	for (NSString *t in special) [final addObject:t];
	for (NSString *s in newlist) [final addObject:s];
	
	
	return final;
}
@end


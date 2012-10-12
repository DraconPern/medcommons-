//
//  DataManager.m
//  MCProvider
//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
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



#pragma mark -
#pragma mark Public Class DataManager
#pragma mark -
@interface DataManager ()
@end
@implementation DataManager

@synthesize starttime;
@synthesize applicationName;
@synthesize applicationVersion;

@synthesize docInteractionController;
@synthesize appMusicPlayer;
@synthesize progressString;

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

#define STATUS_BAR_HEIGHT 20.f
#define NAV_BAR_HEIGHT 44.f
#define SEARCH_BAR_HEIGHT 50.f
#define STANDARD_THUMB_SIZE 60.f
#define LARGER_THUMB_SIZE 70.f
#define STANDARD_ROW_SIZE 60.f
#define LARGER_ROW_SIZE 70.f

#define INFO_ROW_SIZE 40.f



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
+(float) topFudgeFactor;

{
	
	return [DataManager navBarHeight];
}
+(CGRect) busyOverlayFrame:(CGRect)frame;
{
	return frame;
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
	BOOL settingsCanEdit = ![[NSUserDefaults standardUserDefaults] boolForKey:@"SettingsLocked"];	
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



#pragma mark MPMediaQuery to play Audio from iPod
+ (void) playMusic:(NSString *)title;
{
	
	if ([[SettingsManager sharedInstance] disableMediaPlayer]==NO)
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
	
	if ([[SettingsManager sharedInstance] disableMediaPlayer]==YES) return NO;
	
	MPMediaQuery *iTunesQuery = [[[MPMediaQuery alloc] init] autorelease];	
	MPMediaPropertyPredicate *p = [MPMediaPropertyPredicate predicateWithValue:s forProperty:MPMediaItemPropertyTitle];
	[iTunesQuery addFilterPredicate:p];
	return  ( [ [iTunesQuery items] count] > 0) ;
}

+(NSString *) newLongPath:(NSString *)filePath forArchive:(NSString *)archive;
{
	return [[NSString stringWithFormat:@"%@/%@",archive,filePath]retain];
}

+(UINavigationController *) allocOneTuneViewController:(NSString *)longPath title:(NSString *)title items:(NSArray *)items;
{
	NSString *urlp = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],longPath];

	
	NSURL    *docURL = [NSURL fileURLWithPath: urlp isDirectory: NO];
	
	
	
	OneTuneViewController *wvc = [[[OneTuneViewController alloc]
								   initWithURL: docURL andWithTitle:title andWithItems: items andPath: longPath]
								  autorelease];
	
	
	UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController: wvc] ;
	return nav;
	//}
}
+(UIView *)allocTitleView:(NSString *)titletext;
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
	return label;
}
+(UIView *)allocAppTitleView:(NSString *)titletext;
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
	
	return label;
}



+(UIImage *)allocSquareThumbInternal:(NSString *) fullPathToMainImage  size: (float)length  path:(NSString *)path; 
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
	UIImage *thumbnail = [[UIImage imageWithContentsOfFile:fullPathToThumbImage] retain];
	return thumbnail;
}
+(UIImage *)allocSquareThumbFS:(NSString *) path  size: (float)length; 
{//not sure why there's no /
	NSString *fullPathToMainImage = [NSString stringWithFormat: @"%@/%@",[DataStore pathForSharedDocuments ] ,path] ;
	return [DataManager allocSquareThumbInternal:fullPathToMainImage size: length path:path];
}
+(UIImage *)allocSquareThumbRS:(NSString *) path  size: (float)length; 
{
	return [DataManager allocSquareThumbInternal:path size: length path:[NSString stringWithFormat:@"/_thumbs/%@",path]];
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
+ (NSString *) newBlurb:(NSString *)title;
{
	NSArray *variants = [TunesManager allVariantsFromTitle:title];
	
	NSMutableString *result = [[NSMutableString stringWithString:@""] retain];	/// removed a retain here
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
			//NSLog (@"newBlurb %@ count %d",s,blurbCount);
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
	
	return result;
}
@end


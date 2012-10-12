/*

File: TableViewCell.m
Abstract: Custom table cell.

*/

#import "mcTableViewCell.h"
#import "Giver.h"
#import "Patient.h"

static UIImage *magnitude2Image = nil;
static UIImage *magnitude3Image = nil;
static UIImage *magnitude4Image = nil;
static UIImage *magnitude5Image = nil;
static UIImage *unknownUserImage = nil;

@interface TableViewCell()
- (UILabel *)newLabelWithPrimaryColor:(UIColor *)primaryColor selectedColor:(UIColor *)selectedColor fontSize:(CGFloat)fontSize bold:(BOOL)bold;
@end

@implementation TableViewCell

@synthesize person  = _person;
@synthesize locationLabel = _locationLabel;
@synthesize dateLabel = _dateLabel;
@synthesize magnitudeLabel = _magnitudeLabel;
@synthesize personSeverityImageView = _personSeverityImageView;

@synthesize firstnameLabel = _firstnameLabel;
@synthesize lastnameLabel = _lastnameLabel;
@synthesize mcidLabel = _mcidLabel;
@synthesize photoUrlImageView = _photoUrlImageView;
+ (void)initialize
{
    // The magnitude images are cached as part of the class, so they need to be
    // explicitly retained.
    magnitude2Image = [[UIImage imageNamed:@"2.0.png"] retain];
    magnitude3Image = [[UIImage imageNamed:@"3.0.png"] retain];
    magnitude4Image = [[UIImage imageNamed:@"4.0.png"] retain];
    magnitude5Image = [[UIImage imageNamed:@"5.0.png"] retain];
	unknownUserImage = [[UIImage imageNamed:@"unknown-user.png"] retain];
}

- (id)initWithFrame:(CGRect)frame reuseIdentifier:(NSString *)reuseIdentifier
{
    if (self = [super initWithFrame:frame reuseIdentifier:reuseIdentifier]) {
        UIView *myContentView = self.contentView;
        
        // Add an image view to display a waveform based on severity
		self.personSeverityImageView =  [[UIImageView alloc] initWithImage:magnitude2Image];
		
		[myContentView addSubview:self.personSeverityImageView];
        [self.personSeverityImageView release];
		
		// Add an image view to display a photo
		self.photoUrlImageView = [[UIImageView alloc] initWithImage:unknownUserImage];
		
		
		[myContentView addSubview:self.photoUrlImageView];
        [self.photoUrlImageView release];
        
        // A label that displays the first name of the person
        self.firstnameLabel = [self newLabelWithPrimaryColor:[UIColor blackColor] selectedColor:[UIColor whiteColor] fontSize:20.0 bold:YES]; 
		self.firstnameLabel.textAlignment = UITextAlignmentLeft; // default
		[myContentView addSubview:self.firstnameLabel];
		[self.firstnameLabel release];
        
		
		// A label that displays the last name of the person
        self.lastnameLabel =  [self newLabelWithPrimaryColor:[UIColor blackColor] selectedColor:[UIColor lightGrayColor] fontSize:20.0 bold:YES];
		self.lastnameLabel.textAlignment = UITextAlignmentLeft; // default
		[myContentView addSubview:self.lastnameLabel];
		[self.lastnameLabel release];
				
       
        // A label that displays the mcid of the person
        self.mcidLabel = [self newLabelWithPrimaryColor:[UIColor blackColor] selectedColor:[UIColor whiteColor] fontSize:10.0 bold:NO];
		self.mcidLabel.textAlignment = UITextAlignmentRight;
		[myContentView addSubview:self.mcidLabel];
		[self.mcidLabel release];
        
        // Position the personSeverityImageView above all of the other views so
        // it's not obscured. It's a transparent image, so any views
        // that overlap it will still be visible.
        [myContentView bringSubviewToFront:self.personSeverityImageView];
    }
    return self;
}

- (Person *)person
{
    return _person;
}

- (UIImage *)imageForPersonPhoto:(Person *)p
{
	NSLog(@"Looking up %@ with status %@",p.photoFileSpec,p.photoState);
	UIImage *xunknownUserImage = [[UIImage alloc] initWithContentsOfFile:p.photoFileSpec];
	return  xunknownUserImage;
}

- (UIImage *)imageForPersonSeverity:(NSString *)severity
{
	
	if ([severity isEqualToString:@"severe"]) {
		return magnitude5Image;
	}
	if ([severity isEqualToString:@"high"]) {
		return magnitude4Image;
	}
	if ([severity isEqualToString:@"warning"]) {
		return magnitude3Image;
	}
	if ([severity isEqualToString:@"normal"]) {
		return magnitude2Image;
	}
	return  magnitude2Image;
}
// Rather than using one of the standard UITableViewCell content properties like 'text',
// we're using a custom property called 'quake' to populate the table cell. Whenever the
// value of that property changes, we need to call [self setNeedsDisplay] to force the
// cell to be redrawn.
- (void)setPerson:(Person *)newPerson
{
	if (newPerson.firstname)
		if (newPerson.lastname)
			if (newPerson.mcid)
			{
				[newPerson retain];
				[_person release];
				_person = newPerson;
				NSLog(@"setPerson %@ %@ %@ %@", newPerson.firstname,newPerson.lastname,newPerson.mcid,newPerson.photoFileSpec);
				self.firstnameLabel.text = newPerson.firstname;
				self.lastnameLabel.text = newPerson.lastname;
				self.mcidLabel.text = newPerson.mcid;
				self.personSeverityImageView.image = [self imageForPersonSeverity:newPerson.alertlevel];
				self.photoUrlImageView.image =	[self imageForPersonPhoto:newPerson] ;
				[self setNeedsDisplay];
			}
}

- (void)layoutSubviews {
  // custom layout depending on whether showing a patient or caregiver  
	
#define UPPER_ROW_TOP 4
#define LOWER_ROW_TOP 30
    
    [super layoutSubviews];
    CGRect contentRect = self.contentView.bounds;
	
	// In this example we will never be editing, but this illustrates the appropriate pattern
    if (!self.editing) {
		
        CGFloat boundsX = contentRect.origin.x;
		CGRect frame;
		int NAME_COLUMN_OFFSET;
		int NAME_COLUMN_WIDTH;
		int PHOTO_COLUMN_OFFSET;
		int PHOTO_COLUMN_WIDTH;		
		int WAVEFORM_COLUMN_OFFSET;
		int WAVEFORM_COLUMN_WIDTH;
		int MCID_COLUMN_OFFSET;
		int MCID_ROW_TOP;
       	if ([self.person isKindOfClass: [Giver class]])
		{
			NAME_COLUMN_OFFSET=80;
			NAME_COLUMN_WIDTH=180;
			PHOTO_COLUMN_OFFSET=263;
			PHOTO_COLUMN_WIDTH=57;
			WAVEFORM_COLUMN_OFFSET=0;
			WAVEFORM_COLUMN_WIDTH=120;
			MCID_COLUMN_OFFSET=10;
			MCID_ROW_TOP=41;
		}
		else
		{
			NAME_COLUMN_OFFSET= 80;
			NAME_COLUMN_WIDTH=180;
			PHOTO_COLUMN_OFFSET= 0;
			PHOTO_COLUMN_WIDTH= 58;
			WAVEFORM_COLUMN_OFFSET =200;
			WAVEFORM_COLUMN_WIDTH =120;
			MCID_COLUMN_OFFSET =250;
			MCID_ROW_TOP =41;
		}
		// Place the firstname label.
		frame = CGRectMake(boundsX + NAME_COLUMN_OFFSET, UPPER_ROW_TOP, NAME_COLUMN_WIDTH, 18);
		self.firstnameLabel.frame = frame;
		
		// Place the lastname label.
		frame = CGRectMake(boundsX + NAME_COLUMN_OFFSET, LOWER_ROW_TOP, NAME_COLUMN_WIDTH, 18);
		self.lastnameLabel.frame = frame;
		
		// Place the waveform image.
		UIImageView *imageView = self.personSeverityImageView;
		frame = [imageView frame];
		frame.origin.x = boundsX + WAVEFORM_COLUMN_OFFSET;
		frame.origin.y = 0;
		imageView.frame = frame;
		
		// Place the photo image.
		UIImageView *pimageView = self.photoUrlImageView;
		frame = [pimageView frame];
		frame.origin.x = boundsX + PHOTO_COLUMN_OFFSET;
		frame.origin.y = 0;
		pimageView.frame = frame;
		
		
		// Place the mcid label.
		CGSize mcidSize = [self.mcidLabel.text sizeWithFont:self.mcidLabel.font forWidth:PHOTO_COLUMN_WIDTH lineBreakMode:UILineBreakModeTailTruncation];
		frame = CGRectMake(boundsX + MCID_COLUMN_OFFSET, MCID_ROW_TOP, mcidSize.width, mcidSize.height);
		self.mcidLabel.frame = frame;
	}
	
	
	
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
	/*
	 Views are drawn most efficiently when they are opaque and do not have a clear background, so in newLabelForMainText: the labels are made opaque and given a white background.  To show selection properly, however, the views need to be transparent (so that the selection color shows through).  
     */
	[super setSelected:selected animated:animated];
	
	UIColor *backgroundColor = nil;
	if (selected) {
	    backgroundColor = [UIColor clearColor];
		//clicked on table row, so do something
		
	} else {
		backgroundColor = [UIColor whiteColor];
	}
    
	self.firstnameLabel.backgroundColor = backgroundColor;
	self.firstnameLabel.highlighted = selected;
	self.firstnameLabel.opaque = !selected;
	
	self.lastnameLabel.backgroundColor = backgroundColor;
	self.lastnameLabel.highlighted = selected;
	self.lastnameLabel.opaque = !selected;
	
	self.mcidLabel.backgroundColor = backgroundColor;
	self.mcidLabel.highlighted = selected;
	self.mcidLabel.opaque = !selected;
}

- (UILabel *)newLabelWithPrimaryColor:(UIColor *)primaryColor selectedColor:(UIColor *)selectedColor fontSize:(CGFloat)fontSize bold:(BOOL)bold
{
	/*
        Create and configure a label.
    */

    UIFont *font;
    if (bold) {
        font = [UIFont boldSystemFontOfSize:fontSize];
    } else {
        font = [UIFont systemFontOfSize:fontSize];
    }
    
    /*
        Views are drawn most efficiently when they are opaque and do not have a clear background, so set these defaults.  To show selection properly, however, the views need to be transparent (so that the selection color shows through).  This is handled in setSelected:animated:.
    */
	UILabel *newLabel = [[UILabel alloc] initWithFrame:CGRectZero];
	newLabel.backgroundColor = [UIColor whiteColor];
	newLabel.opaque = YES;
	newLabel.textColor = primaryColor;
	newLabel.highlightedTextColor = selectedColor;
	newLabel.font = font;
	
	return newLabel;
}

@end

//
//  DocumentTableViewCell.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCDocument.h"
#import "MCDocumentTableViewCell.h"
#import "NSString+MCToolbox.h"

#pragma mark -
#pragma mark Public Class MCDocumentTableViewCell
#pragma mark -

#pragma mark Internal Constants

#define PADDING_BOTTOM_INSET  1.0f
#define PADDING_LEFT_INSET   10.0f
#define PADDING_RIGHT_INSET   5.0f
// tweaked top
#define PADDING_TOP_INSET     5.0f

@interface MCDocumentTableViewCell ()

- (NSString *) formatDecimalSize: (NSDecimal *) size
                           scale: (NSInteger) scale
                           units: (NSString *) units;

@end

@implementation MCDocumentTableViewCell

@synthesize alwaysFormatsSizeInBytes = alwaysFormatsSizeInBytes_;
@dynamic    dateFormatter;
@synthesize dateLabel                = dateLabel_;
@synthesize document                 = document_;
@synthesize formatsSizeInBinaryUnits = formatsSizeInBinaryUnits_;
@synthesize nameLabel                = nameLabel_;
@synthesize sizeLabel                = sizeLabel_;
@synthesize typeLabel                = typeLabel_;
@synthesize usesCreationDate         = usesCreationDate_;
@synthesize usesPath                 = usesPath_;

#pragma mark Public Instance Methods

- (NSDateFormatter *) dateFormatter
{
    if (!self->dateFormatter_)
    {
        self->dateFormatter_ = [NSDateFormatter new];
		
        [self->dateFormatter_ setDateStyle: NSDateFormatterMediumStyle];
        [self->dateFormatter_ setTimeStyle: NSDateFormatterMediumStyle];
    }
	
    return self->dateFormatter_;
}

- (NSString *) formatDate: (NSDate *) date
{
    return [self.dateFormatter stringFromDate: date];
}

- (NSString *) formatName: (NSString *) name
{
    return name;
}

- (NSString *) formatSize: (FileSize) size
{
    NSDecimal  decSize = [[NSNumber numberWithUnsignedLongLong: size] decimalValue];
    NSDecimal  oneK = [[NSNumber numberWithInt: (self.formatsSizeInBinaryUnits ?
                                                 1024 :
                                                 1000)] decimalValue];
    NSString  *tmpString = nil;
	
    if (self.alwaysFormatsSizeInBytes ||
        (NSDecimalCompare (&decSize,
                           &oneK) == NSOrderedAscending))
        tmpString = [self formatDecimalSize: &decSize
                                      scale: 0
                                      units: ((size != 1) ?
                                              NSLocalizedString (@"Bytes", @"") :
                                              NSLocalizedString (@"Byte", @""))];
    else
        NSDecimalDivide (&decSize,
                         &decSize,
                         &oneK,
                         NSRoundPlain);
	
    if (!tmpString)
    {
        if (NSDecimalCompare (&decSize,
                              &oneK) == NSOrderedAscending)
            tmpString = [self formatDecimalSize: &decSize
                                          scale: 0
                                          units: (self.formatsSizeInBinaryUnits ?
                                                  NSLocalizedString (@"KiB", @"") :
                                                  NSLocalizedString (@"KB", @""))];
        else
            NSDecimalDivide (&decSize,
                             &decSize,
                             &oneK,
                             NSRoundPlain);
    }
	
    if (!tmpString)
    {
        if (NSDecimalCompare (&decSize,
                              &oneK) == NSOrderedAscending)
            tmpString = [self formatDecimalSize: &decSize
                                          scale: 1
                                          units: (self.formatsSizeInBinaryUnits ?
                                                  NSLocalizedString (@"MiB", @"") :
                                                  NSLocalizedString (@"MB", @""))];
        else
            NSDecimalDivide (&decSize,
                             &decSize,
                             &oneK,
                             NSRoundPlain);
    }
	
    if (!tmpString)
        tmpString = [self formatDecimalSize: &decSize
                                      scale: 2
                                      units: (self.formatsSizeInBinaryUnits ?
                                              NSLocalizedString (@"GiB", @"") :
                                              NSLocalizedString (@"GB", @""))];
	
    return tmpString;
}

- (NSString *) formatType: (NSString *) type
{
    return [type UTIDescription];
}

- (id) initWithReuseIdentifier: (NSString *) reuseIdentifier
{
    self = [super initWithStyle: UITableViewCellStyleDefault
                reuseIdentifier: reuseIdentifier];
	
    if (self)
    {
        self->dateLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];
        self->nameLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];
        self->sizeLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];
        self->typeLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];
		
        UIFont  *nameFont = [UIFont boldSystemFontOfSize: 24.0f];  // sort of sticky right now
        UIFont  *attrFont = [UIFont systemFontOfSize: 14.0f];
        CGFloat  minFontSize = 12.0f;
		
        self.dateLabel.adjustsFontSizeToFitWidth = YES;
        self.dateLabel.font = attrFont;
        self.dateLabel.lineBreakMode = UILineBreakModeMiddleTruncation;
        self.dateLabel.minimumFontSize = minFontSize;
        self.dateLabel.textAlignment = UITextAlignmentLeft;
        self.dateLabel.textColor = [UIColor grayColor];
		
        self.nameLabel.adjustsFontSizeToFitWidth = YES;
        self.nameLabel.font = nameFont;
        self.nameLabel.lineBreakMode = UILineBreakModeMiddleTruncation;
        self.nameLabel.minimumFontSize = minFontSize;
        self.nameLabel.textAlignment = UITextAlignmentLeft;
        self.nameLabel.textColor = [UIColor darkTextColor];
		
        self.sizeLabel.adjustsFontSizeToFitWidth = YES;
        self.sizeLabel.font = attrFont;
        self.sizeLabel.lineBreakMode = UILineBreakModeMiddleTruncation;
        self.sizeLabel.minimumFontSize = minFontSize;
        self.sizeLabel.textAlignment = UITextAlignmentRight;
        self.sizeLabel.textColor = [UIColor grayColor];
		
        self.typeLabel.adjustsFontSizeToFitWidth = YES;
        self.typeLabel.font = attrFont;
        self.typeLabel.lineBreakMode = UILineBreakModeMiddleTruncation;
        self.typeLabel.minimumFontSize = minFontSize;
        self.typeLabel.textAlignment = UITextAlignmentRight;
        self.typeLabel.textColor = [UIColor grayColor];
		
        [self.contentView addSubview: self.dateLabel];
        [self.contentView addSubview: self.nameLabel];
       // [self.contentView addSubview: self.sizeLabel];
       // [self.contentView addSubview: self.typeLabel];
    }
	
    return self;
}

- (void) setAlwaysFormatsSizeInBytes: (BOOL) alwaysFormatsSizeAsBytes
{
    if (self->alwaysFormatsSizeInBytes_ != alwaysFormatsSizeAsBytes)
    {
        self->alwaysFormatsSizeInBytes_ = alwaysFormatsSizeAsBytes;
		
        self.sizeLabel.text = [self formatSize: self.document.size];
    }
}

- (void) setDateFormatter: (NSDateFormatter *) dateFormatter
{
    if (self->dateFormatter_ != dateFormatter)
    {
        [self->dateFormatter_ release];
		
        self->dateFormatter_ = [dateFormatter retain];
		
        self.dateLabel.text = [self formatDate: (self.usesCreationDate ?
                                                 self.document.creationDate :
                                                 self.document.modificationDate)];
    }
}

- (void) setDocument: (MCDocument *) document
{
    if (self->document_ != document)
    {
        [self->document_ release];
		
        self->document_ = [document retain];
		
        self.dateLabel.text = [self formatDate: (self.usesCreationDate ?
                                                 self.document.creationDate :
                                                 self.document.modificationDate)];
		NSString *name = [self formatName: (self.usesPath ?
											self.document.path :
											self.document.title)]; 
		NSArray *parts = [name componentsSeparatedByString:@"-"];
		if ([parts count] !=2 )
		{
			// no hyphen, just make it plain
			
			self.nameLabel.text = name; 
			self.sizeLabel.text = [self formatSize: self.document.size];
			self.typeLabel.text = [self formatType: self.document.UTI];  
		} 
		else // if ([parts count] ==2)
		{
			// one hyphen , take part to the right make part of type label
			
			
			self.nameLabel.text = [parts objectAtIndex:0]; 
			self.sizeLabel.text = [self formatSize: self.document.size];
			self.typeLabel.text = [NSString stringWithFormat:@"%@-%@", [parts objectAtIndex:1],[self formatType: self.document.UTI]];
		}
    }
}

- (void) setFormatsSizeInBinaryUnits: (BOOL) formatsSizeInBinaryUnits
{
    if (self->formatsSizeInBinaryUnits_ != formatsSizeInBinaryUnits)
    {
        self->formatsSizeInBinaryUnits_ = formatsSizeInBinaryUnits;
		
        self.sizeLabel.text = [self formatSize: self.document.size];
    }
}

- (void) setUsesCreationDate: (BOOL) showsCreationDate
{
    if (self->usesCreationDate_ != showsCreationDate)
    {
        self->usesCreationDate_ = showsCreationDate;
		
        self.dateLabel.text = [self formatDate: (self.usesCreationDate ?
                                                 self.document.creationDate :
                                                 self.document.modificationDate)];
    }
}

- (void) setUsesPath: (BOOL) showsPath
{
    if (self->usesPath_ != showsPath)
    {
        self->usesPath_ = showsPath;
		
        self.nameLabel.text = [self formatName: (self.usesPath ?
                                                 self.document.path :
                                                 self.document.title)];
    }
}

#pragma mark Private Instance Methods

- (NSString *) formatDecimalSize: (NSDecimal *) size
                           scale: (NSInteger) scale
                           units: (NSString *) units
{
    static NSNumberFormatter *DecimalFormatter = nil;
	
    if (!DecimalFormatter)
    {
        DecimalFormatter = [NSNumberFormatter new];
		
        [DecimalFormatter setNumberStyle: NSNumberFormatterDecimalStyle];
    }
	
    NSDecimalNumberHandler *handler = [NSDecimalNumberHandler decimalNumberHandlerWithRoundingMode: NSRoundPlain
                                                                                             scale: scale
                                                                                  raiseOnExactness: NO
                                                                                   raiseOnOverflow: NO
                                                                                  raiseOnUnderflow: NO
                                                                               raiseOnDivideByZero: NO];
    NSDecimalNumber        *number = [NSDecimalNumber decimalNumberWithDecimal: *size];
	
    return [NSString stringWithFormat:
            @"%@ %@",
            [DecimalFormatter stringFromNumber:
             [number decimalNumberByRoundingAccordingToBehavior: handler]],
            units];
}

#pragma mark Overridden UITableViewCell Methods

- (id) initWithStyle: (UITableViewCellStyle) style
     reuseIdentifier: (NSString *) reuseIdentifier
{
    return [self initWithReuseIdentifier: reuseIdentifier];
}

#pragma mark Overridden UIView Methods

- (void) layoutSubviews
{
    [super layoutSubviews]; // Apple docs say we MUST do this ...
	
    UIEdgeInsets padding = UIEdgeInsetsMake (PADDING_TOP_INSET,
                                             PADDING_LEFT_INSET,
                                             PADDING_BOTTOM_INSET,
                                             PADDING_RIGHT_INSET);
	
    //
    // Size of content view (less padding):
    //
    CGSize  cvSize = CGSizeMake ((CGRectGetWidth (self.contentView.bounds) -
                                  padding.left -
                                  padding.right),
                                 (CGRectGetHeight (self.contentView.bounds) -
                                  padding.top -
                                  padding.bottom));
    CGFloat cvHalfWidth = (cvSize.width / 2.0f) - 2.0f;
	
    //
    // Compute name label frame:
    //
    CGRect nlFrame = CGRectZero;
	
    nlFrame.size = [self.nameLabel sizeThatFits: cvSize];
	
    if (nlFrame.size.width > cvSize.width)
        nlFrame.size.width = cvSize.width;
	
    nlFrame.origin.x = padding.left;
    nlFrame.origin.y = padding.top;
	
    //
    // Compute date label frame:
    //
    CGRect dlFrame = CGRectZero;
	
    dlFrame.size = [self.dateLabel sizeThatFits: cvSize];
	
    if (dlFrame.size.width > cvSize.width)
        dlFrame.size.width = cvSize.width;
	
    dlFrame.origin.x = padding.left;
    dlFrame.origin.y = (padding.top +
                        cvSize.height -
                        CGRectGetHeight (dlFrame) -
                        padding.bottom);
	
    //
    // Compute type label frame:
    //
    CGRect tlFrame = CGRectZero;
	
    tlFrame.size = [self.typeLabel sizeThatFits: cvSize];
	
    if (tlFrame.size.width > cvHalfWidth)
        tlFrame.size.width = cvHalfWidth;
	
    tlFrame.origin.x = (padding.left +
                        cvSize.width -
                        CGRectGetWidth (tlFrame) -
                        padding.right);
    tlFrame.origin.y = (CGRectGetMaxY (nlFrame) -
                        CGRectGetHeight (tlFrame) - 1.0f);
	
    //
    // Compute size label frame:
    //
    CGRect slFrame = CGRectZero;
	
    slFrame.size = [self.sizeLabel sizeThatFits: cvSize];
	
    if (slFrame.size.width > cvHalfWidth)
        slFrame.size.width = cvHalfWidth;
	
    slFrame.origin.x = (padding.left +
                        cvSize.width -
                        CGRectGetWidth (slFrame) -
                        padding.right);
    slFrame.origin.y = (padding.top +
                        cvSize.height -
                        CGRectGetHeight (slFrame) -
                        padding.bottom);
	
    //
    // Make any final adjustments:
    //
    if (CGRectIntersectsRect (nlFrame, tlFrame))
        nlFrame.size.width = cvSize.width - tlFrame.size.width - 4.0f;
	
    if (CGRectIntersectsRect (dlFrame, slFrame))
        dlFrame.size.width = cvSize.width - slFrame.size.width - 4.0f;
	
    //
    // Set all label frames:
    //
    self.nameLabel.frame = nlFrame;
    self.dateLabel.frame = dlFrame;
    self.typeLabel.frame = tlFrame;
    self.sizeLabel.frame = slFrame;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->dateFormatter_ release];
    [self->dateLabel_ release];
    [self->document_ release];
    [self->nameLabel_ release];
    [self->sizeLabel_ release];
    [self->typeLabel_ release];
	
    [super dealloc];
}

@end

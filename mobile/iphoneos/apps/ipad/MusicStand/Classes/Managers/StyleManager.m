//
//  StyleManager.m
//  MCProvider
//
//  Created by J. G. Pusey on 6/14/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "StyleManager.h"
#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>


#pragma mark -
#pragma mark Public Class StyleManager
#pragma mark -

#pragma mark Internal Constants

//
//  Sizes   images      fonts
//  -----   ------      -----
//  XXL       ?x?       24
//  XL      120x120     18
//  L        96x96      17
//  M        72x72      14
//  S        48x48      12
//  XS        ?x?       ?
//  XXS       ?x?       ?
//

#define BACKGROUND_COLOR_LIGHT_KEY         @"backgroundColorLight"
#define BACKGROUND_COLOR_LIGHTER_KEY       @"backgroundColorLighter"
#define DATE_FORMATTER_MEDIUM              @"dateFormatterMedium"
#define DEFAULT_CCR_THUMB_IMAGE_KEY        @"defaultCCRThumbImage"
#define DEFAULT_PART_THUMB_IMAGE_KEY       @"defaultPartThumbImage"
#define DEFAULT_SUBJECT_THUMB_IMAGE_KEY    @"defaultSubjectThumbImage"
#define FALLBACK_GROUP_LOGO_IMAGE_S_KEY    @"fallbackGroupLogoImageS"
#define FALLBACK_GROUP_LOGO_IMAGE_XXL_KEY  @"fallbackGroupLogoImageXXL"
#define FALLBACK_MEMBER_PHOTO_IMAGE_XL_KEY @"fallbackMemberPhotoImageXL"
#define FALLBACK_MEMBER_PHOTO_IMAGE_S_KEY  @"fallbackMemberPhotoImageS"
#define FALLBACK_USER_PHOTO_IMAGE_XL_KEY   @"fallbackUserPhotoImageXL"
#define LABEL_FONT_BOLD_L_KEY              @"labelFontBoldL"
#define LABEL_FONT_BOLD_M_KEY              @"labelFontBoldM"
#define LABEL_FONT_BOLD_S_KEY              @"labelFontBoldS"
#define LABEL_FONT_BOLD_XL_KEY             @"labelFontBoldXL"
#define LABEL_FONT_BOLD_XXL_KEY            @"labelFontBoldXXL"
#define LABEL_FONT_ITALIC_L_KEY            @"labelFontItalicL"
#define LABEL_FONT_ITALIC_M_KEY            @"labelFontItalicM"
#define LABEL_FONT_ITALIC_S_KEY            @"labelFontItalicS"
#define LABEL_FONT_ITALIC_XL_KEY           @"labelFontItalicXL"
#define LABEL_FONT_ITALIC_XXL_KEY          @"labelFontItalicXXL"
#define LABEL_FONT_L_KEY                   @"labelFontL"
#define LABEL_FONT_M_KEY                   @"labelFontM"
#define LABEL_FONT_S_KEY                   @"labelFontS"
#define LABEL_FONT_XL_KEY                  @"labelFontXL"
#define LABEL_FONT_XXL_KEY                 @"labelFontXXL"
#define TEXT_FONT_L_KEY                    @"textFontL"
#define TEXT_FONT_M_KEY                    @"textFontM"
#define TEXT_FONT_S_KEY                    @"textFontS"
#define THUMB_BORDER_HIGHLIGHTED_KEY       @"thumbBorderHighlighted"
#define THUMB_BORDER_HIGHLIGHTED_BOLD_KEY  @"thumbBorderHighlightedBold"
#define THUMB_BORDER_NORMAL_KEY            @"thumbBorderNormal"
#define THUMB_BORDER_PLACEHOLDER_KEY       @"thumbBorderPlaceholder"

#define TEXT_FONT_NAME                    @"Arial"

#define THUMB_BORDER_WIDTH                1.0f
#define THUMB_CORNER_RADIUS               4.0f

@interface StyleManager ()

@property (nonatomic, retain, readwrite) NSMutableDictionary *styleDict;

@end

@implementation StyleManager

@dynamic    backgroundColorLight;
@dynamic    backgroundColorLighter;
@dynamic    dateFormatterMedium;
@dynamic    defaultCCRThumbImage;
@dynamic    defaultPartThumbImage;
@dynamic    defaultSubjectThumbImage;
@dynamic    fallbackGroupLogoImageS;
@dynamic    fallbackGroupLogoImageXXL;
@dynamic    fallbackMemberPhotoImageXL;
@dynamic    fallbackMemberPhotoImageS;
@dynamic    fallbackUserPhotoImageXL;
@dynamic    labelFontBoldL;
@dynamic    labelFontBoldM;
@dynamic    labelFontBoldS;
@dynamic    labelFontBoldXL;
@dynamic    labelFontBoldXXL;
@dynamic    labelFontItalicL;
@dynamic    labelFontItalicM;
@dynamic    labelFontItalicS;
@dynamic    labelFontItalicXL;
@dynamic    labelFontItalicXXL;
@dynamic    labelFontL;
@dynamic    labelFontM;
@dynamic    labelFontS;
@dynamic    labelFontXL;
@dynamic    labelFontXXL;
@synthesize styleDict                  = styleDict_;
@dynamic    textFontL;
@dynamic    textFontM;
@dynamic    textFontS;
@dynamic    thumbBorderHighlighted;
@dynamic    thumbBorderHighlightedBold;
@dynamic    thumbBorderNormal;
@dynamic    thumbBorderPlaceholder;

#pragma mark Public Class Methods

+ (StyleManager *) sharedInstance
{
    static StyleManager *SharedInstance;

    if (!SharedInstance)
        SharedInstance = [[StyleManager alloc] init];

    return SharedInstance;
}

#pragma mark Private Instance Methods

- (UIColor *) backgroundColorLight
{
    UIColor *color = [self.styleDict objectForKey: BACKGROUND_COLOR_LIGHT_KEY];

    if (!color)
    {
        color = [UIColor colorWithWhite: 0.92f
                                  alpha: 1.0f];

        [self.styleDict setObject: color
                           forKey: BACKGROUND_COLOR_LIGHT_KEY];
    }

    return color;
}

- (UIColor *) backgroundColorLighter
{
    UIColor *color = [self.styleDict objectForKey: BACKGROUND_COLOR_LIGHTER_KEY];

    if (!color)
    {
        color = [UIColor colorWithWhite: 0.96f
                                  alpha: 1.0f];

        [self.styleDict setObject: color
                           forKey: BACKGROUND_COLOR_LIGHTER_KEY];
    }

    return color;
}

- (NSDateFormatter *) dateFormatterMedium
{
    NSDateFormatter *formatter = [self.styleDict objectForKey: DATE_FORMATTER_MEDIUM];

    if (!formatter)
    {
        formatter = [[[NSDateFormatter alloc] init]
                     autorelease];

        formatter.dateStyle = NSDateFormatterMediumStyle;
        formatter.timeStyle = NSDateFormatterMediumStyle;

        [self.styleDict setObject: formatter
                           forKey: DATE_FORMATTER_MEDIUM];
    }

    return formatter;
}

- (UIImage *) defaultCCRThumbImage
{
    UIImage *image = [self.styleDict objectForKey: DEFAULT_CCR_THUMB_IMAGE_KEY];

    if (!image)
    {
        image = [UIImage imageNamed: @"FallbackThumb-136x136.png"];

        [self.styleDict setObject: image
                           forKey: DEFAULT_CCR_THUMB_IMAGE_KEY];
    }

    return image;
}

- (UIImage *) defaultPartThumbImage
{
    UIImage *image = [self.styleDict objectForKey: DEFAULT_PART_THUMB_IMAGE_KEY];

    if (!image)
    {
        NSString *imageName;

        switch (UI_USER_INTERFACE_IDIOM ())
        {
            case UIUserInterfaceIdiomPad :
                imageName = @"LiteSilhouette-96x96.png";    // L
                break;

            case UIUserInterfaceIdiomPhone :
            default :
                imageName = @"LiteSilhouette-48x48.png";    // S
                break;
        }

        image = [UIImage imageNamed: imageName];

        [self.styleDict setObject: image
                           forKey: DEFAULT_PART_THUMB_IMAGE_KEY];
    }

    return image;
}

- (UIImage *) defaultSubjectThumbImage
{
    UIImage *image = [self.styleDict objectForKey: DEFAULT_SUBJECT_THUMB_IMAGE_KEY];

    if (!image)
    {
        NSString *imageName;

        switch (UI_USER_INTERFACE_IDIOM ())
        {
            case UIUserInterfaceIdiomPad :
                imageName = @"LiteSilhouette-120x120.png";  // XL
                break;

            case UIUserInterfaceIdiomPhone :
            default :
                imageName = @"LiteSilhouette-72x72.png";    // M
                break;
        }

        image = [UIImage imageNamed: imageName];

        [self.styleDict setObject: image
                           forKey: DEFAULT_SUBJECT_THUMB_IMAGE_KEY];
    }

    return image;
}

- (UIImage *) fallbackGroupLogoImageS
{
    return nil; // none for now ...
}

- (UIImage *) fallbackGroupLogoImageXXL
{
    return nil; //none for now ...
}

- (UIImage *) fallbackMemberPhotoImageXL
{
    UIImage *image = [self.styleDict objectForKey: FALLBACK_MEMBER_PHOTO_IMAGE_XL_KEY];

    if (!image)
    {
        image = [UIImage imageNamed: @"DarkSilhouette-120x120.png"];

        [self.styleDict setObject: image
                           forKey: FALLBACK_MEMBER_PHOTO_IMAGE_XL_KEY];
    }

    return image;
}

- (UIImage *) fallbackMemberPhotoImageS
{
    UIImage *image = [self.styleDict objectForKey: FALLBACK_MEMBER_PHOTO_IMAGE_S_KEY];

    if (!image)
    {
        image = [UIImage imageNamed: @"DarkSilhouette-48x48.png"];

        [self.styleDict setObject: image
                           forKey: FALLBACK_MEMBER_PHOTO_IMAGE_S_KEY];
    }

    return image;
}

- (UIImage *) fallbackUserPhotoImageXL
{
    UIImage *image = [self.styleDict objectForKey: FALLBACK_USER_PHOTO_IMAGE_XL_KEY];

    if (!image)
    {
        image = [UIImage imageNamed: @"DarkSilhouette-120x120.png"];

        [self.styleDict setObject: image
                           forKey: FALLBACK_USER_PHOTO_IMAGE_XL_KEY];
    }

    return image;
}

- (UIFont *) labelFontBoldL
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_BOLD_L_KEY];

    if (!font)
    {
        font = [UIFont boldSystemFontOfSize: [UIFont labelFontSize]];   // 17.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_BOLD_L_KEY];
    }

    return font;
}

- (UIFont *) labelFontBoldM
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_BOLD_M_KEY];

    if (!font)
    {
        font = [UIFont boldSystemFontOfSize: [UIFont systemFontSize]];  // 14.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_BOLD_M_KEY];
    }

    return font;
}

- (UIFont *) labelFontBoldS
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_BOLD_S_KEY];

    if (!font)
    {
        font = [UIFont boldSystemFontOfSize: [UIFont smallSystemFontSize]]; // 12.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_BOLD_S_KEY];
    }

    return font;
}

- (UIFont *) labelFontBoldXL
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_BOLD_XL_KEY];

    if (!font)
    {
        font = [UIFont boldSystemFontOfSize: [UIFont buttonFontSize]];  // 18.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_BOLD_XL_KEY];
    }

    return font;
}

- (UIFont *) labelFontBoldXXL
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_BOLD_XXL_KEY];

    if (!font)
    {
        font = [UIFont boldSystemFontOfSize: 24.0f];

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_BOLD_XXL_KEY];
    }

    return font;
}

- (UIFont *) labelFontItalicL
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_ITALIC_L_KEY];

    if (!font)
    {
        font = [UIFont italicSystemFontOfSize: [UIFont labelFontSize]]; // 17.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_ITALIC_L_KEY];
    }

    return font;
}

- (UIFont *) labelFontItalicM
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_ITALIC_M_KEY];

    if (!font)
    {
        font = [UIFont italicSystemFontOfSize: [UIFont systemFontSize]];    // 14.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_ITALIC_M_KEY];
    }

    return font;
}

- (UIFont *) labelFontItalicS
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_ITALIC_S_KEY];

    if (!font)
    {
        font = [UIFont italicSystemFontOfSize: [UIFont smallSystemFontSize]];   // 12.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_ITALIC_S_KEY];
    }

    return font;
}

- (UIFont *) labelFontItalicXL
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_ITALIC_XL_KEY];

    if (!font)
    {
        font = [UIFont italicSystemFontOfSize: [UIFont buttonFontSize]];    // 18.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_ITALIC_XL_KEY];
    }

    return font;
}

- (UIFont *) labelFontItalicXXL
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_ITALIC_XXL_KEY];

    if (!font)
    {
        font = [UIFont italicSystemFontOfSize: 24.0f];

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_ITALIC_XXL_KEY];
    }

    return font;
}

- (UIFont *) labelFontL
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_L_KEY];

    if (!font)
    {
        font = [UIFont systemFontOfSize: [UIFont labelFontSize]];   // 17.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_L_KEY];
    }

    return font;
}

- (UIFont *) labelFontM
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_M_KEY];

    if (!font)
    {
        font = [UIFont systemFontOfSize: [UIFont systemFontSize]];  // 14.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_M_KEY];
    }

    return font;
}

- (UIFont *) labelFontS
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_S_KEY];

    if (!font)
    {
        font = [UIFont systemFontOfSize: [UIFont smallSystemFontSize]]; // 12.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_S_KEY];
    }

    return font;
}

- (UIFont *) labelFontXL
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_XL_KEY];

    if (!font)
    {
        font = [UIFont systemFontOfSize: [UIFont buttonFontSize]];  // 18.0f

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_XL_KEY];
    }

    return font;
}

- (UIFont *) labelFontXXL
{
    UIFont *font = [self.styleDict objectForKey: LABEL_FONT_XXL_KEY];

    if (!font)
    {
        font = [UIFont systemFontOfSize: 24.0f];

        [self.styleDict setObject: font
                           forKey: LABEL_FONT_XXL_KEY];
    }

    return font;
}

- (UIFont *) textFontL
{
    UIFont *font = [self.styleDict objectForKey: TEXT_FONT_L_KEY];

    if (!font)
    {
        font = [UIFont fontWithName: TEXT_FONT_NAME
                               size: [UIFont labelFontSize]];   // 17.0f

        [self.styleDict setObject: font
                           forKey: TEXT_FONT_L_KEY];
    }

    return font;
}

- (UIFont *) textFontM
{
    UIFont *font = [self.styleDict objectForKey: TEXT_FONT_M_KEY];

    if (!font)
    {
        font = [UIFont fontWithName: TEXT_FONT_NAME
                               size: [UIFont systemFontSize]];  // 14.0f

        [self.styleDict setObject: font
                           forKey: TEXT_FONT_M_KEY];
    }

    return font;
}

- (UIFont *) textFontS
{
    UIFont *font = [self.styleDict objectForKey: TEXT_FONT_S_KEY];

    if (!font)
    {
        font = [UIFont fontWithName: TEXT_FONT_NAME
                               size: [UIFont smallSystemFontSize]]; // 12.0f

        [self.styleDict setObject: font
                           forKey: TEXT_FONT_S_KEY];
    }

    return font;
}

- (MCBorder *) thumbBorderHighlighted
{
    MCBorder *border = [self.styleDict objectForKey: THUMB_BORDER_HIGHLIGHTED_KEY];

    if (!border)
    {
        border = [[[MCBorder alloc] initWithColor: [UIColor blueColor]
                                            width: THUMB_BORDER_WIDTH * 2.0f
                                     cornerRadius: THUMB_CORNER_RADIUS]
                  autorelease];

        [self.styleDict setObject: border
                           forKey: THUMB_BORDER_HIGHLIGHTED_KEY];
    }

    return border;
}

- (MCBorder *) thumbBorderHighlightedBold
{
    MCBorder *border = [self.styleDict objectForKey: THUMB_BORDER_HIGHLIGHTED_BOLD_KEY];

    if (!border)
    {
        border = [[[MCBorder alloc] initWithColor: [UIColor whiteColor]
                                            width: THUMB_BORDER_WIDTH * 4.0f
                                     cornerRadius: THUMB_CORNER_RADIUS]
                  autorelease];

        [self.styleDict setObject: border
                           forKey: THUMB_BORDER_HIGHLIGHTED_BOLD_KEY];
    }

    return border;
}

- (MCBorder *) thumbBorderNormal
{
    MCBorder *border = [self.styleDict objectForKey: THUMB_BORDER_NORMAL_KEY];

    if (!border)
    {
        border = [[[MCBorder alloc] initWithColor: [UIColor blackColor]
                                            width: THUMB_BORDER_WIDTH
                                     cornerRadius: THUMB_CORNER_RADIUS]
                  autorelease];

        [self.styleDict setObject: border
                           forKey: THUMB_BORDER_NORMAL_KEY];
    }

    return border;
}

- (MCBorder *) thumbBorderPlaceholder
{
    MCBorder *border = [self.styleDict objectForKey: THUMB_BORDER_PLACEHOLDER_KEY];

    if (!border)
    {
        border = [[[MCBorder alloc] initWithColor: [UIColor lightGrayColor]
                                            width: THUMB_BORDER_WIDTH
                                     cornerRadius: THUMB_CORNER_RADIUS]
                  autorelease];

        [self.styleDict setObject: border
                           forKey: THUMB_BORDER_PLACEHOLDER_KEY];
    }

    return border;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->styleDict_ release];

    [super dealloc];
}

- (id) init
{
    self = [super init];

    if (self)
        self->styleDict_ = [[NSMutableDictionary alloc] init];

    return self;
}

@end

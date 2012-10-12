//
//  StyleManager.h
//  MCProvider
//
//  Created by J. G. Pusey on 6/14/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MCBorder;

@interface StyleManager : NSObject
{
@private

    NSMutableDictionary *styleDict_;
}

@property (nonatomic, retain, readonly) UIColor         *backgroundColorDark;
@property (nonatomic, retain, readonly) UIColor         *backgroundColorLight;
@property (nonatomic, retain, readonly) UIColor         *backgroundColorLighter;
@property (nonatomic, retain, readonly) NSDateFormatter *dateFormatterMedium;
@property (nonatomic, retain, readonly) UIImage         *defaultCCRThumbImage;
@property (nonatomic, retain, readonly) UIImage         *defaultPartThumbImage;
@property (nonatomic, retain, readonly) UIImage         *defaultSubjectThumbImage;
@property (nonatomic, retain, readonly) UIImage         *fallbackGroupLogoImageS;
@property (nonatomic, retain, readonly) UIImage         *fallbackGroupLogoImageXXL;
@property (nonatomic, retain, readonly) UIImage         *fallbackMemberPhotoImageXL;
@property (nonatomic, retain, readonly) UIImage         *fallbackMemberPhotoImageS;
@property (nonatomic, retain, readonly) UIImage         *fallbackUserPhotoImageXL;
@property (nonatomic, retain, readonly) UIFont          *labelFontBoldL;
@property (nonatomic, retain, readonly) UIFont          *labelFontBoldM;
@property (nonatomic, retain, readonly) UIFont          *labelFontBoldS;
@property (nonatomic, retain, readonly) UIFont          *labelFontBoldXL;
@property (nonatomic, retain, readonly) UIFont          *labelFontBoldXXL;
@property (nonatomic, retain, readonly) UIFont          *labelFontItalicL;
@property (nonatomic, retain, readonly) UIFont          *labelFontItalicM;
@property (nonatomic, retain, readonly) UIFont          *labelFontItalicS;
@property (nonatomic, retain, readonly) UIFont          *labelFontItalicXL;
@property (nonatomic, retain, readonly) UIFont          *labelFontItalicXXL;
@property (nonatomic, retain, readonly) UIFont          *labelFontL;
@property (nonatomic, retain, readonly) UIFont          *labelFontM;
@property (nonatomic, retain, readonly) UIFont          *labelFontS;
@property (nonatomic, retain, readonly) UIFont          *labelFontXL;
@property (nonatomic, retain, readonly) UIFont          *labelFontXXL;
@property (nonatomic, retain, readonly) UIFont          *textFontL;
@property (nonatomic, retain, readonly) UIFont          *textFontM;
@property (nonatomic, retain, readonly) UIFont          *textFontS;
@property (nonatomic, retain, readonly) MCBorder        *thumbBorderHighlighted;
@property (nonatomic, retain, readonly) MCBorder        *thumbBorderHighlightedBold;
@property (nonatomic, retain, readonly) MCBorder        *thumbBorderNormal;
@property (nonatomic, retain, readonly) MCBorder        *thumbBorderPlaceholder;

+ (StyleManager *) sharedInstance;

@end

//
//  CCRGestures.h
//  MCProvider
//
//  Created by J. G. Pusey on 10/26/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CCRGestures : NSObject
{
@private

    SEL                       pan1DownAction_;
    SEL                       pan1LeftAction_;
    SEL                       pan1RightAction_;
    SEL                       pan1UpAction_;
    UIPanGestureRecognizer   *panGesture_;
    UIPinchGestureRecognizer *pinchGesture_;
    SEL                       pinchInAction_;
    SEL                       pinchOutAction_;
    SEL                       swipe2DownAction_;
    UISwipeGestureRecognizer *swipe2DownGesture_;
    SEL                       swipe2LeftAction_;
    UISwipeGestureRecognizer *swipe2LeftGesture_;
    SEL                       swipe2RightAction_;
    UISwipeGestureRecognizer *swipe2RightGesture_;
    SEL                       swipe2UpAction_;
    UISwipeGestureRecognizer *swipe2UpGesture_;
    SEL                       tap1DoubleAction_;
    UITapGestureRecognizer   *tap1DoubleGesture_;
    SEL                       tap1SingleAction_;
    UITapGestureRecognizer   *tap1SingleGesture_;
    SEL                       tap1TripleAction_;
    UITapGestureRecognizer   *tap1TripleGesture_;
    id                        target_;
    UIView                   *view_;
}

@property (nonatomic, assign, readwrite) SEL     pan1DownAction;
@property (nonatomic, assign, readwrite) SEL     pan1LeftAction;
@property (nonatomic, assign, readwrite) SEL     pan1RightAction;
@property (nonatomic, assign, readwrite) SEL     pan1UpAction;
@property (nonatomic, assign, readwrite) SEL     pinchInAction;
@property (nonatomic, assign, readwrite) SEL     pinchOutAction;
@property (nonatomic, assign, readwrite) SEL     swipe2DownAction;
@property (nonatomic, assign, readwrite) SEL     swipe2LeftAction;
@property (nonatomic, assign, readwrite) SEL     swipe2RightAction;
@property (nonatomic, assign, readwrite) SEL     swipe2UpAction;
@property (nonatomic, assign, readwrite) SEL     tap1DoubleAction;
@property (nonatomic, assign, readwrite) SEL     tap1SingleAction;
@property (nonatomic, assign, readwrite) SEL     tap1TripleAction;
@property (nonatomic, assign, readwrite) id      target;
@property (nonatomic, retain, readwrite) UIView *view;

@end

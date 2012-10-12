//
//  CCRShareActionController.m
//  MCProvider
//
//  Created by J. G. Pusey on 9/29/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "CCRActionItem.h"
#import "CCRShareActionController.h"

#pragma mark -
#pragma mark Public Class CCRActionController
#pragma mark -

@interface CCRShareActionController ()

@property (nonatomic, retain, readonly) NSMutableArray *items;

- (NSUInteger) indexOfItemWithIdentifier: (NSString *) identifier;

@end

@implementation CCRShareActionController

@synthesize items = items_;

#pragma mark Public Instance Methods

- (void) addItem: (CCRActionItem *) item
{
    if (item)
    {
        [self.items addObject: item];

        [self addOtherButtonWithTitle: item.title
                               target: item
                               action: @selector (performAction)
                             userInfo: nil];
    }
}

- (CCRActionItem *) itemWithIdentifier: (NSString *) identifier
{
    NSUInteger idx = [self indexOfItemWithIdentifier: identifier];

    return ((idx != NSNotFound) ?
            [self.items objectAtIndex: idx] :
            nil);
}

- (void) removeAllItems
{
    NSArray *tmpItems = [self.items copy];

    [self.items removeAllObjects];

    for (CCRActionItem *item in tmpItems)
        [self removeOtherButtonWithTitle: item.title
                                  target: item
                                  action: @selector (performAction)
                                userInfo: nil];
    
    [tmpItems release];
}

- (void) removeItemWithIdentifier: (NSString *) identifier
{
    NSUInteger idx = [self indexOfItemWithIdentifier: identifier];

    if (idx != NSNotFound)
    {
        CCRActionItem *item = [self.items objectAtIndex: idx];

        [self removeOtherButtonWithTitle: item.title
                                  target: item
                                  action: @selector (performAction)
                                userInfo: nil];

        [self.items removeObjectAtIndex: idx];
    }
}

#pragma mark Private Instance Methods

- (NSUInteger) indexOfItemWithIdentifier: (NSString *) identifier
{
    if (identifier)
    {
        NSUInteger idx = 0;

        for (CCRActionItem *item in self.items)
        {
            if ([item.identifier isEqualToString: identifier])
                return idx;

            idx++;
        }
    }

    return NSNotFound;
}

#pragma mark Overridden ShareActionController Methods

- (id) initWithViewController: (UIViewController *) viewController
                      webView: (UIWebView *) webView
{
    self = [super initWithViewController: viewController
                                 webView: webView];

    if (self)
        self->items_ = [NSMutableArray new];

    return self;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->items_ release];

    [super dealloc];
}

@end

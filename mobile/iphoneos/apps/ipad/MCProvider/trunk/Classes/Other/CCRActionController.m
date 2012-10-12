//
//  CCRActionController.m
//  MCProvider
//
//  Created by J. G. Pusey on 9/15/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "CCRActionController.h"
#import "CCRActionItem.h"

#pragma mark -
#pragma mark Public Class CCRActionController
#pragma mark -

@interface CCRActionController ()

@property (nonatomic, retain, readonly) NSMutableArray *items;

- (NSUInteger) indexOfItemWithIdentifier: (NSString *) identifier;

@end

@implementation CCRActionController

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
    [tmpItems release]; //wld 1may11
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

#pragma mark Overridden MCActionController Methods

- (id) initWithTitle: (NSString *) title
{
    self = [super initWithTitle: title];

    if (self)
    {
        self->items_ = [NSMutableArray new];

        if (self.appDelegate.targetIdiom != UIUserInterfaceIdiomPad)
            [self addCancelButtonWithTitle: NSLocalizedString (@"Cancel", @"")
                                    target: nil
                                    action: NULL
                                  userInfo: nil];
    }

    return self;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->items_ release];

    [super dealloc];
}

@end

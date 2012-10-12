//
//  CCRThumbListView.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/31/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CCRThumbCell.h"
#import "CCRThumbListView.h"
#import "CCRView.h"

#pragma mark -
#pragma mark Public Class CCRThumbListView
#pragma mark -

//
// Apparently the following declaration:
//
//  @interface CCRThumbListView () <AQGridViewDataSource>
//
// does NOT suffice when the AQGridView setDataSource: method actually calls
// the conformsToProtocol: method -- go figure ...
//
@interface CCRThumbListView (AQGridViewDataSourceKluge) <AQGridViewDataSource>

@end

//
// Likewise the following declaration:
//
//  @interface CCRThumbListView () <AQGridViewDelegate>
//
// does NOT suffice when the AQGridView setDelegate: method actually calls
// the conformsToProtocol: method -- go figure ...
//
@interface CCRThumbListView (AQGridViewDelegateKluge) <AQGridViewDelegate>

@end

@interface CCRThumbListView ()

@property (nonatomic, retain, readonly) NSMutableArray *cells;
@property (nonatomic, assign, readonly) BOOL            isVertical;

- (NSUInteger) indexOfCellWithIdentifier: (NSString *) identifier;

@end

@implementation CCRThumbListView

@synthesize activeCell = activeCell_;
@synthesize cells      = cells_;
@dynamic    isVertical;

#pragma mark Public Instance Methods

- (void) addCell: (CCRThumbCell *) cell
{
    if (cell)
    {
        [self.cells addObject: cell];

        [self reloadData];  // for now ...
        [self sizeToFit];   // MUST come AFTER reload ...
    }
}

- (CCRThumbCell *) cellWithIdentifier: (NSString *) identifier
{
    NSUInteger idx = [self indexOfCellWithIdentifier: identifier];

    return ((idx != NSNotFound) ?
            [self.cells objectAtIndex: idx] :
            nil);
}

- (void) removeAllCells
{
    self.activeCell = nil;

    [self.cells removeAllObjects];

    [self reloadData];
    [self sizeToFit];   // MUST come AFTER reload ...
}

- (void) removeCellWithIdentifier: (NSString *) identifier
{
    NSUInteger idx = [self indexOfCellWithIdentifier: identifier];

    if (idx != NSNotFound)
    {
        [self.cells removeObjectAtIndex: idx];

        [self reloadData];  // for now ...
        [self sizeToFit];   // MUST come AFTER reload ...
    }
}

- (void) setActiveCell: (CCRThumbCell *) cell
{
    if (self->activeCell_ != cell)
    {
        //
        // Deactivate old active cell (if any):
        //
        if (self->activeCell_)
        {
            [self->activeCell_ deactivate];
            [self->activeCell_ release];
        }

        self->activeCell_ = [cell retain];

        //
        // Activate new active cell (if any):
        //
        if (self->activeCell_)
            [self->activeCell_ activate];
    }
}

- (CGSize) sizeThatFitsHorizontal: (CGSize) size
{
    size.height = [CCRThumbCell preferredSize].height;

    return size;
}

- (CGSize) sizeThatFitsVertical: (CGSize) size
{
    size.width = [CCRThumbCell preferredSize].width;

    return size;
}

#pragma mark Private Instance Methods

- (NSUInteger) indexOfCellWithIdentifier: (NSString *) identifier
{
    if (identifier)
    {
        NSUInteger idx = 0;

        for (CCRThumbCell *cell in self.cells)
        {
            if ([cell.identifier isEqualToString: identifier])
                return idx;

            idx++;
        }
    }

    return NSNotFound;
}

- (BOOL) isVertical
{
    switch (self.layoutDirection)
    {
        case AQGridViewLayoutDirectionHorizontal :
            return NO;

        case AQGridViewLayoutDirectionVertical :
        default :
            return YES;
    }
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame: frame];

    if (self)
    {
        self->cells_ = [NSMutableArray new];

        self.backgroundColor = [UIColor orangeColor];
        self.dataSource = self;
        self.delegate = self;
        self.resizesCellWidthToFit = YES;
    }

    return self;
}

- (CGSize) sizeThatFits: (CGSize) size
{
    return (self.isVertical ?
            [self sizeThatFitsVertical: size] :
            [self sizeThatFitsHorizontal: size]);
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->cells_ release];

    [super dealloc];
}

@end

#pragma mark AQGridViewDataSource Methods

@implementation CCRThumbListView (AQGridViewDataSourceKluge)

- (AQGridViewCell *) gridView: (AQGridView *) gridView
           cellForItemAtIndex: (NSUInteger) idx
{
    AQGridViewCell *cell =  [self.cells objectAtIndex: idx];

    cell.contentView.backgroundColor = gridView.backgroundColor;

    return cell;
}

- (NSUInteger) numberOfItemsInGridView: (AQGridView *) gridView
{
    return [self.cells count];
}

- (CGSize) portraitGridCellSizeForGridView: (AQGridView *) gridView
{
    CGSize  size = [CCRThumbCell preferredSize];
    CGFloat rows = floorf (CGRectGetHeight (gridView.bounds) / size.height);
    CGFloat cols = floorf (CGRectGetWidth (gridView.bounds) / size.width);

    size.width = floorf (CGRectGetWidth (gridView.bounds) / cols);
    size.height = floorf (CGRectGetHeight (gridView.bounds) / rows);

    return size;
}

@end

#pragma mark AQGridViewDelegate Methods

@implementation CCRThumbListView (AQGridViewDelegateKluge)

- (void) gridView: (AQGridView *) gridView
didSelectItemAtIndex: (NSUInteger) idx
{
    [(CCRView *) self.superview normalizeThumbListViewAnimated: YES];

    CCRThumbCell *cell = [self.cells objectAtIndex: idx];

    [cell.target performSelector: cell.action
                      withObject: cell];
}

- (void) gridView: (AQGridView *) gridView
  willDisplayCell: (AQGridViewCell *) cell
   forItemAtIndex: (NSUInteger) idx
{
    if (cell == self.activeCell)
        [(CCRThumbCell *) cell activate];
    else
        [(CCRThumbCell *) cell deactivate];
}

@end

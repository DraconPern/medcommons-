//
//  CCRToolListView.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/31/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CCRToolCell.h"
#import "CCRToolListView.h"

#pragma mark -
#pragma mark Public Class CCRToolListView
#pragma mark -

//
// Apparently the following declaration:
//
//  @interface CCRToolListView () <AQGridViewDataSource>
//
// does NOT suffice when the AQGridView setDataSource: method actually calls
// the conformsToProtocol: method -- go figure ...
//
@interface CCRToolListView (AQGridViewDataSourceKluge) <AQGridViewDataSource>

@end

@interface CCRToolListView ()

@property (nonatomic, retain, readonly) NSMutableArray *cells;
@property (nonatomic, assign, readonly) BOOL            isVertical;

- (NSUInteger) indexOfCellWithIdentifier: (NSString *) identifier;

@end

@implementation CCRToolListView

@synthesize cells      = cells_;
@dynamic    isVertical;

#pragma mark Public Instance Methods

- (void) addCell: (CCRToolCell *) cell
{
    if (cell)
    {
        [self.cells addObject: cell];

        [self reloadData];  // for now ...
        [self sizeToFit];   // MUST come AFTER reload ...
    }
}

- (CCRToolCell *) cellWithIdentifier: (NSString *) identifier
{
    NSUInteger idx = [self indexOfCellWithIdentifier: identifier];

    return ((idx != NSNotFound) ?
            [self.cells objectAtIndex: idx] :
            nil);
}

- (CCRToolCell *) cellWithImage: (UIImage *) image
{
    for (CCRToolCell *cell in self.cells)
    {
        if (cell.image == image)
            return cell;
    }

    return nil;
}

- (void) removeAllCells
{
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

- (CGSize) sizeThatFitsHorizontal: (CGSize) size
{
    size = [CCRToolCell preferredSize];

    NSUInteger maxCells = (NSUInteger) floorf (CGRectGetWidth (self.superview.bounds) /
                                               size.width);
    NSUInteger numCells = self.numberOfItems;

    if (numCells < 1)
        numCells = 1;

    if (numCells > maxCells)
        numCells = maxCells;

    size.width *= numCells;

    return size;
}

- (CGSize) sizeThatFitsVertical: (CGSize) size
{
    size = [CCRToolCell preferredSize];

    NSUInteger maxCells = (NSUInteger) floorf (CGRectGetHeight (self.superview.bounds) /
                                               size.height);
    NSUInteger numCells = self.numberOfItems;

    if (numCells < 1)
        numCells = 1;

    if (numCells > maxCells)
        numCells = maxCells;

    size.height *= numCells;

    return size;
}

#pragma mark Private Instance Methods

- (NSUInteger) indexOfCellWithIdentifier: (NSString *) identifier
{
    if (identifier)
    {
        NSUInteger idx = 0;

        for (CCRToolCell *cell in self.cells)
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

        self.backgroundColor = [UIColor blueColor];
        self.dataSource = self;
        self.resizesCellWidthToFit = YES;
        self.separatorStyle = AQGridViewCellSeparatorStyleNone;
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

@implementation CCRToolListView (AQGridViewDataSourceKluge)

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
    return [CCRToolCell preferredSize];
}

@end

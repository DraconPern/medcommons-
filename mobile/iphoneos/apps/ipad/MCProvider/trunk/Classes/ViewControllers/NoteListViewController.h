//
//  NoteListViewController.h
//  MCProvider
//
//  Created by J. G. Pusey on 4/2/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "Member.h"

@protocol NoteListViewControllerDelegate;

//
// IMPORTANT: Note list view controllers _must_ be used inside of a
// navigation controller in order to function properly.
//
@interface NoteListViewController : UIViewController
{
@private

    UIBarButtonItem                     *addButton_;
    UIBarButtonItem                     *backButton_;
    id <NoteListViewControllerDelegate>  delegate_;
    NSMutableArray                      *editableNotes_;
    Member                              *member_;
    NSMutableIndexSet                   *pendingDeletes_;
    NSUInteger                           pendingInsertCount_;
}

@property (nonatomic, assign, readwrite) id <NoteListViewControllerDelegate>  delegate;
@property (nonatomic, assign, readonly)  BOOL                                 hidesMasterViewInLandscape;
@property (nonatomic, retain, readonly)  Member                              *member;

- (id) initWithMember: (Member *) member;

@end

@protocol NoteListViewControllerDelegate <NSObject>

@optional

- (void) noteListViewControllerDidFinish: (NoteListViewController *) nlvc;

@end

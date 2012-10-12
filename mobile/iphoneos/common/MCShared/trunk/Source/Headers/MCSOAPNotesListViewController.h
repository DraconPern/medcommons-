//
//  MCSOAPNotesListViewController.h
//  MCShared
//
//  Created by J. G. Pusey on 4/2/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "MCSOAPNotesProvider.h";
#import "MCSOAPNoteViewController.h"

@protocol MCSOAPNotesListViewControllerDelegate;

//
// IMPORTANT: SOAP notes list view controllers _must_ be used inside of a
// navigation controller in order to function properly.
//
@interface MCSOAPNotesListViewController : UIViewController <UISplitViewControllerDelegate>
{
@private

    UIBarButtonItem                            *addButton_;
    UIBarButtonItem                            *backButton_;
    id <MCSOAPNotesListViewControllerDelegate>  delegate_;
    UIPopoverController                        *popoverController_;
    MCSOAPNotesProvider                        *provider_;
    //    UIBarButtonItem                            *refreshButton_;
    UIBarButtonItem                            *unwidenButton_;
    UIBarButtonItem                            *widenButton_;
}

@property (nonatomic, assign)   id <MCSOAPNotesListViewControllerDelegate>  delegate;
@property (nonatomic, readonly) MCSOAPNotesProvider                        *provider;

- (id) initWithProvider: (MCSOAPNotesProvider *) provider;

@end

@protocol MCSOAPNotesListViewControllerDelegate <NSObject>

@optional

- (void) soapNotesListViewControllerDidFinish: (MCSOAPNotesListViewController *) controller;

@end

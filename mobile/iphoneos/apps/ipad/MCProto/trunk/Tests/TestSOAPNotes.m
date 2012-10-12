//
//  TestSOAPNotes.m
//  MedPad
//
//  Created by J. G. Pusey on 4/1/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "DataManager.h"
#import "MCSOAPNotesListViewController.h"
#import "TestSOAPNotes.h"

@implementation TestSOAPNotes

- (void) listSOAPNotes
{
    MCPatientID                   *testPatient = [MCPatientID patientIDWithFirstName: @"Jane"
                                                                            lastName: @"Hernandez"
                                                                              gender: @"Female"
                                                                         dateOfBirth: @"Unknown DOB"];
    MCSOAPNotesProvider           *testProvider = [MCSOAPNotesProvider providerWithPatientID: testPatient];

    MCSOAPNotesListViewController *snlvc = [[[MCSOAPNotesListViewController alloc] initWithProvider: testProvider]
                                            autorelease];

    [[DataManager sharedInstance].appDelegate replaceDetailViewController: snlvc
                                              splitViewControllerDelegate: snlvc];
}

#if 0
- (void) soapNotesListViewControllerDidFinish: (MCSOAPNotesListViewController *) controller
{
    [[DataManager sharedInstance].appDelegate restoreDetailViewController];
}
#endif

@end

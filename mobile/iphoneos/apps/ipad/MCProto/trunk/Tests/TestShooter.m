//
//  TestShooter.m
//  MedPad
//
//  Created by bill donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "DataManager.h"
#import "DetailViewController.h"
#import "MCShooterController.h"
#import "TestShooter.h"

@implementation TestShooter

-(void) testShooter
{
    MCShooterController *sc = [[[MCShooterController alloc] initWithShootingController:
                                [DataManager sharedInstance].detailController]
                               autorelease];

    [[DataManager sharedInstance].appDelegate replaceDetailViewController: sc
                                              splitViewControllerDelegate: (id <UISplitViewControllerDelegate>) sc];
}

@end

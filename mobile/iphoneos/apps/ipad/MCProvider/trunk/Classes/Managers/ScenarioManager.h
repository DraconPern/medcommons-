//
//  ScenarioManager.h
//  MCProvider
//
//  Created by Bill Donner on 4/16/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ScenarioManager : NSObject

+ (ScenarioManager *) sharedInstance;

- (NSDictionary *) configureDetailViewFromSceneBlock: (NSUInteger) cb;

- (BOOL) setupScenarioButtons;

@end

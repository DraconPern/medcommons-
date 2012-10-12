    //
//  FavoritesViewController.m
//  MusicStand
//
//  Created by bill donner on 10/16/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "FavoritesViewController.h"
#import "DataManager.h"


@implementation FavoritesViewController

-(id) init
{ 
	self = [super initWithPlist:@"favorites" name:@"GigStand: Favorites" ];
	return self;
}

@end


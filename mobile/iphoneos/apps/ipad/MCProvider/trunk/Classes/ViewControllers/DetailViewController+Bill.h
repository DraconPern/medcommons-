//
//  DetailViewControl+Bill.h
//  MCProvider
//
//  Created by J. G. Pusey on 4/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "DetailViewController.h"

@interface DetailViewController (Bill)

- (void) displayDetailWebView: (NSString *) urlString
              backgroundColor: (UIColor *) bc
                        title: (NSString *) titl;

- (void) displayFullScreenWebView: (NSString *) urlString
                  backgroundColor: (UIColor *) bc
                            title: (NSString *) titl;

@end

//
//  NSError+MCToolbox.h
//  MCToolbox
//
//  Created by J. G. Pusey on 6/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSError (MCToolbox)

+ (id) errorWithDomain: (NSString *) domain
                  code: (NSInteger) code
  localizedDescription: (NSString *) locDesc;

@end

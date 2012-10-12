//
//  ASIHTTPRequest+MCToolbox.h
//  MCToolbox
//
//  Created by J. G. Pusey on 8/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "ASIHTTPRequest.h"  // <ASIHTTPRequest/ASIHTTPRequest.h>

@interface ASIHTTPRequest (MCToolbox)

- (NSDictionary *) responseDictionary;

- (NSString *) responseMIMEType;

@end

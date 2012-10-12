//
//  BreadCrumbs.h
//  MedPad
//
//  Created by bill donner on 4/11/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BreadCrumbs : NSObject {
    NSMutableArray *crumbs;
    NSMutableArray  *inrecoverycrumbs;
    NSString *patientIndex;
}
-(BreadCrumbs *) init;
-(id) pop;
-(void) push:(NSString *)obj;
-(NSString *)popRecoveryCrumb;
@end

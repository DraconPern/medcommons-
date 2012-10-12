//
//  BreadCrumbs.m
//  MedPad
//
//  Created by bill donner on 4/11/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//
#import "BreadCrumbs.h"
#import "DataManager.h"

@implementation BreadCrumbs
-(BreadCrumbs *) init
{
    self = [super init];

    inrecoverycrumbs =[NSMutableArray arrayWithArray:[[NSUserDefaults standardUserDefaults] objectForKey:@"breadcrumbs"] ]; // this drives the restart process
    [[NSUserDefaults standardUserDefaults] setObject:nil forKey:@"breadcrumbs"]; // start clean
    crumbs = [[NSMutableArray alloc] initWithCapacity:10];
    [inrecoverycrumbs retain];
    [crumbs retain];
    patientIndex = @"-1";
    BREADCRUMBS_LOG (@"initially>> crumbs %@ recover %@",crumbs,inrecoverycrumbs);
    return self;
}
- (void) dealloc
{
    //[crumbs release];
    [super dealloc];
}

-(NSString *)popRecoveryCrumb
{
    // this will be called from each view controller to get the name of a controller to run
    if ([inrecoverycrumbs count]<1) return nil;

    // get first controller off the inrecovery crumbs trail
    // rewrite the crumbs trail

    id ob = [inrecoverycrumbs objectAtIndex:0]; //last
    [inrecoverycrumbs removeObjectAtIndex:0];
    BREADCRUMBS_LOG (@"poprecoverycrumb>>> %@ patient %@ crumbs %@ recover %@",[ob description], [[NSUserDefaults standardUserDefaults] valueForKey:@"patientcrumbs"],
                     crumbs,inrecoverycrumbs);
    return [ob description];

}

-(id) pop
{

    id ob = [crumbs objectAtIndex:[crumbs count]-1]; //last
    [crumbs removeLastObject];


    BREADCRUMBS_LOG (@"popped>>> %@ patient %@ crumbs %@ recover %@",[ob description], [[NSUserDefaults standardUserDefaults] valueForKey:@"patientcrumbs"],
                     crumbs,inrecoverycrumbs);

    [[NSUserDefaults standardUserDefaults] setObject:crumbs forKey:@"breadcrumbs"];
    return ob;
}

-(void) push:(NSString *)s
{

    [crumbs addObject:s];

    BREADCRUMBS_LOG (@"pushed>>> %@ patient %@ crumbs %@ recover %@",[s description],[[NSUserDefaults standardUserDefaults] valueForKey:@"patientcrumbs"],crumbs,inrecoverycrumbs);


    [[NSUserDefaults standardUserDefaults] setObject :crumbs forKey:@"breadcrumbs"];
}
@end
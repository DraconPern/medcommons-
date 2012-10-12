//
//  GigStandAppDelegate.h
//  MCProvider
//
//  Created by Bill Donner 
//

#import <UIKit/UIKit.h>

@class   HTTPServer,NSManagedObjectContext,NSManagedObjectModel,NSPersistentStoreCoordinator;

@interface GigStandAppDelegate :  NSObject <UIApplicationDelegate>
{
    
    
    
@private
            UIViewController *vc;
    UINavigationController *nav;
    UIWindow *window;
    NSManagedObjectContext *managedObjectContext_;
    NSManagedObjectModel *managedObjectModel_;
    NSPersistentStoreCoordinator *persistentStoreCoordinator_;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;

@property (nonatomic, retain, readonly) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, retain, readonly) NSManagedObjectModel *managedObjectModel;
@property (nonatomic, retain, readonly) NSPersistentStoreCoordinator *persistentStoreCoordinator;

+ (GigStandAppDelegate *) sharedInstance;
- (NSURL *)applicationDocumentsDirectory;

- (void) dieFromMisconfiguration: (NSString *) msg;
- (void)saveContext:(NSString *)backtrace;
-(void) dump:(NSString *) tag;

- (NSManagedObjectContext *)managedObjectContext:(NSString *)backtrace;

@end

@interface  UIViewController (UIViewControllerCategory)
-(void) setColorForNavBar;
-(void) setColorForOneTuneNavBar;
-(void) popOrNot;
@end

@interface NSString (NSStringCategory)
-(NSComparisonResult) reverseCompare : (NSString *) a;
@end
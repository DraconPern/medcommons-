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

    NSString      *sessionRandomID;
    UIWindow *window;
	HTTPServer *httpServer;
	//IBOutlet UILabel *displayInfo;
	NSString *myLocalIP;
	NSUInteger myLocalPort;

    NSManagedObjectContext *managedObjectContext_;
    NSManagedObjectModel *managedObjectModel_;
    NSPersistentStoreCoordinator *persistentStoreCoordinator_;
}


@property (nonatomic, retain, readonly) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, retain, readonly) NSManagedObjectModel *managedObjectModel;
@property (nonatomic, retain, readonly) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) NSString *myLocalIP;
@property (nonatomic) NSUInteger myLocalPort;

- (void) dieFromMisconfiguration: (NSString *) msg;
+ (GigStandAppDelegate *) sharedInstance;
- (NSURL *)applicationDocumentsDirectory;
- (void)saveContext;

@end

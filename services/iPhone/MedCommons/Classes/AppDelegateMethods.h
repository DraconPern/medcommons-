/*

File: AppDelegateMethods.h
Abstract: An interface that provides some method declarations so other classes
can use methods in AppDelegate.

*/

@class Person, MedCommonsAppDelegate;

@interface MedCommonsAppDelegate (AppDelegateMethods)

- (void)showPersonInfo:(Person *)dictionary;
- (void)addToPersonList:(Person *)newPerson;
- (BOOL)isDataSourceAvailable;

@end

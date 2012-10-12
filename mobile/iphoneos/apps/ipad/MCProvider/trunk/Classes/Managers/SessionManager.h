//
//  SessionManager.h
//  MCProvider
//
//  Created by J. G. Pusey on 7/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

//
// Notifications:
//
extern NSString *const SessionManagerGroupInfoDidChangeNotification;    // OBSOLETE ???
extern NSString *const SessionManagerSessionInfoDidChangeNotification;  // OBSOLETE ???

extern NSString *const SessionManagerDeleteGroupDidFinishNotification;
extern NSString *const SessionManagerDeleteMemberDidFinishNotification;
extern NSString *const SessionManagerDeleteNoteDidFinishNotification;
extern NSString *const SessionManagerDeletePhotoDidFinishNotification;
extern NSString *const SessionManagerDeleteVideoDidFinishNotification;
extern NSString *const SessionManagerGetGroupDidFinishNotification;
extern NSString *const SessionManagerGetMemberDidFinishNotification;
extern NSString *const SessionManagerGetNoteDidFinishNotification;
extern NSString *const SessionManagerGetPhotoDidFinishNotification;
extern NSString *const SessionManagerGetVideoDidFinishNotification;
extern NSString *const SessionManagerInsertGroupDidFinishNotification;
extern NSString *const SessionManagerInsertMemberDidFinishNotification;
extern NSString *const SessionManagerInsertNoteDidFinishNotification;
extern NSString *const SessionManagerInsertPhotoDidFinishNotification;
extern NSString *const SessionManagerInsertVideoDidFinishNotification;
extern NSString *const SessionManagerListGroupsDidFinishNotification;
extern NSString *const SessionManagerListMembersDidFinishNotification;
extern NSString *const SessionManagerListNotesDidFinishNotification;
extern NSString *const SessionManagerListPhotosDidFinishNotification;
extern NSString *const SessionManagerListVideosDidFinishNotification;
extern NSString *const SessionManagerLogInSessionDidFinishNotification;
extern NSString *const SessionManagerLogInSessionDidStartNotification;
extern NSString *const SessionManagerUpdateGroupDidFinishNotification;
extern NSString *const SessionManagerUpdateMemberDidFinishNotification;
extern NSString *const SessionManagerUpdateNoteDidFinishNotification;
extern NSString *const SessionManagerUpdatePhotoDidFinishNotification;
extern NSString *const SessionManagerUpdateVideoDidFinishNotification;

extern NSString *const SessionManagerReplacePortraitDidFinishNotification;

//
// Notification User Info keys:
//
extern NSString *const SessionManagerNotificationErrorKey;

//
// Error domains:
//
extern NSString *const SessionManagerErrorDomain;

//
// Error codes (returned in user info NSError):
//
enum
{
    SessionManagerErrorUnknown         = 1000,
    SessionManagerErrorServerFailure,
    SessionManagerErrorInternalFailure
};

//
// Options:
//
enum
{
    SessionManagerOptionNone        = 0,
    SessionManagerOptionInteractive = 1 << 0
};

@class Group;
@class MCFormDataRequest;
@class Member;
@class Note;
@class Photo;
@class Session;
@class Video;

@interface SessionManager : NSObject
{
@private

    MCFormDataRequest   *groupInfoRequest_;
    NSTimer             *groupInfoTimer_;
    NSUInteger           lastPostRequestSize_;
    NSString            *lastPostResponseLine_;
    NSUInteger           lastPostResponseSize_;
    NSTimeInterval       lastPostResponseTime_;
    MCFormDataRequest   *loginRequest_;
    Session             *loginSession_;
    NSNotificationQueue *notificationQueue_;
    NSUInteger           requestCount_;
    MCFormDataRequest   *sessionInfoRequest_;
    NSTimer             *sessionInfoTimer_;
    NSString            *uniqueDeviceID_;
    //
    // Flags:
    //
    BOOL                 isLoggingIn_;
}

@property (nonatomic, assign, readonly) BOOL        isLoggedIn;
@property (nonatomic, assign, readonly) BOOL        isLoggingIn;
@property (nonatomic, assign, readonly) BOOL        isLoginExpired;
@property (nonatomic, retain, readonly) Session    *loginSession;
@property (nonatomic, assign, readonly) NSUInteger  requestCount;

+ (SessionManager *) sharedInstance;

- (void) checkNetworkStatus;    // make private ???


- (BOOL) replacePortraitURL: (NSString *) url mcid: (NSString *) mcid;

- (BOOL) deleteNote: (Note *) note
            options: (NSUInteger) options;

- (BOOL) deletePhoto: (Photo *) photo
             options: (NSUInteger) options;

- (BOOL) deleteVideo: (Video *) video
             options: (NSUInteger) options;

- (BOOL) insertMember: (Member *) member
            intoGroup: (Group *) group
              options: (NSUInteger) options;

- (BOOL) insertNote: (Note *) note
         intoMember: (Member *) member
            options: (NSUInteger) options;

- (BOOL) insertPhoto: (Photo *) photo
          intoMember: (Member *) member
             options: (NSUInteger) options;

- (BOOL) insertVideo: (Video *) video
          intoMember: (Member *) member
             options: (NSUInteger) options;

- (BOOL) listNotesForMember: (Member *) member
                    options: (NSUInteger) options;

- (BOOL) listPhotosForMember: (Member *) member
                     options: (NSUInteger) options;

- (BOOL) listVideosForMember: (Member *) member
                     options: (NSUInteger) options;

- (BOOL) logInSessionToAppliance: (NSString *) appliance
                          userID: (NSString *) userID
                        password: (NSString *) password
                         options: (NSUInteger) options;

- (BOOL) logOutSessionWithOptions: (NSUInteger) options;

- (void) updateLoginExpiration;

@end

//
//  SessionManager.m
//  MCProvider
//
//  Created by J. G. Pusey on 7/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"           // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "DictionaryAdditions.h"
#import "Group.h"
#import "Member.h"
#import "MemberStore.h"
#import "Note.h"
#import "Photo.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "Video.h"

#pragma mark -
#pragma mark Public Class SessionManager
#pragma mark -

#pragma mark Internal Constants

//
// Web service-related constants:
//
#define COMMON_WEB_SERVICE_PREFIX            @"http://%@/acct/pws/"

#define GROUP_INFO_WEB_SERVICE               COMMON_WEB_SERVICE_PREFIX @"mcgroupinfoservice.php"
#define SESSION_INFO_WEB_SERVICE             COMMON_WEB_SERVICE_PREFIX @"mcpollerservice.php"

#define DELETE_NOTE_WEB_SERVICE              COMMON_WEB_SERVICE_PREFIX @"deleteBlurt.php"
#define INSERT_MEMBER_WEB_SERVICE            @"http://%@/router/NewPatient.action"
#define INSERT_NOTE_WEB_SERVICE              COMMON_WEB_SERVICE_PREFIX @"newBlurt.php"
#define INSERT_PHOTO_WEB_SERVICE             @"http://%@/router/put/%@?auth=%@"
#define LIST_NOTES_WEB_SERVICE               COMMON_WEB_SERVICE_PREFIX @"getBlurts.php"
#define LOG_IN_SESSION_WEB_SERVICE           COMMON_WEB_SERVICE_PREFIX @"mcloginservice.php"

#define REPLACE_PORTRAITURL_WEB_SERVICE		COMMON_WEB_SERVICE_PREFIX @"replacePortraitURL.php"
//
// Generic JSON response keys/values:
//
#define JSON_RESPONSE_STATUS_FAILED          @"failed"
#define JSON_RESPONSE_STATUS_OK              @"ok"

#define JSON_RESPONSE_REQUEST_ID_KEY         @"reqid"
#define JSON_RESPONSE_RESULT_KEY             @"result"
#define JSON_RESPONSE_SERVICE_TIME_KEY       @"servicetime"
#define JSON_RESPONSE_STATUS_KEY             @"status"

//
// Group info-specific response keys:
//
#define GROUP_INFO_RESPONSE_IDENTIFIER_KEY   @"groupidx"

//
// Login-specific request keys:
//
#define LOGIN_REQUEST_APPLIANCE_KEY          @"appliance"
#define LOGIN_REQUEST_PASSWORD_KEY           @"password"
#define LOGIN_REQUEST_USER_ID_KEY            @"email"

//
// Login-specific response keys:
//
#define LOGIN_RESPONSE_APPLIANCE_KEY         @"servername"
#define LOGIN_RESPONSE_ERROR_MESSAGE_KEY     @"message"
#define LOGIN_RESPONSE_RESULT_GROUPS_KEY     @"groups"

//
// Session info-specific response keys:
//
#define SESSION_INFO_RESPONSE_IDENTIFIER_KEY @"accid"

//
// Time intervals and calculations:
//
#define GROUP_INFO_TIME_INTERVAL             90.0f
#define SESSION_INFO_TIME_INTERVAL           180.0f

#define SECONDS_PER_DAY                      (24.0f * 60.0f * 60.0f)

#pragma mark Notifications

NSString *const SessionManagerGroupInfoDidChangeNotification    = @"SessionManagerGroupInfoDidChangeNotification";
NSString *const SessionManagerSessionInfoDidChangeNotification  = @"SessionManagerSessionInfoDidChangeNotification";

NSString *const SessionManagerDeleteGroupDidFinishNotification  = @"SessionManagerDeleteGroupDidFinishNotification";
NSString *const SessionManagerDeleteMemberDidFinishNotification = @"SessionManagerDeleteMemberDidFinishNotification";
NSString *const SessionManagerDeleteNoteDidFinishNotification   = @"SessionManagerDeleteNoteDidFinishNotification";
NSString *const SessionManagerDeletePhotoDidFinishNotification  = @"SessionManagerDeletePhotoDidFinishNotification";
NSString *const SessionManagerDeleteVideoDidFinishNotification  = @"SessionManagerDeleteVideoDidFinishNotification";
NSString *const SessionManagerGetGroupDidFinishNotification     = @"SessionManagerGetGroupDidFinishNotification";
NSString *const SessionManagerGetMemberDidFinishNotification    = @"SessionManagerGetMemberDidFinishNotification";
NSString *const SessionManagerGetNoteDidFinishNotification      = @"SessionManagerGetNoteDidFinishNotification";
NSString *const SessionManagerGetPhotoDidFinishNotification     = @"SessionManagerGetPhotoDidFinishNotification";
NSString *const SessionManagerGetVideoDidFinishNotification     = @"SessionManagerGetVideoDidFinishNotification";
NSString *const SessionManagerInsertGroupDidFinishNotification  = @"SessionManagerInsertGroupDidFinishNotification";
NSString *const SessionManagerInsertMemberDidFinishNotification = @"SessionManagerInsertMemberDidFinishNotification";
NSString *const SessionManagerInsertNoteDidFinishNotification   = @"SessionManagerInsertNoteDidFinishNotification";
NSString *const SessionManagerInsertPhotoDidFinishNotification  = @"SessionManagerInsertPhotoDidFinishNotification";
NSString *const SessionManagerInsertVideoDidFinishNotification  = @"SessionManagerInsertVideoDidFinishNotification";
NSString *const SessionManagerListGroupsDidFinishNotification   = @"SessionManagerListGroupsDidFinishNotification";
NSString *const SessionManagerListMembersDidFinishNotification  = @"SessionManagerListMembersDidFinishNotification";
NSString *const SessionManagerListNotesDidFinishNotification    = @"SessionManagerListNotesDidFinishNotification";
NSString *const SessionManagerListPhotosDidFinishNotification   = @"SessionManagerListPhotosDidFinishNotification";
NSString *const SessionManagerListVideosDidFinishNotification   = @"SessionManagerListVideosDidFinishNotification";
NSString *const SessionManagerLogInSessionDidFinishNotification = @"SessionManagerLogInSessionDidFinishNotification";
NSString *const SessionManagerLogInSessionDidStartNotification  = @"SessionManagerLogInSessionDidStartNotification";
NSString *const SessionManagerUpdateGroupDidFinishNotification  = @"SessionManagerUpdateGroupDidFinishNotification";
NSString *const SessionManagerUpdateMemberDidFinishNotification = @"SessionManagerUpdateMemberDidFinishNotification";
NSString *const SessionManagerUpdateNoteDidFinishNotification   = @"SessionManagerUpdateNoteDidFinishNotification";
NSString *const SessionManagerUpdatePhotoDidFinishNotification  = @"SessionManagerUpdatePhotoDidFinishNotification";
NSString *const SessionManagerUpdateVideoDidFinishNotification  = @"SessionManagerUpdateVideoDidFinishNotification";

NSString *const SessionManagerReplacePortraitDidFinishNotification  = @"SessionManagerReplacePortraitDidFinishNotification";

#pragma mark Notification User Info Keys

NSString *const SessionManagerNotificationErrorKey              = @"SessionManagerNotificationErrorKey";

#pragma mark Error Domains

NSString *const SessionManagerErrorDomain                       = @"SessionManagerErrorDomain";

@interface SessionManager () <MCFormDataRequestDelegate>

@property (nonatomic, retain, readwrite) MCFormDataRequest   *groupInfoRequest;
@property (nonatomic, retain, readwrite) NSTimer             *groupInfoTimer;
@property (nonatomic, assign, readwrite) BOOL                 isLoggingIn;
@property (nonatomic, assign, readwrite) NSUInteger           lastPostRequestSize;
@property (nonatomic, retain, readwrite) NSString            *lastPostResponseLine;
@property (nonatomic, assign, readwrite) NSUInteger           lastPostResponseSize;
@property (nonatomic, assign, readwrite) NSTimeInterval       lastPostResponseTime;
@property (nonatomic, retain, readwrite) MCFormDataRequest   *loginRequest;
@property (nonatomic, retain, readwrite) Session             *loginSession;
@property (nonatomic, assign, readonly)  NSString            *nextRequestID;
@property (nonatomic, retain, readonly)  NSNotificationQueue *notificationQueue;
@property (nonatomic, retain, readwrite) MCFormDataRequest   *sessionInfoRequest;
@property (nonatomic, retain, readwrite) NSTimer             *sessionInfoTimer;
@property (nonatomic, copy,   readonly)  NSString            *uniqueDeviceID;

- (void) deleteNoteRequestFinished: (MCFormDataRequest *) request;

- (void) insertMemberRequestFinished: (MCFormDataRequest *) request;

- (void) insertNoteRequestFinished: (MCFormDataRequest *) request;

- (void) insertPhotoRequestFinished: (MCFormDataRequest *) request;

- (void) insertVideoRequestFinished: (MCFormDataRequest *) request;

- (void) groupInfoRequestFinished: (MCFormDataRequest *) request;

- (void) listNotesRequestFinished: (MCFormDataRequest *) request;

- (void) logInSessionRequestFailed: (MCFormDataRequest *) request;

- (void) logInSessionRequestFinished: (MCFormDataRequest *) request;

- (void) logInSessionRequestStarted: (MCFormDataRequest *) request;

- (void) postGroupInfoDidChangeNotification;

- (void) postLogInSessionDidFinishNotificationWithError: (NSError *) error;

- (void) postLogInSessionDidStartNotification;

- (void) postNotificationOnMainThreadASAP: (NSNotification *) notification;

- (void) postSessionInfoDidChangeNotification;

- (void) sessionInfoRequestFinished: (MCFormDataRequest *) request;

- (void) startGroupInfoTimer;

- (void) startSessionInfoTimer;

- (void) stopGroupInfoTimer;

- (void) stopSessionInfoTimer;

- (void) updateGroupInfo: (NSTimer *) timer;

- (void) updateSessionInfo: (NSTimer *) timer;

- (BOOL) writeGroupInfoToPropertyList: (NSDictionary *) info;

@end

@implementation SessionManager

@synthesize groupInfoRequest     = groupInfoRequest_;
@synthesize groupInfoTimer       = groupInfoTimer_;
@dynamic    isLoggedIn;
@synthesize isLoggingIn          = isLoggingIn_;
@dynamic    isLoginExpired;
@synthesize lastPostRequestSize  = lastPostRequestSize_;
@synthesize lastPostResponseLine = lastPostResponseLine_;
@synthesize lastPostResponseSize = lastPostResponseSize_;
@synthesize lastPostResponseTime = lastPostResponseTime_;
@synthesize loginRequest         = loginRequest_;
@synthesize loginSession         = loginSession_;
@dynamic    nextRequestID;
@synthesize notificationQueue    = notificationQueue_;
@synthesize requestCount         = requestCount_;
@synthesize sessionInfoRequest   = sessionInfoRequest_;
@synthesize sessionInfoTimer     = sessionInfoTimer_;
@synthesize uniqueDeviceID       = uniqueDeviceID_;

#pragma mark Public Class Methods

+ (SessionManager *) sharedInstance
{
    static SessionManager *SharedInstance;

    if (!SharedInstance)
        SharedInstance = [[SessionManager alloc] init];

    return SharedInstance;
}

#pragma mark Public Instance Methods

- (void) checkNetworkStatus
{
    AppDelegate *appDel = self.appDelegate;

    NSLog (@">>> Host reachability flags:        %@, status: %@",
           appDel.hostReachability.formattedFlags,
           appDel.hostReachability.formattedStatus);

    NSLog (@">>> Internet reachability flags:    %@, status: %@",
           appDel.internetReachability.formattedFlags,
           appDel.internetReachability.formattedStatus);

    NSLog (@">>> Local Wi-Fi reachability flags: %@, status: %@",
           appDel.localWiFiReachability.formattedFlags,
           appDel.localWiFiReachability.formattedStatus);

    if ((appDel.hostReachability.status == MCNetworkStatusNotReachable) &&
        (appDel.internetReachability.status == MCNetworkStatusNotReachable) &&
        (appDel.localWiFiReachability.status == MCNetworkStatusNotReachable))
    {
        NSLog (@">>> Network is NOT reachable");

        UIAlertView *av = [[[UIAlertView alloc] initWithTitle: @"Cannot Connect to Network"
                                                      message: @"You must connect to a Wi-Fi network to use this application."
                                                     delegate: nil
                                            cancelButtonTitle: @"OK"
                                            otherButtonTitles: nil]
                           autorelease];

        [av show];
    }
}
- (void) replacePortraitURLRequestFinished: (MCFormDataRequest *) request
{
    [self requestFinished: request];
}
- (BOOL) replacePortraitURL: (NSString *) url mcid: (NSString *) mcid;
{
    Session *session = self.loginSession;
 //   Member  *member = session.memberInFocus;
	
    if (!session)   // not logged in ...
        return NO;
	
  //  NSString          *requestID = self.nextRequestID;
    MCFormDataRequest *request = [MCFormDataRequest requestWithURL: [NSURL URLWithFormat:
                                                                     REPLACE_PORTRAITURL_WEB_SERVICE,
                                                                     session.appliance]];
	
    request.delegate = self;
    request.didFinishSelector = @selector (replacePortraitURLRequestFinished:);
    request.userInfo = [NSDictionary dictionaryWithObjectsAndKeys:
                        mcid, @"mcid",
                        url,   @"url",
                        nil];
	
    [request addPostValue: session.authToken
                   forKey: @"auth"];
	
    [request addPostValue: mcid
                   forKey: @"mcid"];
	
    [request addPostValue: url
                   forKey: @"url"];
	
    [request addPostValue: self.uniqueDeviceID
                   forKey: @"uid"];
	
    [request startAsynchronous];
	
    return YES;
}
- (BOOL) deleteNote: (Note *) note
            options: (NSUInteger) options
{
    Session *session = self.loginSession;
    Member  *member = session.memberInFocus;

    if (!session)   // not logged in ...
        return NO;

    NSString          *requestID = self.nextRequestID;
    MCFormDataRequest *request = [MCFormDataRequest requestWithURL: [NSURL URLWithFormat:
                                                                     DELETE_NOTE_WEB_SERVICE,
                                                                     session.appliance]];

    request.delegate = self;
    request.didFinishSelector = @selector (deleteNoteRequestFinished:);
    request.userInfo = [NSDictionary dictionaryWithObjectsAndKeys:
                        member, @"member",
                        note,   @"note",
                        nil];

    [request addPostValue: session.authToken
                   forKey: @"auth"];

    [request addPostValue: note.identifier
                   forKey: @"noteid"];

    [request addPostValue: requestID
                   forKey: @"reqid"];

    [request addPostValue: self.uniqueDeviceID
                   forKey: @"uid"];

    [request startAsynchronous];

    return YES;
}

- (BOOL) deletePhoto: (Photo *) photo
             options: (NSUInteger) options
{
    return NO;
}

- (BOOL) deleteVideo: (Video *) video
             options: (NSUInteger) options
{
    return NO;
}

- (BOOL) insertMember: (Member *) member
            intoGroup: (Group *) group
              options: (NSUInteger) options
{
    Session *session = self.loginSession;

    if (!session)   // not logged in ...
        return NO;

    MCFormDataRequest *request = [MCFormDataRequest requestWithURL: [NSURL URLWithFormat:
                                                                     INSERT_MEMBER_WEB_SERVICE,
                                                                     session.appliance]];

    request.delegate = self;
    request.didFinishSelector = @selector (insertMemberRequestFinished:);

    [request addPostValue: session.authToken
                   forKey: @"auth"];

    [request addPostValue: member.familyName
                   forKey: @"familyName"];

    [request addPostValue: member.givenName
                   forKey: @"givenName"];

    [request addPostValue: group.identifier
                   forKey: @"sponsorAccountId"];

    [request addPostValue: self.uniqueDeviceID
                   forKey: @"udid"];

    [request startAsynchronous];

    return YES;
}

- (BOOL) insertNote: (Note *) note
         intoMember: (Member *) member
            options: (NSUInteger) options
{
    Session *session = self.loginSession;

    if (!session)   // not logged in ...
        return NO;

    NSString          *requestID = self.nextRequestID;
    MCFormDataRequest *request = [MCFormDataRequest requestWithURL: [NSURL URLWithFormat:
                                                                     INSERT_NOTE_WEB_SERVICE,
                                                                     session.appliance]];

    request.delegate = self;
    request.didFinishSelector = @selector (insertNoteRequestFinished:);
    request.userInfo = [NSDictionary dictionaryWithObjectsAndKeys:
                        member, @"member",
                        note,   @"note",
                        nil];

    [request addPostValue: session.authToken
                   forKey: @"auth"];

    [request addPostValue: member.identifier
                   forKey: @"pid"];

    [request addPostValue: requestID
                   forKey: @"reqid"];

    [request addPostValue: self.uniqueDeviceID
                   forKey: @"uid"];

    NSDictionary *plist = [note propertyList];

    for (id key in plist)
        [request addPostValue: [plist objectForKey: key]
                       forKey: [key description]];

    [request startAsynchronous];

    return YES;
}

- (BOOL) insertPhoto: (Photo *) photo
          intoMember: (Member *) member
             options: (NSUInteger) options
{
    Session *session = self.loginSession;

    if (!session)   // not logged in ...
        return NO;

    MCFormDataRequest *request = [MCFormDataRequest requestWithURL: [NSURL URLWithFormat:
                                                                     INSERT_PHOTO_WEB_SERVICE,
                                                                     session.appliance,
                                                                     member.identifier,
                                                                     session.authToken]];

    request.delegate = self;
    request.didFinishSelector = @selector (insertPhotoRequestFinished:);
    request.userInfo = [NSDictionary dictionaryWithObjectsAndKeys:
                        member, @"member",
                        photo,  @"photo",
                        nil];

    [request addPostValue: @"iPhone-date"
                   forKey: [NSDate date]];

    [request addPostValue: @"iPhone-id"
                   forKey: [[UIDevice currentDevice] uniqueIdentifier]];

    [request addPostValue: @"photo_type"
                   forKey: @"patient_photo"];

    for (id key in photo.attributes)
        [request addPostValue: [photo.attributes objectForKey: key]
                       forKey: [key description]];

    [request addFile: photo.path
        withFileName: photo.path
      andContentType: @"image/png"
              forKey: @"imageFile"];

    [request startAsynchronous];

    return YES;
}

- (BOOL) insertVideo: (Video *) video
          intoMember: (Member *) member
             options: (NSUInteger) options
{
    return NO;
}

- (BOOL) isLoggedIn
{
    return (self.loginSession != nil);
}

- (BOOL) isLoginExpired
{
    if (self.isLoggedIn || self.isLoggingIn)    // may change later ...
        return NO;

    SettingsManager *settings = self.appDelegate.settingsManager;

    //
    // Do we have "remembered" login credentials?
    //
    if (settings.userID && settings.password && settings.rememberLogin)
    {
        NSDate *expDate = [NSDate dateWithTimeIntervalSinceReferenceDate:
                           settings.loginExpiration];

        //
        // Have those login credentials expired:
        //
        if ([expDate compare: [NSDate date]] == NSOrderedDescending)
        {
            NSLog (@"Login credentials will expire %@", expDate);

            return NO;
        }
        else
            NSLog (@"Login credentials have expired");
    }
    else
        NSLog (@"No login credentials");

    return YES;
}

- (BOOL) listNotesForMember: (Member *) member
                    options: (NSUInteger) options
{
    Session *session = self.loginSession;

    if (!session)   // not logged in ...
        return NO;

    NSString *requestID = self.nextRequestID;

    MCFormDataRequest *request = [MCFormDataRequest requestWithURL: [NSURL URLWithFormat:
                                                                     LIST_NOTES_WEB_SERVICE,
                                                                     session.appliance]];

    request.delegate = self;
    request.didFinishSelector = @selector (listNotesRequestFinished:);
    request.userInfo = [NSDictionary dictionaryWithObject: member
                                                   forKey: @"member"];

    [request addPostValue: session.authToken
                   forKey: @"auth"];

    [request addPostValue: member.identifier
                   forKey: @"pid"];

    [request addPostValue: requestID
                   forKey: @"reqid"];

    [request addPostValue: self.uniqueDeviceID
                   forKey: @"uid"];

    [request startAsynchronous];

    return YES;
}

- (BOOL) listPhotosForMember: (Member *) member
                     options: (NSUInteger) options
{
    return NO;
}

- (BOOL) listVideosForMember: (Member *) member
                     options: (NSUInteger) options
{
    return NO;
}

- (BOOL) logInSessionToAppliance: (NSString *) appliance
                          userID: (NSString *) userID
                        password: (NSString *) password
                         options: (NSUInteger) options
{
    if (self.isLoggedIn || self.isLoggingIn)
        return NO;

    self.loginRequest = [MCFormDataRequest requestWithURL: [NSURL URLWithFormat:
                                                            LOG_IN_SESSION_WEB_SERVICE,
                                                            appliance]];

    self.loginRequest.delegate = self;
    self.loginRequest.didFailSelector = @selector (logInSessionRequestFailed:);
    self.loginRequest.didFinishSelector = @selector (logInSessionRequestFinished:);
    self.loginRequest.didStartSelector = @selector (logInSessionRequestStarted:);

    NSNumber *interactive = [NSNumber numberWithBool:
                             ((options & SessionManagerOptionInteractive) ==
                              SessionManagerOptionInteractive)];

    self.loginRequest.userInfo = [NSDictionary dictionaryWithObjectsAndKeys:
                                  interactive, @"interactive",
                                  password,    @"password",
                                  nil];

    [self.loginRequest addPostValue: userID
                             forKey: @"email"];

    [self.loginRequest addPostValue: password
                             forKey: @"password"];

    [self.loginRequest addPostValue: self.uniqueDeviceID
                             forKey: @"uid"];

    self.isLoggingIn = YES;
    self.loginSession = nil;

    [self.loginRequest startAsynchronous];

    return YES;
}

- (BOOL) logOutSessionWithOptions: (NSUInteger) options
{
    [self stopSessionInfoTimer];    // ???
    [self stopGroupInfoTimer];      // ???

    self.loginSession = nil;

    SettingsManager *settings = self.appDelegate.settingsManager;

    settings.loginExpiration = 0.0f;
    settings.password = nil;
    settings.userID = nil;

    [settings synchronizeReadWriteSettings];

    return YES;
}

- (void) updateLoginExpiration
{
    SettingsManager *settings = self.appDelegate.settingsManager;

    //
    // If remembering login credentials and already logged in:
    //
    if (settings.rememberLogin && self.isLoggedIn)
    {
        //
        // If (and only if) login expiration time interval is currently zero,
        // set it to 2 weeks (14 days) from now:
        //
        if (settings.loginExpiration == 0.0f)
        {
            NSDate *expDate = [NSDate dateWithTimeIntervalSinceNow:
                               14.0f * SECONDS_PER_DAY];

            settings.loginExpiration = [expDate timeIntervalSinceReferenceDate];
        }

        //
        // Pull login expiration time interval fresh:
        //
        NSLog (@"Login credentials will expire %@",
               [NSDate dateWithTimeIntervalSinceReferenceDate:
                settings.loginExpiration]);
    }
}

#pragma mark Private Instance Methods

- (void) deleteNoteRequestFinished: (MCFormDataRequest *) request
{
    [self requestFinished: request];

    Note *note = [request.userInfo objectForKey: @"note"];

    if (!note)      // no note object associated with request ...
        return;

    Member *member = [request.userInfo objectForKey: @"member"];

    if (!member)    // no member object associated with request ...
        return;

    Session *session = self.loginSession;

    if (!session)   // no longer logged in ...
        return;

    if (member != session.memberInFocus)    // no longer member in focus ...
        return;

    NSDictionary *response = [request responseDictionary];
    NSString     *status = [response stringForKey: JSON_RESPONSE_STATUS_KEY];

    if (![JSON_RESPONSE_STATUS_OK isEqualToString: status])
        return;

    NSNotification *ntf = [NSNotification notificationWithName: SessionManagerDeleteNoteDidFinishNotification
                                                        object: [request.userInfo objectForKey: @"note"]
                                                      userInfo: nil];

    [self postNotificationOnMainThreadASAP: ntf];
}

- (void) groupInfoRequestFinished: (MCFormDataRequest *) request
{
    [self requestFinished: request];

    if (request != self.groupInfoRequest)           // be absolutely sure
        return;

    Session *session = self.loginSession;

    if (!session)   // no longer logged in ...
        return;

    NSDictionary *response = [request responseDictionary];
    NSString     *status = [response stringForKey: JSON_RESPONSE_STATUS_KEY];

    if (![JSON_RESPONSE_STATUS_OK isEqualToString: status])
        return;

    NSDictionary *result = [response dictionaryForKey: JSON_RESPONSE_RESULT_KEY];
    Group        *group = session.groupInFocus;

    if (![group.identifier isEqualToString: [result stringForKey: GROUP_INFO_RESPONSE_IDENTIFIER_KEY]])
        return;

    [group updateInfo: result];

    [self writeGroupInfoToPropertyList: result];

    [self postGroupInfoDidChangeNotification];
}

- (void) insertMemberRequestFinished: (MCFormDataRequest *) request
{
    [self requestFinished: request];
}

- (void) insertNoteRequestFinished: (MCFormDataRequest *) request
{
    [self requestFinished: request];

    Note *note = [request.userInfo objectForKey: @"note"];

    if (!note)      // no note object associated with request ...
        return;

    Member *member = [request.userInfo objectForKey: @"member"];

    if (!member)    // no member object associated with request ...
        return;

    Session *session = self.loginSession;

    if (!session)   // no longer logged in ...
        return;

    if (member != session.memberInFocus)    // no longer member in focus ...
        return;

    NSDictionary *response = [request responseDictionary];
    NSString     *status = [response stringForKey: JSON_RESPONSE_STATUS_KEY];

    if (![JSON_RESPONSE_STATUS_OK isEqualToString: status])
        return;

    MemberStore *mstore = member.store;
    NSUInteger   idx = 0;

    while (idx < mstore.numberOfNotes)
    {
        if ([mstore noteAtIndex: idx] == note)
        {
            [mstore beginUpdates];
            [mstore removeNoteAtIndex: idx];
            [mstore endUpdates];

            break;
        }
    }

    NSNotification *ntf = [NSNotification notificationWithName: SessionManagerInsertNoteDidFinishNotification
                                                        object: note
                                                      userInfo: nil];

    [self postNotificationOnMainThreadASAP: ntf];
}

- (void) insertPhotoRequestFinished: (MCFormDataRequest *) request
{
    [self requestFinished: request];

    Photo *photo = [request.userInfo objectForKey: @"photo"];

    if (!photo)     // no photo object associated with request ...
        return;

    Member *member = [request.userInfo objectForKey: @"member"];

    if (!member)    // no member object associated with request ...
        return;

    Session *session = self.loginSession;

    if (!session)   // no longer logged in ...
        return;

    if (member != session.memberInFocus)    // no longer member in focus ...
        return;

    NSDictionary *response = [request responseDictionary];
    NSString     *status = [response stringForKey: JSON_RESPONSE_STATUS_KEY];

    if (![JSON_RESPONSE_STATUS_OK isEqualToString: status])
        return;

    MemberStore *mstore = member.store;

    if (mstore.hasSubjectPhoto && (mstore.subjectPhoto == photo))
    {
        [mstore beginUpdates];
        [mstore removeSubjectPhoto];
        [mstore endUpdates];
    }
    else
    {
        NSUInteger idx = 0;

        while (idx < mstore.numberOfPartPhotos)
        {
            if ([mstore partPhotoAtIndex: idx] == photo)
            {
                [mstore beginUpdates];
                [mstore removePartPhotoAtIndex: idx];
                [mstore endUpdates];

                break;
            }
        }
    }

    NSNotification *ntf = [NSNotification notificationWithName: SessionManagerInsertPhotoDidFinishNotification
                                                        object: photo
                                                      userInfo: nil];

    [self postNotificationOnMainThreadASAP: ntf];
}

- (void) insertVideoRequestFinished: (MCFormDataRequest *)request
{
    [self requestFinished: request];

    Video *video = [request.userInfo objectForKey: @"video"];

    if (!video)     // no video object associated with request ...
        return;

    Member *member = [request.userInfo objectForKey: @"member"];

    if (!member)    // no member object associated with request ...
        return;

    Session *session = self.loginSession;

    if (!session)   // no longer logged in ...
        return;

    if (member != session.memberInFocus)    // no longer member in focus ...
        return;

    NSDictionary *response = [request responseDictionary];
    NSString     *status = [response stringForKey: JSON_RESPONSE_STATUS_KEY];

    if (![JSON_RESPONSE_STATUS_OK isEqualToString: status])
        return;

    MemberStore *mstore = member.store;
    NSUInteger   idx = 0;

    while (idx < mstore.numberOfVideos)
    {
        if ([mstore videoAtIndex: idx] == video)
        {
            [mstore beginUpdates];
            [mstore removeVideoAtIndex: idx];
            [mstore endUpdates];

            break;
        }
    }

    NSNotification *ntf = [NSNotification notificationWithName: SessionManagerInsertVideoDidFinishNotification
                                                        object: video
                                                      userInfo: nil];

    [self postNotificationOnMainThreadASAP: ntf];
}

- (void) listNotesRequestFinished: (MCFormDataRequest *) request
{
    [self requestFinished: request];

    Member *member = [request.userInfo objectForKey: @"member"];

    if (!member)    // no member object associated with request ...
        return;

    Session *session = self.loginSession;

    if (!session)   // no longer logged in ...
        return;

    if (member != session.memberInFocus)    // no longer member in focus ...
        return;

    NSDictionary *response = [request responseDictionary];
    NSString     *status = [response stringForKey: JSON_RESPONSE_STATUS_KEY];

    if (![JSON_RESPONSE_STATUS_OK isEqualToString: status])
        return;

    [member updateNoteInfo: response];

    NSNotification *ntf = [NSNotification notificationWithName: SessionManagerListNotesDidFinishNotification
                                                        object: member
                                                      userInfo: nil];

    [self postNotificationOnMainThreadASAP: ntf];
}

- (void) logInSessionRequestFailed: (MCFormDataRequest *) request
{
    //NSLog (@"*** SessionManager.logInSessionRequestFailed: %@", request);

    [self requestFailed: request];

    if (request != self.loginRequest)           // be absolutely sure
        return;

    self.isLoggingIn = NO;

    SettingsManager *settings = self.appDelegate.settingsManager;

    settings.password = nil;
    settings.userID = nil;

    NSAssert (request.error != nil,
              @"Failure callback called with no error provided!");

    [self postLogInSessionDidFinishNotificationWithError: request.error];

    [settings synchronizeReadWriteSettings];
}

- (void) logInSessionRequestFinished: (MCFormDataRequest *) request
{
    //NSLog (@"*** SessionManager.logInSessionRequestFinished: %@", request);

    [self requestFinished: request];

    if (request != self.loginRequest)           // be absolutely sure
        return;

    NSDictionary *response = [request responseDictionary];
    NSString     *status = [response stringForKey: JSON_RESPONSE_STATUS_KEY];

    if ([JSON_RESPONSE_STATUS_OK isEqualToString: status])
    {
        NSString *password = [request.userInfo stringForKey: @"password"];

        self.loginSession = [Session sessionWithInfo: response
                                            password: password];

        [self.loginSession updateInfo: [response dictionaryForKey: JSON_RESPONSE_RESULT_KEY]];  // for now ...
    }

    self.isLoggingIn = NO;

    SettingsManager *settings = self.appDelegate.settingsManager;

    if (self.loginSession)
    {
        settings.appliance = self.loginSession.appliance;
        settings.password = self.loginSession.password;
        settings.userID = self.loginSession.userID;

        //
        // If remembering login credentials AND login was interactive, set
        // login expiration time interval to 2 weeks (14 days) from now:
        //
        if (settings.rememberLogin &&
            [request.userInfo boolForKey: @"interactive"])
        {
            NSDate *expDate = [NSDate dateWithTimeIntervalSinceNow:
                               14.0f * SECONDS_PER_DAY];

            settings.loginExpiration = [expDate timeIntervalSinceReferenceDate];

            NSLog (@"Login credentials will expire %@", expDate);
        }

        [self postLogInSessionDidFinishNotificationWithError: nil];
    }
    else
    {
        settings.password = nil;
        settings.userID = nil;

        NSString  *locDesc;
        NSInteger  code;

        if ([JSON_RESPONSE_STATUS_OK isEqualToString: status])
        {
            code = SessionManagerErrorInternalFailure;
            locDesc = NSLocalizedString (@"Unable to allocate/initialize session instance", @"");
        }
        else
        {
            code = ([JSON_RESPONSE_STATUS_FAILED isEqualToString: status] ?
                    SessionManagerErrorServerFailure :
                    SessionManagerErrorUnknown);

            locDesc = [response stringForKey: LOGIN_RESPONSE_ERROR_MESSAGE_KEY];

            if (!locDesc)
                locDesc = ((code != SessionManagerErrorServerFailure) ?
                           NSLocalizedString (@"Unknown failure -- no information available", @"") :
                           NSLocalizedString (@"No further information provided by server", @""));
        }

        NSError  *error = [NSError errorWithDomain: SessionManagerErrorDomain
                                              code: code
                              localizedDescription: locDesc];

        [self postLogInSessionDidFinishNotificationWithError: error];
    }

    [settings synchronizeReadWriteSettings];

    if (self.loginSession)
    {
        [self postSessionInfoDidChangeNotification];  // for now ...

        [self startGroupInfoTimer];     // ???
        [self startSessionInfoTimer];   // ???
    }
}

- (void) logInSessionRequestStarted: (MCFormDataRequest *) request
{
    //NSLog (@"*** SessionManager.logInSessionRequestStarted: %@", request);

    [self requestStarted: request];

    if (request != self.loginRequest)           // be absolutely sure
        return;

    [self postLogInSessionDidStartNotification];
}

- (NSString *) nextRequestID
{
    return [NSString stringWithFormat: @"%011lu", ++self->requestCount_];
}

- (void) postGroupInfoDidChangeNotification
{
    NSNotification *ntf = [NSNotification notificationWithName: SessionManagerGroupInfoDidChangeNotification
                                                        object: self
                                                      userInfo: nil];

    [self postNotificationOnMainThreadASAP: ntf];
}

- (void) postLogInSessionDidFinishNotificationWithError: (NSError *) error
{
    NSDictionary *userInfo = nil;

    if (error)
        userInfo = [NSDictionary dictionaryWithObject: error
                                               forKey: SessionManagerNotificationErrorKey];

    NSNotification *ntf = [NSNotification notificationWithName: SessionManagerLogInSessionDidFinishNotification
                                                        object: self.loginSession
                                                      userInfo: userInfo];

    [self postNotificationOnMainThreadASAP: ntf];
}

- (void) postLogInSessionDidStartNotification
{
    NSNotification *ntf = [NSNotification notificationWithName: SessionManagerLogInSessionDidStartNotification
                                                        object: nil
                                                      userInfo: nil];

    [self postNotificationOnMainThreadASAP: ntf];
}

- (void) postNotificationOnMainThreadASAP: (NSNotification *) notification
{
    if ([NSThread isMainThread])
        [self.notificationQueue enqueueNotification: notification
                                       postingStyle: NSPostASAP];
    else
        [self performSelectorOnMainThread: @selector (postNotificationOnMainThreadASAP:)
                               withObject: notification
                            waitUntilDone: NO];
}

- (void) postSessionInfoDidChangeNotification
{
    NSNotification *ntf = [NSNotification notificationWithName: SessionManagerSessionInfoDidChangeNotification
                                                        object: self
                                                      userInfo: nil];

    [self postNotificationOnMainThreadASAP: ntf];
}

- (void) sessionInfoRequestFinished: (MCFormDataRequest *) request
{
    //NSLog (@"*** SessionManager.sessionInfoRequestFinished: %@", request);

    [self requestFinished: request];

    if (request != self.sessionInfoRequest)         // be absolutely sure
        return;

    Session *session = self.loginSession;

    if (!session)   // no longer logged in ...
        return;

    NSDictionary *response = [request responseDictionary];
    NSString     *status = [response stringForKey: JSON_RESPONSE_STATUS_KEY];

    if (![JSON_RESPONSE_STATUS_OK isEqualToString: status])
        return;

    NSDictionary *result = [response dictionaryForKey: JSON_RESPONSE_RESULT_KEY];

    if (![session.identifier isEqualToString: [result stringForKey: SESSION_INFO_RESPONSE_IDENTIFIER_KEY]])
        return;

    [session updateInfo: result];

    [self postSessionInfoDidChangeNotification];
}

- (void) startGroupInfoTimer
{
    //NSLog (@"*** SessionManager.startGroupInfoTimer");

    self.groupInfoTimer = [NSTimer scheduledTimerWithTimeInterval: GROUP_INFO_TIME_INTERVAL
                                                           target: self
                                                         selector: @selector (updateGroupInfo:)
                                                         userInfo: nil
                                                          repeats: YES];

    //
    // Fire off timer immediately first time:
    //
    [self.groupInfoTimer fire];
}

- (void) startSessionInfoTimer
{
    //NSLog (@"*** SessionManager.startSessionInfoTimer");

    self.sessionInfoTimer = [NSTimer scheduledTimerWithTimeInterval: SESSION_INFO_TIME_INTERVAL
                                                             target: self
                                                           selector: @selector (updateSessionInfo:)
                                                           userInfo: nil
                                                            repeats: YES];

    //
    // Fire off timer immediately first time:
    //
    //[self.sessionInfoTimer fire];
}

- (void) stopGroupInfoTimer
{
    //NSLog (@"*** SessionManager.stopGroupInfoTimer");

    [self.groupInfoTimer invalidate];

    self.groupInfoTimer = nil;
}

- (void) stopSessionInfoTimer
{
    //NSLog (@"*** SessionManager.stopSessionInfoTimer");

    [self.sessionInfoTimer invalidate];

    self.sessionInfoTimer = nil;
}

- (void) updateGroupInfo: (NSTimer *) timer
{
    //NSLog (@"*** SessionManager.updateGroupInfo: %@", timer);

    if (timer)  // kluge ...
    {
        if ((timer != self.groupInfoTimer) || ![timer isValid])
            return;
    }

    Session *session = self.loginSession;

    if (!session)   // not logged in ...
        return;

    Group *group = session.groupInFocus;

    if (!group)     // no group in focus ...
        return;

    self.groupInfoRequest = [MCFormDataRequest requestWithURL: [NSURL URLWithFormat:
                                                                GROUP_INFO_WEB_SERVICE,
                                                                session.appliance]];

    self.groupInfoRequest.delegate = self;
    self.groupInfoRequest.didFinishSelector = @selector (groupInfoRequestFinished:);

    [self.groupInfoRequest addPostValue: session.authToken
                                 forKey: @"auth"];

    [self.groupInfoRequest addPostValue: group.identifier
                                 forKey: @"groupid"];

    [self.groupInfoRequest addPostValue: self.uniqueDeviceID
                                 forKey: @"uid"];

    [self.groupInfoRequest startAsynchronous];
}

- (void) updateSessionInfo: (NSTimer *) timer
{
    //NSLog (@"*** SessionManager.updateSessionInfo: %@", timer);

    if ((timer != self.sessionInfoTimer) || ![timer isValid])
        return;

    Session *session = self.loginSession;

    if (!session)   // not logged in ...
        return;

    self.sessionInfoRequest = [MCFormDataRequest requestWithURL: [NSURL URLWithFormat:
                                                                  SESSION_INFO_WEB_SERVICE,
                                                                  session.appliance]];

    self.sessionInfoRequest.delegate = self;
    self.sessionInfoRequest.didFinishSelector = @selector (sessionInfoRequestFinished:);

    [self.sessionInfoRequest addPostValue: session.authToken
                                   forKey: @"auth"];

    [self.sessionInfoRequest addPostValue: self.uniqueDeviceID
                                   forKey: @"uid"];

    [self.sessionInfoRequest startAsynchronous];
}

- (BOOL) writeGroupInfoToPropertyList: (NSDictionary *) info    // move to Group class or ???
{
    //    id      plist = [NSPropertyListSerialization sanitizePropertyList: info];
    //    BOOL    ok = (plist != nil);
    //    NSData *plistXML;
    //
    //    if (ok)
    //    {
    //        NSString *errDesc = nil;
    //
    //        plistXML = [NSPropertyListSerialization dataFromPropertyList: plist
    //                                                              format: NSPropertyListXMLFormat_v1_0
    //                                                    errorDescription: &errDesc];
    //
    //        if (errDesc)
    //        {
    //            NSLog (@"Error serializing property list: %@", errDesc);
    //
    //            [errDesc release];
    //
    //            ok = NO;
    //        }
    //    }
    //
    //    NSString *groupID = [info stringForKey: GROUP_INFO_RESPONSE_IDENTIFIER_KEY];
    //    NSString *plistPath = [DataStore pathForGroupWithIdentifier: groupID];
    //
    //    if (ok)
    //        ok = [plistXML writeToFile: plistPath
    //                        atomically: YES];
    //
    //    if (!ok)
    //        NSLog (@"Error writing serialized property list to %@", plistPath);
    //
    //    return ok;

    return NO;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->groupInfoRequest_ release];
    [self->groupInfoTimer_ release];
    [self->lastPostResponseLine_ release];
    [self->loginRequest_ release];
    [self->loginRequest_ release];
    [self->loginSession_ release];
    [self->notificationQueue_ release];
    [self->sessionInfoRequest_ release];
    [self->sessionInfoTimer_ release];
    [self->uniqueDeviceID_ release];

    [super dealloc];
}

- (id) init
{
    self = [super init];

    if (self)
    {
        self->notificationQueue_ = [[NSNotificationQueue alloc]
                                    initWithNotificationCenter: [NSNotificationCenter defaultCenter]];
        self->uniqueDeviceID_ = [[[UIDevice currentDevice] uniqueIdentifier]
                                 retain];
    }

    return self;
}

#pragma mark Generic MCFormDataRequestDelegate Methods

- (void) requestFailed: (MCFormDataRequest *) request
{
    //NSLog (@"*** SessionManager.requestFailed: %@", request);

    [self.appDelegate didStopNetworkActivity];
}

- (void) requestFinished: (MCFormDataRequest *) request
{
    //NSLog (@"*** SessionManager.requestFinished: %@", request);

    [self.appDelegate didStopNetworkActivity];
}

- (void) requestStarted: (MCFormDataRequest *) request
{
    //NSLog (@"*** SessionManager.requestStarted: %@", request);

    [self.appDelegate didStartNetworkActivity];
}

@end

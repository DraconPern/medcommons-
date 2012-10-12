//
//  MemberStore.h
//  MCProvider
//
//  Created by Bill Donner on 4/5/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Member;
@class Note;
@class Photo;
@class Video;

@interface MemberStore : NSObject
{
@private

    NSFileManager  *fileManager_;
    Member         *member_;
    NSUInteger      nestingLevel_;
    NSMutableArray *notes_;
    NSMutableArray *partPhotos_;
    Photo          *subjectPhoto_;
    NSMutableArray *videos_;
}

@property (nonatomic, assign, readonly)  BOOL        hasSubjectPhoto;
@property (nonatomic, assign, readonly)  Member     *member;            // NOT retained!
@property (nonatomic, assign, readonly)  NSUInteger  numberOfNotes;
@property (nonatomic, assign, readonly)  NSUInteger  numberOfPartPhotos;
@property (nonatomic, assign, readonly)  NSUInteger  numberOfVideos;
@property (nonatomic, retain, readwrite) Photo      *subjectPhoto;

- (void) addNote: (Note *) note;

- (void) addPartPhoto: (Photo *) photo;

- (void) addVideo: (Video *) video;

- (void) beginUpdates;

- (void) endUpdates;

- (void) exchangeNoteAtIndex: (NSUInteger) idx1
             withNoteAtIndex: (NSUInteger) idx2;

- (void) exchangePartPhotoAtIndex: (NSUInteger) idx1
             withPartPhotoAtIndex: (NSUInteger) idx1;

- (void) exchangeSubjectPhotoWithPartPhotoAtIndex: (NSUInteger) idx;

- (void) exchangeVideoAtIndex: (NSUInteger) idx1
             withVideoAtIndex: (NSUInteger) idx2;

- (BOOL) hasNoteAtIndex: (NSUInteger) idx;

- (BOOL) hasPartPhotoAtIndex: (NSUInteger) idx;

- (BOOL) hasSubjectPhoto;

- (BOOL) hasVideoAtIndex: (NSUInteger) idx;

- (id) initWithMember: (Member *) member;

- (NSString *) nextFreePathForMediaDataWithType: (NSString *) mediaType;

- (Note *) noteAtIndex: (NSUInteger) idx;

- (Photo *) partPhotoAtIndex: (NSUInteger) idx;

- (void) removeNoteAtIndex: (NSUInteger) idx;

- (void) removePartPhotoAtIndex: (NSUInteger) idx;

- (void) removeSubjectPhoto;

- (void) removeVideoAtIndex: (NSUInteger) idx;

- (void) replaceNoteAtIndex: (NSUInteger) idx
                   withNote: (Note *) note;

- (void) replacePartPhotoAtIndex: (NSUInteger) idx
                   withPartPhoto: (Photo *) photo;

- (void) replaceVideoAtIndex: (NSUInteger) idx
                   withVideo: (Video *) video;

- (Video *) videoAtIndex: (NSUInteger) idx;

@end

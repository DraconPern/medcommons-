//
//  MemberStore.m
//  MCProvider
//
//  Created by Bill Donner on 4/5/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "DataManager.h"
#import "DataStore.h"
#import "DictionaryAdditions.h"
#import "Member.h"
#import "MemberStore.h"
#import "Note.h"
#import "Photo.h"
#import "Video.h"

#pragma mark -
#pragma mark Public Class MemberStore
#pragma mark -

#pragma mark Internal Constants

#define NOTES_KEY         @"Notes"
#define PART_PHOTOS_KEY   @"PartPhotos"
#define SUBJECT_PHOTO_KEY @"SubjectPhoto"
#define VIDEOS_KEY        @"Videos"

//
// Plist structure:
//
//  <dict>
//      <key>SUBJECT_PHOTO_KEY</key>
//      <dict>
//          <key>MEDIA_PATH_KEY</key>
//          <string>...</string>
//          <key>MEDIA_ATTRS_KEY</key>
//          <dict>...</dict>
//      </dict>
//      <key>PART_PHOTOS_KEY</key>
//      <array>
//          <dict>
//              <key>MEDIA_PATH_KEY</key>
//              <string>...</string>
//              <key>MEDIA_ATTRS_KEY</key>
//              <dict>...</dict>
//          </dict>
//          .
//          .
//          .
//      </array>
//      <key>VIDEOS_KEY</key>
//      <array>
//          <dict>
//              <key>MEDIA_PATH_KEY</key>
//              <string>...</string>
//              <key>MEDIA_ATTRS_KEY</key>
//              <dict>...</dict>
//          </dict>
//          .
//          .
//          .
//      </array>
//      <key>NOTES_KEY</key>
//      <array>
//          <dict>
//              <key>???</key>
//              <string>...</string>
//              <key>???</key>
//              <dict>...</dict>
//          </dict>
//          .
//          .
//          .
//      </array>
//  </dict>
//

#pragma mark Internal Functions

static BOOL isValidMediaPath (NSString *path);

static BOOL isValidMediaPath (NSString *path)
{
    return (path &&
            ([path length] > 0) &&
            [[NSFileManager defaultManager] fileExistsAtPath: path]);
}

@interface MemberStore ()

@property (nonatomic, retain, readonly)  NSFileManager  *fileManager;
@property (nonatomic, retain, readonly)  NSMutableArray *notes;
@property (nonatomic, retain, readonly)  NSMutableArray *partPhotos;
@property (nonatomic, retain, readwrite) Photo          *subjectPhotoInternal;
@property (nonatomic, retain, readonly)  NSMutableArray *videos;

- (BOOL) readFromPropertyList;

- (BOOL) writeToPropertyList;

@end

@implementation MemberStore

@synthesize fileManager          = fileManager_;
@dynamic    hasSubjectPhoto;
@synthesize member               = member_;
@synthesize notes                = notes_;
@dynamic    numberOfNotes;
@dynamic    numberOfPartPhotos;
@dynamic    numberOfVideos;
@synthesize partPhotos           = partPhotos_;
@dynamic    subjectPhoto;
@synthesize subjectPhotoInternal = subjectPhoto_;
@synthesize videos               = videos_;

#pragma mark Public Instance Methods

- (void) addNote: (Note *) note
{
    NSLog (@"Adding new note %d", self.numberOfNotes);

    [self.notes addObject: note];
}

- (void) addPartPhoto: (Photo *) photo
{
    NSLog (@"Adding new part photo %d at path %@",
           self.numberOfPartPhotos,
           photo.path);

    [self.partPhotos addObject: photo];
}

- (void) addVideo: (Video *) video
{
    NSLog (@"Adding new video %d at path %@",
           self.numberOfVideos,
           video.path);

    [self.videos addObject: video];
}

- (void) beginUpdates
{
    self->nestingLevel_++;
}

- (void) endUpdates
{
    if (self->nestingLevel_ > 0)
    {
        if (--self->nestingLevel_ == 0)
            [self writeToPropertyList];
    }
}

- (void) exchangeNoteAtIndex: (NSUInteger) idx1
             withNoteAtIndex: (NSUInteger) idx2
{
    NSLog (@"Exchanging note %d with note %d",
           idx1,
           idx2);

    [self.notes exchangeObjectAtIndex: idx1
                    withObjectAtIndex: idx2];
}

- (void) exchangePartPhotoAtIndex: (NSUInteger) idx1
             withPartPhotoAtIndex: (NSUInteger) idx2
{
    NSLog (@"Exchanging part photo %d at path %@ with part photo %d at path %@",
           idx1,
           [self partPhotoAtIndex: idx1].path,
           idx2,
           [self partPhotoAtIndex: idx2].path);

    [self.partPhotos exchangeObjectAtIndex: idx1
                         withObjectAtIndex: idx2];
}

- (void) exchangeSubjectPhotoWithPartPhotoAtIndex: (NSUInteger) idx
{
    Photo *tmpSubjectPhoto = [self.subjectPhotoInternal retain];
    Photo *tmpPartPhoto = [self partPhotoAtIndex: idx];

    if (tmpSubjectPhoto)
        NSLog (@"Exchanging subject photo at path %@ with part photo %d at path %@",
               tmpSubjectPhoto.path,
               idx,
               tmpPartPhoto.path);
    else
        NSLog (@"Exchanging empty subject photo with part photo %d at path %@",
               idx,
               tmpPartPhoto.path);

    self.subjectPhotoInternal = tmpPartPhoto;

    if (tmpSubjectPhoto)
        [self.partPhotos replaceObjectAtIndex: idx
                                   withObject: tmpSubjectPhoto];
    else
        [self.partPhotos removeObjectAtIndex: idx];

    [tmpSubjectPhoto release];
}

- (void) exchangeVideoAtIndex: (NSUInteger) idx1
             withVideoAtIndex: (NSUInteger) idx2
{
    NSLog (@"Exchanging video %d at path %@ with video %d at path %@",
           idx1,
           [self videoAtIndex: idx1].path,
           idx2,
           [self videoAtIndex: idx2].path);

    [self.videos exchangeObjectAtIndex: idx1
                     withObjectAtIndex: idx2];
}

- (BOOL) hasNoteAtIndex: (NSUInteger) idx
{
    return (idx < self.numberOfNotes);
}

- (BOOL) hasPartPhotoAtIndex: (NSUInteger) idx
{
    return (idx < self.numberOfPartPhotos);
}

- (BOOL) hasSubjectPhoto
{
    return (self.subjectPhotoInternal != nil);
}

- (BOOL) hasVideoAtIndex: (NSUInteger) idx
{
    return (idx < self.numberOfVideos);
}

- (id) initWithMember: (Member *) member
{
    self = [super init];

    if (self)
    {
        self->fileManager_ = [[NSFileManager alloc] init];
        self->member_ = member;                         // do NOT retain!!!
        self->notes_ = [[NSMutableArray alloc] init];
        self->partPhotos_ = [[NSMutableArray alloc] init];
        self->videos_ = [[NSMutableArray alloc] init];

        [self readFromPropertyList];
    }

    return self;
}

- (NSString *) nextFreePathForMediaDataWithType: (NSString *) mediaType
{
    AppDelegate *appDel = self.appDelegate;
    DataManager *dm = appDel.dataManager;
    NSUInteger   idx = ++dm.nextMediaPathIndex;

    return [DataStore pathForMediaDataWithIdentifier: [NSString stringWithFormat:
                                                       @"%@-%d",
                                                       appDel.sessionRandomID,
                                                       idx]
                                           mediaType: mediaType];
}

- (Note *) noteAtIndex: (NSUInteger) idx
{
    return (Note *) [self.notes objectAtIndex: idx];
}

- (NSUInteger) numberOfNotes
{
    return [self.notes count];
}

- (NSUInteger) numberOfPartPhotos
{
    return [self.partPhotos count];
}

- (NSUInteger) numberOfVideos
{
    return [self.videos count];
}

- (Photo *) partPhotoAtIndex: (NSUInteger) idx
{
    return (Photo *) [self.partPhotos objectAtIndex: idx];
}

- (void) removeNoteAtIndex: (NSUInteger) idx
{
    NSLog (@"Removing note %d", idx);

    [self.notes removeObjectAtIndex: idx];
}

- (void) removePartPhotoAtIndex: (NSUInteger) idx
{
    Photo *oldPhoto = [[self partPhotoAtIndex: idx] retain];   // IMPORTANT!!!

    NSLog (@"Removing part photo %d at path %@",
           idx,
           oldPhoto.path);

    [self.partPhotos removeObjectAtIndex: idx];

    //
    // Remove part photo from file system:
    //
    if (oldPhoto)
    {
        NSError *error;

        if (![self.fileManager removeItemAtPath: oldPhoto.path
                                          error: &error])
            NSLog (@"Error removing part photo %d at path %@: %@",
                   idx,
                   oldPhoto.path,
                   error);

        [oldPhoto release];  // IMPORTANT!!!
    }
}

- (void) removeSubjectPhoto
{
    Photo *oldPhoto = [self.subjectPhotoInternal retain];    // IMPORTANT!!!

    NSLog (@"Removing subject photo at path %@",
           oldPhoto.path);

    self.subjectPhotoInternal = nil;

    //
    // Remove subject photo from file system:
    //
    if (oldPhoto)
    {
        NSError *error;

        if (![self.fileManager removeItemAtPath: oldPhoto.path
                                          error: &error])
            NSLog (@"Error removing subject photo at path %@: %@",
                   oldPhoto.path,
                   error);

        [oldPhoto release];  // IMPORTANT!!!
    }
}

- (void) removeVideoAtIndex: (NSUInteger) idx
{
    Video *oldVideo = [[self videoAtIndex: idx] retain];   // IMPORTANT!!!

    NSLog (@"Removing video %d at path %@",
           idx,
           oldVideo.path);

    [self.videos removeObjectAtIndex: idx];

    //
    // Remove video from file system:
    //
    if (oldVideo)
    {
        NSError *error;

        if (![self.fileManager removeItemAtPath: oldVideo.path
                                          error: &error])
            NSLog (@"Error removing video %d at path %@: %@",
                   idx,
                   oldVideo.path,
                   error);

        [oldVideo release];  // IMPORTANT!!!
    }
}

- (void) replaceNoteAtIndex: (NSUInteger) idx
                   withNote: (Note *) note
{
    NSLog (@"Replacing note %d", idx);

    [self.notes replaceObjectAtIndex: idx
                          withObject: note];
}

- (void) replacePartPhotoAtIndex: (NSUInteger) idx
                   withPartPhoto: (Photo *) photo
{
    Photo *oldPhoto = [[self partPhotoAtIndex: idx] retain];    // IMPORTANT!!!

    NSLog (@"Replacing part photo %d at path %@ with photo at path %@",
           idx,
           oldPhoto.path,
           photo.path);

    [self.partPhotos replaceObjectAtIndex: idx
                               withObject: photo];

    //
    // Remove old part photo from file system:
    //
    if (oldPhoto)
    {
        NSError *error;

        if (![self.fileManager removeItemAtPath: oldPhoto.path
                                          error: &error])
            NSLog (@"Error removing part photo %d at path %@: %@",
                   idx,
                   oldPhoto.path,
                   error);

        [oldPhoto release];  // IMPORTANT!!!
    }
}

- (void) replaceVideoAtIndex: (NSUInteger) idx
                   withVideo: (Video *) video
{
    Video *oldVideo = [[self videoAtIndex: idx] retain];    // IMPORTANT!!!

    NSLog (@"Replacing video %d at path %@ with video at path %@",
           idx,
           oldVideo.path,
           video.path);

    [self.videos replaceObjectAtIndex: idx
                           withObject: video];

    //
    // Remove old video from file system:
    //
    if (oldVideo)
    {
        NSError *error;

        if (![self.fileManager removeItemAtPath: oldVideo.path
                                          error: &error])
            NSLog (@"Error removing video %d at path %@: %@",
                   idx, oldVideo.path,
                   error);

        [oldVideo release];  // IMPORTANT!!!
    }
}

- (void) setSubjectPhoto: (Photo *) photo
{
    Photo *oldPhoto = [self.subjectPhotoInternal retain]; // IMPORTANT!!!

    if (oldPhoto)
        NSLog (@"Replacing subject photo at path %@ with photo at path %@",
               oldPhoto.path,
               photo.path);
    else
        NSLog (@"Replacing empty subject photo with photo at path %@",
               photo.path);

    self.subjectPhotoInternal = photo;

    //
    // Remove old subject photo from file system:
    //
    if (oldPhoto)
    {
        NSError *error;

        if (![self.fileManager removeItemAtPath: oldPhoto.path
                                          error: &error])
            NSLog (@"Error removing subject photo at path %@: %@",
                   oldPhoto.path,
                   error);

        [oldPhoto release];  // IMPORTANT!!!
    }
}

- (Photo *) subjectPhoto
{
    return self.subjectPhotoInternal;
}

- (Video *) videoAtIndex: (NSUInteger) idx
{
    return (Video *) [self.videos objectAtIndex: idx];
}

#pragma mark Private Instance Methods

- (BOOL) readFromPropertyList
{
    NSString            *plistPath = [DataStore pathForMemberWithIdentifier: self.member.identifier];
    BOOL                 ok = [self.fileManager fileExistsAtPath: plistPath];
    NSMutableDictionary *plistDict;
    NSData              *plistXML;

    if (ok)
    {
        plistXML = [self.fileManager contentsAtPath: plistPath];

        if (!plistXML)
        {
            NSLog (@"Error reading serialized property list from %@", plistPath);

            ok = NO;
        }
    }

    if (ok)
    {
        NSString *errDesc = nil;

        plistDict = (NSMutableDictionary *) [NSPropertyListSerialization propertyListFromData: plistXML
                                                                             mutabilityOption: NSPropertyListMutableContainers
                                                                                       format: NULL
                                                                             errorDescription: &errDesc];

        if (errDesc)
        {
            NSLog (@"Error deserializing property list: %@", errDesc);

            [errDesc release];

            ok = NO;
        }
    }

    if (ok)
    {
        NSMutableDictionary *tmpPlist;

        tmpPlist = [plistDict mutableDictionaryForKey: SUBJECT_PHOTO_KEY];

        if (tmpPlist)
        {
            Photo *tmpSubjectPhoto = [Photo photoWithPropertyList: tmpPlist];

            if (tmpSubjectPhoto && isValidMediaPath (tmpSubjectPhoto.path))
                self.subjectPhoto = tmpSubjectPhoto;
        }

        for (tmpPlist in [plistDict dictionaryForKey: PART_PHOTOS_KEY])
        {
            Photo *tmpPartPhoto = [Photo photoWithPropertyList: tmpPlist];

            if (tmpPartPhoto && isValidMediaPath (tmpPartPhoto.path))
                [self.partPhotos addObject: tmpPartPhoto];
        }

        for (tmpPlist in [plistDict dictionaryForKey: VIDEOS_KEY])
        {
            Video *tmpVideo = [Video videoWithPropertyList: tmpPlist];

            if (tmpVideo && isValidMediaPath (tmpVideo.path))
                [self.partPhotos addObject: tmpVideo];
        }

        for (tmpPlist in [plistDict dictionaryForKey: NOTES_KEY])
        {
            Note *tmpNote = [Note noteWithPropertyList: tmpPlist];

            if (tmpNote)
                [self.notes addObject: tmpNote];
        }
    }

    return ok;
}

- (BOOL) writeToPropertyList
{
    NSDictionary *spPlist = [self.subjectPhoto propertyList];

    if (!spPlist)
        spPlist = [NSDictionary dictionary];

    NSMutableArray *ppPlists = [NSMutableArray arrayWithCapacity: [self.partPhotos count]];

    for (Photo *partPhoto in self.partPhotos)
    {
        NSDictionary *ppPlist = [partPhoto propertyList];

        if (ppPlist)
            [ppPlists addObject: ppPlist];
    }

    NSMutableArray *vPlists = [NSMutableArray arrayWithCapacity: [self.videos count]];

    for (Video *video in self.videos)
    {
        NSDictionary *vPlist = [video propertyList];

        if (vPlist)
            [vPlists addObject: vPlist];
    }

    NSMutableArray *nPlists = [NSMutableArray arrayWithCapacity: [self.notes count]];

    for (Note *note in self.notes)
    {
        NSDictionary *nPlist = [note propertyList];

        if (nPlist)
            [nPlists addObject: nPlist];
    }

    NSString     *plistPath = [DataStore pathForMemberWithIdentifier: self.member.identifier];
    NSDictionary *plistDict = [NSDictionary dictionaryWithObjectsAndKeys:
                               nPlists,  NOTES_KEY,
                               ppPlists, PART_PHOTOS_KEY,
                               spPlist,  SUBJECT_PHOTO_KEY,
                               vPlists,  VIDEOS_KEY,
                               nil];
    NSData       *plistXML;
    BOOL          ok = YES;

    if (ok)
    {
        NSString *errDesc = nil;

        plistXML = [NSPropertyListSerialization dataFromPropertyList: plistDict
                                                              format: NSPropertyListXMLFormat_v1_0
                                                    errorDescription: &errDesc];

        if (errDesc)
        {
            NSLog (@"Error serializing property list: %@", errDesc);

            [errDesc release];

            ok = NO;
        }
    }

    if (ok)
        ok = [plistXML writeToFile: plistPath
                        atomically: YES];

    if (!ok)
        NSLog (@"Error writing serialized property list to %@", plistPath);

    return ok;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->fileManager_ release];
    [self->notes_ release];
    [self->partPhotos_ release];
    [self->subjectPhoto_ release];
    [self->videos_ release];

    [super dealloc];
}

@end

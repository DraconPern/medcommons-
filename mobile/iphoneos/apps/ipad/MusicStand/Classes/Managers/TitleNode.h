//
//  TitleNode.h
//  MusicStand
//
//  Created by bill donner on 10/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TitleNode :NSObject {
	NSString *title;
	NSMutableDictionary *variants;
	NSInteger lastvariantsegmentindex;  //TODO - must persist this
	
}

@property (nonatomic, copy) NSString *title;
@property (nonatomic, retain) NSMutableDictionary *variants;

@property (nonatomic, assign) NSInteger lastvariantsegmentindex;

-(TitleNode *)  initWithTitle:(NSString *)titl ;

-(BOOL) addSourceDocument:(NSString *)name segment:(NSUInteger)segmentindex resetLast:(BOOL) resetLast;


@end
@interface RefNode :NSObject
{
	NSString *title;
	NSString *archive;
}

@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *archive;

-(RefNode *)  initWithTitle:(NSString *)titl andWithArchive:(NSString *) arch;
+(RefNode *) inArray:(NSMutableArray *)array findTitle:(NSString *)titl;


@end

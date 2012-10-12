#import "HTTPResponse.h"


@implementation HTTPFileResponse

- (id)initWithFilePath:(NSString *)filePathParam
{
	self = [super init];
	if (self)
	{
		filePath = [filePathParam copy];
		fileHandle = [[NSFileHandle fileHandleForReadingAtPath:filePath] retain];
		NSLog (@"========= HTTPFileResponse copied from %@\n\n\n",filePathParam);
	}
	return self;
}

- (void)dealloc
{
	[filePath release];
	[fileHandle closeFile];
	[fileHandle release];
	[super dealloc];
}

- (UInt64)contentLength
{
	NSError *error;
	
	NSDictionary *fileAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:&error];
	
	NSNumber *fileSize = [fileAttributes objectForKey:NSFileSize];
	
	UInt64 d = (UInt64)[fileSize unsignedLongLongValue];
	
	//NSLog (@"HTTPFileResponse: contentLength is %D:",d);
	
	return d;
}

- (UInt64)offset
{
	UInt64 d =  (UInt64)[fileHandle offsetInFile];
	
	//NSLog (@"HTTPFileResponse: offset is %D:",d);
	return d;
}

- (void)setOffset:(UInt64)offset
{
	[fileHandle seekToFileOffset:offset];
	
	//NSLog (@"HTTPFileResponse: setOffset is %D:",offset);
}

- (NSData *)readDataOfLength:(unsigned int)length
{
	NSData *d =  [fileHandle readDataOfLength:length];
	
	
	//NSLog (@"HTTPFileResponse: readDataOfLength is %D:%D",length,[d length]);
	return d;
	
}

- (NSString *)filePath
{
	return filePath;
}

@end

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark -
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@implementation HTTPDataResponse

- (id)initWithData:(NSData *)dataParam
{
	self = [super init];
	if(self)
	{
		offset = 0;
		data = [dataParam retain]; 
		NSLog (@"========= HTTPDataResponse generated %D bytes\n\n\n",[data length]);
	}
	return self;
}

- (void)dealloc
{
	[data release];
	[super dealloc];
}

- (UInt64)contentLength
{
		   
	UInt64 d =  (UInt64)[data length];
	
	NSLog (@"HTTPDataResponse: contentLength is %D:",d);
	return d;
	
}

- (UInt64)offset
{
	
	//NSLog (@"HTTPDataResponse: offset is %D:",offset);
	return offset;
}

- (void)setOffset:(UInt64)offsetParam
{
	offset = offsetParam;
	
	//NSLog (@"HTTPDataResponse: setOffset is %D:",offsetParam);
}

- (NSData *)readDataOfLength:(unsigned int)lengthParameter
{
	UInt64 remaining = [data length] - offset;
	UInt64 length = lengthParameter < remaining ? lengthParameter : remaining;
	
	void *bytes = (void *)([data bytes] + offset);
	offset += length;	
	
	NSData *d =   [NSData dataWithBytesNoCopy:bytes length:(NSUInteger) length freeWhenDone:NO];
	
	//NSLog (@"HTTPDataResponse: readDataOfLength is %D:%D",lengthParameter,[d length]);
	return d;
}

@end

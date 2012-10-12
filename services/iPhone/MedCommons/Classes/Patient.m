

#import "mcUrlConnection.h"
#import "Patient.h"



@implementation Patient
@synthesize magnitude = _magnitude;
@synthesize title = _title;
@synthesize location = _location;
- (NSString *)description
{
    // Override of -[NSObject description] to print a meaningful representation of self.
    return [NSString stringWithFormat:@"%@ %@", self.firstname, self.lastname];
}


- (void)setTitle:(NSString *)newTitle
{
    [newTitle retain];
    [_title release];
    _title = newTitle;
    
    // The location and magnitude of the Patient are parsed from the title.

}

- (NSString *)locationAndMagnitude:(NSString **)outMagnitude inString:(NSString *)wholeTitle
{
	// <title>M 3.6, Virgin Islands region<title/>,
	// Pull out the magnitude and the title using a scanner.
			
	NSScanner *scanner = [NSScanner scannerWithString:wholeTitle];
	static NSString *magnitudeSeparator = @", ";
	NSString *magnitude = nil;
	[scanner scanUpToString:@" " intoString:nil]; // Scan past the "M " before the number.
    // Scan from the space up to the comma separator, which gives us the magnitude.
	BOOL foundSpace = [scanner scanUpToString:magnitudeSeparator intoString:&magnitude];
	if (foundSpace && magnitude && outMagnitude) {
		// If we found the pattern, set the outMagnitude argument to this method to the value we found.
		*outMagnitude = magnitude;
	}
	
	NSString *title = nil;
    // Scan from after the locaion of the separator up to the end of the string.
    // That gives us the location of the Patient.
	[scanner setScanLocation:[scanner scanLocation] + [magnitudeSeparator length]];
	BOOL foundTitle = [scanner scanUpToString:@"" intoString:&title];
	if (foundTitle && title) {
		// Virgin Islands region
		return [title capitalizedString];
	}
	
    // Failed to find the location and magnitude of the Patient.
	return nil;
}

- (void)getMagnitudeAndLocationFromTitle:(NSString *)wholeTitle
{
    NSString *magnitude = nil;
    NSString *location = [self locationAndMagnitude:&magnitude inString:wholeTitle];
    
    self.magnitude = magnitude;
    self.location = location;
}

@end

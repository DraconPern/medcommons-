

#import <Foundation/Foundation.h>
#import "mcUrlConnection.h"
#import "Person.h"

@interface Patient : Person {

@private    
    NSString *_magnitude;       // Derived from the title property.
    NSString *_location;        // Derived from the title property.
    NSString *_title;           // Holds location and magnitude.

}

@property (nonatomic, retain) NSString *magnitude;
@property (nonatomic, retain) NSString *location;
@property (nonatomic, retain) NSString *title;

@end


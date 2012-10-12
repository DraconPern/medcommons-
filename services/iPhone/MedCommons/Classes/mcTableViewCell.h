/*

File: TableViewCell.h
Abstract: Custom table cell.

*/

#import <UIKit/UIKit.h>
#import "Person.h"


@interface TableViewCell : UITableViewCell {

@private	
	Person *_person;
    UILabel *_locationLabel;
    UILabel *_dateLabel;
    UILabel *_magnitudeLabel;
    UIImageView *_personSeverityImageView;
    UILabel *_firstnameLabel;
    UILabel *_lastnameLabel;
    UILabel *_mcidLabel;
	UIImageView *_photoUrlImageView;
}

@property (nonatomic, retain) Person  *person;
@property (nonatomic, retain) UILabel *locationLabel;
@property (nonatomic, retain) UILabel *dateLabel;
@property (nonatomic, retain) UILabel *magnitudeLabel;
@property (nonatomic, retain) UIImageView *personSeverityImageView;
@property (nonatomic, retain) UILabel *firstnameLabel;
@property (nonatomic, retain) UILabel *lastnameLabel;
@property (nonatomic, retain) UILabel *mcidLabel;
@property (nonatomic, retain) UIImageView *photoUrlImageView;


@end
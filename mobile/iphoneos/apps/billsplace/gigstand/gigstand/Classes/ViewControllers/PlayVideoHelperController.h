//
//  PlayVideoHelperController.h
//  gigstand
//
//  Created by bill donner on 4/27/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
    
    @interface PlayVideoHelperController : UIViewController<UITableViewDataSource, UITableViewDelegate> {
        
        NSMutableArray *listNames;
        
        NSMutableArray *listURLs;
 
        UITableView *tableView;
        
        
    }
    
    -(id) init;

@end

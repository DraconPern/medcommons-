//
//  SetListChooserControl.m
//  GigStand
//
//  Created by bill donner on 1/31/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "SetListChooserControl.h"
#import "DataManager.h"
#import "SetListsManager.h"



@implementation SetListChooserControl



-(void) show;
{
    [self->mysheet showInView:self->viewController.view];
}

-(void) dealloc {
    
    
   // NSLog (@"SetListChooserControl dealloc");
	[self->mysheet dismissWithClickedButtonIndex:-1 animated:NO];
    [self->mysheet release];
    [self->tune release];
    [self->names release];
	[super dealloc];
}

- (id) initWithTune:(NSString *)tunex
          andAction: (SEL) action andController: (UIViewController *) controller;
{
    self = [super init]; // plain nsobject
    if (self)
    {
        
        self->names = [[DataManager list:[SetListsManager makeSetlistsScanNoRecents]  // recents need to go first so the "choices" for set list control are linear
                           bringToTop:[NSArray arrayWithObjects:@"favorites",nil]] retain];
        
        

      
        NSString *titl = [NSString stringWithFormat:@"add tune %@ to setlist",tunex,nil];
        
        NSString *cancel = (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad)?@"cancel":nil;
        
        self->mysheet = [[UIActionSheet alloc] initWithTitle: titl 
                                                    delegate:self
                                           cancelButtonTitle:cancel
                                      destructiveButtonTitle:nil
                                           otherButtonTitles:nil];
        
        
         //  [self->mysheet addButtonWithTitle:@"Cancel"];
        
        
        for (NSString *s in self->names) 
            
        {
            
       [self->mysheet addButtonWithTitle:s];
        
         }
        self->completionAction = action;
		self->viewController = controller;
		self->tune = [tunex copy];
    }
	
	return self;
}

#pragma mark UIActionSheet delegate

- (void)actionSheet:(UIActionSheet *)aSheet didDismissWithButtonIndex:(NSInteger)buttonIndex 


{ 		
    if (buttonIndex < 0) return;
    
    if ((buttonIndex == 0)&& (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad)) return;
    
    NSUInteger  idx = (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)?buttonIndex:buttonIndex-1;
    
    NSString *list = [self->names objectAtIndex: idx];//[self->names count]-1-idx];
    
    NSLog (@"selected idx %d list %@",idx,list);
    
    {
        NSDictionary *dict = [NSDictionary dictionaryWithObjectsAndKeys:
                              self->tune,@"tune",list,@"list",nil];
        
        [self->viewController performSelector:self->completionAction withObject:dict]; 
    }	
}
@end

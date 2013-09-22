//
//  SecondViewController.h
//  Chatter
//
//  Created by Steven on 4/5/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "ChannelHelper.h"

#import <UIKit/UIKit.h>

@interface HomeViewController : UIViewController <ChannelUpdatesDelegate, UITextFieldDelegate>

@property (strong, nonatomic) NSMutableArray *lstChannels;

@property (strong, nonatomic) IBOutlet UITableView *myTableView;

- (IBAction)addNewChannel:(id)sender;

@end

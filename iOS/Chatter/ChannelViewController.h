//
//  ChannelViewController.h
//  Chatter
//
//  Created by Steven on 5/15/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "ChannelHelper.h"

#import <UIKit/UIKit.h>

@interface ChannelViewController : UIViewController <ChannelUpdatesDelegate, UITextFieldDelegate>

@property (strong, nonatomic) Channel *channel;

@property (strong, nonatomic) NSMutableArray *lstEvents;

@property (strong, nonatomic) IBOutlet UIView *myContainerView;

@property (strong, nonatomic) IBOutlet UITableView *myTableView;

@property (strong, nonatomic) IBOutlet UITextField *txtInput;

@property (strong, nonatomic) IBOutlet UIButton *btnSend;

- (IBAction)btnSendTouchDown:(id)sender;

- (IBAction)txtInputEditingBegin:(id)sender;

- (IBAction)txtInputEditingEnd:(id)sender;

@end

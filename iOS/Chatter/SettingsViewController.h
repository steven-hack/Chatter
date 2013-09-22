//
//  SettingsViewController.h
//  Chatter
//
//  Created by Steven on 4/5/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SettingsViewController : UIViewController

@property (strong, nonatomic) IBOutlet UITextField *txtNickname;
@property (strong, nonatomic) IBOutlet UISwitch *btnHighlighting;

- (IBAction)save:(id)sender;
- (IBAction)toggle:(id)sender;

@end
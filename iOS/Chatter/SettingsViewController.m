//
//  SettingsViewController.m
//  Chatter
//
//  Created by Steven on 4/5/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "SocketHelper.h"

#import "SettingsViewController.h"

@interface SettingsViewController ()

@end

@implementation SettingsViewController

@synthesize txtNickname;
@synthesize btnHighlighting;

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Retrieve storage
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString *nickname = [defaults objectForKey:@"nickname"];
    BOOL state = [defaults boolForKey:@"highlighting"];

    // Set switch state
    [btnHighlighting setOn:state animated:NO];

    // Set nickname if exists in storage
    if (nickname != nil) txtNickname.text = nickname;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)save:(id)sender
{
    // Hide the keyboard
    [txtNickname resignFirstResponder];

    // Retrieve input from text field
    NSString *nickname = [[txtNickname text] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];

    // Retrieve storage
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];

    if ([nickname length] > 0 && ![[defaults objectForKey:@"nickname"] isEqualToString:nickname])
    {
        // Store data
        [defaults setObject:nickname forKey:@"nickname"];

        // Notify server
        NSMutableDictionary *data = [NSMutableDictionary dictionary];
        [data setObject:nickname forKey:@"nickname"];
        [[[SocketHelper getInstance] getClient] sendEvent:@"nickchange" withData:data];
    }
}

- (IBAction)toggle:(id)sender
{
    // Retrieve switch state and store data
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setBool:[btnHighlighting isOn] forKey:@"highlighting"];
}
	
@end
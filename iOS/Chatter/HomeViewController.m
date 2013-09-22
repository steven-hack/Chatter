//
//  SecondViewController.m
//  Chatter
//
//  Created by Steven on 4/5/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "Channel.h"

#import "SocketHelper.h"

#import "HomeViewController.h"
#import "ChannelViewController.h"

@interface HomeViewController ()

@end

@implementation HomeViewController

@synthesize lstChannels;
@synthesize myTableView;

UIAlertView *alert;
UITextField *textField;

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Initiate new channel list
    lstChannels = [NSMutableArray array];
    
    // Subscribe to channel updates
    [ChannelHelper subscribe:self];

    // Create Socket.IO instance
    [[SocketHelper getInstance] connect];

    // Create alert prompt for joining a new channel
    alert = [[UIAlertView alloc] initWithTitle:@"Join Channel" message:@"Give the name of the channel you wish to join.\n\n\n" delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"Join", nil];
    textField = [[UITextField alloc] init];
    [textField setBackgroundColor:[UIColor whiteColor]];
    
    // Set Textfield properties
    textField.delegate = self;
    textField.borderStyle = UITextBorderStyleLine;
    textField.frame = CGRectMake(15, 100, 255, 30);
    textField.placeholder = @"Channel name";
    textField.keyboardAppearance = UIKeyboardAppearanceAlert;
    
    // Add text field to prompt and show to user
    [textField becomeFirstResponder];
    [alert addSubview:textField];
}

- (void)dealloc
{
    // Unsubscribe from channel updates
    [ChannelHelper unsubscribe:self];
}

- (void)channelUpdated:(Channel *)updatedChannel
{
    // Nothing to do here
}

- (void)channelsUpdated:(NSMutableDictionary *)updatedChannels
{
    // Copy channel names (Basically the keys)
    lstChannels = [NSMutableArray arrayWithArray:[updatedChannels allKeys]];

    // Tell the tableview to reload his data
    [myTableView reloadData];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Check if we have the correct segue
    if ([segue.identifier isEqualToString:@"showChannelDetail"])
    {
        // Pass along the channel object
        NSIndexPath *indexPath = [myTableView indexPathForSelectedRow];
        NSString *channelName = [lstChannels objectAtIndex:indexPath.row];
        ChannelViewController *channelViewController = segue.destinationViewController;
        channelViewController.channel = [ChannelHelper getChannel:channelName];
    }
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [lstChannels count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Retrieve cell
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ChannelCell"];

    // If cell does not exist, create it
    if (!cell)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"ChannelCell"];
    }

    // Place channel name in cell
    cell.textLabel.text = [lstChannels objectAtIndex:[indexPath row]];

    return cell;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Notify Server
    NSMutableDictionary *data = [NSMutableDictionary dictionary];
    [data setObject:[lstChannels objectAtIndex:indexPath.row] forKey:@"channel"];
    [[[SocketHelper getInstance] getClient] sendEvent:@"part" withData:data];

    // Remove channel
    [ChannelHelper removeChannel:[lstChannels objectAtIndex:indexPath.row]];
    [lstChannels removeObjectAtIndex:indexPath.row];
    
    // Update table
    [myTableView reloadData];
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    return YES;
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return UITableViewCellEditingStyleDelete;
}

- (IBAction)addNewChannel:(id)sender
{
    // The alert view is created when this view is shown, so just show it
    [alert show];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if ([textField.text length] <= 0 || buttonIndex == 0)
    {
        return;
    }

    if (buttonIndex == 1)
    {
        // Create the channel
        NSString *channelName = [NSString stringWithFormat:@"#%@", textField.text];
        [ChannelHelper addChannel:[[Channel alloc] initWithName:channelName]];
        [ChannelHelper notifySubscribers];

        // Notify the server
        NSMutableDictionary *data = [NSMutableDictionary dictionary];
        [data setObject:channelName forKey:@"channel"];
        [[[SocketHelper getInstance] getClient] sendEvent:@"join" withData:data];
    }
}

@end

//
//  ChannelViewController.m
//  Chatter
//
//  Created by Steven on 5/15/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "SocketHelper.h"

#import "ChannelViewController.h"

@interface ChannelViewController ()

@end

@implementation ChannelViewController

@synthesize channel;
@synthesize lstEvents;
@synthesize myContainerView;
@synthesize myTableView;
@synthesize txtInput;
@synthesize btnSend;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Subscribe to channel updates
    [ChannelHelper subscribe:self];

    // Show channel name in title
    self.navigationItem.title = [channel getName];

    // Show channel log
    lstEvents = [NSMutableArray arrayWithArray:[channel getEvents]];
    
    // Tell the tableview to reload his data
    [myTableView reloadData];

    // Make sure the last message is displayed
    if ([lstEvents count] > 0)
    {
        NSIndexPath* path = [NSIndexPath indexPathForRow:[lstEvents count] - 1 inSection:0];
        [myTableView scrollToRowAtIndexPath:path atScrollPosition:UITableViewScrollPositionBottom animated:YES];
    }
}

- (void)dealloc
{
    // Unsubscribe from channel updates
    [ChannelHelper unsubscribe:self];
}

- (void)channelUpdated:(Channel *)updatedChannel
{
    // Only handle updates of the shown channel
    if ([[updatedChannel getName] isEqualToString:[channel getName]])
    {
        // Copy channel events
        lstEvents = [NSMutableArray arrayWithArray:[updatedChannel getEvents]];
    
        // Tell the tableview to reload his data
        [myTableView reloadData];

        // Make sure the last message is shown
        if ([lstEvents count] > 0)
        {
            NSIndexPath* path = [NSIndexPath indexPathForRow:[lstEvents count] - 1 inSection:0];
            [myTableView scrollToRowAtIndexPath:path atScrollPosition:UITableViewScrollPositionBottom animated:YES];
        }
    }
}

- (void)channelsUpdated:(NSMutableDictionary *)updatedChannels
{
    // Nothing to do here
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [lstEvents count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Retrieve cell
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"MessageCell"];
    
    // If cell does not exist, create it
    if (!cell)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"MessageCell"];
    }
    
    // Place channel name in cell
    cell.textLabel.numberOfLines = 0;
    cell.textLabel.lineBreakMode = NSLineBreakByWordWrapping;
    cell.textLabel.text = [lstEvents objectAtIndex:[indexPath row]];
    
    return cell;
}

- (IBAction)btnSendTouchDown:(id)sender
{
    // Retrieve the message and trim it
    NSString *message = txtInput.text;

    // Only continue if there is an actual message
    if (message.length > 0)
    {
        // Send message to the server
        NSMutableDictionary *data = [NSMutableDictionary dictionary];
        [data setObject:message forKey:@"content"];
        [data setObject:[channel getName] forKey:@"channel"];
        [[[SocketHelper getInstance] getClient] sendEvent:@"msg" withData:data];

        // Clear the text field
        [txtInput setText:@""];
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [txtInput resignFirstResponder];
    return YES;
}

- (IBAction)txtInputEditingBegin:(id)sender
{
    myContainerView.frame = CGRectMake(0, -160, 320, 360);
}

- (IBAction)txtInputEditingEnd:(id)sender
{
    myContainerView.frame = CGRectMake(0, 0, 320, 360);
}

@end

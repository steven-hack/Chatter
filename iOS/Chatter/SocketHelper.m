//
//  SocketHelper.m
//  Chatter
//
//  Created by Steven on 4/6/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "DataHelper.h"
#import "SocketHelper.h"
#import "ChannelHelper.h"

#import "SocketIOPacket.h"

@implementation SocketHelper

static SocketHelper *instance = nil;

+ (SocketHelper *)getInstance
{
    // Implement singleton pattern
    if (instance == nil)
    {
        instance = [[super allocWithZone:nil] init];
    }

    return instance;
}

+ (id)allocWithZone:(NSZone *)zone
{
    return [self getInstance];
}

@synthesize client;

- (void)connect
{
    client = [[SocketIO alloc] initWithDelegate:self];
    [client connectToHost:@"chatter-7482.onmodulus.net" onPort:80];
}

- (SocketIO *)getClient
{
    return client;
}

- (void)socketIODidConnect:(SocketIO *)socket
{
    // Create data to send
    NSMutableDictionary *data = [NSMutableDictionary dictionary];
    [data setObject:[DataHelper getIdentifier] forKey:@"identifier"];

    [client sendEvent:@"channel_list" withData:data];
}

- (void)socketIO:(SocketIO *)socket didReceiveJSON:(SocketIOPacket *)packet
{
    NSLog(@"didReceiveJSON: %@", packet.data);
}

- (void)socketIO:(SocketIO *)socket didReceiveEvent:(SocketIOPacket *)packet
{
    NSLog(@"didReceiveEvent() >>> data: %@", packet.data);

    // The global event 'nickchanged' is handled seperately.
    if ([packet.name isEqualToString:@"nickchanged"])
    {
        NSUserDefaults *defaults  = [NSUserDefaults standardUserDefaults];
        NSString *local_nickname  = [defaults objectForKey:@"nickname"];
        NSString *server_nickname = [[[packet.dataAsJSON objectForKey:@"args"] objectAtIndex:0] objectForKey:@"nickname"];

        if (!local_nickname || [local_nickname isEqualToString:@""])
        {
            // Save te given nickname
            NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
            [defaults setObject:local_nickname forKey:@"nickname"];
        }
        else if (![local_nickname isEqualToString:server_nickname])
        {
            // Notify the server of the local nickname
            NSMutableDictionary *data = [NSMutableDictionary dictionary];
            [data setObject:local_nickname forKey:@"nickname"];
            [client sendEvent:@"nickchange" withData:data];
        }

        return;
    }

    // The global event 'channel_list' is handled seperately.
    if ([packet.name isEqualToString:@"channel_list"])
    {
        // Retrieve JSON output
        NSDictionary *data = [[[packet.dataAsJSON objectForKey:@"args"] objectAtIndex:0] objectForKey:@"channels"];

        if (data)
        {
            // Loop through channels and add them
            for (NSDictionary *dict in data)
            {
                NSString *name = [dict objectForKey:@"channel"];
                [ChannelHelper addChannel:[[Channel alloc] initWithName:name]];
            }

            // Notify subscribers
            [ChannelHelper notifySubscribers];
        }

        return;
    }

    // Every other event is attached to a channel. Retrieve it.
    NSMutableDictionary *data = [[packet.dataAsJSON objectForKey:@"args"] objectAtIndex:0];
    Channel *channel = [ChannelHelper getChannel:[data objectForKey:@"channel"]];

    // There is no reason to go any further if there isn't a channel
    if (!channel) return;

    if ([packet.name isEqualToString:@"log"])
    {
        // Retrieve the history
        NSArray *history = [[[data objectForKey:@"history"] reverseObjectEnumerator] allObjects];

        // Loop through all events and add them (Backwards ofcourse)
        for (NSDictionary *event in history)
        {
            // Retrieve the event type
            NSString *type = [event objectForKey:@"type"];

            if ([type isEqualToString:@"msg"])
            {
                // Add event to channel
                NSString *nickname = [event objectForKey:@"nickname"];
                NSString *content  = [event objectForKey:@"message"];
                [channel addEvent:[NSString stringWithFormat:@"[%@] %@", nickname, content]];
            }
            else if ([type isEqualToString:@"join"])
            {
                // Add event to channel
                NSString *nickname = [event objectForKey:@"nickname"];
                [channel addEvent:[NSString stringWithFormat:@"%@ joined the room.", nickname]];
            }
            else if ([type isEqualToString:@"part"])
            {
                // Add event to channel
                NSString *nickname = [event objectForKey:@"nickname"];
                [channel addEvent:[NSString stringWithFormat:@"%@ left the room.", nickname]];
            }
            else if ([type isEqualToString:@"nickchange"])
            {
                // Add event to channel
                NSString *old_nick = [event objectForKey:@"message"];
                NSString *new_nick = [event objectForKey:@"nickname"];
                [channel addEvent:[NSString stringWithFormat:@"%@ renamed to %@", old_nick, new_nick]];
            }
        }

        // Update subscribers
        [ChannelHelper notifySubscribers:channel];
    }
    else if ([packet.name isEqualToString:@"msg"])
    {
        // Add event to channel
        NSString *nickname = [data objectForKey:@"nickname"];
        NSString *content  = [data objectForKey:@"content"];
        [channel addEvent:[NSString stringWithFormat:@"[%@] %@", nickname, content]];

        // Update subscribers
        [ChannelHelper notifySubscribers:channel];
    }
    else if ([packet.name isEqualToString:@"user_list"])
    {
        // TODO
    }
    else if ([packet.name isEqualToString:@"user_joined"])
    {
        // Add event to channel
        NSString *nickname = [data objectForKey:@"nickname"];
        [channel addEvent:[NSString stringWithFormat:@"%@ joined the room.", nickname]];

        // Update subscribers
        [ChannelHelper notifySubscribers:channel];
    }
    else if ([packet.name isEqualToString:@"user_parted"])
    {
        // Add event to channel
        NSString *nickname = [data objectForKey:@"nickname"];
        [channel addEvent:[NSString stringWithFormat:@"%@ left the room.", nickname]];

        // Update subscribers
        [ChannelHelper notifySubscribers:channel];
    }
    else if ([packet.name isEqualToString:@"user_nickchanged"])
    {
        // Add event to channel
        NSString *old_nick = [data objectForKey:@"old_nick"];
        NSString *new_nick = [data objectForKey:@"new_nick"];
        [channel addEvent:[NSString stringWithFormat:@"%@ renamed to %@", old_nick, new_nick]];

        // Update subscribers
        [ChannelHelper notifySubscribers:channel];
    }
}

- (void)socketIO:(SocketIO *)socket didReceiveMessage:(SocketIOPacket *)packet
{
    NSLog(@"didReceiveMessage: %@", packet.data);
}

@end
//
//  Channel.m
//  Chatter
//
//  Created by Steven on 4/6/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "SocketHelper.h"

#import "Channel.h"

@implementation Channel

@synthesize name;
@synthesize events;

- (id) init
{
    // Initialization without channel name is not allowed
    return nil;
}

- (id) initWithName:(NSString *)channelName
{
    self = [super init];

    if (self)
    {
        name   = channelName;
        events = nil;
    }

    return self;
}

- (NSString *)getName
{
    return name;
}

- (NSMutableArray *)getEvents
{
    if (!events)
    {
        // Retrieve events from server
        NSMutableDictionary *data = [NSMutableDictionary dictionary];
        [data setObject:name forKey:@"channel"];
        [[[SocketHelper getInstance] getClient] sendEvent:@"channel_log" withData:data];

        // While loading, return an empty list
        events = [[NSMutableArray alloc] init];
    }
    return events;
}

- (void)addEvent:(NSString *)event
{
    [events addObject:event];
}

@end

//
//  ChannelHelper.m
//  Chatter
//
//  Created by Steven on 4/6/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "ChannelHelper.h"

@implementation ChannelHelper

static NSMutableArray *lstSubscribers;
static NSMutableDictionary *lstChannels;

+ (void)addChannel:(Channel *)channel
{
    if (!lstChannels) lstChannels = [[NSMutableDictionary alloc] init];
    [lstChannels setObject:channel forKey:[channel getName]];
}

+ (Channel *)getChannel:(NSString *)name
{
    if (!lstChannels) return nil;
    return [lstChannels objectForKey:name];
}
         
+ (void)removeChannel:(NSString *)name
{
    if (!lstChannels) nil;
    [lstChannels removeObjectForKey:name];
}

+ (void)notifySubscribers
{
    // Make sure the dictionary exists
    if (!lstChannels) lstChannels = [[NSMutableDictionary alloc] init];

    // Loop through all subscribers
    for (NSObject<ChannelUpdatesDelegate> *subscriber in lstSubscribers)
    {
        // Send an update to the subscriber
        [subscriber channelsUpdated:lstChannels];
    }
}

+ (void)notifySubscribers:(Channel *)channel
{
    // Make sure the dictionary exists
    if (!lstChannels) lstChannels = [[NSMutableDictionary alloc] init];

    // Loop through all subscribers
    for (NSObject<ChannelUpdatesDelegate> *subscriber in lstSubscribers)
    {
        // Send an update to the subscriber
        [subscriber channelUpdated:channel];
    }
}

+ (void)subscribe:(NSObject<ChannelUpdatesDelegate> *)subscriber
{
    if (!lstSubscribers) lstSubscribers = [[NSMutableArray alloc] init];
    [lstSubscribers addObject:subscriber];
}

+ (void)unsubscribe:(NSObject<ChannelUpdatesDelegate> *)subscriber
{
    if (!lstSubscribers) lstSubscribers = [[NSMutableArray alloc] init];
    [lstSubscribers removeObject:subscriber];
}

@end

//
//  ChannelHelper.h
//  Chatter
//
//  Created by Steven on 4/6/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "Channel.h"

#import <Foundation/Foundation.h>

@protocol ChannelUpdatesDelegate

@optional
- (void)channelUpdated:(Channel *)updatedChannel;
- (void)channelsUpdated:(NSMutableDictionary *)updatedChannels;

@end

@interface ChannelHelper : NSObject

+ (void)addChannel:(Channel *)channel;
+ (Channel *)getChannel:(NSString *)name;
+ (void)removeChannel:(NSString *)name;

+ (void)notifySubscribers;
+ (void)notifySubscribers:(Channel *)channel;

+ (void)subscribe:(NSObject<ChannelUpdatesDelegate> *)subscriber;
+ (void)unsubscribe:(NSObject<ChannelUpdatesDelegate> *)subscriber;

@end

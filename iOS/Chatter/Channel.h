//
//  Channel.h
//  Chatter
//
//  Created by Steven on 4/6/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Channel : NSObject

@property (strong, nonatomic) NSString *name;
@property (strong, nonatomic) NSMutableArray *events;

- (id) initWithName:(NSString *)channelName;

- (NSString *) getName;
- (NSMutableArray *) getEvents;
- (void) addEvent:(NSString *)event;

@end

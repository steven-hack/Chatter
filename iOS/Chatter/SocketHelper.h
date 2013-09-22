//
//  SocketHelper.h
//  Chatter
//
//  Created by Steven on 4/6/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "SocketIO.h"

#import <Foundation/Foundation.h>

@interface SocketHelper : NSObject <SocketIODelegate>

+ (id)getInstance;

@property (strong, nonatomic) SocketIO *client;

- (void)connect;

- (SocketIO *)getClient;

@end

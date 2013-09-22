//
//  DataHelper.m
//  Chatter
//
//  Created by Steven on 4/6/13.
//  Copyright (c) 2013 Steven. All rights reserved.
//

#import "DataHelper.h"

@implementation DataHelper

static NSString *identifier = nil;

+ (NSString *)getIdentifier
{
    if (identifier)
        return identifier;

    // Retrieve storage
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString *identifier = [defaults objectForKey:@"identifier"];
    
    // First time we need a unique identifer? Create one
    if (identifier == nil)
    {
        // Create an uniue identifier
        CFUUIDRef theUUID = CFUUIDCreate(NULL);
        CFStringRef string = CFUUIDCreateString(NULL, theUUID);
        CFRelease(theUUID);

        // Save the identifier to storage
        identifier = (NSString *)CFBridgingRelease(string);
        [defaults setValue:identifier forKey:@"identifier"];
    }

    return identifier;
}

@end

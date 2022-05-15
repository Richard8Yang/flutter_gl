// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <AVKit/AVKit.h>
#import <AVFoundation/AVFoundation.h>
#import <GLKit/GLKit.h>
#import "BetterPlayerTimeUtils.h"
#import "BetterPlayer.h"
#import <MediaPlayer/MediaPlayer.h>

@interface BetterPlayerPlugin : NSObject

@property(readonly, weak, nonatomic) NSObject<FlutterBinaryMessenger>* messenger;
@property(readonly, strong, nonatomic) NSMutableDictionary* players;
@property(readonly, strong, nonatomic) NSObject<FlutterPluginRegistrar>* registrar;

- (instancetype)initWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar;
- (void) setSharedEglContext:(EAGLContext*)sharedEglCtx;
- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result;

@end
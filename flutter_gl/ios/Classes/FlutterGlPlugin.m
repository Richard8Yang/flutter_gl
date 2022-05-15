#import "FlutterGlPlugin.h"
#if __has_include(<flutter_gl/flutter_gl-Swift.h>)
#import <flutter_gl/flutter_gl-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_gl-Swift.h"
#endif

@implementation FlutterGlPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterGlPlugin* instance = [[FlutterGlPlugin alloc] initWithRegistrar:registrar];
  FlutterMethodChannel* channel =
      [FlutterMethodChannel methodChannelWithName:@"flutter_gl"
                                  binaryMessenger:[registrar messenger]];
  [registrar addMethodCallDelegate:instance channel:channel];
  //[SwiftFlutterGlPlugin registerWithRegistrar:registrar];
}

- (instancetype)initWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  self = [super init];
  NSAssert(self, @"super init cannot be nil");
  _messenger = [registrar messenger];
  _registrar = registrar;
  _glPlugin = [SwiftFlutterGlPlugin alloc];
  [_glPlugin initializeWithRegistrar:registrar];
  _videoPlugin = [[BetterPlayerPlugin alloc] initWithRegistrar:registrar];
    return self;
}

- (void)detachFromEngineForRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  //_videoPlugin.dispose()
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if (![_glPlugin handleWithCall:call result:result]) {
    [_videoPlugin setSharedEglContext:[_glPlugin getSharedEglContext]];
    [_videoPlugin handleMethodCall:call result:result];
  }
}
@end

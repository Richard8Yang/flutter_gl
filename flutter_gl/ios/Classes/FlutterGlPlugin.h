#import <Flutter/Flutter.h>
#import "BetterPlayerPlugin.h"

@class SwiftFlutterGlPlugin;

@interface FlutterGlPlugin : NSObject<FlutterPlugin>

@property(readonly, weak, nonatomic) NSObject<FlutterBinaryMessenger>* messenger;
@property(readonly, strong, nonatomic) NSObject<FlutterPluginRegistrar>* registrar;
@property(nonatomic, readonly) SwiftFlutterGlPlugin* glPlugin;
@property(nonatomic, readonly) BetterPlayerPlugin* videoPlugin;

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result;

@end

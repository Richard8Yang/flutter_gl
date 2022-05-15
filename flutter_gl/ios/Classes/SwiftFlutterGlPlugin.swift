import Flutter
import UIKit

@objc public class SwiftFlutterGlPlugin: NSObject {
  
  var renders: [Int64: CustomRender];

  var registry: FlutterTextureRegistry?;
  var textureId: Int64?;
  var sharedEglCtx: EAGLContext?;
  
  override init() {
    self.renders = [Int64: CustomRender]();
  }

  @objc public func initialize(registrar: FlutterPluginRegistrar) {
    self.renders = [Int64: CustomRender]();
    self.registry = registrar.textures();
  }

  @objc public func getSharedEglContext() -> EAGLContext? {
    return sharedEglCtx;
  }

  @objc public func handle(call: FlutterMethodCall, result: @escaping FlutterResult) -> Bool {
    //    result("iOS " + UIDevice.current.systemVersion)
    let methodName = call.method;

    var handled: Bool = true;
  
//    print("call method: \(methodName)  ");
    
    switch methodName {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion);
    case "initialize":
      guard let args = call.arguments as? [String : Any] else {
        result(" arguments error.... ")
        return true;
      }
      
      let options = args["options"] as! Dictionary<String, Any>;
      let renderToVideo = args["renderToVideo"] as! Bool;
      
      let render = CustomRender(
        options: options,
        renderToVideo: renderToVideo,
        onNewFrame: {() -> Void in
//          print(" self.registry.textureFrameAvailable(self.textureId!): \(self.textureId) ")
          self.registry!.textureFrameAvailable(self.textureId!)
        }
      );

      sharedEglCtx = render.getEglContext();
      
      self.textureId = self.registry!.register(render);
      print("Created renderer \(render), texture id \(self.textureId!)");

      self.renders[self.textureId!] = render;
      
      let _info = [
        "textureId": textureId
      ];
      
      result(_info);
   
    case "dispose":
      guard let args = call.arguments as? [String : Any] else {
        result(" arguments error.... ")
        return true;
      }
      let textureId = args["textureId"] as? Int64;
      
      if(textureId != nil) {
        registry!.unregisterTexture(textureId!);
        let render = self.renders[textureId!];
         render?.dispose();
        self.renders.removeValue(forKey: textureId!);
      }
      result(nil);
    
    case "getEgl":
      guard let args = call.arguments as? [String : Any] else {
        result(" arguments error.... ")
        return true;
      }

      let textureId = args["textureId"] as? Int64;
      var render = self.renders[textureId!];

      var eglResult = render?.getEgl();

      result(eglResult);
    case "updateTexture":
      guard let args = call.arguments as? [String : Any] else {
        result(" arguments error.... ")
        return true;
      }

      let textureId = args["textureId"] as? Int64;
      let sourceTexture = args["sourceTexture"] as? Int64;

      let render = self.renders[textureId!];

      let resp = render!.updateTexture(sourceTexture: sourceTexture!);

      result(resp);
    default:
      //result(nil);
      handled = false;
    }

    return handled;
  }
}

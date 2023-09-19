package com.futouapp.flutter_gl.flutter_gl

import android.content.Context

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.view.TextureRegistry

class FlutterGlPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var registry: TextureRegistry
    private lateinit var context: Context
    private lateinit var messenger: BinaryMessenger;
    private lateinit var videoPlugin: VideoPlayerPlugin;

    private var renders = mutableMapOf<Int, CustomRender>()

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_gl")
        channel.setMethodCallHandler(this)
        registry = flutterPluginBinding.textureRegistry
        context = flutterPluginBinding.applicationContext
        messenger = flutterPluginBinding.binaryMessenger;

        videoPlugin = VideoPlayerPlugin(context, messenger, registry)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "initialize" -> {
                val args = (call.arguments as Map<*, *>).mapKeys { it.key as String }
                val options = (args["options"] as Map<*, *>?)!!.mapKeys { it.key as String }

                val glWidth = ((options["width"] as Int) * (options["dpr"] as Double)).toInt()
                val glHeight = ((options["height"] as Int) * (options["dpr"] as Double)).toInt()

                val entry = registry.createSurfaceTexture()
                val textureID = entry.id().toInt()

                val render = CustomRender(entry, glWidth, glHeight)
                renders[textureID] = render

                videoPlugin.setShareEglContext(render.getSharedEglContext())

                result.success(mapOf("textureId" to textureID))
            }
            "getEgl" -> {
                val args = (call.arguments as Map<*, *>).mapKeys { it.key as String }
                val textureId = args["textureId"] as Int

                val render = this.renders[textureId]
                val eglResult = render?.getEgl()

                result.success(eglResult)
            }
            "updateTexture" -> {
                val args = (call.arguments as Map<*, *>).mapKeys { it.key as String }
                val textureId = args["textureId"] as Int
                val sourceTexture = args["sourceTexture"] as Int

                val resp = this.renders[textureId]?.updateTexture(sourceTexture)

                result.success(resp)
            }
            "dispose" -> {
                val args = (call.arguments as Map<*, *>).mapKeys { it.key as String }
                val textureId = args["textureId"] as? Int

                if (textureId != null) {
                    val render = this.renders[textureId]
                    render?.dispose()
                    this.renders.remove(textureId)
                }

                result.success(null)
            }
            else -> {
                videoPlugin.onMethodCall(call, result)
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}

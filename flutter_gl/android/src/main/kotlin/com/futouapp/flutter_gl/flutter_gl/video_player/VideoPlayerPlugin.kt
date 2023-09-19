// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
package com.futouapp.flutter_gl.flutter_gl

import android.os.Build
import android.util.Log
import android.util.LongSparseArray
import android.opengl.*
import android.content.Context
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.view.TextureRegistry

/** Android platform implementation of the VideoPlayerPlugin.  */
class VideoPlayerPlugin(
    private val appContext: Context,
    private val messenger: BinaryMessenger,
    private val textureEntry: TextureRegistry,
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
) {
    private val videoPlayers = LongSparseArray<VideoPlayer>()
    private val options = VideoPlayerOptions()

    public fun setShareEglContext(shareEglContext: EGLContext) {
        eglContext = shareEglContext;
    }

    public fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        //Log.d("PLAYER", call.method)
        when (call.method) {
            "video.init" -> initialize()
            "video.create" -> {
                val videoTex = textureEntry.createSurfaceTexture()
                val eventChannel = EventChannel(messenger, "videoPlayer/videoEvents" + videoTex.id())
                val options: VideoPlayerOptions = VideoPlayerOptions()
                val mixWithOthers = call.argument<Boolean?>("mixWithOthers")
                val allowBgPlayback = call.argument<Boolean?>("allowBackgroundPlayback")
                if (mixWithOthers != null) {
                    options.mixWithOthers = mixWithOthers
                }
                if (allowBgPlayback != null) {
                    options.allowBackgroundPlayback = allowBgPlayback
                }
                // create player with share EGL context if specified
                val player = VideoPlayer(appContext, eventChannel, videoTex, options, eglContext)
                videoPlayers.put(videoTex.id(), player)

                val reply: MutableMap<String, Any> = HashMap()
                reply["textureId"] = videoTex.id()
                reply["sharedTextureId"] = player.getOffScreenTextureId()   // returns shared offscreen texture
                Log.d("PLAYER", "Onscreen texture %d, Offscreen texture %d".format(videoTex.id(), player.getOffScreenTextureId()))
                result.success(reply)
            }
            //"video.preCache" -> preCache(call, result)
            //"video.stopPreCache" -> stopPreCache(call, result)
            //"video.clearCache" -> clearCache(result)
            else -> {
                val textureId = (call.argument<Any>("textureId") as Number?)!!.toLong()
                handlePlayerControlCmd(call, result, textureId)
            }
        }
    }

    private fun handlePlayerControlCmd(
        call: MethodCall,
        result: MethodChannel.Result,
        textureId: Long
    ) {
        val player = videoPlayers[textureId]
        if (player == null) {
            result.error(
                "Unknown textureId",
                "No video player associated with texture id $textureId",
                null
            )
            return
        }
        when (call.method) {
            "video.player.setDataSource" -> {
                val dataSrcUri = call.argument<String?>("uri")
                val dataSrcFmtHint = call.argument<String?>("fmtHint")
                if (dataSrcUri != null) {
                    player.setDataSource(dataSrcUri, dataSrcFmtHint)
                    result.success(null)
                }
            }
            "video.player.play" -> {
                //setupNotification(player)
                player.play()
                result.success(null)
            }
            "video.player.pause" -> {
                player.pause()
                result.success(null)
            }
            "video.player.setLooping" -> {
                player.setLooping(call.argument("looping")!!)
                result.success(null)
            }
            "video.player.setVolume" -> {
                player.setVolume(call.argument("volume")!!)
                result.success(null)
            }
            "video.player.setPlaybackSpeed" -> {
                player.setPlaybackSpeed(call.argument("speed")!!)
                result.success(null)
            }
            "video.player.seekToPos" -> {
                val pos = (call.argument<Any>("position") as Number?)!!.toInt()
                player.seekTo(pos)
                result.success(null)
            }
            "video.player.getPos" -> {
                result.success(player.position)
                player.sendBufferingUpdate()
            }
            "video.player.getAbsolutePos" -> result.success(player.absolutePosition)
            // "video.player.setTrackParams" -> {
            //     player.setTrackParameters(
            //         call.argument(WIDTH_PARAMETER)!!,
            //         call.argument(HEIGHT_PARAMETER)!!,
            //         call.argument(BITRATE_PARAMETER)!!
            //     )
            //     result.success(null)
            // }
            // "video.player.setAudioTrack" -> {
            //     val name = call.argument<String?>(NAME_PARAMETER)
            //     val index = call.argument<Int?>(INDEX_PARAMETER)
            //     if (name != null && index != null) {
            //         player.setAudioTrack(name, index)
            //     }
            //     result.success(null)
            // }
            "video.player.setMixWithOthers" -> {
                val mixWitOthers = call.argument<Boolean?>("mixWithOthers")
                if (mixWitOthers != null) {
                    options.mixWithOthers = mixWitOthers
                    player.setMixWithOthers(mixWitOthers)
                }
            }
            "video.player.dispose" -> {
                player.dispose()
                videoPlayers.remove(textureId)
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    private fun disposeAllPlayers() {
        for (i in 0 until videoPlayers.size()) {
            videoPlayers.valueAt(i).dispose()
        }
        videoPlayers.clear()
    }

    private fun onDestroy() {
        // The whole FlutterView is being destroyed. Here we release resources acquired for all
        // instances
        // of VideoPlayer. Once https://github.com/flutter/flutter/issues/19358 is resolved this may
        // be replaced with just asserting that videoPlayers.isEmpty().
        // https://github.com/flutter/flutter/issues/20989 tracks this.
        disposeAllPlayers()
    }

    private fun initialize() {
        disposeAllPlayers()
    }
}
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

import './MyApp.dart';

requestPermisions() async {
  // request for permissions
  bool allowed = true;
  allowed = allowed && await Permission.storage.request().isGranted;
  //allowed = allowed && await Permission.camera.request().isGranted;
  allowed = allowed && await Permission.photos.request().isGranted;
  //allowed = allowed && await Permission.microphone.request().isGranted;
  allowed = allowed && await Permission.mediaLibrary.request().isGranted;
  allowed =
      allowed && await Permission.manageExternalStorage.request().isGranted;
  //allowed = allowed && await Permission.sensors.request().isGranted;
  if (!allowed) {
    // TODO: show some prompt message
  }
}

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await requestPermisions();
  SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp])
      .then((_) {
    runApp(MyApp());
  });
}

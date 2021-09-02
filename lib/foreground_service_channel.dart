import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';

class ForegroundServiceChannel {
  static const _channel = MethodChannel('com.rtirl.chat/foreground_service');

  static start() {
    _channel.invokeMethod<bool>('start', {
      "callbackHandle":
          PluginUtilities.getCallbackHandle(audioIsolate)?.toRawHandle()
    });
  }

  static stop() {
    _channel.invokeMethod<bool>('stop');
  }

  static setUrls(Set<String> urls) {
    _channel.invokeMethod<void>('setUrls', {"urls": urls.toList()});
  }
}

Future<void> audioIsolate() async {
  WidgetsFlutterBinding.ensureInitialized();

  final Map<String, HeadlessInAppWebView> views = {};

  final initialOptions = InAppWebViewGroupOptions(
      crossPlatform: InAppWebViewOptions(
          mediaPlaybackRequiresUserGesture: false, javaScriptEnabled: true));

  // listen on the channel.
  final stream = const EventChannel('com.rtirl.chat/audio_sources')
      .receiveBroadcastStream()
      .cast<List<String>>()
      .map((urls) => urls.toSet());
  await for (final next in stream) {
    final current = views.keys.toSet();
    for (final add in next.difference(current)) {
      final view = HeadlessInAppWebView(
        initialUrlRequest: URLRequest(url: Uri.parse(add)),
        initialOptions: initialOptions,
      );
      view.run();
      views[add] = view;
    }

    for (final sub in current.difference(next)) {
      views.remove(sub)?.dispose();
    }
  }
}

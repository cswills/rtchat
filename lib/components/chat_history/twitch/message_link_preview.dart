import 'package:metadata_fetch/metadata_fetch.dart';
import 'package:flutter/material.dart';
import 'package:flutter_image/flutter_image.dart';
import 'package:async/async.dart';

class _TwitchClipData {
  final String? imageUrl;
  final String? url;
  final String? title;
  final String? description;

  const _TwitchClipData(
      {required this.imageUrl,
      required this.url,
      required this.title,
      required this.description});
}

class _TwitchMessageLinkPreviewWidgetState
    extends State<TwitchMessageLinkPreviewWidget> {
  late Future<dynamic> fetchClipData;

  @override
  void initState() {
    fetchClipData = _fetchClipData(widget.url);
    super.initState();
  }

  _fetchClipData(String url) async {
    return widget._memoizer.runOnce(() async {
      print("fetching: ${url}");
      final data = await MetadataFetch.extract(url);
      final res = _TwitchClipData(
          imageUrl: data!.image,
          url: data.url,
          title: data.title,
          description: data.description);
      return res;
    });
  }

  @override
  Widget build(BuildContext context) {
    String url = widget.url;
    return Column(
      children: [
        Padding(
            padding: const EdgeInsets.symmetric(vertical: 4),
            child: Text.rich(
              TextSpan(children: widget.children),
            )),
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 4),
          child: FutureBuilder(
            future: fetchClipData,
            builder: (BuildContext context, AsyncSnapshot snapshot) {
              if (!snapshot.hasData) {
                return const Card(child: CircularProgressIndicator());
              }
              return Card(
                child: ListTile(
                  leading: Image(
                      image: NetworkImageWithRetry(snapshot.data.imageUrl)),
                  title: Text(snapshot.data.title),
                  subtitle: Text(snapshot.data.description),
                  isThreeLine: true,
                ),
              );
            },
          ),
        )
      ],
    );
  }
}

class TwitchMessageLinkPreviewWidget extends StatefulWidget {
  final List<InlineSpan> children;
  final String url;
  final AsyncMemoizer _memoizer = AsyncMemoizer();

  TwitchMessageLinkPreviewWidget(
      {required this.children, required this.url, Key? key})
      : super(key: key);

  @override
  _TwitchMessageLinkPreviewWidgetState createState() =>
      _TwitchMessageLinkPreviewWidgetState();
}

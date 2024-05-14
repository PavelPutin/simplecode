import 'dart:convert';
import 'dart:typed_data';
import 'dart:html' as html;

import 'package:file_picker/file_picker.dart';
import 'package:flutter/cupertino.dart';

class SimpleCodeViewModel extends ChangeNotifier {

  Future<void> openYamlFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();
    if (result != null) {
      String fileName = result.files.first.name;

      Uint8List fileBytes = result.files.first.bytes!;
      String input = utf8.decode(fileBytes);
    }
  }

  Future<void> downloadYamlFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();
    if (result != null) {
      String fileName = result.files.first.name;

      Uint8List fileBytes = result.files.first.bytes!;
      String input = utf8.decode(fileBytes);

      final base64 = base64Encode(utf8.encode(input));
      final anchor = html.AnchorElement(href: 'data:application/octet-stream;base64,$base64');
      anchor.download = fileName;
      anchor.click();
      anchor.remove();
    }
  }
}
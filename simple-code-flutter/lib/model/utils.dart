import 'dart:convert';
import 'dart:html' as html;
import 'dart:typed_data';

import 'package:file_picker/file_picker.dart';
import 'package:xml/xml.dart';
import 'package:xml/xpath.dart';

void downloadFile(String fileName, String extension, String data) {
  final base64 = base64Encode(utf8.encode(data));
  final anchor = html.AnchorElement(href: 'data:application/octet-stream;base64,$base64');
  anchor.download = "$fileName.$extension";
  anchor.click();
  anchor.remove();
}

String getFileNameWithoutExtension(PlatformFile file) {
  String fileName = file.name;
  String? extension = file.extension;
  if (extension != null) {
    int index = fileName.lastIndexOf(RegExp(".$extension"));
    fileName = fileName.replaceFirst(RegExp(".$extension"), "", index);
  }
  return fileName;
}

String readAsString(PlatformFile file) {
  Uint8List fileBytes = file.bytes!;
  return utf8.decode(fileBytes);
}

bool xmlContainsAll(XmlNode xml, List<String> xpathList) {
  for (String path in xpathList) {
    XmlNode? result = xml.xpath(path).firstOrNull;
    if (result == null) {
      return false;
    }
  }
  return true;
}

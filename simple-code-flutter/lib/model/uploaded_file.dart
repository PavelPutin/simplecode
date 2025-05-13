import 'package:flutter/foundation.dart';

class UploadedFile {
  static const maxFileSize = 20 * 1024 * 1024 * 1024; // 20 MiB

  String name;
  int sizeBytes;
  Uint8List? value;

  UploadedFile(this.name, this.sizeBytes, {this.value});
}
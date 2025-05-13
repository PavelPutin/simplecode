import 'package:flutter/foundation.dart';
import 'package:simple_code/model/task.dart';

import 'data_size.dart';

class UploadedFile {
  static const maxFileSize = 20 * 1024 * 1024; // 20 MiB

  String name;
  DataSize sizeBytes;
  Uint8List? value;
  Task? _task;

  UploadedFile({
    required this.name,
    required this.sizeBytes,
    this.value
  });

  bool get isValidSize => sizeBytes.value < maxFileSize;

  set task(Task? value) {
    task = value;
  }

  Task? get task => _task;

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is UploadedFile &&
          runtimeType == other.runtimeType &&
          name == other.name;

  @override
  int get hashCode =>
      Object.hash(runtimeType, name);
}
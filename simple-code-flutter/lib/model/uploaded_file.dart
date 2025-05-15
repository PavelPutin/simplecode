import 'package:flutter/foundation.dart';
import 'package:simple_code/model/convertation_result.dart';
import 'package:simple_code/model/task.dart';

import 'data_size.dart';

class UploadedFile {
  static const maxFileSize = 20 * 1024 * 1024; // 20 MiB

  String name;
  DataSize sizeBytes;
  Uint8List? value;
  Future<ConvertationResult?>? _converting;
  ConvertationResult? _task;

  UploadedFile({
    required this.name,
    required this.sizeBytes,
  });

  bool get isValidSize => sizeBytes.value < maxFileSize;

  bool get isConverted => _task != null;

  set task(ConvertationResult? value) {
    _task = value;
  }

  Future<ConvertationResult?>? get converting => _converting;

  set converting(Future<ConvertationResult?>? value) {
    _converting = value;
  }

  ConvertationResult? get task => _task;

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
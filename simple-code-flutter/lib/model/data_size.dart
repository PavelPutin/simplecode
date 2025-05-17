import 'package:simple_code/model/utils.dart';

import 'data_size_suffix.dart';

class DataSize {
  final int value;

  DataSize(this.value);

  factory DataSize.fromValueAndDataSize(int value, DataSizeSuffix suffix) {
    return DataSize(value * suffix.power);
  }

  @override
  String toString() {
    return formatBytes(value);
  }

  @override
  bool operator ==(Object other) {
    if (other is DataSize) {
      return value == other.value;
    }
    return false;
  }

  @override
  int get hashCode => value.hashCode;
}
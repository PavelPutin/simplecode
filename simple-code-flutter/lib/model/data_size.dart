import 'package:simple_code/model/utils.dart';

class DataSize {
  final int value;

  DataSize(this.value);

  @override
  String toString() {
    return formatBytes(value);
  }
}
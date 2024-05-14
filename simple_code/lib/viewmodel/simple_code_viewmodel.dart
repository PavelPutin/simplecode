import 'dart:convert';
import 'dart:js_interop';
import 'dart:typed_data';
import 'dart:html' as html;

import 'package:file_picker/file_picker.dart';
import 'package:flutter/cupertino.dart';
import 'package:simple_code/model/task.dart';
import 'package:yaml/yaml.dart';

import '../model/testcase.dart';

class SimpleCodeViewModel extends ChangeNotifier {
  Task _task = Task("", "", "", "", [], {});

  Task get task => _task;

  String? _fileNameWithoutExtension;

  String get FileName => _fileNameWithoutExtension ?? _task.name;

  String _yamlData = "";

  String get YamlData => _yamlData;

  String _moodleXmlData = "";

  String get MoodleXmlData => _moodleXmlData;

  /// throws
  /// YamlException
  /// Exception
  Future<void> openYamlFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();
    if (result != null) {
      String fileName = result.files.first.name;
      String? extension = result.files.first.extension;

      Uint8List fileBytes = result.files.first.bytes!;
      String input = utf8.decode(fileBytes);

      YamlMap data = loadYaml(input);
      if (!validYamlTaskFile(data)) {
        throw Exception("invalid yaml format");
      }

      if (extension != null) {
        int index = fileName.lastIndexOf(RegExp(".$extension"));
        fileName = fileName.replaceFirst(RegExp(".$extension"), "", index);
      }
      _fileNameWithoutExtension = fileName;

      _yamlData = input;

      _task.name = data["name"].toString().trim();
      _task.questionText = data["questionText"].toString().trim();
      _task.answer = data["answer"].toString().trim();
      _task.testcases.clear();

      for (YamlMap testcase in data["testcases"]) {
        _task.testcases.add(Testcase(testcase["stdin"].toString().trim(),
            testcase["expected"].toString().trim()));
      }
      notifyListeners();
    }
  }

  Future<void> downloadYamlFile() async {
    final base64 = base64Encode(utf8.encode(YamlData));
    final anchor = html.AnchorElement(
        href: 'data:application/octet-stream;base64,$base64');
    anchor.download = "$FileName.yaml";
    anchor.click();
    anchor.remove();
  }

  bool validYamlTaskFile(YamlMap data) {
    bool valid = data.containsKey("name") &&
        data.containsKey("questionText") &&
        data.containsKey("answer") &&
        data.containsKey("testcases");

    valid &= data["testcases"] is YamlList;
    var testcases = data["testcases"] as YamlList;
    valid &= testcases.isNotEmpty;
    for (YamlMap testcase in testcases) {
      valid &=
          testcase.containsKey("stdin") && testcase.containsKey("expected");
    }
    return valid;
  }
}

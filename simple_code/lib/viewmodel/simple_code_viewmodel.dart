import 'package:file_picker/file_picker.dart';
import 'package:flutter/cupertino.dart';
import 'package:simple_code/model/task.dart';
import 'package:xml/xml.dart';
import 'package:xml/xpath.dart';
import 'package:yaml/yaml.dart';

import '../model/utils.dart';
import '../model/testcase.dart';

class SimpleCodeViewModel extends ChangeNotifier {
  final Task _task = Task("", "", "", "", [], {});

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
      String input = readAsString(result.files.first);

      YamlMap data = loadYaml(input);
      if (!validYamlTaskFile(data)) {
        throw Exception("invalid yaml format");
      }

      _fileNameWithoutExtension =
          getFileNameWithoutExtension(result.files.first);

      _yamlData = input;

      _task.name = data["name"].toString().trim();
      _task.questionText = data["questionText"].toString().trim();
      _task.defaultGrade = data["defaultGrade"];
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
    downloadFile(FileName, "yaml", YamlData);
  }

  bool validYamlTaskFile(YamlMap data) {
    bool valid = data.containsKey("name") &&
        data.containsKey("defaultGrade") &&
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

  /// throws
  /// XmlException
  /// Exception
  Future<void> openXmlFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();
    if (result != null) {
      String input = readAsString(result.files.first);

      XmlDocument data = XmlDocument.parse(input);
      if (!validXmlTaskFile(data)) {
        throw Exception("invalid yaml format");
      }

      _fileNameWithoutExtension =
          getFileNameWithoutExtension(result.files.first);

      _moodleXmlData = input;

      _task.name =
          data.xpath("/quiz/question/name/text").first.innerText.trim();
      _task.questionText =
          data.xpath("/quiz/question/questiontext/text").first.innerText.trim();
      _task.defaultGrade =
          data.xpath("/quiz/question/defaultgrade").first.innerText.trim();
      _task.answer =
          data.xpath("/quiz/question/answer").first.innerText.trim();
      _task.testcases.clear();

      for (XmlNode testcase in data.xpath("/quiz/question/testcases/testcase")) {
        _task.testcases.add(Testcase(
            testcase.xpath("stdin/text").first.innerText,
            testcase.xpath("expected/text").first.innerText));
      }
      notifyListeners();
    }
  }

  Future<void> downloadXmlFile() async {
    downloadFile(FileName, "xml", MoodleXmlData);
  }

  bool validXmlTaskFile(XmlDocument data) {
    bool valid = xmlContainsAll(data, [
      "/quiz/question/name/text",
      "/quiz/question/questiontext/text",
      "/quiz/question/defaultgrade",
      "/quiz/question/answer",
      "/quiz/question/testcases",
      "/quiz/question/testcases/testcase"
    ]);
    for (XmlNode node in data.xpath("/quiz/question/testcases/testcase")) {
      valid &= xmlContainsAll(node, ["stdin/text", "expected/text"]);
    }
    return valid;
  }
}

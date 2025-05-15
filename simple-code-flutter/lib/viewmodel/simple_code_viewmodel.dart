import 'dart:collection';
import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_dropzone/flutter_dropzone.dart';
import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart';
import 'package:simple_code/model/available_language.dart';
import 'package:simple_code/model/convertation_result.dart';
import 'package:simple_code/model/task.dart';
import 'package:xml/xml.dart';
import 'package:xml/xpath.dart';
import 'package:yaml/yaml.dart';
import 'package:yaml_writer/yaml_writer.dart';

import '../model/data_size.dart';
import '../model/testcase.dart';
import '../model/uploaded_file.dart';
import '../model/utils.dart';

class SimpleCodeViewModel extends ChangeNotifier {
  static const String emptyDaraPlaceholder = "Нет данных";

  final List<UploadedFile> uploadedFiles = [];

  Future<void> uploadFile(DropzoneFileInterface file, DropzoneViewController controller) async {
    final name = await controller.getFilename(file);
    final size = await controller.getFileSize(file);
    final uploadedFile = UploadedFile(name: name, sizeBytes: DataSize(size));
    if (uploadedFiles.contains(uploadedFile)) {
      return;
    }
    final Uint8List? bytes;
    if (size < UploadedFile.maxFileSize) {
      bytes = await controller.getFileData(file);
    } else {
      bytes = null;
    }
    uploadedFile.value = bytes;
    uploadedFiles.add(uploadedFile);
    notifyListeners();
  }

  final Task _task = Task("", "", "", "", [Testcase("", "", true)], {});

  Task get task => _task;

  set task(Task value) {
    _task.name = value.name;
    _task.questionText = value.questionText;
    _task.answer = value.answer;
    _task.testcases = value.testcases;
    _task.testGenerator = value.testGenerator;
  }

  set questionText(String? value) {
    task.questionText = value ?? "";
    _updateXmlData();
    _updateYamlData();
    notifyListeners();
  }

  int _generatedTestsAmount = 1;

  int get generatedTestsAmount => _generatedTestsAmount;

  set generatedTestsAmount(int value) => _generatedTestsAmount = value;

  List<Testcase> _generatedTests = [];

  UnmodifiableListView<Testcase> get generatedTests => UnmodifiableListView(_generatedTests);

  AvailableLanguage _answerLanguage = AvailableLanguage.java;

  AvailableLanguage get answerLanguage => _answerLanguage;

  set answerLanguage(AvailableLanguage value) => _answerLanguage = value;

  AvailableLanguage _testGeneratorLanguage = AvailableLanguage.java;

  AvailableLanguage get testGeneratorLanguage => _testGeneratorLanguage;

  set testGeneratorLanguage(AvailableLanguage value) => _testGeneratorLanguage = value;

  int _showingIndex = 0;

  int get showingIndex => _showingIndex;

  set showingIndex(int value) {
    if (value < 0 || value > 1) {
      throw ArgumentError("Showing index must be 0 or 1");
    }
    _showingIndex = value;
    notifyListeners();
  }

  bool _showingQuestionTextHtmlEditor = true;

  bool get showingQuestionTextHtmlEditor => _showingQuestionTextHtmlEditor;

  set showingQuestionTextHtmlEditor(bool value) {
    _showingQuestionTextHtmlEditor = value;
    notifyListeners();
  }

  bool get showingQuestionTextPreview => !_showingQuestionTextHtmlEditor;

  set showingQuestionTextPreview(bool value) {
    _showingQuestionTextHtmlEditor = !value;
    notifyListeners();
  }

  final List<String> _errorMessages = [];

  UnmodifiableListView<String> get errorMessages => UnmodifiableListView(_errorMessages);

  String _fileNameWithoutExtension = "";

  String get fileName {
    if (_fileNameWithoutExtension.isNotEmpty) {
      return _fileNameWithoutExtension;
    }
    if (_task.name.isNotEmpty) {
      return _task.name;
    }
    return "task";
  }

  String _yamlData = emptyDaraPlaceholder;

  String get yamlData => _yamlData;

  set yamlData(String value) {
    _yamlData = value;
    notifyListeners();
  }

  String _moodleXmlData = emptyDaraPlaceholder;

  String get moodleXmlData => _moodleXmlData;

  String? _activeImage = null;
  String? _activeImageName = null;

  set activeImageName(String? value) {
    _activeImageName = value;
    _activeImage = task.images[value];
    notifyListeners();
  }

  String? get activeImageName => _activeImageName;

  String? get activeImage => _activeImage;

  set moodleXmlData(String value) {
    _moodleXmlData = value;
    notifyListeners();
  }

  bool _showingTaskForm = true;

  bool get showingTaskForm => _showingTaskForm;

  set showingTaskForm(bool value) {
    _showingTaskForm = value;
    notifyListeners();
  }

  bool get showingMultiFileConverter => !_showingTaskForm;

  set showingMultiFileConverter(bool value) {
    _showingTaskForm = !value;
    notifyListeners();
  }

  void updateTestCaseShow(int number, bool value) {
    task.testcases[number].show = value;
    notifyListeners();
    _updateYamlData();
    _updateXmlData();
  }

  void deleteTestCaseImage(String name) {
    if (name == activeImageName) {
      activeImageName = null;
    }
    task.images.remove(name);
    notifyListeners();
    _updateYamlData();
    _updateXmlData();
  }

  Future<void> generateTask() async {
    var requestTask = _task.toJson();
    requestTask.remove("images");
    Map<String, dynamic> request = {
      "answerLanguage": answerLanguage.jobeLanguageId,
      "testGeneratorLanguage": testGeneratorLanguage.jobeLanguageId,
      "generatedTestsAmount": generatedTestsAmount,
      "task": requestTask
    };

    final response = await http.post(Uri.parse("http://localhost:8080/v1/runs"),
        headers: <String, String>{'Content-Type': 'application/json; charset=UTF-8'}, body: jsonEncode(request));

    if (response.statusCode == 200) {
      var body = jsonDecode(response.body) as Map<String, dynamic>;

      _generatedTests.clear();
      for (Map<String, dynamic> testcase in body["testcases"]) {
        var stdin = testcase["stdin"];
        var expected = testcase["expected"];
        _generatedTests.add(Testcase(stdin, expected, true));
      }

      if (_generatedTests.isNotEmpty) {
        _updateYamlData();
        _updateXmlData();
      }

      _errorMessages.clear();
      for (var error in body["errors"]) {
        _errorMessages.add(error);
      }

      notifyListeners();
    }
  }

  void _updateYamlData() {
    var writer = YamlWriter();
    _yamlData = writer.write(_task);
  }

  void _updateXmlData() {
    moodleXmlData = createXmlDocument(_task, _generatedTests);
  }

  String createXmlDocument(Task sourceTask, List<Testcase> sourceGeneratedTests) {
    final builder = XmlBuilder();
    builder.processing("xml", "version=\"1.0\"");
    builder.element("quiz", nest: () {
      builder.element("question", attributes: {"type": "coderunner"}, nest: () {
        builder.element("name", nest: () {
          builder.element("text", nest: () {
            builder.text(sourceTask.name);
          });
        });
        builder.element("questiontext", attributes: {"format": "html"}, nest: () {
          builder.element("text", nest: () {
            builder.cdata(sourceTask.questionText);
          });
          for (MapEntry<String, String> image in sourceTask.images.entries) {
            builder.element("file", attributes: {"name": image.key, "path": "/", "encoding": "base64"}, nest: () {
              builder.text(image.value);
            });
          }
        });
        builder.element("generalfeedback", attributes: {"format": "html"}, nest: () {
          builder.element("text");
        });
        builder.element("defaultgrade", nest: () {
          builder.text(int.parse(sourceTask.defaultGrade != "" ? sourceTask.defaultGrade : "1"));
        });
        builder.element("penalty", nest: () {
          builder.text(0);
        });
        builder.element("hidden", nest: () {
          builder.text(0);
        });
        builder.element("idnumber");
        builder.element("coderunnertype", nest: () {
          builder.text("multilanguage");
        });
        builder.element("prototypetype", nest: () {
          builder.text(0);
        });
        builder.element("allornothing", nest: () {
          builder.text(1);
        });
        builder.element("penaltyregime", nest: () {
          builder.text("10, 20, ...");
        });
        builder.element("precheck", nest: () {
          builder.text(0);
        });
        builder.element("hidecheck", nest: () {
          builder.text(0);
        });
        builder.element("showsource", nest: () {
          builder.text(0);
        });
        builder.element("answerboxlines", nest: () {
          builder.text(18);
        });
        builder.element("answerboxcolumns", nest: () {
          builder.text(100);
        });
        builder.element("answerpreload");
        builder.element("globalextra");
        builder.element("useace");
        builder.element("resultcolumns");
        builder.element("template");
        builder.element("iscombinatortemplate");
        builder.element("allowmultiplestdins");
        builder.element("answer", nest: () {
          builder.cdata(sourceTask.answer);
        });
        builder.element("validateonsave", nest: () {
          builder.text(1);
        });
        builder.element("testsplitterre");
        builder.element("language");
        builder.element("acelang");
        builder.element("sandbox");
        builder.element("grader");
        builder.element("cputimelimitsecs");
        builder.element("memlimitmb");
        builder.element("sandboxparams");
        builder.element("templateparams");
        builder.element("hoisttemplateparams", nest: () {
          builder.text(1);
        });
        builder.element("extractcodefromjson", nest: () {
          builder.text(1);
        });
        builder.element("templateparamslang", nest: () {
          builder.text("None");
        });
        builder.element("templateparamsevalpertry", nest: () {
          builder.text("0");
        });
        builder.element("templateparamsevald", nest: () {
          builder.text("{}");
        });
        builder.element("templateparamsevald", nest: () {
          builder.text(0);
        });
        builder.element("uiplugin");
        builder.element("uiparameters");
        builder.element("attachments", nest: () {
          builder.text(0);
        });
        builder.element("attachmentsrequired", nest: () {
          builder.text(0);
        });
        builder.element("maxfilesize", nest: () {
          builder.text(10240);
        });
        builder.element("filenamesregex");
        builder.element("filenamesexplain");
        builder.element("displayfeedback", nest: () {
          builder.text(1);
        });
        builder.element("giveupallowed", nest: () {
          builder.text(0);
        });
        builder.element("prototypeextra");
        builder.element("testcases", nest: () {
          for (var testcase in sourceTask.testcases) {
            builder.element("testcase", nest: () {
              builder.attribute("testtype", "0");
              builder.attribute("useasexample", testcase.show ? "1" : "0");
              builder.attribute("hiderestiffail", "0");
              builder.attribute("mark", "1.000000");

              builder.element("testcode", nest: () {
                builder.element("text");
              });
              builder.element("stdin", nest: () {
                builder.element("text", nest: () {
                  builder.text(testcase.stdin);
                });
              });
              builder.element("expected", nest: () {
                builder.element("text", nest: () {
                  builder.text(testcase.expected);
                });
              });
              builder.element("extra", nest: () {
                builder.element("text");
              });
              builder.element("display", nest: () {
                builder.element("text", nest: () {
                  builder.text("SHOW");
                });
              });
            });
          }

          for (var testcase in sourceGeneratedTests) {
            builder.element("testcase", nest: () {
              builder.attribute("testtype", "0");
              builder.attribute("useasexample", "0");
              builder.attribute("hiderestiffail", "0");
              builder.attribute("mark", "1.000000");

              builder.element("testcode", nest: () {
                builder.element("text");
              });
              builder.element("stdin", nest: () {
                builder.element("text", nest: () {
                  builder.text(testcase.stdin);
                });
              });
              builder.element("expected", nest: () {
                builder.element("text", nest: () {
                  builder.text(testcase.expected);
                });
              });
              builder.element("extra", nest: () {
                builder.element("text");
              });
              builder.element("display", nest: () {
                builder.element("text", nest: () {
                  builder.text("HIDE");
                });
              });
            });
          }
        });
      });
    });

    final document = builder.buildDocument();
    return document.toXmlString(pretty: true, indent: "    ");
  }

  /// throws
  /// YamlException
  /// Exception
  Future<void> openYamlFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();
    if (result != null) {
      String input = readAsString(result.files.first);

      try {
        YamlMap data = loadYaml(input);
        if (!validYamlTaskFile(data)) {
          _errorMessages.add("Недопустимый формат yaml файла");
          notifyListeners();
          return;
        }

        _fileNameWithoutExtension = getFileNameWithoutExtension(result.files.first);

        _yamlData = input;

        _task.name = data["name"].toString().trim();
        _task.questionText = data["questionText"].toString().trim();
        _task.defaultGrade = data["defaultGrade"].toString();
        _task.answer = data["answer"].toString().trim();
        if (data.containsKey("testGenerator") && data["testGenerator"].containsKey("customCode")) {
          _task.testGenerator["customCode"] = data["testGenerator"]["customCode"].toString();
        }
        _task.testcases.clear();

        for (YamlMap testcase in data["testcases"]) {
          _task.testcases.add(Testcase(testcase["stdin"].toString().trim(), testcase["expected"].toString().trim(), true));
        }
        _showingIndex = 0;
        _updateXmlData();
        notifyListeners();
      } catch (e) {
        _errorMessages.add("Не удалось загрузить файл");
        notifyListeners();
        return;
      }
    }
  }

  Future<void> openPolygonFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();
    if (result == null) {
      return;
    }
    try {
      var converted = await convertPolygonFile(result.files.first.name, result.files.first.bytes);

      task = converted.task;
      _answerLanguage = converted.answerLanguage;

      _errorMessages.clear();
      _fileNameWithoutExtension = getFileNameWithoutExtension(result.files.first);
      _showingIndex = 1;
      _updateYamlData();
      _updateXmlData();
      notifyListeners();
    } catch (e) {
      _errorMessages.add(e.toString());
      _errorMessages.add("Не удалось загрузить файл");
      notifyListeners();
      return;
    }
  }

  Future<void> convertUploadedFiles() async {
    final convertingFiles = uploadedFiles.where((v) => v.isValidSize && !v.isConverted);
    for (var file in convertingFiles) {
      file.converting = convertPolygonFile(file.name, file.value);
      var result = await file.converting;
      file.converting = null;
      file.task = result;
      notifyListeners();
    }
  }

  Future<ConvertationResult> convertPolygonFile(String fileName, Uint8List? bytes) async {
    final request = http.MultipartRequest("POST", Uri.parse("http://localhost:8080/v1/polygon-converter"));
    request.files.add(http.MultipartFile.fromBytes("package", bytes!.toList(), contentType: MediaType("multipart", "form-data"), filename: fileName));

    var streamedResponse = await request.send();
    var response = await http.Response.fromStream(streamedResponse);
    if (response.statusCode != 200) {
      throw HttpException('${response.statusCode}');
    }

    var body = jsonDecode(const Utf8Decoder().convert(response.body.codeUnits)) as Map<String, dynamic>;

    String name = body["problem"]["name"];
    String questionText = body["problem"]["statement"];
    Map<String, String> images = {};
    var responseImages = body["problem"]["images"] as List<dynamic>;
    for (var image in responseImages) {
      images[image["name"]] = image["base64Data"];
    }
    String defaultGrade = "1";
    String answer = body["problem"]["mainSolution"]["content"];
    AvailableLanguage answerLanguage = AvailableLanguage.fromJobeLanguageId(body["problem"]["mainSolution"]["language"] as String);

    List<Testcase> testcases = [];
    var responseTestCases = body["problem"]["testCases"] as List<dynamic>;
    for (Map<String, dynamic> testcase in responseTestCases) {
      var stdin = testcase["stdin"];
      var expected = testcase["expected"];
      var show = testcase["display"];
      testcases.add(Testcase(stdin, expected, show));
    }

    var task = Task(name, questionText, defaultGrade, answer, testcases, {"customCode": ""});
    return ConvertationResult(task, answerLanguage);
  }

  Future<void> downloadYamlFile() async {
    downloadFile(fileName, "yaml", yamlData);
  }

  bool validYamlTaskFile(YamlMap data) {
    _errorMessages.clear();

    bool valid = _checkYamlProperty(data, "name") &&
        _checkYamlProperty(data, "questionText") &&
        _checkYamlProperty(data, "defaultGrade") &&
        _checkYamlProperty(data, "answer") &&
        _checkYamlProperty(data, "testcases");

    bool isYamlList = data["testcases"] is YamlList;
    if (!isYamlList) {
      _errorMessages.add("testcases не является массивом");
    }
    valid &= isYamlList;

    if (isYamlList) {
      var testcases = data["testcases"] as YamlList;

      bool notEmpty = testcases.isNotEmpty;
      if (!notEmpty) {
        _errorMessages.add("testcases пуст");
      }
      valid &= notEmpty;

      int i = 1;
      for (YamlMap testcase in testcases) {
        valid &= _checkYamlProperty(testcase, "stdin", message: "Тест $i не содержит свойство stdin") &&
            _checkYamlProperty(testcase, "expected", message: "Тест $i не содержит свойство expected");
        i++;
      }
    }
    notifyListeners();
    return valid;
  }

  bool _checkYamlProperty(YamlMap data, String name, {String? message}) {
    if (!data.containsKey(name)) {
      _errorMessages.add(message ?? "Не найдено свойство $name");
      return false;
    }
    return true;
  }

  /// throws
  /// XmlException
  /// Exception
  Future<void> openXmlFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();
    if (result != null) {
      String input = readAsString(result.files.first);

      try {
        XmlDocument data = XmlDocument.parse(input);
        if (!validXmlTaskFile(data)) {
          _errorMessages.add("Недопустимый формат xml файла");
          notifyListeners();
          return;
        }

        _fileNameWithoutExtension = getFileNameWithoutExtension(result.files.first);

        _moodleXmlData = input;

        _task.name = data.xpath("/quiz/question/name/text").first.innerText.trim();
        _task.questionText = data.xpath("/quiz/question/questiontext/text").first.innerText.trim();
        _task.defaultGrade = data.xpath("/quiz/question/defaultgrade").first.innerText.trim();
        _task.answer = data.xpath("/quiz/question/answer").first.innerText.trim();
        _task.testcases.clear();

        for (XmlNode testcase in data.xpath("/quiz/question/testcases/testcase")) {
          _task.testcases.add(Testcase(testcase.xpath("stdin/text").first.innerText, testcase.xpath("expected/text").first.innerText, true));
        }
        _showingIndex = 1;
        _updateYamlData();
        notifyListeners();
      } catch (e) {
        _errorMessages.add("Не удалось загрузить файл");
        notifyListeners();
        return;
      }
    }
  }

  Future<void> downloadXmlFile() async {
    downloadFile(fileName, "xml", moodleXmlData);
  }

  bool validXmlTaskFile(XmlDocument data) {
    _errorMessages.clear();

    bool valid = _checkXmlProperty(data, "/quiz/question/name/text") &&
        _checkXmlProperty(data, "/quiz/question/questiontext/text") &&
        _checkXmlProperty(data, "/quiz/question/defaultgrade") &&
        _checkXmlProperty(data, "/quiz/question/answer") &&
        _checkXmlProperty(data, "/quiz/question/testcases");

    var testcases = data.xpath("/quiz/question/testcases/testcase");
    bool notEmpty = testcases.isNotEmpty;
    if (!notEmpty) {
      _errorMessages.add("testcases пуст");
    }
    valid &= notEmpty;

    int i = 1;
    for (XmlNode node in testcases) {
      valid &= _checkXmlProperty(node, "stdin/text", message: "Тест $i не содержит свойство stdin/text") &&
          _checkXmlProperty(node, "expected/text", message: "Тест $i не содержит свойство expected/text");
      i++;
    }

    notifyListeners();
    return valid;
  }

  bool _checkXmlProperty(XmlNode xml, String name, {String? message}) {
    XmlNode? result = xml.xpath(name).firstOrNull;
    if (result == null) {
      _errorMessages.add(message ?? "Не найдено свойство $name");
      return false;
    }
    return true;
  }

  void selectImage(String imageName) {
    activeImageName = imageName;
  }
}

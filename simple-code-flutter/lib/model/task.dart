import 'dart:collection';

import 'package:json_annotation/json_annotation.dart';
import 'package:simple_code/model/testcase.dart';

part 'task.g.dart';

@JsonSerializable(explicitToJson: true)
class Task {
  String name;
  String questionText;
  String defaultGrade;
  String answer;
  List<Testcase> testcases;
  Map<String, dynamic> testGenerator;
  Map<String, String> images = HashMap();

  Task(this.name, this.questionText, this.defaultGrade, this.answer, this.testcases, this.testGenerator);

  factory Task.fromJson(Map<String, dynamic> json) => _$TaskFromJson(json);

  Map<String, dynamic> toJson() => _$TaskToJson(this);
}

import 'package:json_annotation/json_annotation.dart';

part 'testcase.g.dart';

@JsonSerializable()
class Testcase {
  String stdin;
  String expected;
  bool show;

  Testcase(this.stdin, this.expected, this.show);

  factory Testcase.fromJson(Map<String, dynamic> json) => _$TestcaseFromJson(json);

  Map<String, dynamic> toJson() => _$TestcaseToJson(this);
}

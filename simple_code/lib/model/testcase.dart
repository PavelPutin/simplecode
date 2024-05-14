import 'package:json_annotation/json_annotation.dart';

part 'testcase.g.dart';

@JsonSerializable()
class Testcase {
  final String stdin;
  final String expected;

  Testcase(this.stdin, this.expected);

  factory Testcase.fromJson(Map<String, dynamic> json) => _$TestcaseFromJson(json);

  Map<String, dynamic> toJson() => _$TestcaseToJson(this);
}
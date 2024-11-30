// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Task _$TaskFromJson(Map<String, dynamic> json) => Task(
      json['name'] as String,
      json['questionText'] as String,
      json['defaultGrade'] as String,
      json['answer'] as String,
      (json['testcases'] as List<dynamic>)
          .map((e) => Testcase.fromJson(e as Map<String, dynamic>))
          .toList(),
      json['testGenerator'] as Map<String, dynamic>,
    );

Map<String, dynamic> _$TaskToJson(Task instance) => <String, dynamic>{
      'name': instance.name,
      'questionText': instance.questionText,
      'defaultGrade': instance.defaultGrade,
      'answer': instance.answer,
      'testcases': instance.testcases.map((e) => e.toJson()).toList(),
      'testGenerator': instance.testGenerator,
    };

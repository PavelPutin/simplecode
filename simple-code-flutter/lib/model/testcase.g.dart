// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'testcase.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Testcase _$TestcaseFromJson(Map<String, dynamic> json) => Testcase(
      json['stdin'] as String,
      json['expected'] as String,
      json['show'] as bool,
    );

Map<String, dynamic> _$TestcaseToJson(Testcase instance) => <String, dynamic>{
      'stdin': instance.stdin,
      'expected': instance.expected,
      'show': instance.show,
    };

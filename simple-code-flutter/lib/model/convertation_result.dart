import 'package:simple_code/model/available_language.dart';
import 'package:simple_code/model/task.dart';

class ConvertationResult {
  final Task task;
  final AvailableLanguage answerLanguage;

  ConvertationResult(this.task, this.answerLanguage);
}
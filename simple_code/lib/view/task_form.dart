import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_code_editor/flutter_code_editor.dart';
import 'package:flutter_quill/flutter_quill.dart';
import 'package:highlight/highlight.dart';
import 'package:highlight/languages/java.dart';
import 'package:re_editor/re_editor.dart';

class TaskForm extends StatefulWidget {
  const TaskForm({super.key});

  @override
  State<TaskForm> createState() => _TaskFormState();
}

class _TaskFormState extends State<TaskForm> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController nameController = TextEditingController();

  // final TextEditingController questionTextController = TextEditingController();
  final QuillController questionTextController = QuillController.basic();
  bool questionTextEmpty = false;
  final TextEditingController gradeController = TextEditingController();
  Mode language = java;

  // final CodeController answerController = CodeController();
  final CodeLineEditingController answerController =
      CodeLineEditingController();
  bool answerEmpty = false;

  int testsNumber = 1;
  late final List<TextEditingController> testStdinControllers;
  late final List<TextEditingController> testExpectedControllers;

  @override
  void initState() {
    super.initState();
    // answerController.language = language;

    testStdinControllers =
        List.filled(testsNumber, TextEditingController(), growable: true);
    testExpectedControllers =
        List.filled(testsNumber, TextEditingController(), growable: true);
  }

  @override
  void dispose() {
    super.dispose();
    nameController.dispose();
    questionTextController.dispose();
    gradeController.dispose();
    answerController.dispose();
    for (var element in testStdinControllers) {
      element.dispose();
    }
    for (var element in testExpectedControllers) {
      element.dispose();
    }
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        mainAxisSize: MainAxisSize.max,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Card(
            child: Form(
              key: _formKey,
              child: Column(
                children: <Widget>[
                  NameTextField(nameController: nameController),
                  Container(
                    decoration: _boxWithValidation(questionTextEmpty),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text("Условие задачи*",
                            style: Theme.of(context)
                                .textTheme
                                .bodyMedium
                                ?.copyWith(
                                    color: !questionTextEmpty
                                        ? Theme.of(context)
                                            .textTheme
                                            .bodyMedium
                                            ?.color
                                        : Theme.of(context).colorScheme.error)),
                        if (questionTextEmpty)
                          Text("Обязательное поле",
                              style: Theme.of(context)
                                  .textTheme
                                  .bodySmall
                                  ?.copyWith(
                                      color:
                                          Theme.of(context).colorScheme.error)),
                        QuillToolbar.simple(
                          configurations: QuillSimpleToolbarConfigurations(
                            controller: questionTextController,
                            sharedConfigurations:
                                const QuillSharedConfigurations(
                              locale: Locale('ru'),
                            ),
                          ),
                        ),
                        ConstrainedBox(
                          constraints: const BoxConstraints(minHeight: 100),
                          child: Container(
                            color: Colors.white,
                            child: QuillEditor.basic(
                              configurations: QuillEditorConfigurations(
                                controller: questionTextController,
                                sharedConfigurations:
                                    const QuillSharedConfigurations(
                                  locale: Locale('ru'),
                                ),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  TextFormField(
                    controller: gradeController,
                    decoration: const InputDecoration(
                        border: OutlineInputBorder(), labelText: "Оценка*"),
                    validator: (value) {
                      if (value == null || value.trim().isEmpty) {
                        return "Обязательное поле";
                      }
                      if (int.tryParse(value) == null) {
                        return "Оценка должна быть целым числом";
                      }

                      if (int.parse(value) <= 0) {
                        return "Оценка должан быть больше нуля";
                      }

                      return null;
                    },
                  ),
                  Container(
                    decoration: _boxWithValidation(answerEmpty),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text("Ответ (программа, решающая задачу)*",
                            style: Theme.of(context)
                                .textTheme
                                .bodyMedium
                                ?.copyWith(
                                color: !answerEmpty
                                    ? Theme.of(context)
                                    .textTheme
                                    .bodyMedium
                                    ?.color
                                    : Theme.of(context).colorScheme.error)),
                        if (answerEmpty)
                          Text("Обязательное поле",
                              style: Theme.of(context)
                                  .textTheme
                                  .bodySmall
                                  ?.copyWith(
                                  color:
                                  Theme.of(context).colorScheme.error)),
                        ConstrainedBox(
                          constraints: const BoxConstraints(maxHeight: 500),
                          child: CodeEditor(
                            controller: answerController,
                            indicatorBuilder: (context, editingController,
                                chunkController, notifier) {
                              return Row(
                                children: [
                                  DefaultCodeLineNumber(
                                    controller: editingController,
                                    notifier: notifier,
                                  ),
                                  DefaultCodeChunkIndicator(
                                      width: 20,
                                      controller: chunkController,
                                      notifier: notifier)
                                ],
                              );
                            },
                          ),
                        ),
                      ],
                    ),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      setState(() {
                        questionTextEmpty = false;
                        answerEmpty = false;
                      });
                      if (_formKey.currentState!.validate() &&
                          !questionTextController.document.isEmpty() &&
                          !answerController.isEmpty) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                              duration: Duration(seconds: 1),
                              content: Text('Данные отправлены')),
                        );
                      }

                      if (questionTextController.document.isEmpty()) {
                        setState(() => questionTextEmpty = true);
                      }

                      if (answerController.isEmpty) {
                        setState(() => answerEmpty = true);
                      }
                    },
                    child: const Text('Создать задачу'),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  BoxDecoration _boxWithValidation(bool predicateValue) {
    return BoxDecoration(
        border: Border.all(
            width: 1,
            color: !predicateValue
                ? Theme.of(context).colorScheme.outline
                : Theme.of(context).colorScheme.error),
        borderRadius: const BorderRadius.all(Radius.circular(4)));
  }
}

class NameTextField extends StatelessWidget {
  const NameTextField({
    super.key,
    required this.nameController,
  });

  final TextEditingController nameController;

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: nameController,
      decoration: const InputDecoration(
          border: OutlineInputBorder(), labelText: "Название*"),
      validator: (value) {
        if (value == null || value.trim().isEmpty) {
          return "Обязательное поле";
        }
        return null;
      },
    );
  }
}

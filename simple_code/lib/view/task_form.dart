import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_code_editor/flutter_code_editor.dart';
import 'package:flutter_highlight/theme_map.dart';
import 'package:flutter_highlight/themes/monokai.dart';
import 'package:flutter_quill/flutter_quill.dart';
import 'package:highlight/highlight.dart';
import 'package:highlight/languages/java.dart';

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
  final CodeController answerController = CodeController();
  int testsNumber = 1;
  late final List<TextEditingController> testStdinControllers;
  late final List<TextEditingController> testExpectedControllers;

  @override
  void initState() {
    super.initState();
    answerController.language = language;

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
    for (var element in testStdinControllers) {element.dispose();}
    for (var element in testExpectedControllers) {element.dispose();}
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Expanded(
            child: Card(
          child: CustomScrollView(
            slivers: [
              SliverFillRemaining(
                hasScrollBody: false,
                child: Form(
                  key: _formKey,
                  child: Column(
                    children: <Widget>[
                      NameTextField(nameController: nameController),
                      Container(
                        decoration: BoxDecoration(
                            border: Border.all(
                                width: 1,
                                color: !questionTextEmpty
                                    ? Theme.of(context).colorScheme.outline
                                    : Theme.of(context).colorScheme.error),
                            borderRadius:
                                const BorderRadius.all(Radius.circular(4))),
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
                                            : Theme.of(context)
                                                .colorScheme
                                                .error)),
                            if (questionTextEmpty)
                              Text("Условие задачи обязательно",
                                  style: Theme.of(context)
                                      .textTheme
                                      .bodySmall
                                      ?.copyWith(
                                          color: Theme.of(context)
                                              .colorScheme
                                              .error)),
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
                      ElevatedButton(
                        onPressed: () {
                          if (_formKey.currentState!.validate() &&
                              !questionTextController.document.isEmpty()) {
                            setState(() => questionTextEmpty = false);
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                  duration: Duration(seconds: 1),
                                  content: Text('Данные отправлены')),
                            );
                          }

                          if (questionTextController.document.isEmpty()) {
                            setState(() => questionTextEmpty = true);
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
        )),
      ],
    );
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

import 'package:flutter/material.dart';
import 'package:flutter_highlight/themes/monokai.dart';
import 'package:flutter_quill/flutter_quill.dart';
import 'package:provider/provider.dart';
import 'package:re_editor/re_editor.dart';
import 'package:re_highlight/languages/java.dart';
import 'package:re_highlight/languages/c.dart';
import 'package:re_highlight/languages/cpp.dart';
import 'package:re_highlight/languages/node-repl.dart';
import 'package:re_highlight/languages/delphi.dart';
import 'package:re_highlight/languages/php.dart';
import 'package:re_highlight/languages/python.dart';
import 'package:simple_code/model/testcase.dart';
import 'package:simple_code/viewmodel/simple_code_viewmodel.dart';
import 'package:quill_html_converter/quill_html_converter.dart';

import '../model/available_language.dart';

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

  // Mode language = java;

  // final CodeController answerController = CodeController();
  final CodeLineEditingController answerController =
      CodeLineEditingController();
  bool answerEmpty = false;
  AvailableLanguage selectedAnswerLanguage = AvailableLanguage.java;
  final TextEditingController answerLanguageController =
      TextEditingController();

  int testsNumber = 1;
  final List<TextEditingController> testStdinControllers = [];
  final List<TextEditingController> testExpectedControllers = [];
  List<bool> testEmpty = [];

  final CodeLineEditingController testGeneratorController =
      CodeLineEditingController();
  bool testGeneratorEmpty = false;
  AvailableLanguage selectedTestGeneratorLanguage = AvailableLanguage.java;
  final TextEditingController testGeneratorLanguageController =
      TextEditingController();

  @override
  void initState() {
    super.initState();
    // answerController.language = language;
    questionTextController.addListener(() {
      context.read<SimpleCodeViewModel>().task.questionText =
          questionTextController.document.toDelta().toHtml();
    });

    testStdinControllers.clear();
    testExpectedControllers.clear();
    for (int i = 0; i < testsNumber; i++) {
      testStdinControllers.add(TextEditingController());
      testExpectedControllers.add(TextEditingController());
    }
    testEmpty = List.filled(testsNumber, false, growable: true);
  }

  @override
  void dispose() {
    super.dispose();
    nameController.dispose();
    questionTextController.dispose();
    gradeController.dispose();
    answerController.dispose();
    answerLanguageController.dispose();
    for (var element in testStdinControllers) {
      element.dispose();
    }
    for (var element in testExpectedControllers) {
      element.dispose();
    }
  }

  @override
  Widget build(BuildContext context) {
    nameController.text = context.watch<SimpleCodeViewModel>().task.name;
    String question = context.watch<SimpleCodeViewModel>().task.questionText;
    if (question.isNotEmpty) {
      questionTextController.document = Document.fromHtml(question);
    }
    gradeController.text =
        context.watch<SimpleCodeViewModel>().task.defaultGrade;
    answerController.text = context.watch<SimpleCodeViewModel>().task.answer;
    var testcases =
        Provider.of<SimpleCodeViewModel>(context, listen: false).task.testcases;
    if (testcases.isNotEmpty) {
      testsNumber = testcases.length;
    }

    testStdinControllers.clear();
    testExpectedControllers.clear();
    for (int i = 0; i < testsNumber; i++) {
      testStdinControllers.add(TextEditingController());
      testExpectedControllers.add(TextEditingController());

      if (i < testcases.length) {
        testStdinControllers[i].text = testcases[i].stdin;
        testExpectedControllers[i].text = testcases[i].expected;
      }
    }

    testEmpty = List.filled(testsNumber, false, growable: true);

    testGeneratorController.text =
        context.watch<SimpleCodeViewModel>().task.testGenerator["customCode"] ??
            "";

    List<Widget> taskTestcases = [];
    for (int i = 0; i < testsNumber; i++) {
      taskTestcases.add(TestCaseField(
        number: i + 1,
        stdinController: testStdinControllers[i],
        expectedController: testExpectedControllers[i],
        onDelete: (int number) {
          setState(() {
            if (testsNumber == 1) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                    duration: Duration(seconds: 1),
                    content: Text('Нельзя удалить единственный тест')),
              );
            } else {
              testStdinControllers.removeAt(number - 1).dispose();
              testExpectedControllers.removeAt(number - 1).dispose();
              testEmpty.removeAt(number - 1);
              context
                  .read<SimpleCodeViewModel>()
                  .task
                  .testcases
                  .removeAt(number - 1);
              testsNumber--;
            }
          });
        },
        boxDecoration: _boxWithValidation(testEmpty[i]),
        textStyle: _textStyleWithValidation(testEmpty[i]),
      ));
    }

    return SingleChildScrollView(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        mainAxisSize: MainAxisSize.max,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 20),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Container(margin: const EdgeInsets.only(bottom: 30), child: NameTextField(nameController: nameController)),
                    Container(
                      margin: const EdgeInsets.only(bottom: 30),
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
                    Container(
                      margin: const EdgeInsets.only(bottom: 30),
                      child: TextFormField(
                        controller: gradeController,
                        onChanged: (value) {
                          context.read<SimpleCodeViewModel>().task.defaultGrade =
                              value;
                        },
                        decoration: const InputDecoration(
                            border: OutlineInputBorder(), labelText: "Оценка*", filled: true, fillColor: Colors.white),
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
                    ),
                    Container(
                      margin: const EdgeInsets.only(bottom: 30),
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
                          DropdownMenu<AvailableLanguage>(
                            controller: answerLanguageController,
                            initialSelection: AvailableLanguage.java,
                            dropdownMenuEntries: AvailableLanguage.values
                                .map((e) => DropdownMenuEntry(
                                    value: e, label: "${e.name} ${e.version}"))
                                .toList(),
                            onSelected: (value) => setState(() {
                              selectedAnswerLanguage =
                                  value ?? AvailableLanguage.java;
                              if (value == null) {
                                answerLanguageController.value = TextEditingValue(
                                    text:
                                        "${selectedAnswerLanguage.name} ${selectedAnswerLanguage.version}");
                              }
                            }),
                          ),
                          ConstrainedBox(
                            constraints: const BoxConstraints(maxHeight: 500),
                            child: CodeEditor(
                              style: CodeEditorStyle(
                                backgroundColor: Colors.white,
                                  codeTheme: CodeHighlightTheme(languages: {
                                "java": CodeHighlightThemeMode(mode: langJava),
                                "c": CodeHighlightThemeMode(mode: langC)
                              }, theme: monokaiTheme)),
                              wordWrap: false,
                              controller: answerController,
                              onChanged: (CodeLineEditingValue? value) {
                                context.read<SimpleCodeViewModel>().task.answer =
                                    answerController.text;
                              },
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
                    Container(
                      margin: const EdgeInsets.only(bottom: 30),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text("Тестовые данные*"),
                          ...taskTestcases,
                          FilledButton(
                              style: ButtonStyle(
                                  backgroundColor: MaterialStateProperty.all(
                                      const Color(0xff0f6cbf)),
                                  overlayColor: MaterialStateProperty.all(
                                      const Color(0xff0c589c)),
                                  shape: MaterialStateProperty.all(
                                    RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(8.0),
                                    ),
                                  )),
                              onPressed: () {
                                setState(() {
                                  testsNumber++;
                                  testStdinControllers
                                      .add(TextEditingController());
                                  testExpectedControllers
                                      .add(TextEditingController());
                                  testEmpty.add(false);
                                  //todo: move to viewmodel
                                  context
                                      .read<SimpleCodeViewModel>()
                                      .task
                                      .testcases
                                      .add(Testcase("", ""));
                                });
                              },
                              child: const Text("Добавить тест"))
                        ],
                      ),
                    ),
                    Container(
                      margin: const EdgeInsets.only(bottom: 30),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text("Генератор тестов*",
                              style: Theme.of(context)
                                  .textTheme
                                  .bodyMedium
                                  ?.copyWith(
                                      color: !testGeneratorEmpty
                                          ? Theme.of(context)
                                              .textTheme
                                              .bodyMedium
                                              ?.color
                                          : Theme.of(context).colorScheme.error)),
                          if (testGeneratorEmpty)
                            Text("Обязательное поле",
                                style: Theme.of(context)
                                    .textTheme
                                    .bodySmall
                                    ?.copyWith(
                                        color:
                                            Theme.of(context).colorScheme.error)),
                          DropdownMenu<AvailableLanguage>(
                            controller: testGeneratorLanguageController,
                            initialSelection: AvailableLanguage.java,
                            dropdownMenuEntries: AvailableLanguage.values
                                .map((e) => DropdownMenuEntry(
                                    value: e, label: "${e.name} ${e.version}"))
                                .toList(),
                            onSelected: (value) => setState(() {
                              selectedTestGeneratorLanguage =
                                  value ?? AvailableLanguage.java;
                              if (value == null) {
                                testGeneratorLanguageController.value =
                                    TextEditingValue(
                                        text:
                                            "${selectedTestGeneratorLanguage.name} ${selectedTestGeneratorLanguage.version}");
                              }
                            }),
                          ),
                          ConstrainedBox(
                            constraints: const BoxConstraints(maxHeight: 200),
                            child: CodeEditor(
                              style: CodeEditorStyle(
                                backgroundColor: Colors.white,
                                  codeTheme: CodeHighlightTheme(languages: {
                                "java": CodeHighlightThemeMode(mode: langJava),
                                "python": CodeHighlightThemeMode(mode: langPython)
                              }, theme: monokaiTheme)),
                              wordWrap: false,
                              controller: testGeneratorController,
                              onChanged: (CodeLineEditingValue? value) {
                                context
                                        .read<SimpleCodeViewModel>()
                                        .task
                                        .testGenerator["customCode"] =
                                    testGeneratorController.text;
                              },
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
                    FilledButton(
                      style: ButtonStyle(
                          backgroundColor:
                              MaterialStateProperty.all(const Color(0xff0f6cbf)),
                          overlayColor:
                              MaterialStateProperty.all(const Color(0xff0c589c)),
                          shape: MaterialStateProperty.all(
                            RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(8.0),
                            ),
                          )),
                      onPressed: () {
                        setState(() {
                          questionTextEmpty = false;
                          answerEmpty = false;
                          testGeneratorEmpty = false;
                          testEmpty =
                              List.filled(testsNumber, false, growable: true);
                        });
                        if (_formKey.currentState!.validate() &&
                            !questionTextController.document.isEmpty() &&
                            !answerController.isEmpty &&
                            !testGeneratorController.isEmpty) {
                          context.read<SimpleCodeViewModel>().task.name =
                              nameController.text;
                          context.read<SimpleCodeViewModel>().task.questionText =
                              questionTextController.document.toDelta().toHtml();
                          context.read<SimpleCodeViewModel>().task.defaultGrade =
                              gradeController.text;
                          context.read<SimpleCodeViewModel>().task.answer =
                              answerController.text;
                          context.read<SimpleCodeViewModel>().answerLanguage = selectedAnswerLanguage;
                          for (int i = 0;
                              i <
                                  context
                                      .read<SimpleCodeViewModel>()
                                      .task
                                      .testcases
                                      .length;
                              i++) {
                            context
                                .read<SimpleCodeViewModel>()
                                .task
                                .testcases[i]
                                .stdin = testStdinControllers[i].text;
                            context
                                .read<SimpleCodeViewModel>()
                                .task
                                .testcases[i]
                                .expected = testExpectedControllers[i].text;
                          }
                          context
                                  .read<SimpleCodeViewModel>()
                                  .task
                                  .testGenerator["customCode"] =
                              testGeneratorController.text;
                          context.read<SimpleCodeViewModel>().testGeneratorLanguage = selectedTestGeneratorLanguage;

                          context.read<SimpleCodeViewModel>().generateTask();

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

                        if (testGeneratorController.isEmpty) {
                          setState(() => testGeneratorEmpty = true);
                        }

                        for (int i = 0; i < testsNumber; i++) {
                          if (testStdinControllers[i].text.isEmpty ||
                              testExpectedControllers[i].text.isEmpty) {
                            setState(() => testEmpty[i] = true);
                          }
                        }
                      },
                      child: const Text('Создать задачу'),
                    ),
                  ],
                ),
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

  TextStyle? _textStyleWithValidation(bool predicateValue) {
    return Theme.of(context).textTheme.bodyMedium?.copyWith(
        color: !predicateValue
            ? Theme.of(context).textTheme.bodyMedium?.color
            : Theme.of(context).colorScheme.error);
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
      onChanged: (value) {
        context.read<SimpleCodeViewModel>().task.name = value;
      },
      decoration: const InputDecoration(
          border: OutlineInputBorder(), labelText: "Название*", filled: true, fillColor: Colors.white),
      validator: (value) {
        if (value == null || value.trim().isEmpty) {
          return "Обязательное поле";
        }
        return null;
      },
    );
  }
}

class TestCaseField extends StatelessWidget {
  final int number;
  final TextEditingController stdinController;
  final TextEditingController expectedController;
  final BoxDecoration boxDecoration;
  final TextStyle? textStyle;
  final Function(int i) onDelete;

  const TestCaseField(
      {super.key,
      required this.number,
      required this.stdinController,
      required this.expectedController,
      required this.boxDecoration,
      this.textStyle,
      required this.onDelete});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 15),
      child: Column(
        children: [
          Row(
            children: [
              Text("Тест $number", style: textStyle),
              IconButton(
                  onPressed: () => onDelete(number),
                  icon: const Icon(Icons.delete))
            ],
          ),
          Container(
            margin: const EdgeInsets.only(bottom: 8),
            child: TextFormField(
              controller: stdinController,
              maxLines: null,
              onChanged: (value) {
                context
                    .read<SimpleCodeViewModel>()
                    .task
                    .testcases[number - 1]
                    .stdin = value;
              },
              decoration: const InputDecoration(
                  border: OutlineInputBorder(), labelText: "Стандартный ввод*", filled: true, fillColor: Colors.white),
              validator: (value) {
                if (value == null || value.trim().isEmpty) {
                  return "Обязательное поле";
                }
                return null;
              },
            ),
          ),
          TextFormField(
            controller: expectedController,
            maxLines: null,
            onChanged: (value) {
              context
                  .read<SimpleCodeViewModel>()
                  .task
                  .testcases[number - 1]
                  .expected = value;
            },
            decoration: const InputDecoration(
                border: OutlineInputBorder(), labelText: "Ожидаемый вывод*", filled: true, fillColor: Colors.white),
            validator: (value) {
              if (value == null || value.trim().isEmpty) {
                return "Обязательное поле";
              }
              return null;
            },
          ),
        ],
      ),
    );
  }
}

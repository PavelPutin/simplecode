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

class TaskForm extends StatefulWidget {
  const TaskForm({super.key});

  @override
  State<TaskForm> createState() => _TaskFormState();
}

enum AvailableLanguage {
  java("Java", "11.0.21"),
  c("C", "11.4.0"),
  cpp("C++", "11.4.0"),
  nodejs("Node.js", "12.22.9"),
  pascal("Pascal", "3.2.2"),
  php("PHP", "8.1.2"),
  python3("Python", "3.10.12");

  const AvailableLanguage(this.name, this.version);

  final String name;
  final String version;
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
  final TextEditingController answerLanguageController = TextEditingController();

  int testsNumber = 1;
  final List<TextEditingController> testStdinControllers = [];
  final List<TextEditingController> testExpectedControllers = [];
  List<bool> testEmpty = [];

  final CodeLineEditingController testGeneratorController = CodeLineEditingController();
  bool testGeneratorEmpty = false;
  AvailableLanguage testGeneratorLanguage = AvailableLanguage.java;
  final TextEditingController testGeneratorLanguageController = TextEditingController();

  @override
  void initState() {
    super.initState();
    // answerController.language = language;

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
    gradeController.text = context.watch<SimpleCodeViewModel>().task.defaultGrade;
    answerController.text = context.watch<SimpleCodeViewModel>().task.answer;
    var testcases = Provider.of<SimpleCodeViewModel>(context, listen: false).task.testcases;
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

    testGeneratorController.text = context.watch<SimpleCodeViewModel>().task.testGenerator["customCode"] ?? "";

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
              context.read<SimpleCodeViewModel>().task.testcases.removeAt(number - 1);
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
                            style: Theme
                                .of(context)
                                .textTheme
                                .bodyMedium
                                ?.copyWith(
                                color: !questionTextEmpty
                                    ? Theme
                                    .of(context)
                                    .textTheme
                                    .bodyMedium
                                    ?.color
                                    : Theme
                                    .of(context)
                                    .colorScheme
                                    .error)),
                        if (questionTextEmpty)
                          Text("Обязательное поле",
                              style: Theme
                                  .of(context)
                                  .textTheme
                                  .bodySmall
                                  ?.copyWith(
                                  color:
                                  Theme
                                      .of(context)
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
                      if (value == null || value
                          .trim()
                          .isEmpty) {
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
                            style: Theme
                                .of(context)
                                .textTheme
                                .bodyMedium
                                ?.copyWith(
                                color: !answerEmpty
                                    ? Theme
                                    .of(context)
                                    .textTheme
                                    .bodyMedium
                                    ?.color
                                    : Theme
                                    .of(context)
                                    .colorScheme
                                    .error)),
                        if (answerEmpty)
                          Text("Обязательное поле",
                              style: Theme
                                  .of(context)
                                  .textTheme
                                  .bodySmall
                                  ?.copyWith(
                                  color:
                                  Theme
                                      .of(context)
                                      .colorScheme
                                      .error)),
                        DropdownMenu<AvailableLanguage>(
                          controller: answerLanguageController,
                          initialSelection: AvailableLanguage.java,
                          dropdownMenuEntries: AvailableLanguage.values.map((
                              e) =>
                              DropdownMenuEntry(
                                  value: e, label: "${e.name} ${e.version}"))
                              .toList(),
                          onSelected: (value) =>
                              setState(() {
                                selectedAnswerLanguage =
                                    value ?? AvailableLanguage.java;
                                if (value == null) {
                                  answerLanguageController.value =
                                      TextEditingValue(
                                          text: "${selectedAnswerLanguage
                                              .name} ${selectedAnswerLanguage
                                              .version}");
                                }
                              }),
                        ),
                        ConstrainedBox(
                          constraints: const BoxConstraints(maxHeight: 500),
                          child: CodeEditor(
                            style: CodeEditorStyle(
                                codeTheme: CodeHighlightTheme(
                                    languages: {
                                      "java": CodeHighlightThemeMode(
                                          mode: langJava),
                                      "c": CodeHighlightThemeMode(mode: langC)
                                    }, theme: monokaiTheme
                                )
                            ),
                            wordWrap: false,
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
                  Container(
                    decoration: BoxDecoration(
                        border: Border.all(
                            width: 1,
                            color: Theme.of(context).colorScheme.outline),
                        borderRadius: const BorderRadius.all(Radius.circular(4))),
                    child: Column(
                      children: [
                        const Text("Тестовые данные*"),
                        ...taskTestcases,
                        ElevatedButton(
                            onPressed: () {
                              setState(() {
                                testsNumber++;
                                testStdinControllers.add(TextEditingController());
                                testExpectedControllers.add(TextEditingController());
                                testEmpty.add(false);
                                //todo: move to viewmodel
                                context.read<SimpleCodeViewModel>().task.testcases.add(Testcase("", ""));
                              });
                            },
                            child: const Text("Добавить тест")
                        )
                      ],
                    ),
                  ),
                  Container(
                    decoration: _boxWithValidation(testGeneratorEmpty),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text("Генератор тестов*",
                            style: Theme
                                .of(context)
                                .textTheme
                                .bodyMedium
                                ?.copyWith(
                                color: !testGeneratorEmpty
                                    ? Theme
                                    .of(context)
                                    .textTheme
                                    .bodyMedium
                                    ?.color
                                    : Theme
                                    .of(context)
                                    .colorScheme
                                    .error)),
                        if (testGeneratorEmpty)
                          Text("Обязательное поле",
                              style: Theme
                                  .of(context)
                                  .textTheme
                                  .bodySmall
                                  ?.copyWith(
                                  color:
                                  Theme
                                      .of(context)
                                      .colorScheme
                                      .error)),
                        DropdownMenu<AvailableLanguage>(
                          controller: testGeneratorLanguageController,
                          initialSelection: AvailableLanguage.java,
                          dropdownMenuEntries: AvailableLanguage.values.map((
                              e) =>
                              DropdownMenuEntry(
                                  value: e, label: "${e.name} ${e.version}"))
                              .toList(),
                          onSelected: (value) =>
                              setState(() {
                                testGeneratorLanguage =
                                    value ?? AvailableLanguage.java;
                                if (value == null) {
                                  testGeneratorLanguageController.value =
                                      TextEditingValue(
                                          text: "${testGeneratorLanguage
                                              .name} ${testGeneratorLanguage
                                              .version}");
                                }
                              }),
                        ),
                        ConstrainedBox(
                          constraints: const BoxConstraints(maxHeight: 200),
                          child: CodeEditor(
                            style: CodeEditorStyle(
                                codeTheme: CodeHighlightTheme(
                                    languages: {
                                      "java": CodeHighlightThemeMode(
                                          mode: langJava),
                                      "python": CodeHighlightThemeMode(
                                        mode: langPython
                                      )
                                    }, theme: monokaiTheme
                                )
                            ),
                            wordWrap: false,
                            controller: testGeneratorController,
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
                        testGeneratorEmpty = false;
                        testEmpty = List.filled(testsNumber, false, growable: true);
                      });
                      if (_formKey.currentState!.validate() &&
                          !questionTextController.document.isEmpty() &&
                          !answerController.isEmpty &&
                          !testGeneratorController.isEmpty) {
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
                        if (testStdinControllers[i].text.isEmpty || testExpectedControllers[i].text.isEmpty) {
                          setState(() =>testEmpty[i] = true);
                        }
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
                ? Theme
                .of(context)
                .colorScheme
                .outline
                : Theme
                .of(context)
                .colorScheme
                .error),
        borderRadius: const BorderRadius.all(Radius.circular(4)));
  }

  TextStyle? _textStyleWithValidation(bool predicateValue) {
    return Theme
        .of(context)
        .textTheme
        .bodyMedium
        ?.copyWith(
        color: !predicateValue
            ? Theme
            .of(context)
            .textTheme
            .bodyMedium
            ?.color
            : Theme
            .of(context)
            .colorScheme
            .error);
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
        if (value == null || value
            .trim()
            .isEmpty) {
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
      decoration: boxDecoration,
      child: Column(
        children: [
          Row(
            children: [
              Text("Тест $number", style: textStyle),
              IconButton(onPressed: () => onDelete(number), icon: const Icon(Icons.delete))],
          ),
          TextFormField(
            controller: stdinController,
            decoration: const InputDecoration(
                border: OutlineInputBorder(), labelText: "Стандартный ввод*"),
            validator: (value) {
              if (value == null || value
                  .trim()
                  .isEmpty) {
                return "Обязательное поле";
              }
              return null;
            },
          ),
          TextFormField(
            controller: expectedController,
            decoration: const InputDecoration(
                border: OutlineInputBorder(), labelText: "Ожидаемый вывод*"),
            validator: (value) {
              if (value == null || value
                  .trim()
                  .isEmpty) {
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

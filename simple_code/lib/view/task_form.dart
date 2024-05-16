import 'package:flutter/material.dart';
import 'package:flutter_code_editor/flutter_code_editor.dart';
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
  final TextEditingController questionTextController = TextEditingController();
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

    testStdinControllers = List.filled(testsNumber, TextEditingController(), growable: true);
    testExpectedControllers = List.filled(testsNumber, TextEditingController(), growable: true);
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
                        SubmitButton(formKey: _formKey),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          )
        ),
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
        border: OutlineInputBorder(),
        labelText: "Название*"
      ),
      validator: (value) {
        if (value == null || value.trim().isEmpty) {
          return "Название не может быть пустым";
        }
        return null;
      },
    );
  }
}

class SubmitButton extends StatelessWidget {
  const SubmitButton({
    super.key,
    required GlobalKey<FormState> formKey,
  }) : _formKey = formKey;

  final GlobalKey<FormState> _formKey;

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: () {
        if (_formKey.currentState!.validate()) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(duration: Duration(seconds: 1), content: Text('Данные отправлены')),
          );
        }
      },
      child: const Text('Создать задачу'),
    );
  }
}
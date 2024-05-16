import 'package:flutter/material.dart';
import 'package:flutter_code_editor/flutter_code_editor.dart';
import 'package:flutter_highlight/themes/monokai.dart';
import 'package:highlight/languages/yaml.dart';
import 'package:highlight/languages/xml.dart';
import 'package:provider/provider.dart';
import 'package:simple_code/viewmodel/simple_code_viewmodel.dart';

class Output extends StatefulWidget {
  const Output({super.key});

  @override
  State<Output> createState() => _OutputState();
}

class _OutputState extends State<Output> {
  int _showingOutput = 0;
  final CodeController _yamlController =
      CodeController(text: "yaml doc:\n  status: 'cool'\n" * 300, language: yaml);
  final CodeController _moodleXmlController =
      CodeController(text: "<tagName>moodle xml doc</tagName>\n" * 300, language: xml);

  @override
  Widget build(BuildContext context) {
    var outputs = [
      TextField(
        controller: _yamlController,
        maxLines: null,
      ),
      TextField(
        controller: _moodleXmlController,
        maxLines: null,
      )
    ];

    int showingOutput = context.watch<SimpleCodeViewModel>().showingIndex;

    _yamlController.text = context.watch<SimpleCodeViewModel>().yamlData;
    _moodleXmlController.text = context.watch<SimpleCodeViewModel>().moodleXmlData;
    var errors = context.watch<SimpleCodeViewModel>().errorMessages;

    Widget errorBoxChild = const Center(child: Text("Нет ошибок"));
    if (errors.isNotEmpty) {
      errorBoxChild = ListView.builder(
          itemCount: errors.length,
          itemBuilder: (context, index) => ListTile(
              title: Text("${index + 1}) ${errors[index]}")
          )
      );
    }

    return Column(
      children: [
        Row(
          children: [
            TextButton(onPressed: _showYamlOutput, child: const Text("yaml")),
            TextButton(
                onPressed: _showMoodleXMLOutput, child: const Text("MoodleXML"))
          ],
        ),
        Expanded(
          flex: 3,
          child: Card(
            child: CustomScrollView(
              slivers: [
                SliverFillRemaining(
                  hasScrollBody: false,
                  child: Column(
                    children: <Widget>[
                      Expanded(
                          child: SizedBox(
                        width: double.infinity,
                        child: CodeTheme(
                            data: CodeThemeData(styles: monokaiTheme),
                            child: outputs[showingOutput]),
                      )),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
        Expanded(
            flex: 1,
            child: Card(
                child: errorBoxChild
            )
        )
      ],
    );
  }

  void _showYamlOutput() {
    setState(() => context.read<SimpleCodeViewModel>().showingIndex = 0);
  }

  void _showMoodleXMLOutput() {
    setState(() => context.read<SimpleCodeViewModel>().showingIndex = 1);
  }
}

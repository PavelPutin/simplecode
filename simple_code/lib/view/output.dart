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

    _yamlController.text = context.watch<SimpleCodeViewModel>().YamlData;

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
          flex: 1,
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
                          child: outputs[_showingOutput]),
                    )),
                  ],
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  void _showYamlOutput() {
    setState(() => _showingOutput = 0);
  }

  void _showMoodleXMLOutput() {
    setState(() => _showingOutput = 1);
  }
}

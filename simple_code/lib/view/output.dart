import 'package:flutter/material.dart';
import 'package:flutter_code_editor/flutter_code_editor.dart';
import 'package:flutter_highlight/themes/monokai-sublime.dart';
import 'package:highlight/languages/yaml.dart';
import 'package:highlight/languages/xml.dart';

class Output extends StatefulWidget {
  const Output({super.key});

  @override
  State<Output> createState() => _OutputState();
}

class _OutputState extends State<Output> {
  int _showingOutput = 0;
  final CodeController _yamlController =
      CodeController(text: "yaml doc: 'cool'", language: yaml);
  final CodeController _moodleXmlController =
      CodeController(text: "<tagName>moodle xml doc</tagName>", language: xml);

  @override
  Widget build(BuildContext context) {
    var outputs = [
      CodeField(
        controller: _yamlController,
      ),
      CodeField(controller: _moodleXmlController)
    ];

    return CustomScrollView(
      slivers: [
        SliverFillRemaining(
          hasScrollBody: false,
          child: Column(
            children: <Widget>[
              const Text('Header'),
              Expanded(
                  child: SizedBox(
                    width: double.infinity,
                    child: CodeTheme(
                      data: CodeThemeData(styles: monokaiSublimeTheme),
                      child: outputs[0],
                    ),
              )),
              const Text('Footer'),
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

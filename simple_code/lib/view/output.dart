import 'package:flutter/material.dart';
import 'package:flutter_code_editor/flutter_code_editor.dart';
import 'package:flutter_highlight/themes/monokai-sublime.dart';
import 'package:highlight/languages/java.dart';

class Output extends StatefulWidget {
  const Output({super.key});

  @override
  State<Output> createState() => _OutputState();
}

class _OutputState extends State<Output> {
  int _showingOutput = 0;
  final TextEditingController _yamlController = TextEditingController(text: "yaml doc");
  final TextEditingController _moodleXmlController = TextEditingController(text: "moodle xml doc");

  @override
  Widget build(BuildContext context) {
    var outputs = [
      TextField(
        controller: _yamlController,
        maxLines: 50,
      ),
      TextField(
        controller: _moodleXmlController,
        maxLines: 50,
      )
    ];

    return Container(
      color: Colors.deepOrangeAccent,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(children: [
              TextButton(
                  onPressed: _showYamlOutput,
                  child: const Text("yaml")
              ),
              TextButton(
                  onPressed: _showMoodleXMLOutput,
                  child: const Text("MoodleXML")
              )
            ]),
            Expanded(
                flex: 1,
                child: outputs[_showingOutput]
            )
          ]
      ),
    );
  }

  void _showYamlOutput() {
    setState(() => _showingOutput = 0);
  }

  void _showMoodleXMLOutput() {
    setState(() => _showingOutput = 1);
  }
}

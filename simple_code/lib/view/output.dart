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
  final CodeController _yamlController =
      CodeController(language: yaml);
  final CodeController _moodleXmlController =
      CodeController(language: xml);


  @override
  void dispose() {
    super.dispose();
    _yamlController.dispose();
    _moodleXmlController.dispose();
  }

  @override
  Widget build(BuildContext context) {
    var outputs = [
      TextField(
        controller: _yamlController,
        decoration: const InputDecoration(border: InputBorder.none),
        maxLines: null,
      ),
      TextField(
        controller: _moodleXmlController,
        decoration: const InputDecoration(border: InputBorder.none),
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
                onPressed: _showMoodleXMLOutput, child: const Text("MoodleXML")),
            const Spacer(),
            IconButton(onPressed: _download, icon: const Icon(Icons.download))
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

  void _download() {
    int index = context.read<SimpleCodeViewModel>().showingIndex;
    if (index == 0) {
      context.read<SimpleCodeViewModel>().yamlData = _yamlController.text;
      context.read<SimpleCodeViewModel>().downloadYamlFile();
    } else {
      context.read<SimpleCodeViewModel>().moodleXmlData = _moodleXmlController.text;
      context.read<SimpleCodeViewModel>().downloadXmlFile();
    }
  }
}

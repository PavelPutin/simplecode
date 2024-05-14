import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:simple_code/view/task_form.dart';
import 'package:simple_code/view/output.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.title});

  final String title;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();

  void _closeDrawer() {
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(title: Text(widget.title)),
      body: const SizedBox.expand(
        child: Row(
          children: [
            Expanded(flex: 1, child: TaskForm()),
            Expanded(flex:1, child: Output()),
          ]
        ),
      ),
      drawer: Drawer(
        child: Padding(
          padding: const EdgeInsets.all(15),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              Container(
                margin: const EdgeInsets.only(bottom: 8.0),
                child: OutlinedButton(
                  onPressed: _importYamlFile,
                  child: const Text('Импортировать yaml файл'),
                ),
              ),
              OutlinedButton(
                onPressed: _closeDrawer,
                child: const Text('Импортировать MoodleXml файл'),
              ),
              const Spacer(),
              const Divider(),
              OutlinedButton(
                onPressed: _closeDrawer,
                child: const Text('О приложении'),
              ),
            ],
          ),
        ),
      ),
      // Disable opening the drawer with a swipe gesture.
      drawerEnableOpenDragGesture: false,
    );
  }

  Future<void> _importYamlFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();

    if (result != null) {
      File file = File(result.files.single.path!);
      
    } else {
      // User canceled the picker
    }
  }
}
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:pointer_interceptor/pointer_interceptor.dart';
import 'package:provider/provider.dart';
import 'package:simple_code/view/output.dart';
import 'package:simple_code/view/polygon_multifile_converter.dart';
import 'package:simple_code/view/task_form.dart';
import 'package:simple_code/viewmodel/simple_code_viewmodel.dart';

import 'form_header.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.title});

  final String title;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  late Future<void> _fileLoading;

  @override
  void initState() {
    super.initState();
    _fileLoading = Future.delayed(Duration.zero, () {});
  }

  void _closeDrawer() {
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(title: Text(widget.title)),
      body: SizedBox.expand(
        child: FutureBuilder(
            future: _fileLoading,
            builder: (context, snapshot) {
              if (snapshot.connectionState != ConnectionState.done) {
                return const Center(child: CircularProgressIndicator());
              }
              return Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Expanded(flex: 1, child: SingleChildScrollView(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.start,
                    mainAxisSize: MainAxisSize.max,
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const FormHeader(),
                      if (context.watch<SimpleCodeViewModel>().showingTaskForm)
                        const TaskForm(),
                      if (context.watch<SimpleCodeViewModel>().showingMultiFileConverter)
                        const PolygonMultiFileConverter(),
                    ],
                  ),
                )),
                const Expanded(flex: 1, child: Output()),
              ]);
            }),
      ),
      drawer: PointerInterceptor(
        child: Drawer(
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 15),
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
                Container(
                  margin: const EdgeInsets.only(bottom: 8.0),
                  child: OutlinedButton(
                    onPressed: _importPolygonFile,
                    child: const Text('Импортировать Polygon-zip файл'),
                  ),
                ),
                OutlinedButton(
                  onPressed: _importXmlFile,
                  child: const Text('Импортировать MoodleXml файл'),
                ),
                const Spacer(),
                const Divider(),
                OutlinedButton(
                  onPressed: () => showAboutDialog(
                      context: context,
                      applicationVersion: "1.1.0",
                      children: [const Text("Курсовой проект"), const Text("студента 4 курса ВГУ"), const Text("Путина Павла Александровича")]),
                  child: const Text('О приложении'),
                ),
              ],
            ),
          ),
        ),
      ),
      // Disable opening the drawer with a swipe gesture.
      drawerEnableOpenDragGesture: false,
    );
  }

  void _importYamlFile() {
    setState(() {
      _fileLoading = context.read<SimpleCodeViewModel>().openYamlFile();
    });
  }

  void _importPolygonFile() {
    setState(() {
      _fileLoading = context.read<SimpleCodeViewModel>().openPolygonFile();
    });
  }

  void _importXmlFile() {
    setState(() {
      _fileLoading = context.read<SimpleCodeViewModel>().openXmlFile();
    });
  }
}

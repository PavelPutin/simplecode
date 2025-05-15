import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_dropzone/flutter_dropzone.dart';
import 'package:flutter_quill/flutter_quill.dart';
import 'package:provider/provider.dart';
import 'package:simple_code/viewmodel/simple_code_viewmodel.dart';

class PolygonMultiFileConverter extends StatefulWidget {
  const PolygonMultiFileConverter({super.key});

  @override
  State<PolygonMultiFileConverter> createState() => _PolygonMultiFileConverterState();
}

class _PolygonMultiFileConverterState extends State<PolygonMultiFileConverter> {
  late DropzoneViewController polygonMultiFileConverterController;
  Future<void> uploading = Future.delayed(Duration.zero);
  Future<void> converting = Future.delayed(Duration.zero);

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 20),
            child: FutureBuilder(
                future: converting,
                builder: (context, snapshot) {
                  var loading = snapshot.connectionState != ConnectionState.done;

                  return OutlinedButton(
                      onPressed: () async {
                        if (loading) return;

                        setState(() {
                          converting = context.read<SimpleCodeViewModel>().convertUploadedFiles();
                        });
                      },
                      child: Row(
                        children: [
                          if (loading) const CircularProgressIndicator(),
                          const Text("Конвертировать"),
                        ],
                      ));
                }),
          ),
        ),
        Card(
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 20),
            child: FutureBuilder(
                future: uploading,
                builder: (context, snapshot) {
                  if (snapshot.connectionState != ConnectionState.done) {
                    return const CircularProgressIndicator();
                  }

                  final viewModel = context.watch<SimpleCodeViewModel>();

                  Widget dropzoneList;
                  if (viewModel.uploadedFiles.isEmpty) {
                    dropzoneList = const Text("Нет загруженных файлов");
                  } else {
                    dropzoneList = Column(
                      children: [
                        Text("Загружено файлов: ${viewModel.uploadedFiles.length}"),
                        ListView.builder(
                          scrollDirection: Axis.vertical,
                          shrinkWrap: true,
                          itemCount: viewModel.uploadedFiles.length,
                          itemBuilder: (context, index) => UploadedFileListTile(index: index),
                        )
                      ],
                    );
                  }

                  return Column(
                    children: [
                      SizedBox.fromSize(
                        size: const Size.fromHeight(200),
                        child: Stack(
                          children: [
                            SizedBox.expand(
                                child: DecoratedBox(
                                    decoration: BoxDecoration(
                                      border: Border.all(
                                        color: Colors.black26,
                                      ),
                                    ),
                                    child: const DropzoneDefaultField())),
                            SizedBox.expand(
                              child: DropzoneView(
                                operation: DragOperation.copy,
                                onCreated: (ctrl) => polygonMultiFileConverterController = ctrl,
                                onDropFiles: (files) {
                                  if (files == null) {
                                    print("No files");
                                  }
                                  var uploadingFiles = files
                                          ?.map((f) => context.read<SimpleCodeViewModel>().uploadFile(f, polygonMultiFileConverterController))
                                          .toList() ??
                                      List.empty();
                                  setState(() {
                                    uploading = Future.wait(uploadingFiles);
                                  });
                                },
                              ),
                            )
                          ],
                        ),
                      ),
                      dropzoneList
                    ],
                  );
                }),
          ),
        ),
      ],
    );
  }
}

class UploadedFileListTile extends StatelessWidget {
  const UploadedFileListTile({super.key, required this.index});

  final int index;

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<SimpleCodeViewModel>();
    Widget leading = viewModel.uploadedFiles[index].isValidSize
        ? const Tooltip(message: "Успешно загружен", child: Icon(Icons.file_download_done))
        : const Tooltip(message: "Слишком большой, максимальный размер 20 МиБ", child: Icon(Icons.error_outline, color: Colors.black26));

    if (viewModel.uploadedFiles[index].isConverted) {
      leading = const Tooltip(message: "Файл конвертирован", child: Icon(Icons.check_circle, color: Colors.green));
    }

    Widget? subtitle;
    if (viewModel.uploadedFiles[index].isConverted) {
      subtitle = Row(
        spacing: 10,
        children: [
          RichText(
              text: TextSpan(
                text: "XML",
                style: const TextStyle(color: Colors.blue, decoration: TextDecoration.underline),
                recognizer: TapGestureRecognizer()..onTap = () => print("XML"),
              )
          ),
          RichText(
              text: TextSpan(
                text: "YAML",
                style: const TextStyle(color: Colors.blue, decoration: TextDecoration.underline),
                recognizer: TapGestureRecognizer()..onTap = () => print("YAML"),
              )
          ),
        ],
      );
    }

    final color = viewModel.uploadedFiles[index].isValidSize ? Colors.transparent : Colors.grey;

    return FutureBuilder(
        future: viewModel.uploadedFiles[index].converting,
        builder: (context, snapshot) {
          if (!(snapshot.connectionState == ConnectionState.done || snapshot.connectionState == ConnectionState.none)) {
            leading = const CircularProgressIndicator();
          }
          return ListTile(
            leading: leading,
            title: Text(viewModel.uploadedFiles[index].name),
            subtitle: subtitle,
            trailing: Text(viewModel.uploadedFiles[index].sizeBytes.toString()),
            tileColor: color,
          );
        });
  }
}

class DropzoneDefaultField extends StatelessWidget {
  const DropzoneDefaultField({
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return Container(alignment: Alignment.center, child: const Text("Загрузите файл", textAlign: TextAlign.center));
  }
}

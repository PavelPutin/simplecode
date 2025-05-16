import 'package:flutter/material.dart';
import 'package:flutter_dropzone/flutter_dropzone.dart';
import 'package:provider/provider.dart';
import 'package:simple_code/view/converting_uploaded_files_button.dart';
import 'package:simple_code/view/uploaded_file_list_tile.dart';
import 'package:simple_code/viewmodel/simple_code_viewmodel.dart';

import 'dropzone_default_field.dart';

class PolygonMultiFileConverter extends StatefulWidget {
  const PolygonMultiFileConverter({super.key});

  @override
  State<PolygonMultiFileConverter> createState() => _PolygonMultiFileConverterState();
}

class _PolygonMultiFileConverterState extends State<PolygonMultiFileConverter> {
  late DropzoneViewController polygonMultiFileConverterController;
  Future<void> uploading = Future.delayed(Duration.zero);

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        const Card(
          child: Padding(
            padding: EdgeInsets.symmetric(vertical: 12, horizontal: 20),
            child: Row(
              children: [
                ConvertingUploadedFilesButton(),
              ],
            ),
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
                    spacing: 10,
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
                      if (context.watch<SimpleCodeViewModel>().hasConvertedFiles())
                        Row(
                          children: [
                            OutlinedButton(
                                onPressed: () => context.read<SimpleCodeViewModel>().downloadXmlAllFiles(), child: const Text("Скачать всё XML")),
                            OutlinedButton(
                                onPressed: () => context.read<SimpleCodeViewModel>().downloadYamlAllFiles(), child: const Text("Скачать всё YAML")),
                          ],
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

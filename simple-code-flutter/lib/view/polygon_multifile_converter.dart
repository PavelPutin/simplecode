import 'package:flutter/material.dart';
import 'package:flutter_dropzone/flutter_dropzone.dart';
import 'package:provider/provider.dart';
import 'package:simple_code/viewmodel/simple_code_viewmodel.dart';

import '../model/uploaded_file.dart';

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
        Card(
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 20),
            child: OutlinedButton(
                onPressed: () {
                  print("Hello");
                },
                child: const Text("Конвертировать")
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
                    final uploadedFilesList = viewModel.uploadedFiles.toList();
                    dropzoneList = Column(
                      children: [
                        Text("Загружено файлов: ${uploadedFilesList.length}"),
                        ListView.builder(
                          scrollDirection: Axis.vertical,
                          shrinkWrap: true,
                          itemCount: uploadedFilesList.length,
                          itemBuilder: (context, index) => UploadedFileListTile(uploadedFilesList: uploadedFilesList, index: index),
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
  const UploadedFileListTile({
    super.key,
    required this.uploadedFilesList,
    required this.index
  });

  final List<UploadedFile> uploadedFilesList;
  final int index;

  @override
  Widget build(BuildContext context) {
    final leading = uploadedFilesList[index].isValidSize
        ? const Tooltip(message: "Успешно загружен", child: Icon(Icons.file_download_done))
        : const Tooltip(message: "Слишком большой, максимальный размер 20 МиБ", child: Icon(Icons.error_outline, color: Colors.black26));

    final color = uploadedFilesList[index].isValidSize ? Colors.transparent : Colors.grey;

    return ListTile(
      leading: leading,
      title: Text(uploadedFilesList[index].name),
      trailing: Text(uploadedFilesList[index].sizeBytes.toString()),
      tileColor: color,
    );
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

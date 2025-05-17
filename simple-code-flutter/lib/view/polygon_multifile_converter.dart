import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_dropzone/flutter_dropzone.dart';
import 'package:provider/provider.dart';
import 'package:simple_code/model/data_size.dart';
import 'package:simple_code/view/converting_uploaded_files_button.dart';
import 'package:simple_code/view/uploaded_file_list_tile.dart';
import 'package:simple_code/viewmodel/simple_code_viewmodel.dart';

import '../model/data_size_suffix.dart';
import 'dropzone_default_field.dart';

class PolygonMultiFileConverter extends StatefulWidget {
  const PolygonMultiFileConverter({super.key});

  @override
  State<PolygonMultiFileConverter> createState() => _PolygonMultiFileConverterState();
}

class _PolygonMultiFileConverterState extends State<PolygonMultiFileConverter> {
  late DropzoneViewController polygonMultiFileConverterController;
  Future<void> uploading = Future.delayed(Duration.zero);
  DataSizeSuffix selectedDataSize = DataSizeSuffix.bytes;
  TextEditingController testsAmountConstraintController = TextEditingController();
  TextEditingController testSizeConstraintController = TextEditingController();

  static const WidgetStateProperty<Icon> thumbIcon = WidgetStateProperty<Icon>.fromMap(
    <WidgetStatesConstraint, Icon>{
      WidgetState.selected: Icon(Icons.check),
      WidgetState.any: Icon(Icons.close),
    },
  );

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<SimpleCodeViewModel>();
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Card(
          child: Padding(
            padding: EdgeInsets.symmetric(vertical: 12, horizontal: 20),
            child: Column(
              spacing: 10,
              children: [
                Row(
                  children: [
                    Text("Ограничение количества тестов"),
                    Switch(
                        thumbIcon: thumbIcon,
                        value: viewModel.hasTestsAmountConstraint,
                        onChanged: (bool value) {
                          context.read<SimpleCodeViewModel>().hasTestsAmountConstraint = value;
                        }),
                    if (viewModel.hasTestsAmountConstraint)
                      Expanded(
                        child: TextField(
                          controller: testsAmountConstraintController,
                          keyboardType: TextInputType.number,
                          inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                        ),
                      )
                  ],
                ),
                Row(children: [
                  Text("Ограничение объёма теста"),
                  Switch(
                      thumbIcon: thumbIcon,
                      value: viewModel.hasTestSizeConstraint,
                      onChanged: (bool value) {
                        context.read<SimpleCodeViewModel>().hasTestSizeConstraint = value;
                        if (!value) {
                          context.read<SimpleCodeViewModel>().testSizeConstraint = null;
                        } else {
                          final constraintValue = testSizeConstraintController.text;
                          updateTestSizeConstraint(constraintValue, context);
                        }
                      }),
                  if (viewModel.hasTestSizeConstraint)
                    Expanded(
                      child: Row(
                        children: [
                          Expanded(
                            child: TextField(
                              controller: testSizeConstraintController,
                              keyboardType: TextInputType.number,
                              inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                              onChanged: (value) {
                                updateTestSizeConstraint(value, context);
                              },
                            ),
                          ),
                          DropdownMenu<DataSizeSuffix>(
                            initialSelection: selectedDataSize,
                            dropdownMenuEntries: DataSizeSuffix.values.map((e) => DropdownMenuEntry(value: e, label: e.name)).toList(),
                            onSelected: (DataSizeSuffix? value) => setState(() {
                              selectedDataSize = value ?? DataSizeSuffix.bytes;
                              updateTestSizeConstraint(testSizeConstraintController.text, context);
                            }),
                          ),
                        ],
                      ),
                    )
                ]),
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

  void updateTestSizeConstraint(String value, BuildContext context) {
    if (value.isNotEmpty) {
      var size = int.parse(value);
      context.read<SimpleCodeViewModel>().testSizeConstraint = DataSize.fromValueAndDataSize(size, selectedDataSize);
    } else {
      context.read<SimpleCodeViewModel>().testSizeConstraint = null;
    }
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_dropzone/flutter_dropzone.dart';
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

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 20),
        child: SizedBox.fromSize(
          size: Size(300, 300),
          child: FutureBuilder(
            future: uploading,
            builder: (context, snapshot) {
              if (snapshot.connectionState != ConnectionState.done) {
                return const CircularProgressIndicator();
              }
              return Stack(
                children: [
                  const SizedBox.expand(child: DropzoneDefaultField()),
                  SizedBox.expand(
                    child: DropzoneView(
                      operation: DragOperation.copy,
                      cursor: CursorType.grab,
                      onCreated: (ctrl) => polygonMultiFileConverterController = ctrl,
                      onLoaded: () => print("Loaded"),
                      onDropFiles: (files) {
                        if (files == null) {
                          print("No files");
                        }
                        var uploadingFiles =
                            files?.map((f) => context.read<SimpleCodeViewModel>().uploadFile(f, polygonMultiFileConverterController)).toList() ??
                                List.empty();
                        setState(() {
                          uploading = Future.wait(uploadingFiles);
                        });
                      },
                    ),
                  )
                ],
              );
            }
          ),
        ),
      ),
    );
  }
}

class DropzoneDefaultField extends StatelessWidget {
  const DropzoneDefaultField({
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
        decoration: BoxDecoration(
          border: Border.all(
            color: Colors.black26,
          ),
        ),
        child: Container(alignment: Alignment.center, child: const Text("Загрузите файл", textAlign: TextAlign.center)));
  }
}

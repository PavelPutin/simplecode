import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../viewmodel/simple_code_viewmodel.dart';

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
    if (viewModel.uploadedFiles[index].hasTask) {
      subtitle = Row(
        spacing: 10,
        children: [
          RichText(
              text: TextSpan(
                text: "XML",
                style: const TextStyle(color: Colors.blue, decoration: TextDecoration.underline),
                recognizer: TapGestureRecognizer()
                  ..onTap = () {
                    context.read<SimpleCodeViewModel>().downloadXmlUploadedFile(viewModel.uploadedFiles[index]);
                  },
              )
          ),
          RichText(
              text: TextSpan(
                text: "YAML",
                style: const TextStyle(color: Colors.blue, decoration: TextDecoration.underline),
                recognizer: TapGestureRecognizer()
                  ..onTap = () {
                    context.read<SimpleCodeViewModel>().downloadYamlUploadedFile(viewModel.uploadedFiles[index]);
                  },
              )
          ),
        ],
      );
    }

    final color = viewModel.uploadedFiles[index].isValidSize ? Colors.transparent : Colors.grey;

    return FutureBuilder(
        future: viewModel.uploadedFiles[index].converting,
        builder: (context, snapshot) {
          final loading = !(snapshot.connectionState == ConnectionState.done || snapshot.connectionState == ConnectionState.none);
          if (loading) {
            leading = const CircularProgressIndicator();
          }
          return ListTile(
            leading: leading,
            title: Text("${viewModel.uploadedFiles[index].name} (${viewModel.uploadedFiles[index].sizeBytes.toString()})"),
            subtitle: subtitle,
            trailing: IconButton(
                icon: Icon(Icons.delete, color: loading ? Colors.grey : Colors.red),
                onPressed: () {
                  if (loading) return;
                  context.read<SimpleCodeViewModel>().deleteUploadedFile(viewModel.uploadedFiles[index]);
                }
            ),
            tileColor: color,
          );
        });
  }
}
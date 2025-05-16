import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../viewmodel/simple_code_viewmodel.dart';

class ConvertingUploadedFilesButton extends StatefulWidget {
  const ConvertingUploadedFilesButton({super.key});

  @override
  State<ConvertingUploadedFilesButton> createState() => _ConvertingUploadedFilesButtonState();
}

class _ConvertingUploadedFilesButtonState extends State<ConvertingUploadedFilesButton> {
  Future<void> converting = Future.delayed(Duration.zero);

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
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
        });
  }
}

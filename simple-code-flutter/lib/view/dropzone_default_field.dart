import 'package:flutter/material.dart';

class DropzoneDefaultField extends StatelessWidget {
  const DropzoneDefaultField({
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return Container(alignment: Alignment.center, child: const Text("Загрузите файл", textAlign: TextAlign.center));
  }
}
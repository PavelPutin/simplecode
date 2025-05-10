import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:simple_code/viewmodel/simple_code_viewmodel.dart';

class FormHeader extends StatelessWidget {
  const FormHeader({super.key});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 20),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.start,
          children: [
            TextButton(
              child: const Text("Редактирование задачи"),
              onPressed: () => context.read<SimpleCodeViewModel>().showingTaskForm = true,
            ),
            TextButton(
              child: const Text("Загрузка Polygon"),
              onPressed: () => context.read<SimpleCodeViewModel>().showingMultiFileConverter = true,
            ),
          ]
        ),
      )
    );
  }
}

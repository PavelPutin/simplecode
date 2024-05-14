import 'package:flutter/material.dart';

class TaskForm extends StatelessWidget {
  const TaskForm({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.blueGrey,
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text("Форма ввода привет"),
        ],
      ),
    );
  }

}
import 'package:flutter/material.dart';

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
              child: Text("Button 1"),
              onPressed: () {},
            ),
            TextButton(
              child: Text("Button 2"),
              onPressed: () {},
            ),
          ]
        ),
      )
    );
  }
}

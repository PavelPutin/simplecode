import 'package:flutter/material.dart';
import 'package:flutter_dropzone/flutter_dropzone.dart';

class PolygonMultiFileConverter extends StatelessWidget {
  const PolygonMultiFileConverter({super.key});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 20),
        child: SizedBox.fromSize(
          size: Size(300, 300),
          child: Stack(
            children: [
              SizedBox.expand(
                  child: Container(
                child: DecoratedBox(
                    decoration: BoxDecoration(
                      border: Border.all(
                        color: Colors.black26,
                      ),
                    ),
                    child: Container(
                        alignment: Alignment.center,
                        child: Text("Загрузите файл", textAlign: TextAlign.center)
                    )
                ),
              )),
              SizedBox.expand(
                child: DropzoneView(
                  operation: DragOperation.copy,
                  cursor: CursorType.grab,
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}

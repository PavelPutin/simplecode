import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter/material.dart';

class Base64Image extends StatelessWidget {
  final String base64String;

  const Base64Image({super.key, required this.base64String});

  @override
  Widget build(BuildContext context) {
    try {
      final cleanBase64 = base64String.split(',').last;

      final Uint8List bytes = base64Decode(cleanBase64);

      return Image.memory(
        bytes,
        fit: BoxFit.contain,
        gaplessPlayback: true,
        errorBuilder: (context, error, stackTrace) {
          return _buildErrorWidget();
        },
      );
    } catch (e) {
      return _buildErrorWidget();
    }
  }

  Widget _buildErrorWidget() {
    return Container(
      color: Colors.grey[200],
      child: const Center(
        child: Icon(
          Icons.broken_image,
          color: Colors.grey,
          size: 40,
        ),
      ),
    );
  }
}
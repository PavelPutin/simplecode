import 'package:flutter/material.dart';

class CollapsibleWidget extends StatefulWidget {
  final Widget header;
  final Widget content;

  const CollapsibleWidget({
    super.key,
    required this.header,
    required this.content,
  });

  @override
  _CollapsibleWidgetState createState() => _CollapsibleWidgetState();
}

class _CollapsibleWidgetState extends State<CollapsibleWidget>
    with SingleTickerProviderStateMixin {
  bool _isExpanded = false;
  late AnimationController _controller;
  late Animation<double> _iconAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
    _iconAnimation = Tween<double>(begin: 0, end: 0.5).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _toggleExpansion() {
    setState(() {
      _isExpanded = !_isExpanded;
      if (_isExpanded) {
        _controller.forward();
      } else {
        _controller.reverse();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        // Заголовок с иконкой
        InkWell(
          onTap: _toggleExpansion,
          child: Row(
            children: [
              Expanded(child: widget.header),
              RotationTransition(
                turns: _iconAnimation,
                child: const Icon(Icons.expand_more),
              ),
            ],
          ),
        ),
        // Анимированное содержимое
        AnimatedSize(
          duration: const Duration(milliseconds: 300),
          child: Column(
            children: [
              if (_isExpanded) widget.content,
            ],
          ),
        ),
      ],
    );
  }
}
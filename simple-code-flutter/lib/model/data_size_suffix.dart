enum DataSizeSuffix {
  bytes('B', 1),
  kilobytes('KB', 1024),
  megabytes('MB', 1024 * 1024);

  const DataSizeSuffix(this.name, this.power);

  final String name;
  final int power;
}
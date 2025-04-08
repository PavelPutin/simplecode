enum AvailableLanguage {
  java("Java", "11.0.21", "java"),
  c("C", "11.4.0", "c"),
  cpp("C++", "11.4.0", "cpp"),
  nodejs("Node.js", "12.22.9", "nodejs"),
  pascal("Pascal", "3.2.2", "pascal"),
  php("PHP", "8.1.2", "php"),
  python3("Python", "3.10.12", "python3");

  const AvailableLanguage(this.name, this.version, this.jobeLanguageId);

  final String name;
  final String version;
  final String jobeLanguageId;

  factory AvailableLanguage.fromJobeLanguageId(String languageId) {
    try {
      return values.firstWhere(
        (lang) => lang.jobeLanguageId.toLowerCase() == languageId.toLowerCase(),
      );
    } catch (e) {
      throw ArgumentError("Unknown language: $languageId");
    }
  }
}

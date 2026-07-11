/// Who is logged in right now — the Dart twin of the backend's LoginResponse
/// record (token, fullName, role).
///
/// All fields are `final` and the constructor is `const`-able: once created it
/// never changes, exactly like a Java record. To "change" the session you make
/// a new one — the same immutability habit React state pushes you toward.
class AuthSession {
  const AuthSession({
    required this.token,
    required this.fullName,
    required this.role,
  });

  /// Dart has no Jackson doing reflection magic like Spring does — mapping
  /// JSON -> object is written by hand (or generated; we'll keep it manual
  /// while learning). `factory` just means: a named constructor that runs
  /// code before building the object.
  factory AuthSession.fromJson(Map<String, dynamic> json) => AuthSession(
        token: json['token'] as String, // `as String` = a checked cast
        fullName: json['fullName'] as String,
        role: json['role'] as String,
      );

  final String token; // the raw JWT — sent as "Authorization: Bearer <token>"
  final String fullName; // for "Welcome, Alan Tan"
  final String role; // TECHNICIAN or SUPERVISOR — decides what UI to show later

  Map<String, dynamic> toJson() => {
        'token': token,
        'fullName': fullName,
        'role': role,
      };
}

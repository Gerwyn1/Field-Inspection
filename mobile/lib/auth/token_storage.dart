import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// The seam between "we need to persist the session" and "HOW it's persisted".
///
/// Dart's version of a Java interface is an abstract class: methods with no
/// bodies. AuthController depends on THIS, never on flutter_secure_storage
/// directly — the same dependency-inversion Spring gives you when a service
/// takes a repository interface. Why it matters here: flutter_secure_storage
/// calls native Android code, and in headless widget tests there IS no native
/// side — the call never even returns. Tests inject the in-memory fake below;
/// the real app injects [SecureTokenStorage].
abstract class TokenStorage {
  Future<String?> read();
  Future<void> write(String value);
  Future<void> delete();
}

/// The real thing: encrypted at rest via the Android Keystore / iOS Keychain.
class SecureTokenStorage implements TokenStorage {
  const SecureTokenStorage([this._storage = const FlutterSecureStorage()]);

  static const _key = 'session';

  final FlutterSecureStorage _storage;

  @override
  Future<String?> read() => _storage.read(key: _key);

  @override
  Future<void> write(String value) => _storage.write(key: _key, value: value);

  @override
  Future<void> delete() => _storage.delete(key: _key);
}

/// Test double: a Map pretending to be storage. Lives here (not in test/) so
/// a quick demo mode could reuse it too.
class InMemoryTokenStorage implements TokenStorage {
  String? _value;

  @override
  Future<String?> read() async => _value;

  @override
  Future<void> write(String value) async => _value = value;

  @override
  Future<void> delete() async => _value = null;
}

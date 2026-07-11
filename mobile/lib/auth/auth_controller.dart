import 'dart:convert';

import 'package:flutter/foundation.dart';

import '../core/api_client.dart';
import 'auth_session.dart';
import 'token_storage.dart';

/// The three states the app can be in. An enum forces us to handle each one —
/// no "is it null? is it loading?" boolean juggling.
enum AuthStatus {
  restoring, // app just launched, still reading storage — show a splash
  loggedOut, // no session — show the login screen
  loggedIn, // session in hand — show the app
}

/// Holds "who is logged in" for the WHOLE app — the equivalent of a React
/// AuthContext provider.
///
/// ChangeNotifier is the minimal state-management primitive Flutter ships:
/// widgets subscribe to it, and every notifyListeners() call re-runs their
/// build() — exactly what a context value change does to consumers in React.
class AuthController extends ChangeNotifier {
  /// Both dependencies can be injected (tests pass fakes), and default to the
  /// real thing — the same constructor-injection idea Spring does for you
  /// automatically, done by hand here. Note the type is the TokenStorage
  /// INTERFACE: this class neither knows nor cares whether the bytes land in
  /// the Android Keystore or a Map inside a test.
  AuthController({ApiClient? api, TokenStorage? storage})
      : _api = api ?? ApiClient(),
        _storage = storage ?? const SecureTokenStorage();

  final ApiClient _api; // leading _ = private (Dart's only access modifier)
  final TokenStorage _storage;

  AuthStatus _status = AuthStatus.restoring;
  AuthSession? _session; // the ? means nullable — like `Session | null` in TS

  // Public read-only views: fields stay private so ONLY this class can change
  // state (and remember to notifyListeners when it does).
  AuthStatus get status => _status;
  AuthSession? get session => _session;

  /// Called once at startup: is there a session from a previous run?
  /// flutter_secure_storage keeps it encrypted in the Android Keystore —
  /// the mobile equivalent of an httpOnly cookie (NOT localStorage, which any
  /// code on the device could read).
  Future<void> restoreSession() async {
    try {
      final raw = await _storage.read();
      if (raw != null) {
        _session = AuthSession.fromJson(jsonDecode(raw) as Map<String, dynamic>);
        _status = AuthStatus.loggedIn;
      } else {
        _status = AuthStatus.loggedOut;
      }
    } catch (_) {
      // Storage unreadable or the JSON corrupt? Treat it as logged out —
      // worst case the user signs in again.
      _status = AuthStatus.loggedOut;
    }
    notifyListeners();
    // NOTE: the stored JWT might have expired while the app was closed. We
    // don't check that here — in Phase 3 the API layer will treat any 401 as
    // "session expired, log out". Standard mobile-app pattern.
  }

  /// Hits POST /api/auth/login. On success, updates state AND persists the
  /// session. On failure it deliberately does NOT catch — the login screen
  /// catches, because "wrong password" is a UI concern (show a message),
  /// not a state concern.
  Future<void> login(String email, String password) async {
    final json = await _api.postJson(
      '/api/auth/login',
      {'email': email, 'password': password},
    );
    _session = AuthSession.fromJson(json);
    _status = AuthStatus.loggedIn;
    notifyListeners(); // UI flips to the home screen immediately...
    // ! = "I know it's not null here"; persisting finishes in the background.
    await _storage.write(jsonEncode(_session!.toJson()));
  }

  Future<void> logout() async {
    _session = null;
    _status = AuthStatus.loggedOut;
    notifyListeners();
    await _storage.delete();
  }
}

import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;

/// Thrown when the server answers with a non-2xx status.
///
/// Dart has no built-in "HTTP error" type — the `http` package happily returns
/// a 401 response as a normal value (like fetch() in JS, which never rejects on
/// status codes). We convert bad statuses into a thrown exception ourselves so
/// callers can use try/catch instead of checking a status on every call.
class ApiException implements Exception {
  ApiException(this.statusCode, this.message);

  final int statusCode;
  final String message;

  @override
  String toString() => 'ApiException($statusCode): $message';
}

/// One place that knows how to talk to the Spring Boot backend.
///
/// Think of this as your `api.js` module in a React project — the single spot
/// where the base URL, headers, and JSON encoding live, so screens never touch
/// raw HTTP.
class ApiClient {
  ApiClient({String? baseUrl}) : baseUrl = baseUrl ?? defaultBaseUrl;

  final String baseUrl;

  /// WHERE IS "localhost"? Depends on where this code runs:
  ///  * Web build: Spring Boot serves the app itself, so the API lives at the
  ///    SAME origin the page came from (Uri.base = the browser's address bar).
  ///  * Android emulator: localhost means the PHONE. Google reserves the magic
  ///    IP 10.0.2.2 as "the computer running the emulator" — where Spring
  ///    Boot listens. A real phone would need the PC's LAN IP instead.
  ///
  /// Note: dart:io's Platform class doesn't exist in a browser (importing it
  /// would break the web build) — flutter/foundation's kIsWeb and
  /// defaultTargetPlatform are the portable way to ask "where am I running?".
  static String get defaultBaseUrl {
    if (kIsWeb) return Uri.base.origin;
    if (defaultTargetPlatform == TargetPlatform.android) {
      return 'http://10.0.2.2:8080';
    }
    return 'http://localhost:8080';
  }

  /// POST a JSON body, get a JSON object back. The JS equivalent:
  ///
  ///   const res = await fetch(url, { method: 'POST', headers, body: JSON.stringify(body) });
  ///   if (!res.ok) throw ...;
  ///   return res.json();
  ///
  /// `Map<String, dynamic>` is Dart for "a JSON object" — string keys, values
  /// of any type (dynamic = TypeScript's `any`).
  Future<Map<String, dynamic>> postJson(
    String path,
    Map<String, dynamic> body, {
    String? token, // named optional param — like an options object in JS
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl$path'), // '$a$b' is string interpolation, like `${a}${b}`
      headers: {
        'Content-Type': 'application/json',
        // Collection-if: this entry only exists when token != null. Same trick
        // as {...(token && { Authorization: `Bearer ${token}` })} in JS.
        if (token != null) 'Authorization': 'Bearer $token',
      },
      body: jsonEncode(body), // JSON.stringify
    );

    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw ApiException(response.statusCode, response.body);
    }
    if (response.body.isEmpty) return const {};
    return jsonDecode(response.body) as Map<String, dynamic>; // JSON.parse
  }
}

import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

import 'package:field_inspect/auth/auth_controller.dart';
import 'package:field_inspect/auth/token_storage.dart';
import 'package:field_inspect/core/api_client.dart';
import 'package:field_inspect/core/demo_api_client.dart';
import 'package:field_inspect/main.dart';
import 'package:flutter/material.dart';

/// Widget tests = React Testing Library for Flutter: render real widgets in a
/// headless test environment, find things, tap them, assert on the result.
/// No emulator involved — these run on the dev machine in seconds.
///
/// AuthController gets an InMemoryTokenStorage: the real secure-storage
/// plugin is native Android code, which doesn't exist here (a call into it
/// would just hang — we learned that the hard way).
void main() {
  /// Helper: build the same tree main() builds, but with injected fakes so
  /// each test controls the starting state.
  Future<AuthController> pumpApp(
    WidgetTester tester, {
    TokenStorage? storage,
    ApiClient? api,
  }) async {
    final auth = AuthController(
      api: api,
      storage: storage ?? InMemoryTokenStorage(),
    );
    await auth.restoreSession();
    await tester.pumpWidget(
      ChangeNotifierProvider.value(value: auth, child: const FieldInspectApp()),
    );
    return auth;
  }

  testWidgets('shows the login screen when nobody is logged in',
      (WidgetTester tester) async {
    await pumpApp(tester);

    // find.text scans the rendered tree — like screen.getByText.
    expect(find.text('FieldInspect'), findsOneWidget);
    expect(find.text('Email'), findsOneWidget);
    expect(find.text('Password'), findsOneWidget);
    expect(find.text('Sign in'), findsOneWidget);
  });

  testWidgets('empty form shows validation errors instead of submitting',
      (WidgetTester tester) async {
    await pumpApp(tester);

    await tester.tap(find.text('Sign in'));
    await tester.pump(); // advance one frame so the error texts render

    // These are the validator messages from LoginScreen — the client-side
    // mirror of the backend's @NotBlank annotations.
    expect(find.text('Email is required'), findsOneWidget);
    expect(find.text('Password is required'), findsOneWidget);
  });

  testWidgets('restores a stored session straight to the home screen',
      (WidgetTester tester) async {
    // Simulate "user logged in on a previous run": storage already holds a
    // session, exactly as AuthController.login would have written it.
    final storage = InMemoryTokenStorage();
    await storage.write(jsonEncode({
      'token': 'fake-jwt',
      'fullName': 'Siti Rahman',
      'role': 'SUPERVISOR',
    }));

    await pumpApp(tester, storage: storage);

    // No login screen — restoreSession found the session and went straight in.
    expect(find.text('Sign in'), findsNothing);
    expect(find.text('Welcome, Siti Rahman'), findsOneWidget);
    expect(find.text('SUPERVISOR'), findsOneWidget);
  });

  testWidgets('demo mode: full login flow against the in-browser fake API',
      (WidgetTester tester) async {
    // This is exactly what the static Hugging Face Space runs — DemoApiClient
    // instead of the real backend — so it gets its own end-to-end test.
    await pumpApp(tester, api: DemoApiClient());

    await tester.enterText(
        find.byType(TextFormField).first, 'alan@fieldinspect.com');
    await tester.enterText(find.byType(TextFormField).last, 'password123');
    await tester.tap(find.text('Sign in'));

    await tester.pump(); // start the request (spinner appears)
    // Widget tests run on FAKE time: this doesn't sleep 600ms, it instantly
    // advances the clock past DemoApiClient's 450ms artificial latency.
    await tester.pump(const Duration(milliseconds: 600));

    expect(find.text('Welcome, Alan Tan'), findsOneWidget);
    expect(find.text('TECHNICIAN'), findsOneWidget);
  });
}

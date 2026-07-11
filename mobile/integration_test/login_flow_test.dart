import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

import 'package:field_inspect/main.dart' as app;

/// TRUE end-to-end test: runs ON the emulator, drives the real UI, and hits
/// the real Spring Boot + SQL Server backend over the network (Cypress-style,
/// where widget_test.dart is the React-Testing-Library layer).
///
/// Prerequisites: the fieldinspect-mssql container and the backend running on
/// the dev machine.  Run with:
///   flutter test integration_test -d emulator-5554
void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  /// pumpAndSettle() can't be used around network calls here: it waits for
  /// "no animations running", and our loading spinner animates forever while
  /// the request is in flight. So instead: keep pumping frames until the
  /// widget we want shows up (or give up after [timeout]).
  Future<void> pumpUntilFound(
    WidgetTester tester,
    Finder finder, {
    Duration timeout = const Duration(seconds: 20),
  }) async {
    final deadline = DateTime.now().add(timeout);
    while (DateTime.now().isBefore(deadline)) {
      await tester.pump(const Duration(milliseconds: 100));
      if (finder.evaluate().isNotEmpty) return; // found it — done
    }
    throw TestFailure('Timed out waiting for $finder');
  }

  testWidgets('full login flow against the live backend', (tester) async {
    app.main(); // boot the REAL app, exactly as a user launch would
    await tester.pump();

    // Secure storage is real on the emulator: a previous test run's session
    // survives. If we land on the home screen, log out first so the test is
    // repeatable — which conveniently also exercises logout.
    await pumpUntilFound(
      tester,
      find.byWidgetPredicate(
        (w) => w is FilledButton || (w is IconButton && w.tooltip == 'Sign out'),
      ),
    );
    final signOut = find.byTooltip('Sign out');
    if (signOut.evaluate().isNotEmpty) {
      await tester.tap(signOut);
      await pumpUntilFound(tester, find.text('Sign in'));
    }

    // Type the seeded demo credentials (DataSeeder on the backend).
    await tester.enterText(
        find.byType(TextFormField).first, 'alan@fieldinspect.com');
    await tester.enterText(find.byType(TextFormField).last, 'password123');
    await tester.tap(find.text('Sign in'));

    // POST /api/auth/login happens here, for real, over 10.0.2.2.
    await pumpUntilFound(tester, find.text('Welcome, Alan Tan'));

    // The role chip proves we parsed the LoginResponse, not just navigated.
    expect(find.text('TECHNICIAN'), findsOneWidget);
  });
}

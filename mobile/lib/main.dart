import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'auth/auth_controller.dart';
import 'auth/login_screen.dart';
import 'core/demo_api_client.dart';
import 'home/home_screen.dart';

/// Compile-time flag, baked in by `--dart-define=DEMO_MODE=true` at build
/// time (like REACT_APP_* env vars in a CRA build). It's a const, so in
/// normal builds the compiler removes the demo branch — and DemoApiClient
/// with it — entirely (tree-shaking).
const bool kDemoMode = bool.fromEnvironment('DEMO_MODE');

/// The entry point — Dart starts here, like index.js.
void main() {
  runApp( // runApp = ReactDOM.render: mount this widget tree on the screen
    // ChangeNotifierProvider = <AuthContext.Provider value={...}> at the very
    // top of the tree, so every screen can reach the AuthController.
    ChangeNotifierProvider(
      // `..` is the cascade operator: call restoreSession() on the freshly
      // created controller but hand the CONTROLLER to create, not the
      // Future the method returned. Kicks off the storage read at launch.
      // In demo builds the controller talks to the in-browser fake API;
      // passing null means "use the real ApiClient default".
      create: (_) =>
          AuthController(api: kDemoMode ? DemoApiClient() : null)
            ..restoreSession(),
      child: const FieldInspectApp(),
    ),
  );
}

class FieldInspectApp extends StatelessWidget {
  const FieldInspectApp({super.key});

  @override
  Widget build(BuildContext context) {
    // Subscribe to auth state: every notifyListeners() re-runs this build.
    final auth = context.watch<AuthController>();

    return MaterialApp(
      // MaterialApp = the app chrome: theming, navigation, text direction.
      title: 'FieldInspect',
      // One seed color generates the entire Material palette (buttons, chips,
      // focus rings...). Deep orange: hi-vis industrial, fits field work.
      theme: ThemeData(colorSchemeSeed: Colors.deepOrange),
      // STATE-DRIVEN ROUTING: no router, no redirects. The screen is a pure
      // function of auth status — the same pattern as
      //   {status === 'loggedIn' ? <App/> : <Login/>}
      // Login success -> notifyListeners -> this rebuilds -> HomeScreen.
      home: switch (auth.status) {
        AuthStatus.restoring => const _SplashScreen(),
        AuthStatus.loggedOut => const LoginScreen(),
        AuthStatus.loggedIn => const HomeScreen(),
      },
    );
  }
}

/// Shown only for the instant it takes to read secure storage at launch.
class _SplashScreen extends StatelessWidget {
  const _SplashScreen();

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(child: CircularProgressIndicator()),
    );
  }
}
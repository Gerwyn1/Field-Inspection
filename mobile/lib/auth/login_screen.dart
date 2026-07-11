import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../core/api_client.dart';
import 'auth_controller.dart';

/// NEW CONCEPT — StatefulWidget: a component with local state.
///
/// Flutter splits it into two classes:
///   * LoginScreen        — the immutable "props" part, rebuilt freely
///   * _LoginScreenState  — the part that SURVIVES rebuilds (like the closure
///                          a React function component keeps via hooks)
/// Local state lives in the State class and changes via setState(() {...}),
/// which is literally "run this mutation, then re-render me".
class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  // A Form + key gives us "run all field validators, are they happy?" —
  // closest React analogue is a react-hook-form handle.
  final _formKey = GlobalKey<FormState>();

  // TextEditingController = a ref to an input's live value (uncontrolled
  // input + ref in React terms). The alternative (onChanged + setState per
  // keystroke) is the controlled-input style; controllers are idiomatic here.
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();

  bool _submitting = false; // disables the button during the request
  String? _errorMessage; // shown under the form when login fails

  /// Widgets get destroyed; controllers hold platform resources and must be
  /// released — same job as a useEffect cleanup function.
  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    // validate() runs every TextFormField's `validator` below and draws the
    // red error texts. Bail out if any field is invalid.
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _submitting = true;
      _errorMessage = null;
    });

    try {
      // context.read = grab the AuthController WITHOUT subscribing to it
      // (we're in an event handler — no need to rebuild when it changes;
      // watch() in build() does the subscribing).
      await context.read<AuthController>().login(
            _emailController.text.trim(),
            _passwordController.text,
          );
      // No navigation here! main.dart watches AuthController and swaps the
      // screen the moment status becomes loggedIn — state-driven routing,
      // like conditionally rendering <Login/> vs <App/> off a context value.
    } on ApiException catch (e) {
      // `on TYPE catch` — catch clauses filtered by exception type, like
      // Java's `catch (ApiException e)`.
      setState(() {
        _errorMessage = e.statusCode == 401
            ? 'Wrong email or password.'
            : 'Server error (${e.statusCode}). Try again.';
      });
    } catch (_) {
      setState(() {
        _errorMessage = 'Cannot reach the server. Is the backend running?';
      });
    } finally {
      // The widget may already be gone (user navigated away mid-request);
      // calling setState on a dead widget throws — hence the mounted guard.
      if (mounted) setState(() => _submitting = false);
    }
  }

  /// build() = render(). Runs on every setState; must be fast and side-effect
  /// free. The deep nesting is normal Flutter — layout is widgets too
  /// (Padding, Column) instead of CSS.
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // Scaffold = the standard screen chassis: app bar slot, body, etc.
      body: Center(
        child: SingleChildScrollView(
          // keeps the form reachable when the keyboard covers half the screen
          padding: const EdgeInsets.all(32),
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 400),
            child: Form(
              key: _formKey,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Icon(Icons.fact_check_outlined, size: 64),
                  const SizedBox(height: 8), // SizedBox = a fixed-size spacer
                  Text(
                    'FieldInspect',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.headlineMedium,
                  ),
                  const SizedBox(height: 32),
                  TextFormField(
                    controller: _emailController,
                    decoration: const InputDecoration(
                      labelText: 'Email',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.emailAddress,
                    autofillHints: const [AutofillHints.email],
                    // validator: return null = valid, return a string = the
                    // error to show. Mirrors the @NotBlank/@Email annotations
                    // on the backend's LoginRequest.
                    validator: (value) =>
                        (value == null || value.trim().isEmpty)
                            ? 'Email is required'
                            : null,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _passwordController,
                    decoration: const InputDecoration(
                      labelText: 'Password',
                      border: OutlineInputBorder(),
                    ),
                    obscureText: true, // ***** instead of characters
                    validator: (value) => (value == null || value.isEmpty)
                        ? 'Password is required'
                        : null,
                    onFieldSubmitted: (_) => _submit(), // Enter key submits
                  ),
                  const SizedBox(height: 24),
                  FilledButton(
                    // onPressed: null disables the button — that's the
                    // convention, not a separate `disabled` flag.
                    onPressed: _submitting ? null : _submit,
                    child: _submitting
                        ? const SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Text('Sign in'),
                  ),
                  // Collection-if again: the error Text only exists in the
                  // tree when there is a message — {error && <p>...</p>}.
                  if (_errorMessage != null) ...[
                    const SizedBox(height: 16),
                    Text(
                      _errorMessage!,
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        color: Theme.of(context).colorScheme.error,
                      ),
                    ),
                  ],
                  const SizedBox(height: 32),
                  Text(
                    'Demo: alan@fieldinspect.com / password123',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

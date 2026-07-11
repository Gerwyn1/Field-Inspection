import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../auth/auth_controller.dart';

/// The screen you land on after login. StatelessWidget — it owns no state,
/// it just renders whatever AuthController holds (a pure function component).
/// Phase 3 replaces the placeholder with the real asset list.
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // watch() = useContext: read the value AND rebuild when it notifies.
    final auth = context.watch<AuthController>();
    // main.dart only shows HomeScreen when status is loggedIn, so a session
    // is guaranteed to exist — the ! cast is safe by construction.
    final session = auth.session!;

    return Scaffold(
      appBar: AppBar(
        title: const Text('FieldInspect'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Sign out',
            onPressed: () => context.read<AuthController>().logout(),
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Welcome, ${session.fullName}',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 8),
            // Chip = a small labeled pill; shows the role coming straight
            // out of the JWT login response.
            Chip(
              avatar: const Icon(Icons.badge_outlined, size: 18),
              label: Text(session.role),
            ),
            const SizedBox(height: 32),
            // Placeholder card so the shell doesn't feel empty — Phase 3
            // swaps this for the asset list fetched with the bearer token.
            Card(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Row(
                  children: [
                    Icon(
                      Icons.construction,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                    const SizedBox(width: 16),
                    const Expanded(
                      child: Text(
                        'Asset list coming in Phase 3 — this shell proves '
                        'login, secure token storage, and session restore.',
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

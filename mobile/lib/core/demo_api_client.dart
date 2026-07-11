import 'api_client.dart';

/// A fake backend that lives entirely in the browser — used ONLY by the
/// free static demo deployment (Hugging Face static Spaces can't run Java).
///
/// It impersonates the Spring Boot API at the ApiClient seam: AuthController
/// can't tell the difference, which is the payoff of coding against a narrow
/// interface (same reason tests could swap in InMemoryTokenStorage).
/// The real backend, with real JWTs and SQL Server, runs in dev and Docker.
class DemoApiClient extends ApiClient {
  DemoApiClient() : super(baseUrl: 'demo://in-browser');

  /// Same demo accounts DataSeeder creates on the real backend.
  /// (String, String) is a record — Dart's lightweight tuple.
  static const _users = <String, (String, String)>{
    'alan@fieldinspect.com': ('Alan Tan', 'TECHNICIAN'),
    'siti@fieldinspect.com': ('Siti Rahman', 'SUPERVISOR'),
  };

  @override
  Future<Map<String, dynamic>> postJson(
    String path,
    Map<String, dynamic> body, {
    String? token,
  }) async {
    // A touch of fake latency so loading spinners behave like production.
    await Future<void>.delayed(const Duration(milliseconds: 450));

    if (path == '/api/auth/login') {
      final user = _users[body['email']];
      if (user == null || body['password'] != 'password123') {
        throw ApiException(401, 'invalid credentials');
      }
      return {
        'token': 'demo-token-not-a-real-jwt',
        'fullName': user.$1, // record fields are accessed as $1, $2, ...
        'role': user.$2,
      };
    }
    throw ApiException(404, 'no demo handler for $path');
  }
}

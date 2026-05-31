# Agent Instructions: NotesFlow Dual-Platform Project

Welcome! This repository is part of the dual-platform NotesFlow ecosystem. Please read and follow these rules strictly when modifying the codebase.

## Ecosystem Architecture
- **Web client**: Next.js 16 (App Router) using `next-intl` for localized routes.
- **Android client**: Jetpack Compose, OkHttp, Gson, and Jetpack Security (Crypto).
- **Backend/DB**: Supabase (PostgreSQL with Row-Level Security enabled) and GraphQL.

## Developer Rules & Pitfalls to Avoid

### 1. Android Application (`notesflow-android`)
- **SharedPreferences Encryption**:
  - Never use plaintext `SharedPreferences` for sensitive data (e.g., auth tokens).
  - Use `EncryptedSharedPreferences` via `MasterKey.Builder` mapped to the name `"notesflow_secure_prefs"`.
  - **Warning**: Do not modify the pref name back to plaintext file names, or it will crash upgrades.
- **R8 Obfuscation & Minification**:
  - `isMinifyEnabled = true` is configured in release Gradle builds.
  - All serialized data models (under package `de.edittrich.notesflow.data.model.**`) and fields annotated with `@SerializedName` MUST be kept from shrinking/obfuscation. Keep rules are defined in `app/proguard-rules.pro`.
- **Resource Hoisting**:
  - Always hoist localized string resources using the `stringResource(R.string.xxx)` API at the top of `@Composable` functions instead of invoking `LocalContext.current.getString()` directly in validation loops or async scopes.
- **Network Traffic Rules**:
  - Cleartext (HTTP) traffic is forbidden globally (`android:usesCleartextTraffic="false"`).
  - Cleartext is allowed ONLY for local loopback `10.0.2.2` via `network_security_config.xml`.
- **Log Sanitization**:
  - Do not use raw `Log.d` or `Log.e` for sensitive session tokens or API payloads.
  - Route logs through the custom `logDebug` or `logError` helpers in `ApiClient` which filter log outputs using `BuildConfig.DEBUG` checks.

### 2. Next.js Web Application (`notesflow`)
- **HTTP Security Response Headers**:
  - Always return custom headers configured in `next.config.ts` (e.g., `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy`, and HSTS).
- **Dynamic Zod Schemas**:
  - Form validation schemas (e.g. login, signup, note form) MUST accept a closure `(t: (key: string) => string)` dynamically so that error messages translate automatically based on the user locale.
- **Supabase Row-Level Security (RLS)**:
  - Public tables (like `public.users`) must never have public read policies.
  - Enforce `USING (auth.uid() = id)` so users can only fetch or modify their own data.

## Reusable Agent Skills
- **multi-application-optimizer**: Check `/home/edittrich/.gemini/config/skills/multi-application-optimize/SKILL.md` (or the renamed location `/home/edittrich/.agents/skills/multi-application-optimize/SKILL.md`) for detailed optimization and verification scripts.

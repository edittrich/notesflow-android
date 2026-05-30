# NotesFlow Android App

NotesFlow Android is a native companion application for NotesFlow, built with Kotlin, Jetpack Compose (Material 3), and standard modern Android APIs. It integrates with the Next.js/Supabase backend via dynamic build profiles (product flavors) to offer a smooth mobile notes dashboard experience.

---

## Features

*   **Premium Material 3 UI**: Sleek dark/light theme switching based on user settings or system defaults.
*   **Dual Build Flavors (`dev` / `prod`)**: Easy toggling between local debugging and production deployments.
*   **Secure Authentication**: Standard Supabase Auth REST interface (sign in, sign up) with secure local session persistence.
*   **GraphQL Core Operations**: Query and mutate notes directly against the GraphQL `/api/graphql` endpoint with Bearer authentication.
*   **Search & Sorting**: Client-side query filter and remote sorting logic matching the web application.
*   **Dual Localization**: Fully localized in English and German.

---

## Build Flavors Configuration

The app supports two environments configured as product flavors in [app/build.gradle.kts](file:///home/edittrich/Documents/workspaces/git/notesflow-android/app/build.gradle.kts):

| Profile / Flavor | Base GraphQL API URL | Supabase Auth API URL | Suffix |
| :--- | :--- | :--- | :--- |
| **`dev`** | `http://10.0.2.2:3000/api/graphql` | `http://10.0.2.2:54321` | `.dev` |
| **`prod`** | `https://notesflow-edittrich.vercel.app/api/graphql` | `https://oehosgersafpqkhltebu.supabase.co` | *None* |

*Note: The `dev` flavor suffix (`.dev`) changes the application ID to `de.edittrich.dev` so both variants can be installed side-by-side on the same device.*

---

## Build & Run Instructions

Use Gradle commands from the root directory to compile and verify target builds:

### 1. Compile the APKs
*   **Development Variant**:
    ```bash
    ./gradlew assembleDevDebug
    ```
    Outputs APK to: `app/build/outputs/apk/dev/debug/app-dev-debug.apk`
*   **Production Variant**:
    ```bash
    ./gradlew assembleProdDebug
    ```
    Outputs APK to: `app/build/outputs/apk/prod/debug/app-prod-debug.apk`

### 2. Deploy to Emulator or Device
Use the `android` CLI tool (installed in the environment) to deploy compiled packages:
*   **Deploy Dev Variant**:
    ```bash
    android run --apks app/build/outputs/apk/dev/debug/app-dev-debug.apk
    ```
*   **Deploy Prod Variant**:
    ```bash
    android run --apks app/build/outputs/apk/prod/debug/app-prod-debug.apk
    ```

---

## Connecting to a Physical Device (USB Debugging)

To run this application on your physical Android phone:

1.  **Enable USB Debugging on your phone**:
    *   Open **Settings** -> **About Phone**.
    *   Tap **Build Number** 7 times until you unlock developer options.
    *   Go back to **Settings** -> **System** -> **Developer Options** and enable **USB Debugging**.
2.  **Connect to Computer**:
    *   Plug your phone into your computer via a USB cable.
    *   On your phone, select *"Allow USB Debugging"* (check *"Always allow from this computer"*).
3.  **Verify Connection**:
    ```bash
    adb devices
    ```
    Your device should appear in the attached list.
4.  **Run Prod Flavor**:
    We recommend using the **Production** build on physical devices as it accesses public endpoints over the internet:
    ```bash
    ./gradlew assembleProdDebug
    android run --apks app/build/outputs/apk/prod/debug/app-prod-debug.apk
    ```
5.  *(Optional)* **Run Dev Flavor**:
    If you want to debug against your local development server on a physical device:
    *   Make sure both computer and phone are on the **same Wi-Fi network**.
    *   Update the `dev` block configuration inside `app/build.gradle.kts` to use your computer's local Wi-Fi IP (e.g. `192.168.1.x`) instead of the emulator's `10.0.2.2` loopback.
    *   Compile and run:
        ```bash
        ./gradlew assembleDevDebug
        android run --apks app/build/outputs/apk/dev/debug/app-dev-debug.apk
        ```

---

## Project Folder Architecture

Inside `app/src/main/java/de/edittrich/`:
*   `data/`
    *   [ApiClient.kt](file:///home/edittrich/Documents/workspaces/git/notesflow-android/app/src/main/java/de/edittrich/data/ApiClient.kt): Manages Auth REST and GraphQL API queries using OkHttp/Gson and reads dynamic target configurations from `BuildConfig`.
    *   [SessionManager.kt](file:///home/edittrich/Documents/workspaces/git/notesflow-android/app/src/main/java/de/edittrich/data/SessionManager.kt): Securely reads and writes JWT tokens and language/theme settings to `SharedPreferences`.
*   `ui/`
    *   `theme/`: Dynamic custom colors, layouts, and typography.
    *   `auth/`: Jetpack Compose views for Login and Signup forms (with input verification).
    *   `dashboard/`: Grid dashboard displaying notes cards, sorting dropdowns, local queries filtering, and note dialog actions.
    *   `settings/`: Language and theme configuration options.
*   [MainActivity.kt](file:///home/edittrich/Documents/workspaces/git/notesflow-android/app/src/main/java/de/edittrich/MainActivity.kt): Navigation coordinator and dynamic language runtime context manager.

---

## Optimizations & Modern Best Practices

*   **Composition-Scoped Resource Retrieval**: Hoisted string resource queries to `@Composable` scope to resolve `LocalContextGetResourceValueCall` warnings, ensuring correct reactive invalidation when locales change.
*   **Bundle Language Splits Disabled**: Configured the build gradle bundle option `enableSplit = false` to support our dynamic in-app locale toggles without needing the Play Core library.
*   **KTX Extensions Integration**: Refactored `SessionManager` transactions to use standard SharedPreferences `edit { ... }` blocks from the `androidx.core:core-ktx` library.
*   **Typography Standards**: Cleaned up triple dots with standard HTML/XML entities (`&#8230;`) for proper typographical ellipsis.

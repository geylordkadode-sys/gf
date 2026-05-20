# berling marketHUB

## Project Overview

This is a native **Android marketplace application** built with Kotlin, Jetpack Compose, and Supabase. It is **not a web application** — it cannot run in Replit's web preview pane.

## Tech Stack

- **Language**: Kotlin (JVM 17)
- **UI Framework**: Jetpack Compose with Material 3
- **Local Database**: Room ORM (offline-first)
- **Backend**: Supabase (PostgreSQL, Auth, Storage, Edge Functions)
- **HTTP Client**: Retrofit + OkHttp
- **Dependency Injection**: Hilt (Dagger)
- **Background Sync**: WorkManager
- **Image Handling**: Coil + custom compression

## Running This Project

This is a native Android app. To run it, you need:

1. **Android Studio** (Arctic Fox or later)
2. **Android SDK 26+**
3. **JDK 17**
4. A physical Android device or emulator

### Setup Steps

1. Clone the repo into Android Studio
2. Create `local.properties` in the root with your Supabase credentials:
   ```properties
   sdk.dir=/path/to/android/sdk
   supabase_url=https://your-project.supabase.co
   supabase_anon_key=your-anon-key
   supabase_project_id=your-project-id
   ```
3. Run the Supabase migrations: `supabase db push --linked`
4. Deploy edge functions (send-otp, verify-otp, sync-data)
5. Build and run: `./gradlew build`

## Project Structure

```
app/src/main/kotlin/com/berling/marketplace/
├── app/              # Application class & initialization
├── data/
│   ├── local/        # Room database & DAOs
│   ├── remote/       # Supabase API definitions
│   ├── repository/   # Data repositories
│   └── sync/         # Background sync (WorkManager)
├── di/               # Hilt dependency injection modules
└── ui/
    ├── screens/      # Composable screens (auth, home, profile, chats, post, search)
    ├── theme/        # Material 3 color schemes & typography
    └── Navigation.kt # Navigation graph
supabase/
├── functions/        # Deno Edge Functions (TypeScript)
└── migrations/       # SQL schema & RLS policies
```

## User Preferences

- Maintain existing clean architecture pattern
- Keep offline-first approach with Room + Supabase sync
- Image compression target: 200-400 kbps before upload

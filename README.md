# berling marketHUB - Android Marketplace Application

A feature-rich Android marketplace app built with Kotlin, Jetpack Compose, and Supabase. Users can buy and sell products with offline-first sync capabilities.

## Features

✅ **Authentication**
- Email/Password sign up with OTP verification
- Magic link login
- Password reset with OTP
- Profile setup with social media links

✅ **Products & Marketplace**
- Browse featured products by categories
- Search products
- Add products to favorites
- Create new listings

✅ **User Profiles**
- Complete profile with photo, name, location
- Social media accounts (Instagram, Facebook, Twitter)
- Seller stats and ratings
- Follow/Unfollow sellers

✅ **Offline-First Architecture**
- Local database with Room
- Background sync to Supabase
- Optimistic updates

✅ **Image Management**
- Photo compression (200-400 kbps)
- Capacitor integration for file uploads
- Image resizing

✅ **Navigation**
- Home (Products Feed)
- Chats
- Create Listing
- Profile
- Search

## Project Structure

```
app/src/main/kotlin/com/berling/marketplace/
├── app/              # Application initialization
├── data/
│   ├── local/        # Room database & DAOs
│   ├── remote/       # Supabase API
│   ├── repository/   # Data repositories
│   └── sync/         # Background sync service
├── di/               # Dependency injection (Hilt)
├── domain/           # Business logic (optional)
└── ui/
    ├── screens/      # Composable screens
    │   ├── auth/     # Authentication screens
    │   ├── home/     # Home/products feed
    │   ├── profile/  # User profile
    │   ├── chats/    # Messages
    │   ├── post/     # Create listing
    │   └── search/   # Search products
    ├── theme/        # Material 3 theming
    └── Navigation.kt # Navigation setup
```

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room ORM
- **Backend**: Supabase (PostgreSQL + Auth + Edge Functions)
- **HTTP Client**: Retrofit + OkHttp
- **Serialization**: Kotlinx Serialization
- **DI**: Hilt
- **Coroutines**: Kotlin Coroutines
- **Storage**: DataStore Preferences
- **Image Loading**: Coil
- **File Upload**: Capacitor

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 26+
- JDK 17

### Configuration

1. Update `local.properties` with your Supabase credentials:
```properties
sdk.dir=/path/to/android/sdk
supabase_url=https://your-project.supabase.co
supabase_anon_key=your-anon-key
supabase_project_id=your-project-id
```

2. Build the project:
```bash
./gradlew build
```

### Supabase Setup

1. Run migrations to create tables:
```bash
supabase db push --linked
```

2. Deploy edge functions:
```bash
supabase functions deploy send-otp
supabase functions deploy verify-otp
supabase functions deploy sync-data
```

## API Endpoints

### Authentication
- `POST /auth/v1/signup` - Sign up
- `POST /auth/v1/verify` - Verify OTP
- `POST /auth/v1/recover` - Request password reset

### Products
- `GET /rest/v1/products` - List products
- `POST /rest/v1/products` - Create product
- `PATCH /rest/v1/products/{id}` - Update product
- `DELETE /rest/v1/products/{id}` - Delete product

### Users
- `GET /rest/v1/users` - Get profile
- `PATCH /rest/v1/users` - Update profile

## Background Sync

Products and user changes are stored locally first, then synced to Supabase in the background every 5 minutes when internet is available. Failed syncs are retried up to 3 times.

## Image Optimization

Photos are automatically compressed to 200-400 kbps for optimal performance and reduced storage.

## License

Proprietary - berling marketHUB

package com.berling.marketplace.di

import android.content.Context
import androidx.room.Room
import com.berling.marketplace.data.local.MarketplaceDatabase
import com.berling.marketplace.data.local.UserDao
import com.berling.marketplace.data.local.ProductDao
import com.berling.marketplace.data.local.FavoriteDao
import com.berling.marketplace.data.local.PendingSyncDao
import com.berling.marketplace.data.local.MessageDao
import com.berling.marketplace.data.local.ConversationDao
import com.berling.marketplace.data.local.AnalyticsDao
import com.berling.marketplace.data.local.OrderDao
import com.berling.marketplace.data.remote.SupabaseApi
import com.berling.marketplace.data.local.SecurePreferences
import com.berling.marketplace.data.repository.AuthRepository
import com.berling.marketplace.data.repository.ProductRepository
import com.berling.marketplace.data.repository.MessageRepository
import com.berling.marketplace.data.repository.OrderRepository
import com.berling.marketplace.data.repository.AnalyticsRepository
import com.berling.marketplace.data.repository.PaymentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMarketplaceDatabase(
        @ApplicationContext context: Context
    ): MarketplaceDatabase {
        return Room.databaseBuilder(
            context,
            MarketplaceDatabase::class.java,
            "berling_marketplace_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: MarketplaceDatabase) = database.userDao()

    @Provides
    @Singleton
    fun provideProductDao(database: MarketplaceDatabase) = database.productDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(database: MarketplaceDatabase) = database.favoriteDao()

    @Provides
    @Singleton
    fun providePendingSyncDao(database: MarketplaceDatabase) = database.pendingSyncDao()

    @Provides
    @Singleton
    fun provideMessageDao(database: MarketplaceDatabase) = database.messageDao()

    @Provides
    @Singleton
    fun provideConversationDao(database: MarketplaceDatabase) = database.conversationDao()

    @Provides
    @Singleton
    fun provideAnalyticsDao(database: MarketplaceDatabase) = database.analyticsDao()

    @Provides
    @Singleton
    fun provideOrderDao(database: MarketplaceDatabase) = database.orderDao()

    @Provides
    @Singleton
    fun provideSecurePreferences(
        @ApplicationContext context: Context
    ): SecurePreferences = SecurePreferences(context)
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val SUPABASE_URL = "https://fkeuioagahwqgpqjuwqj.supabase.co/"

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZrZXVpb2FnYWh3cWdwcWp1d3FqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkxOTE2NzgsImV4cCI6MjA5NDc2NzY3OH0.rRPejxJ4lVae57Y5IYoBA1dSYCWB24jSVxymZe7bqow")
                    .header("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideSupabaseApi(retrofit: Retrofit): SupabaseApi {
        return retrofit.create(SupabaseApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        api: SupabaseApi,
        userDao: UserDao
    ): AuthRepository = AuthRepository(api, userDao)

    @Provides
    @Singleton
    fun provideProductRepository(
        api: SupabaseApi,
        productDao: ProductDao,
        favoriteDao: FavoriteDao,
        pendingSyncDao: PendingSyncDao
    ): ProductRepository = ProductRepository(api, productDao, favoriteDao, pendingSyncDao)

    @Provides
    @Singleton
    fun provideMessageRepository(
        messageDao: MessageDao,
        conversationDao: ConversationDao,
        api: SupabaseApi
    ): MessageRepository = MessageRepository(messageDao, conversationDao, api)

    @Provides
    @Singleton
    fun provideOrderRepository(
        orderDao: OrderDao,
        api: SupabaseApi
    ): OrderRepository = OrderRepository(orderDao, api)

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        analyticsDao: AnalyticsDao,
        api: SupabaseApi
    ): AnalyticsRepository = AnalyticsRepository(analyticsDao, api)

    @Provides
    @Singleton
    fun providePaymentRepository(
        api: SupabaseApi,
        securePrefs: SecurePreferences
    ): PaymentRepository = PaymentRepository(api, securePrefs)
}


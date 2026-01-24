package com.serqfix.partner.di

import android.content.Context
import com.serqfix.partner.data.api.*
import com.serqfix.partner.data.local.UserPreferencesDataStore
import com.serqfix.partner.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAuthApiService(): AuthApiService {
        return ApiModule.createService()
    }
    
    @Provides
    @Singleton
    fun provideBookingApiService(): BookingApiService {
        return ApiModule.createService()
    }
    
    @Provides
    @Singleton
    fun providePaymentApiService(): PaymentApiService {
        return ApiModule.createService()
    }
    
    @Provides
    @Singleton
    fun provideAMCApiService(): AMCApiService {
        return ApiModule.createService()
    }
    
    @Provides
    @Singleton
    fun provideProfileApiService(): ProfileApiService {
        return ApiModule.createService()
    }
    
    @Provides
    @Singleton
    fun provideTicketApiService(): TicketApiService {
        return ApiModule.createService()
    }
    
    @Provides
    @Singleton
    fun provideWalletApiService(): WalletApiService {
        return ApiModule.createService()
    }
    
    @Provides
    @Singleton
    fun provideDashboardApiService(): DashboardApiService {
        return ApiModule.createService()
    }
    
    @Provides
    @Singleton
    fun provideAvailabilityApiService(): AvailabilityApiService {
        return ApiModule.createService()
    }
    
    @Provides
    @Singleton
    fun provideBookingCompletionApiService(): BookingCompletionApiService {
        return ApiModule.createService()
    }
    
    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(@ApplicationContext context: Context): UserPreferencesDataStore {
        return UserPreferencesDataStore(context)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(authApiService: AuthApiService): AuthRepository {
        return AuthRepository()
    }
    
    @Provides
    @Singleton
    fun provideBookingRepository(bookingApiService: BookingApiService): BookingRepository {
        return BookingRepository()
    }
    
    @Provides
    @Singleton
    fun providePaymentRepository(paymentApiService: PaymentApiService): PaymentRepository {
        return PaymentRepository()
    }
    
    @Provides
    @Singleton
    fun provideAMCRepository(amcApiService: AMCApiService): AMCRepository {
        return AMCRepository()
    }
    
    @Provides
    @Singleton
    fun provideProfileRepository(profileApiService: ProfileApiService): ProfileRepository {
        return ProfileRepository()
    }
    
    @Provides
    @Singleton
    fun provideTicketRepository(ticketApiService: TicketApiService): TicketRepository {
        return TicketRepository()
    }
    
    @Provides
    @Singleton
    fun provideWalletRepository(walletApiService: WalletApiService): WalletRepository {
        return WalletRepository()
    }
    
    @Provides
    @Singleton
    fun provideDashboardRepository(dashboardApiService: DashboardApiService): DashboardRepository {
        return DashboardRepository()
    }
    
    @Provides
    @Singleton
    fun provideAvailabilityRepository(availabilityApiService: AvailabilityApiService): AvailabilityRepository {
        return AvailabilityRepository()
    }
    
    @Provides
    @Singleton
    fun provideBookingCompletionRepository(bookingCompletionApiService: BookingCompletionApiService): BookingCompletionRepository {
        return BookingCompletionRepository()
    }
}

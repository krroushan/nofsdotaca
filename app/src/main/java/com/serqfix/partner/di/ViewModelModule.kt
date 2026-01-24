package com.serqfix.partner.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.serqfix.partner.data.local.UserPreferencesDataStore
import com.serqfix.partner.data.repository.*
import com.serqfix.partner.ui.viewmodel.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Provider

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    
    @Provides
    @ViewModelScoped
    fun provideAuthViewModel(
        authRepository: AuthRepository,
        userPreferences: UserPreferencesDataStore
    ): AuthViewModel {
        return AuthViewModel(authRepository, userPreferences)
    }
    
    @Provides
    @ViewModelScoped
    fun provideBookingViewModel(
        bookingRepository: BookingRepository
    ): BookingViewModel {
        return BookingViewModel(bookingRepository)
    }
    
    @Provides
    @ViewModelScoped
    fun provideProfileViewModel(
        profileRepository: ProfileRepository
    ): ProfileViewModel {
        return ProfileViewModel(profileRepository)
    }
    
    @Provides
    @ViewModelScoped
    fun provideWalletViewModel(
        walletRepository: WalletRepository
    ): WalletViewModel {
        return WalletViewModel(walletRepository)
    }
}

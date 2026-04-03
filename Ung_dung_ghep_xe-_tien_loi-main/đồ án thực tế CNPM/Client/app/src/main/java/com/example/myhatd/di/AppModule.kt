package com.example.myhatd.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Cài đặt vào component tồn tại suốt app
object AppModule {

    /**
     * Hàm này "dạy" Hilt cách tạo ra một đối tượng Gson.
     * Hilt sẽ chỉ tạo một instance duy nhất vì có @Singleton.
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
}
package com.facucastro.focusguard.di

import com.apollographql.apollo.ApolloClient
import com.facucastro.focusguard.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GraphQLModule {

    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(BuildConfig.GRAPHQL_URL)
            .build()
    }
}

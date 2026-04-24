package com.facucastro.focusguard.di

import android.content.Context
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.mockserver.MockResponse
import com.apollographql.mockserver.MockServer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

private const val TAG = "GraphQLModule"

@Module
@InstallIn(SingletonComponent::class)
object GraphQLModule {

    @Provides
    @Singleton
    fun provideApolloClient(
        @ApplicationContext context: Context,
    ): ApolloClient {
        val mockJson = context.assets
            .open("mock_weekly_ranking.json")
            .bufferedReader()
            .use { it.readText() }

        val server = MockServer()

        repeat(100) {
            server.enqueue(
                MockResponse.Builder()
                    .statusCode(200)
                    .addHeader("Content-Type", "application/json")
                    .body(mockJson)
                    .build()
            )
        }

        val serverUrl = runBlocking(Dispatchers.IO) { server.url() }
        Log.d(TAG, "MockServer ready at $serverUrl")

        return ApolloClient.Builder()
            .serverUrl(serverUrl)
            .build()
    }
}

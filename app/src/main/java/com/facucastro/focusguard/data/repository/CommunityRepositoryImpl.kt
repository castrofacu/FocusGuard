package com.facucastro.focusguard.data.repository

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.facucastro.focusguard.data.remote.graphql.mapper.toDomain
import com.facucastro.focusguard.domain.model.CommunityRanking
import com.facucastro.focusguard.domain.repository.CommunityRepository
import com.facucastro.focusguard.graphql.GetWeeklyRankingQuery
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CommunityRepo"

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val apolloClient: ApolloClient,
) : CommunityRepository {

    override suspend fun getWeeklyRanking(): Result<List<CommunityRanking>> {
        return try {
            val response = apolloClient.query(GetWeeklyRankingQuery()).execute()

            if (response.exception != null) {
                Log.e(TAG, "Apollo exception: ${response.exception}")
                return Result.failure(response.exception!!)
            }

            if (response.hasErrors()) {
                val errorMessage = response.errors
                    ?.joinToString(separator = "; ") { it.message }
                    ?: "Unknown GraphQL error"
                Log.e(TAG, "GraphQL errors: $errorMessage")
                return Result.failure(RuntimeException(errorMessage))
            }

            if (response.data == null) {
                Log.e(TAG, "Response data is null")
                return Result.failure(RuntimeException("Response data is null"))
            }

            val rankings = response.data!!.getWeeklyRanking.map { it.toDomain() }
            Log.d(TAG, "Loaded ${rankings.size} rankings")
            Result.success(rankings)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception: $e")
            Result.failure(e)
        }
    }
}

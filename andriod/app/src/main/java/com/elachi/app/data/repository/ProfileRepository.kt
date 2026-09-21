package com.elachi.app.data.repository

import com.elachi.app.data.remote.ApiService
import com.elachi.app.data.remote.dto.UpdateProfileRequest
import com.elachi.app.data.remote.dto.UserProfileDto

//This class handles the profile data.

class ProfileRepository(private val api: ApiService) {

    suspend fun getMyProfile(): Result<UserProfileDto> = try {
        val response = api.getMyProfile()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Couldn't load your profile (HTTP ${response.code()})."))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Couldn't reach the server. Check your connection."))
    }

    suspend fun updateProfile(
        displayName: String? = null,
        bio: String? = null,
        avatarUrl: String? = null,
        cookingInterests: List<String>? = null,
        dietaryRestrictions: List<String>? = null,
    ): Result<UserProfileDto> = try {
        val response = api.updateProfile(UpdateProfileRequest(displayName, bio, avatarUrl, cookingInterests, dietaryRestrictions))
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Couldn't save your profile (HTTP ${response.code()})."))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Couldn't reach the server. Check your connection."))
    }
}
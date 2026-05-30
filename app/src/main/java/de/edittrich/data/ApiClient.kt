package de.edittrich.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import de.edittrich.data.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class AuthResult {
    data class Success(val accessToken: String, val refreshToken: String, val userId: String, val email: String) : AuthResult()
    data class SuccessVerificationRequired(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class ApiClient(private val context: android.content.Context) {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        // Localhost mapping for Android Emulator. Change to server IP for physical devices.
        const val BASE_GRAPHQL_URL = "http://10.0.2.2:3000/api/graphql"
        const val BASE_SUPABASE_URL = "http://10.0.2.2:54321"
        const val SUPABASE_ANON_KEY = "sb_publishable_ACJWlzQHlZjBrEguHvfOxg_3BJgxAaH"
        
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val url = "$BASE_SUPABASE_URL/auth/v1/token?grant_type=password"
        val requestBodyJson = JsonObject().apply {
            addProperty("email", email)
            addProperty("password", password)
        }

        val request = Request.Builder()
            .url(url)
            .post(gson.toJson(requestBodyJson).toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonObj = gson.fromJson(responseBody, JsonObject::class.java)
                    val accessToken = jsonObj.get("access_token")?.asString ?: ""
                    val refreshToken = jsonObj.get("refresh_token")?.asString ?: ""
                    val userObj = jsonObj.getAsJsonObject("user")
                    val userId = userObj?.get("id")?.asString ?: ""
                    val userEmail = userObj?.get("email")?.asString ?: email
                    
                    sessionManager.saveSession(accessToken, refreshToken, userId, userEmail)
                    AuthResult.Success(accessToken, refreshToken, userId, userEmail)
                } else {
                    val errorMsg = parseSupabaseError(responseBody)
                    AuthResult.Error(errorMsg)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun signup(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val url = "$BASE_SUPABASE_URL/auth/v1/signup"
        val requestBodyJson = JsonObject().apply {
            addProperty("email", email)
            addProperty("password", password)
        }

        val request = Request.Builder()
            .url(url)
            .post(gson.toJson(requestBodyJson).toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonObj = gson.fromJson(responseBody, JsonObject::class.java)
                    
                    // Supabase signup might return a session directly if auto-confirm is enabled
                    val sessionObj = jsonObj.getAsJsonObject("session")
                    if (sessionObj != null) {
                        val accessToken = sessionObj.get("access_token")?.asString ?: ""
                        val refreshToken = sessionObj.get("refresh_token")?.asString ?: ""
                        val userObj = jsonObj.getAsJsonObject("user")
                        val userId = userObj?.get("id")?.asString ?: ""
                        val userEmail = userObj?.get("email")?.asString ?: email
                        
                        sessionManager.saveSession(accessToken, refreshToken, userId, userEmail)
                        AuthResult.Success(accessToken, refreshToken, userId, userEmail)
                    } else {
                        AuthResult.SuccessVerificationRequired("Registration successful! Please check your email.")
                    }
                } else {
                    val errorMsg = parseSupabaseError(responseBody)
                    AuthResult.Error(errorMsg)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error occurred")
        }
    }

    private fun parseSupabaseError(responseBody: String?): String {
        if (responseBody.isNullOrEmpty()) return "Unknown server error"
        return try {
            val jsonObj = gson.fromJson(responseBody, JsonObject::class.java)
            jsonObj.get("msg")?.asString 
                ?: jsonObj.get("error_description")?.asString 
                ?: jsonObj.get("message")?.asString 
                ?: "Server authentication failed"
        } catch (e: Exception) {
            "Auth error occurred"
        }
    }

    // Generic GraphQL Request Executor
    private suspend fun <T> executeGraphQL(
        query: String,
        variables: Map<String, Any?>?,
        responseParser: (JsonObject) -> T
    ): T = withContext(Dispatchers.IO) {
        val token = sessionManager.accessToken ?: throw IOException("Unauthorized: No session token found")

        val payload = JsonObject().apply {
            addProperty("query", query)
            if (variables != null) {
                val varsJson = gson.toJsonTree(variables).asJsonObject
                add("variables", varsJson)
            }
        }

        val request = Request.Builder()
            .url(BASE_GRAPHQL_URL)
            .post(gson.toJson(payload).toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful || responseBody == null) {
                throw IOException("GraphQL network call failed: ${response.code}")
            }

            val jsonObj = gson.fromJson(responseBody, JsonObject::class.java)
            
            // Check for GraphQL errors
            val errorsArr = jsonObj.getAsJsonArray("errors")
            if (errorsArr != null && errorsArr.size() > 0) {
                val firstError = errorsArr.get(0).asJsonObject
                val message = firstError.get("message")?.asString ?: "GraphQL execution error"
                throw IOException(message)
            }

            val dataObj = jsonObj.getAsJsonObject("data") 
                ?: throw IOException("Invalid response structure: data field missing")
            
            responseParser(dataObj)
        }
    }

    suspend fun getNotes(sortBy: String): List<Note> {
        val query = """
            query GetNotes(${"$"}sortBy: String) {
              getNotes(sortBy: ${"$"}sortBy) {
                id
                title
                description
                createdAt
                updatedAt
              }
            }
        """.trimIndent()

        val variables = mapOf("sortBy" to sortBy)

        return executeGraphQL(query, variables) { data ->
            val notesArr = data.getAsJsonArray("getNotes")
            val list = mutableListOf<Note>()
            for (elem in notesArr) {
                val note = gson.fromJson(elem, Note::class.java)
                list.add(note)
            }
            list
        }
    }

    suspend fun createNote(title: String, description: String): Note {
        val query = """
            mutation CreateNote(${"$"}title: String!, ${"$"}description: String!) {
              createNote(title: ${"$"}title, description: ${"$"}description) {
                id
                title
                description
                createdAt
                updatedAt
              }
            }
        """.trimIndent()

        val variables = mapOf("title" to title, "description" to description)

        return executeGraphQL(query, variables) { data ->
            val noteJson = data.getAsJsonObject("createNote")
            gson.fromJson(noteJson, Note::class.java)
        }
    }

    suspend fun updateNote(id: String, title: String?, description: String?): Note {
        val query = """
            mutation UpdateNote(${"$"}id: ID!, ${"$"}title: String, ${"$"}description: String) {
              updateNote(id: ${"$"}id, title: ${"$"}title, description: ${"$"}description) {
                id
                title
                description
                createdAt
                updatedAt
              }
            }
        """.trimIndent()

        val variables = mutableMapOf<String, Any?>("id" to id)
        if (title != null) variables["title"] = title
        if (description != null) variables["description"] = description

        return executeGraphQL(query, variables) { data ->
            val noteJson = data.getAsJsonObject("updateNote")
            gson.fromJson(noteJson, Note::class.java)
        }
    }

    suspend fun deleteNote(id: String): Boolean {
        val query = """
            mutation DeleteNote(${"$"}id: ID!) {
              deleteNote(id: ${"$"}id) {
                id
              }
            }
        """.trimIndent()

        val variables = mapOf("id" to id)

        return executeGraphQL(query, variables) { data ->
            val deleteNoteObj = data.getAsJsonObject("deleteNote")
            deleteNoteObj != null && deleteNoteObj.has("id")
        }
    }
}

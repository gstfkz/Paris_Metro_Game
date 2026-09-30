package com.gstfkz.parismetrogame.data.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class GitHubAssetDto(
    val name: String,
    val browser_download_url: String,
    val size: Long = 0
)

@Serializable
data class GitHubReleaseDto(
    val tag_name: String,
    val html_url: String,
    val name: String? = null,
    val body: String? = null,
    val assets: List<GitHubAssetDto> = emptyList()
)

data class UpdateInfo(
    val latestVersion: String = "",
    val url: String = "",
    val downloadUrl: String = "",
    val downloadSize: Long = 0,
    val changelog: String = "",
    val updateAvailable: Boolean = false,
    val errorMessage: String? = null
)

class GitHubReleaseClient(private val repo: String) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun check(current: String): UpdateInfo {
        if (repo.isBlank()) return UpdateInfo(errorMessage = "GitHub repository is not configured.")
        return try {
            val response = get("https://api.github.com/repos/$repo/releases/latest")
            if (response.first !in 200..299) {
                return UpdateInfo(errorMessage = "GitHub update check failed (HTTP ${response.first}). Make sure a GitHub Release exists and the repository is public.")
            }
            val release = json.decodeFromString<GitHubReleaseDto>(response.second)
            val version = release.tag_name.removePrefix("v")
            val apk = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            UpdateInfo(
                latestVersion = version,
                url = release.html_url,
                downloadUrl = apk?.browser_download_url.orEmpty(),
                downloadSize = apk?.size ?: 0,
                changelog = release.body.orEmpty(),
                updateAvailable = isNewer(version, current)
            )
        } catch (e: Exception) {
            UpdateInfo(errorMessage = e.message ?: "Network error while checking for updates.")
        }
    }

    private fun get(url: String): Pair<Int, String> {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Paris-Metro-Game")
            .build()
        return client.newCall(request).execute().use { it.code to it.body?.string().orEmpty() }
    }

    private fun isNewer(a: String, b: String): Boolean {
        val x = a.split(".").map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val y = b.split(".").map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(x.size, y.size)) {
            val xi = x.getOrElse(i) { 0 }
            val yi = y.getOrElse(i) { 0 }
            if (xi != yi) return xi > yi
        }
        return false
    }
}

package com.gstfkz.parismetrogame.data.network
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
@Serializable data class GitHubReleaseDto(val tag_name:String,val html_url:String,val name:String?=null)
@Serializable data class GitHubTagDto(val name:String)
@Serializable data class GitHubRepoDto(val default_branch:String="main",val html_url:String="")
data class UpdateInfo(val latestVersion:String="",val url:String="",val updateAvailable:Boolean=false,val errorMessage:String?=null)
class GitHubReleaseClient(private val repo:String){
 private val client=OkHttpClient();private val json=Json{ignoreUnknownKeys=true}
 fun check(current:String):UpdateInfo{if(repo.isBlank())return UpdateInfo(errorMessage="GitHub repository is not configured.");return try{
  release()?.let{return it.copy(updateAvailable=isNewer(it.latestVersion,current))}
  tag()?.let{return UpdateInfo(it,"https://github.com/$repo/releases",isNewer(it,current))}
  sourceVersion()?.let{return UpdateInfo(it,"https://github.com/$repo",isNewer(it,current))}
  UpdateInfo(errorMessage="No version information was found on GitHub.")
 }catch(e:Exception){UpdateInfo(errorMessage=e.message?:"Network error while checking for updates.")}}
 private fun release():UpdateInfo?{val r=get("https://api.github.com/repos/$repo/releases/latest");if(r.first !in 200..299)return null;val d=json.decodeFromString<GitHubReleaseDto>(r.second);return UpdateInfo(d.tag_name.removePrefix("v"),d.html_url)}
 private fun tag():String?{val r=get("https://api.github.com/repos/$repo/tags?per_page=1");if(r.first !in 200..299)return null;return json.decodeFromString(ListSerializer(GitHubTagDto.serializer()),r.second).firstOrNull()?.name?.removePrefix("v")}
 private fun sourceVersion():String?{val rr=get("https://api.github.com/repos/$repo");if(rr.first !in 200..299)return null;val meta=json.decodeFromString<GitHubRepoDto>(rr.second);val raw=get("https://raw.githubusercontent.com/$repo/${meta.default_branch}/app/build.gradle.kts");if(raw.first !in 200..299)return null;return Regex("versionName\\s*=\\s*\\\"([^\\\"]+)\\\"").find(raw.second)?.groupValues?.get(1)}
 private fun get(url:String):Pair<Int,String>{val req=Request.Builder().url(url).header("Accept","application/vnd.github+json").header("User-Agent","Paris-Metro-Game").build();return client.newCall(req).execute().use{it.code to (it.body?.string().orEmpty())}}
 private fun isNewer(a:String,b:String):Boolean{val x=a.split(".").map{it.filter(Char::isDigit).toIntOrNull()?:0};val y=b.split(".").map{it.filter(Char::isDigit).toIntOrNull()?:0};for(i in 0 until maxOf(x.size,y.size)){val xi=x.getOrElse(i){0};val yi=y.getOrElse(i){0};if(xi!=yi)return xi>yi};return false}
}

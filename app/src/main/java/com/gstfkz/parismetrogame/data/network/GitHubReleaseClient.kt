package com.gstfkz.parismetrogame.data.network
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
@Serializable data class GitHubReleaseDto(val tag_name:String,val html_url:String,val name:String?=null)
data class UpdateInfo(val latestVersion:String,val url:String,val updateAvailable:Boolean)
class GitHubReleaseClient(private val repo:String){
 private val client=OkHttpClient(); private val json=Json{ignoreUnknownKeys=true}
 fun check(current:String):UpdateInfo?{if(repo.isBlank())return null;val req=Request.Builder().url("https://api.github.com/repos/$repo/releases/latest").header("Accept","application/vnd.github+json").build();client.newCall(req).execute().use{r->if(!r.isSuccessful)return null;val dto=json.decodeFromString<GitHubReleaseDto>(r.body?.string()?:return null);val latest=dto.tag_name.removePrefix("v");return UpdateInfo(latest,dto.html_url,isNewer(latest,current))}}
 private fun isNewer(a:String,b:String):Boolean{val x=a.split(".").map{it.filter(Char::isDigit).toIntOrNull()?:0};val y=b.split(".").map{it.filter(Char::isDigit).toIntOrNull()?:0};for(i in 0 until maxOf(x.size,y.size)){val xi=x.getOrElse(i){0};val yi=y.getOrElse(i){0};if(xi!=yi)return xi>yi};return false}
}

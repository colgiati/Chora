package com.craftworks.music.lyrics

import android.util.Log
import com.craftworks.music.data.Lyric
import com.craftworks.music.data.PlainLyrics
import com.craftworks.music.data.SyncedLyric
import com.craftworks.music.player.SongHelper
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private var isGetLyricsRunning = false

fun getLyrics(){
    Log.d("LYRICS", "is lyrics running? $isGetLyricsRunning")

    if (isGetLyricsRunning)
        return

    isGetLyricsRunning = true

    val thread = Thread {
        try {
            if (SongHelper.currentSong.title.isBlank() ||
                SongHelper.currentSong.title == "Song Title" ||
                SongHelper.currentSong.isRadio == true)
                return@Thread

            PlainLyrics = "Getting Lyrics..."
            SyncedLyric.clear()

            val url = URL("https://lrclib.net/api/get?artist_name=${SongHelper.currentSong.artist.replace(" ", "+")}&track_name=${SongHelper.currentSong.title.replace(" ", "+")}&album_name=${SongHelper.currentSong.album.replace(" ", "+")}&duration=${SongHelper.currentSong.duration.div(1000)}")

            with(url.openConnection() as HttpsURLConnection) {
                requestMethod = "GET"

                // Set User-Agent as per LRCLIB documentation: https://lrclib.net/docs
                setRequestProperty("User-Agent", "Chora - Navidrome Client (https://github.com/CraftWorksMC/Chora)")

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                if (responseCode == 404){
                    PlainLyrics = "No Lyrics / Instrumental"
                    return@Thread
                }

                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        //println(line)
                        val jsonObject = JSONObject(line)

                        /* TRY GETTING SYNCED. IF NOT FALLBACK TO PLAIN */
                        if (jsonObject.getString("syncedLyrics") != "null") {
                            val syncedLyrics = jsonObject.getString("syncedLyrics")
                            syncedLyrics.lines().forEach { lyric ->
                                val timeStampsRaw = getTimeStamps(lyric)[0]
                                val time = mmssToMilliseconds(timeStampsRaw)
                                val lyricText: String = lyric.drop(11)

                                // Check for duplicates
                                val syncedLyric = Lyric(time.toInt(),lyricText, false)
                                if (!SyncedLyric.contains(syncedLyric))
                                    SyncedLyric.add(syncedLyric)
                            }
                        }else {
                            /* FALLBACK PLAIN LYRICS */
                            PlainLyrics = jsonObject.getString("plainLyrics")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            Log.d("LYRICS", "Reset isGetLyricsRunning")
            isGetLyricsRunning = false
        }

    }

    thread.start()
}


/* FUNCTIONS */
fun mmssToMilliseconds(mmss: String): Long {
    val parts = mmss.split(":", ".")
    if (parts.size == 3) {
        try {
            val minutes = parts[0].toLong()
            val seconds = parts[1].toLong()
            val ms = parts[2].toLong()
            return (minutes * 60 + seconds) * 1000 + ms * 10
        } catch (e: NumberFormatException) {
            // Handle the case where the input is not in the expected format
            e.printStackTrace()
        }
    }
    return 0L
}
fun getTimeStamps(input: String): List<String> {
    val regex = Regex("\\[(.*?)]")
    val matches = regex.findAll(input)

    val result = mutableListOf<String>()
    for (match in matches) {
        result.add(match.groupValues[1])
    }

    return result
}

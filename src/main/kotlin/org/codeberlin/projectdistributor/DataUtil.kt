package org.codeberlin.projectdistributor

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.codeberlin.projectdistributor.data.ProjectData
import java.io.File
import java.io.InputStream

object DataUtil {
    val gson: Gson by lazy { GsonBuilder().create() }

    fun fromJson(json: String): ProjectData {
        return gson.fromJson(json, ProjectData::class.java)
    }

    fun fromStream(stream: InputStream): ProjectData {
        return stream.bufferedReader().use { gson.fromJson(it, ProjectData::class.java) }
    }

    fun fromFile(path: String): ProjectData {
        return File(path).bufferedReader().use { gson.fromJson(it, ProjectData::class.java) }
    }
}

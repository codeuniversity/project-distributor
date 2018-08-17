package org.codeberlin.projectdistributor

import com.google.gson.GsonBuilder
import org.codeberlin.projectdistributor.data.ProjectData
import java.io.InputStream

object DataUtil {
    fun fromJson(json: String): ProjectData {
        return GsonBuilder().create().fromJson(json, ProjectData::class.java)
    }

    fun fromStream(stream: InputStream): ProjectData {
        return GsonBuilder().create().fromJson(stream.bufferedReader(), ProjectData::class.java)
    }
}

package com.tencent.devops.common.archive.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.devops.common.archive.api.pojo.JFrogProperties
import com.tencent.devops.common.archive.util.JFrogUtil
import com.tencent.devops.common.api.util.OkhttpUtils
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JFrogPropertiesApi @Autowired constructor(
    jFrogConfigProperties: JFrogConfigProperties,
    private val objectMapper: ObjectMapper
) {
//    private val okHttpClient = okhttp3.OkHttpClient.Builder()
//            .connectTimeout(5L, TimeUnit.SECONDS)
//            .readTimeout(60L, TimeUnit.SECONDS)
//            .writeTimeout(60L, TimeUnit.SECONDS)
//            .build()
    private val baseUrl = jFrogConfigProperties.url!!
    private val credential = JFrogUtil.makeCredential(jFrogConfigProperties.username!!, jFrogConfigProperties.password!!)

    fun getProperties(path: String): Map<String, List<String>> {
        val url = "$baseUrl/api/storage/$path?properties"
        val request = Request.Builder()
                .url(url)
                .header("Authorization", credential)
                .get()
                .build()

//        val httpClient = okHttpClient.newBuilder().build()
//        httpClient.newCall(request).execute().use { response ->
        OkhttpUtils.doHttp(request).use { response ->
            val responseContent = response.body()!!.string()
            if (!response.isSuccessful) {
                if (response.code() == 404) {
                    logger.warn("Get 404 code from url $url")
                    return mutableMapOf()
                }
                logger.error("Fail to get jfrog properties $path. $responseContent")
                throw RuntimeException("Fail to get jfrog properties")
            }

            val jFrogProperties = objectMapper.readValue<JFrogProperties>(responseContent)
            return jFrogProperties.properties
        }
    }

    fun setProperties(path: String, properties: Map<String, List<String>>, recursive: Boolean = false) {
        if (properties.isEmpty()) return

        val recursiveInt = if (recursive) 1 else 0
        val url = "$baseUrl/api/storage/$path?properties=${encodeProperties(properties)}&recursive=$recursiveInt"
        val request = Request.Builder()
                .url(url)
                .header("Authorization", credential)
                .put(RequestBody.create(MediaType.parse("application/json"), ""))
                .build()
//        val httpClient = okHttpClient.newBuilder().build()
//        httpClient.newCall(request).execute().use { response ->
        OkhttpUtils.doHttp(request).use { response ->
            val responseContent = response.body()!!.string()
            if (!response.isSuccessful) {
                logger.error("Fail to set jfrog properties $path. $responseContent")
                throw RuntimeException("Fail to set jfrog properties")
            }
        }
    }

    fun deleteProperties(path: String, propertyKeys: List<String>, recursive: Boolean = false) {
        if (propertyKeys.isEmpty()) return

        val recursiveInt = if (recursive) 1 else 0
        val url = "$baseUrl/api/storage/$path?properties=${propertyKeys.joinToString(",")}&recursive=$recursiveInt"
        val request = Request.Builder()
                .url(url)
                .header("Authorization", credential)
                .delete()
                .build()

//        val httpClient = okHttpClient.newBuilder().build()
//        httpClient.newCall(request).execute().use { response ->
        OkhttpUtils.doHttp(request).use { response ->
            val responseContent = response.body()!!.string()
            if (!response.isSuccessful) {
                logger.error("Fail to delete jfrog properties $path. $responseContent")
                throw RuntimeException("Fail to delete jfrog properties")
            }
        }
    }

    private fun encodeProperties(properties: Map<String, List<String>>): String {
        val propertiesSb = StringBuilder()
        properties.forEach { key, values ->
            if (values.isNotEmpty()) {
                if (propertiesSb.isNotEmpty()) propertiesSb.append(";")

                val valueSb = StringBuilder()
                values.forEach { value ->
                    if (valueSb.isNotEmpty()) valueSb.append(",")
                    valueSb.append(encodeProperty(value))
                }
                propertiesSb.append("${encodeProperty(key)}=$valueSb")
            }
        }
        return propertiesSb.toString()
    }

    private fun encodeProperty(str: String): String {
        return str.replace(",", "%5C,")
                .replace("\\", "%5C\\")
                .replace("|", "%5C|")
                .replace("=", "%5C=")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JFrogPropertiesApi::class.java)
    }
}
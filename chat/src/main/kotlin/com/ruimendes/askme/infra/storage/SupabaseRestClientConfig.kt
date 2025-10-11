package com.ruimendes.askme.infra.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class SupabaseRestClientConfig(
    @param:Value("\${supabase.url}") private val url: String,
    @param:Value("\${supabase.service-key}") private val key: String
) {

    @Bean
    fun supabaseRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(url)
            .defaultHeader("Authorization", "Bearer $key")
            .build()
    }
}
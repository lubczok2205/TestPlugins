package com.twofoutfour.cloudstream

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.Jsoup

class ObejrzyjToProvider : MainAPI() {
    // Główna strona, z której będziemy pobierać dane
    override var mainUrl = "https://www.obejrzyj.to"
    override var name = "ObejrzyjTo"  // Nazwa providera
    override val hasMainPage = true  // Obsługuje stronę główną

    // Metoda do wyszukiwania treści na stronie
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/szukaj?q=$query"
        val document = Jsoup.connect(url).get()
        val results = mutableListOf<SearchResponse>()

        // Znalezienie elementów na stronie z wynikami wyszukiwania
        document.select("div.movie-card").forEach { element ->
            val title = element.select("h3.title").text()
            val href = element.select("a").attr("href")
            val posterUrl = element.select("img.poster").attr("src")

            // Dodanie wyniku wyszukiwania do listy
            results.add(SearchResponse(
                title = title,
                url = "$mainUrl$href",
                apiName = name,
                posterUrl = posterUrl
            ))
        }
        return results
    }

    // Metoda do ładowania szczegółów filmu
    override suspend fun load(url: String): LoadResponse {
        val document = Jsoup.connect(url).get()
        val title = document.select("h1.title").text()
        val posterUrl = document.select("img.poster").attr("src")
        val description = document.select("div.description").text()

        // Zwrot szczegółów filmu
        return LoadResponse(
            name = title,
            url = url,
            posterUrl = posterUrl,
            plot = description
        )
    }

    // Metoda do ładowania linków do streamowania
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = Jsoup.connect(data).get()

        // Znalezienie linków do wideo
        document.select("div.video-player a").forEach { element ->
            val videoUrl = element.attr("href")

            // Użycie extractora do obsługi linków wideo
            loadExtractor(videoUrl, data, callback)
        }
        return true
    }
}

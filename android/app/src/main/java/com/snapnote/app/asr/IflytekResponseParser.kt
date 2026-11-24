package com.snapnote.app.asr

import com.google.gson.annotations.SerializedName

data class IflytekWsResponse(
    val code: Int = -1,
    val message: String = "",
    val data: IflytekDataBody? = null
)

data class IflytekDataBody(
    val status: Int? = null,
    val result: IflytekResultBody? = null
)

data class IflytekResultBody(
    val ws: List<IflytekWordSegment>? = null
)

data class IflytekWordSegment(
    val cw: List<IflytekCharWord>? = null
)

data class IflytekCharWord(
    @SerializedName("w")
    val word: String? = null
)

object IflytekResponseParser {
    fun extractText(response: IflytekWsResponse): String {
        return response.data?.result?.ws.orEmpty()
            .flatMap { it.cw.orEmpty() }
            .mapNotNull { it.word }
            .joinToString("")
    }
}

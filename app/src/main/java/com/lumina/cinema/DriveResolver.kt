package com.lumina.cinema

object DriveResolver {

    private val pathId = Regex("""/d/([a-zA-Z0-9_-]+)""")
    private val queryId = Regex("""[?&]id=([a-zA-Z0-9_-]+)""")

    /** Extrai o ID do arquivo de qualquer link de compartilhamento do Drive. */
    fun extractId(link: String): String? =
        pathId.find(link)?.groupValues?.get(1)
            ?: queryId.find(link)?.groupValues?.get(1)

    /**
     * URL de download direto. O domínio drive.usercontent.google.com com confirm=t
     * pula a tela de "arquivo grande, não dá pra verificar vírus".
     */
    fun directUrl(link: String): String? {
        val id = extractId(link.trim()) ?: return null
        return "https://drive.usercontent.google.com/download?id=$id&export=download&confirm=t"
    }
}

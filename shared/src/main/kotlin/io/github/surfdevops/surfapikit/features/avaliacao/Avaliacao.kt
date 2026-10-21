package io.github.surfdevops.surfapikit.features.avaliacao

import kotlinx.serialization.Serializable

/**
 * Jornada de Avaliação do app (CSAT 1 a 5 estrelas na tela "recarga realizada").
 *
 * Elegibilidade e categorias seguem o formato confirmado no spec-mobile
 * (21/10/2026). Exibição, resposta, dispensa e loja seguem o Documento de
 * Esforço de 24/09/2026 e ainda não foram confirmadas no backend. A
 * identidade da linha vem do JWT, nunca do corpo.
 */
object AvaliacaoStatus {
    const val PENDENTE = "PENDENTE"
    const val DISPENSADA = "DISPENSADA"
    const val RESPONDIDA = "RESPONDIDA"
}

/** Resposta comum de exibição, resposta, dispensa e loja: o estado atual da avaliação. */
@Serializable
data class AvaliacaoSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String? = null,
    val resultado: Resultado? = null,
    val transaction: AvaliacaoTransaction? = null
) {
    @Serializable
    data class Resultado(
        val id: String,
        val status: String? = null,
        val nota: Int? = null,
        val dataExibicao: String? = null,
        val dataResposta: String? = null,
        val clicouLoja: Boolean? = null
    )
}

@Serializable
data class AvaliacaoTransaction(
    val globalTransactionId: String? = null,
    val localTransactionId: String? = null,
    val localTransactionDate: String? = null
)

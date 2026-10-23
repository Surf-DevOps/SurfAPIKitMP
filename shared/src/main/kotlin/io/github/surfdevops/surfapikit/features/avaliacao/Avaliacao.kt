package io.github.surfdevops.surfapikit.features.avaliacao

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * Jornada de Avaliação do app (CSAT 1 a 5 estrelas na tela "recarga realizada").
 *
 * Todas as rotas seguem o contrato do spec-mobile (collection "Jornada de
 * Avaliação APP (CSAT)", 22/10/2026). A identidade da linha vem do JWT, nunca
 * do corpo.
 *
 * Os erros da jornada vêm com `erro` em string (`STATUS_INVALIDO`,
 * `AVALIACAO_NAO_ENCONTRADA`, ...) e status HTTP real: chegam como
 * [io.github.surfdevops.surfapikit.core.ApiError.Api] com `code` = status HTTP
 * (400, 404 ou 409) e `apiMessage` = `descricao`.
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
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Resultado(
        /** BIGINT no banco, sempre string. `id` é o nome antigo do Documento de Esforço. */
        @JsonNames("id")
        val idAvaliacao: String,
        val status: String? = null,
        /** Responder: a nota gravada. */
        val nota: Int? = null,
        /** Exibição: `true` quando devolveu a PENDENTE que a linha já tinha em aberto. */
        val reaproveitada: Boolean? = null,
        /** Responder: notas 4 e 5 recebem o convite "Avaliar na loja". */
        val convidarAvaliarLoja: Boolean? = null
    ) {
        @Deprecated("Use idAvaliacao", ReplaceWith("idAvaliacao"))
        val id: String get() = idAvaliacao
    }
}

/** Valores de `erro` das rotas de avaliação, com o status HTTP em que cada um vem. */
object AvaliacaoErro {
    /** 400: DTO inválido (nota fora de 1-5, comentário fora de 10-300...). */
    const val VALIDACAO_INVALIDA = 400
    /** 404: avaliação inexistente, malformada ou de outra linha; ou recarga de outra linha. */
    const val NAO_ENCONTRADA = 404
    /** 409: fora da elegibilidade, status diferente de PENDENTE ou loja sem nota 4-5. */
    const val CONFLITO = 409
}

@Serializable
data class AvaliacaoTransaction(
    val globalTransactionId: String? = null,
    val localTransactionId: String? = null,
    val localTransactionDate: String? = null
)

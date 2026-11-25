package io.github.surfdevops.surfapikit.features.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Grupo de Recarga / Plano Família (spec-mobile `v1/grupos/...`).
 *
 * PROVISÓRIO: as rotas e os nomes de campo seguem as SUGESTÕES do documento
 * "Recarga Grupo/Família — Comunicação entre Aplicações" (seção "App: telas e APIs do
 * spec-mobile"). O backend fecha o Swagger no "Dia 1–2"; quando sair, ajuste só este pacote.
 * Valores monetários vêm em centavos, como no catálogo (`vlPlano`). Totais, quantidades e
 * valores são sempre calculados pelo backend; o app só exibe.
 */

@Serializable
enum class GroupType { GRUPO, FAMILIA }

/** Papel de quem consulta: o administrador gerencia; o integrante só vê e pode sair. */
@Serializable
enum class GroupRole { ADMINISTRADOR, INTEGRANTE }

@Serializable
enum class GroupRecurrenceStatus {
    ATIVA,
    INATIVA,

    /** Cobrança recusada até esgotar as tentativas: volta a valer com a troca de cartão. */
    PAUSADA,
}

@Serializable
data class GroupCard(
    val cardId: String,
    val bandeira: String? = null,
    val final: String,
    /** "MM/AA". */
    val validade: String? = null,
)

@Serializable
data class GroupRecurrence(
    val status: GroupRecurrenceStatus,
    /** ISO-8601 (yyyy-MM-dd). */
    val dtProximaRecarga: String? = null,
    /** Dia fixo do mês da renovação (R4). */
    val diaRecarga: Int? = null,
    val cartao: GroupCard? = null,
)

/** Plano que entra na próxima programada (troca agendada, E3). */
@Serializable
data class GroupScheduledPlan(
    val nuPlano: String,
    val noPlano: String? = null,
    val qtDado: Int? = null,
    val vlPlano: Int,
    val dtVigencia: String? = null,
)

@Serializable
data class GroupMember(
    val msisdn: String,
    /** Nome do cadastro do cliente; nulo quando a linha nunca acessou o app (E21). */
    val nome: String? = null,
    val papel: GroupRole = GroupRole.INTEGRANTE,
    val nuPlano: String? = null,
    val noPlano: String? = null,
    /** Franquia de dados em MB, como no catálogo. */
    val qtDado: Int? = null,
    val vlPlano: Int? = null,
    val novoPlano: GroupScheduledPlan? = null,
    /** Saída agendada para a próxima programada (remoção ou saída). */
    val dtSaida: String? = null,
)

/** Item de `GET v1/grupos/vinculos/:msisdn`. */
@Serializable
data class GroupSummary(
    val id: String,
    val nome: String,
    val tipo: GroupType = GroupType.GRUPO,
    val papel: GroupRole,
    val qtIntegrantes: Int,
    val qtMaxIntegrantes: Int? = null,
    /** Iniciais para os avatares (o integrante não recebe a lista de membros, E12/E22). */
    val iniciais: List<String> = emptyList(),
    val vlTotal: Int? = null,
    val recorrencia: GroupRecurrence? = null,
)

@Serializable
data class GroupLinks(
    val administrador: List<GroupSummary> = emptyList(),
    val integrante: List<GroupSummary> = emptyList(),
)

/** `GET v1/grupos/:id`. Para o integrante, [integrantes] vem vazio (E12/E22). */
@Serializable
data class GroupDetail(
    val id: String,
    val nome: String,
    val tipo: GroupType = GroupType.GRUPO,
    val papel: GroupRole,
    val qtIntegrantes: Int,
    val qtMaxIntegrantes: Int? = null,
    val vlTotal: Int? = null,
    val integrantes: List<GroupMember> = emptyList(),
    val recorrencia: GroupRecurrence? = null,
)

/** `GET v1/grupos/parametros?tipo=`: limite e planos permitidos (CT7). */
@Serializable
data class GroupParameters(
    val qtMaxIntegrantes: Int,
    val planosPermitidos: List<String> = emptyList(),
    /** Plano sugerido ao criar o grupo sem escolher (E2, em aberto). */
    val nuPlanoPadrao: String? = null,
)

@Serializable
enum class GroupIneligibility {
    /** Número de outra operadora/MVNO ("Este número não é Fla-Chip"). */
    OUTRA_MVNO,
    JA_EM_OUTRO_GRUPO,
    LINHA_INATIVA,
    NAO_ENCONTRADO,
}

/** `GET v1/grupos/elegibilidade/:msisdn` (E7, em aberto). */
@Serializable
data class GroupEligibility(
    val elegivel: Boolean,
    val motivo: GroupIneligibility? = null,
    /** Texto pronto para o usuário. */
    val mensagem: String? = null,
    /** Informativo: a linha já tem programada individual (convive com a do grupo, R5). */
    val temProgramadaIndividual: Boolean = false,
)

@Serializable
data class GroupMemberInput(val msisdn: String, val nuPlano: String? = null)

@Serializable
data class CreateGroupRequest(
    val nome: String,
    val tipo: GroupType = GroupType.GRUPO,
    val incluirTitular: Boolean,
    val integrantes: List<GroupMemberInput>,
)

@Serializable
data class UpdateGroupRequest(val nome: String)

@Serializable
data class UpdateGroupMemberRequest(val nuPlano: String)

/** "Mesmo plano para todos": troca em lote (E2). */
@Serializable
data class UpdateGroupMembersRequest(val nuPlano: String)

/** Erro de validação por integrante na criação/inclusão. */
@Serializable
data class GroupMemberError(val msisdn: String, val motivo: GroupIneligibility? = null, val mensagem: String)

@Serializable
enum class GroupPaymentMethod {
    @SerialName("CARTAO") CARD,
    @SerialName("PIX") PIX,
}

@Serializable
data class GroupRechargeItem(val msisdn: String, val nuPlano: String)

/** `POST v1/grupos/:id/recarga-avulsa`. A avulsa não troca o plano da programada (R2). */
@Serializable
data class GroupRechargeRequest(
    val formaPagamento: GroupPaymentMethod,
    val cardId: String? = null,
    val itens: List<GroupRechargeItem>,
    val cupom: String? = null,
)

@Serializable
data class GroupPix(
    val qrCode: String,
    val copiaECola: String,
    val dtVencimento: String? = null,
)

@Serializable
data class GroupRechargeResult(
    val paymentId: String,
    val status: String,
    val vlTotal: Int? = null,
    val pix: GroupPix? = null,
)

/** `POST v1/grupos/:id/recorrencia`. O dia é fixo do mês (R4); qual dia está no E20. */
@Serializable
data class ActivateGroupRecurrenceRequest(
    val cardId: String,
    val diaRecarga: Int? = null,
)

@Serializable
data class ChangeGroupRecurrenceCardRequest(val cardId: String)

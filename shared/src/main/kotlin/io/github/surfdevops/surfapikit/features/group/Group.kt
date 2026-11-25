package io.github.surfdevops.surfapikit.features.group

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

/** Envelope padrão do spec-mobile para as rotas de grupo. */
@Serializable
data class GroupResponse<T>(
    val sucesso: Int,
    val transacao: String? = null,
    val descricao: String? = null,
    val resultado: T,
)

/** Resposta sem conteúdo útil (exclusão, saída, troca de cartão). */
@Serializable
data class GroupAck(
    val sucesso: Int,
    val transacao: String? = null,
    val descricao: String? = null,
)

/** Resposta da criação/inclusão: o grupo e, quando houver, os erros por integrante. */
@Serializable
data class GroupSaveResult(
    val grupo: GroupDetail? = null,
    val erros: List<GroupMemberError> = emptyList(),
)

private const val BASE = "spec-mobile/v1/grupos"

internal data class GroupEndpoint(override val path: String, override val method: HttpMethod) : Endpoint

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getGroupLinks(msisdn: String): GroupResponse<GroupLinks> =
    client.send(GroupEndpoint("$BASE/vinculos/$msisdn", HttpMethod.Get))

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getGroup(groupId: String): GroupResponse<GroupDetail> =
    client.send(GroupEndpoint("$BASE/$groupId", HttpMethod.Get))

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getGroupParameters(type: GroupType = GroupType.GRUPO): GroupResponse<GroupParameters> =
    client.send(GroupEndpoint("$BASE/parametros", HttpMethod.Get), query = mapOf("tipo" to type.name))

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getGroupEligibility(msisdn: String): GroupResponse<GroupEligibility> =
    client.send(GroupEndpoint("$BASE/elegibilidade/$msisdn", HttpMethod.Get))

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.createGroup(request: CreateGroupRequest): GroupResponse<GroupSaveResult> =
    client.send(GroupEndpoint(BASE, HttpMethod.Post), body = request)

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.renameGroup(groupId: String, request: UpdateGroupRequest): GroupResponse<GroupDetail> =
    client.send(GroupEndpoint("$BASE/$groupId", HttpMethod.Patch), body = request)

/** Exclui o grupo; a programada é cancelada junto (E5). */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.deleteGroup(groupId: String): GroupAck =
    client.send(GroupEndpoint("$BASE/$groupId", HttpMethod.Delete))

/** O integrante sai do grupo a partir da próxima programada (E4). */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.leaveGroup(groupId: String): GroupAck =
    client.send(GroupEndpoint("$BASE/$groupId/sair", HttpMethod.Post))

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.addGroupMember(groupId: String, member: GroupMemberInput): GroupResponse<GroupSaveResult> =
    client.send(GroupEndpoint("$BASE/$groupId/membros", HttpMethod.Post), body = member)

/** Troca o plano de um integrante; vale na próxima programada (E3). */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.updateGroupMember(
    groupId: String,
    msisdn: String,
    request: UpdateGroupMemberRequest,
): GroupResponse<GroupDetail> =
    client.send(GroupEndpoint("$BASE/$groupId/membros/$msisdn", HttpMethod.Patch), body = request)

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.updateGroupMembers(groupId: String, request: UpdateGroupMembersRequest): GroupResponse<GroupDetail> =
    client.send(GroupEndpoint("$BASE/$groupId/membros", HttpMethod.Patch), body = request)

/** Remove um integrante; ele sai na próxima programada (E3). */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.removeGroupMember(groupId: String, msisdn: String): GroupResponse<GroupDetail> =
    client.send(GroupEndpoint("$BASE/$groupId/membros/$msisdn", HttpMethod.Delete))

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.rechargeGroup(groupId: String, request: GroupRechargeRequest): GroupResponse<GroupRechargeResult> =
    client.send(GroupEndpoint("$BASE/$groupId/recarga-avulsa", HttpMethod.Post), body = request)

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getGroupRecurrence(groupId: String): GroupResponse<GroupRecurrence> =
    client.send(GroupEndpoint("$BASE/$groupId/recorrencia", HttpMethod.Get))

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.activateGroupRecurrence(
    groupId: String,
    request: ActivateGroupRecurrenceRequest,
): GroupResponse<GroupRecurrence> =
    client.send(GroupEndpoint("$BASE/$groupId/recorrencia", HttpMethod.Post), body = request)

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.changeGroupRecurrenceCard(
    groupId: String,
    request: ChangeGroupRecurrenceCardRequest,
): GroupResponse<GroupRecurrence> =
    client.send(GroupEndpoint("$BASE/$groupId/recorrencia/cartao", HttpMethod.Patch), body = request)

package io.github.surfdevops.surfapikit.features.mandouganhou

import kotlinx.serialization.Serializable

/** Tipo de resgate do programa Mandou Ganhou. */
@Serializable
enum class MandouGanhouTipo { CORREIOS_RASTREIO, LOTERICA_NSU }

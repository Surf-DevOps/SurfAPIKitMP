package io.github.surfdevops.surfapikit.features.cards

enum class CardType(val raw: String) {
    MASTER("MASTERCARD"),
    VISA("VISA"),
    ELO("ELO");

    companion object {
        fun from(flag: String): CardType? = when (flag.uppercase()) {
            "MASTERCARD", "MASTER" -> MASTER
            "VISA" -> VISA
            "ELO" -> ELO
            else -> null
        }
    }
}

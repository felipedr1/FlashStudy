package com.FlashStudy.data.model

enum class CardType(val backendValue: String) {
    MULTIPLE_CHOICE("multipleChoice"),
    TRUE_FALSE      ("trueFalse"),
    SHORT_ANSWER    ("shortAnswer");

    companion object {
        /** Converte string do backend → enum; lança erro se não reconhecer */
        fun fromBackend(value: String): CardType =
            values().find { it.backendValue == value }
                ?: throw IllegalArgumentException("Tipo desconhecido: $value")
    }
}
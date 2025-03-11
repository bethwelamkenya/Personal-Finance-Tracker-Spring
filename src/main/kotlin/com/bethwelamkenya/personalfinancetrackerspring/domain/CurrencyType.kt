package com.bethwelamkenya.personalfinancetrackerspring.domain

// Enum for major currencies
enum class CurrencyType(val code: String, val symbol: String) {
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£"),
    JPY("JPY", "¥"),
    AUD("AUD", "A$"),
    CAD("CAD", "C$"),
    CHF("CHF", "Fr."),
    CNY("CNY", "¥"),
    SEK("SEK", "kr"),
    NZD("NZD", "NZ$");

    companion object {
        fun find(code: String): CurrencyType? {
            var currency: CurrencyType? = null
            CurrencyType.entries.forEach {
                if (it.code == code) {
                    currency = it
                    return@forEach
                }
            }
            return currency
        }
    }
}
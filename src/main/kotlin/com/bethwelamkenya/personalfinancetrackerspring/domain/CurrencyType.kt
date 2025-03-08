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

// Function to get a list of available currency codes
fun getMajorCurrencies(): List<String> {
    return CurrencyType.entries.map { it.code }
}

// Function to format an amount with the currency symbol
fun formatCurrency(amount: Double, currency: CurrencyType, symbol: Boolean): String {
    return if (symbol) {
        "${currency.symbol}${"%,.2f".format(amount)}" // Formats with thousand separators and 2 decimal places
    } else {
        "$amount ${currency.code}" // Fallback if currency is not found
    }
}
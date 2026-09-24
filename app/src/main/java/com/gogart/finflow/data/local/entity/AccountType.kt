package com.gogart.finflow.data.local.entity

enum class AccountType(val title: String) {
    CASH("Готівка"),
    CARD("Картка"),
    BANK("Банківський рахунок"),
    SAVINGS("Заощадження"),
    OTHER("Інше")
}

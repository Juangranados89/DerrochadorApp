package com.derrochador.domain.model

enum class ExpenseCategory(val displayName: String, val keywords: List<String>) {
    FOOD("Comida", listOf("restaurante", "comida", "supermercado", "mercado", "cafe", "pizza", "burger", "domino", "rappi", "ifood", "uber eats", "didi food", "oxxo", "7-eleven")),
    TRANSPORT("Transporte", listOf("uber", "didi", "cabify", "taxi", "metro", "autobus", "bus", "gasolina", "pemex", "shell", "estacionamiento", "parking", "caseta", "peaje")),
    TRANSFER("Transferencia", listOf("transferencia", "spei", "pago", "envio", "remesa", "deposito", "cargo")),
    SHOPPING("Compras", listOf("amazon", "mercadolibre", "tienda", "ropa", "zapatos", "walmart", "soriana", "chedraui", "costco", "sears", "liverpool", "palacio")),
    ENTERTAINMENT("Entretenimiento", listOf("netflix", "spotify", "disney", "youtube", "cine", "cinema", "teatro", "concierto", "juego", "steam", "playstation", "xbox")),
    HEALTH("Salud", listOf("farmacia", "medico", "doctor", "hospital", "clinica", "dentista", "farmacia del ahorro", "similares", "medicina", "consulta")),
    UTILITIES("Servicios", listOf("telmex", "telcel", "movistar", "at&t", "izzi", "totalplay", "cfe", "gas", "agua", "luz", "internet", "telefono", "renta")),
    OTHER("Otro", emptyList())
}

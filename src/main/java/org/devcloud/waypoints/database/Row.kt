package org.devcloud.waypoints.database

data class Row(val fields: MutableMap<String, Any> = mutableMapOf()) {
    fun addField(key: String, value: Any) {
        fields[key] = value
    }

    fun getField(key: String): Any? = fields[key]
}

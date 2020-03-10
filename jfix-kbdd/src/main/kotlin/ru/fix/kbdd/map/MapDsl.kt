package ru.fix.kbdd.map

class MapDsl<ValueType> {

    companion object{
        fun <T> map(mapDsl: (MapDsl<T>)->Unit): MutableMap<String, T>{
            val map = MapDsl<T>()
            mapDsl(map)
            return map.map
        }
    }

    val map = mutableMapOf<String, ValueType>()

    operator fun String.rem(value: ValueType) {
        map[this] = value
    }
}
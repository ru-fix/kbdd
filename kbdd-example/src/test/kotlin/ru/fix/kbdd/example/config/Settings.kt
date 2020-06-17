package ru.fix.kbdd.example.config

import org.koin.core.KoinComponent


class Settings : KoinComponent {
    lateinit var airportBaseUri: String
}
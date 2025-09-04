package ru.fix.kbdd.example.config

import org.koin.core.component.KoinComponent


class Settings : KoinComponent {
    lateinit var baseUri: String
}
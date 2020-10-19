package com.github.jsbeckr.tailwindidea;

import java.util.*

interface TailwindConfigChangedListener: EventListener {
    fun tailwindConfigChanged()
}

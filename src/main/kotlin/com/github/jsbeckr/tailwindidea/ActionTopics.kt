package com.github.jsbeckr.tailwindidea

import com.intellij.util.messages.Topic

object ActionTopics {
    val TAILWIND_CONFIG_CHANGED = Topic("Tailwind Config Changed", TailwindConfigChangedListener::class.java)
}

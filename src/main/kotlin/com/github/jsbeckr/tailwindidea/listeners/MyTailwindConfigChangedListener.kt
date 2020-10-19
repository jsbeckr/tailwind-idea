package com.github.jsbeckr.tailwindidea.listeners;

import com.github.jsbeckr.tailwindidea.TailwindConfigChangedListener
import com.github.jsbeckr.tailwindidea.services.TailwindService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class MyTailwindConfigChangedListener(val project: Project): TailwindConfigChangedListener {
    override fun tailwindConfigChanged() {
        val tailwindService = project.service<TailwindService>()
        tailwindService.generateTailwindData()
    }
}

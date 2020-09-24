package com.github.jsbeckr.tailwindidea.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.github.jsbeckr.tailwindidea.services.TailwindService

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.getService(TailwindService::class.java)
    }
}

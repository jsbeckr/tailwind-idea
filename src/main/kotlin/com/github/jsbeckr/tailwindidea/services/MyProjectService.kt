package com.github.jsbeckr.tailwindidea.services

import com.intellij.openapi.project.Project
import com.github.jsbeckr.tailwindidea.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}

package com.github.jsbeckr.tailwindidea

import com.github.jsbeckr.tailwindidea.services.ProjectSettingsState
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class ProjectSettingsConfigurable(project: Project) : Configurable {
    private var mySettingsComponent: ProjectSettingsComponent? = null
    private var mProject: Project = project

    override fun getDisplayName(): String? {
        return "Tailwind Settings"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return mySettingsComponent!!.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        mySettingsComponent = ProjectSettingsComponent(mProject)
        return mySettingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings = mProject.service<ProjectSettingsState>()
        val modified = mySettingsComponent!!.mainCssPath != settings.mainCssPath
        return modified
    }

    override fun apply() {
        val settings = mProject.service<ProjectSettingsState>()
        settings.mainCssPath = mySettingsComponent!!.mainCssPath
    }

    override fun reset() {
        val settings = mProject.service<ProjectSettingsState>()
        mySettingsComponent!!.mainCssPath = settings.mainCssPath
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}

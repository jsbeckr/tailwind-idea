package com.github.jsbeckr.tailwindidea.services

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "TailwindSettings")
class ProjectSettingsState : PersistentStateComponent<ProjectSettingsState> {
    var mainCssPath = "src/styles/index.css"

    override fun getState(): ProjectSettingsState? {
        return this
    }

    override fun loadState(state: ProjectSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}

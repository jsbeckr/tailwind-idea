package com.github.jsbeckr.tailwindidea

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.Condition
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
class ProjectSettingsComponent(project: Project) {
    val panel: JPanel
    private val mainCssPathField = TextFieldWithBrowseButton()
    private val mProject = project

    init {
        mainCssPathField.addBrowseFolderListener(
                "Choose main css file",
                "Very informative...",
                mProject,
                FileChooserDescriptorFactory.createSingleLocalFileDescriptor()
        )
    }

    val preferredFocusedComponent: JComponent
        get() = mainCssPathField

    var mainCssPath: String
        get() = mainCssPathField.text
        set(newText) {
            mainCssPathField.text = newText
        }


    init {
        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("Tailwind CSS File "), mainCssPathField, 1, false)
                .addComponentFillVertically(JPanel(), 0)
                .panel
    }
}

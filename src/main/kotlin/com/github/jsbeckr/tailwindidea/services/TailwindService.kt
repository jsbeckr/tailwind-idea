package com.github.jsbeckr.tailwindidea.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreterManager
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.jetbrains.annotations.NotNull
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

data class TailwindClass(
    var id: String,
    var value: String,
    var parent: TailwindClass?,
    var children: MutableList<TailwindClass>
) {
    override fun toString(): String {
        return "$id - $value"
    }
}

@Service
class TailwindService(val project: Project) {
    var tailwindClasses: ArrayList<TailwindClass> = ArrayList()

    init {
        generateTailwindData()

        // update tailwindclasses if tailwind settingsclass changes
        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(@NotNull events: List<VFileEvent?>) {
                events.forEach {
                    if (it != null && it.file != null) {
                        val file = it.file as VirtualFile
                        if (ProjectFileIndex.getInstance(project).isInContent(file)) {
                            val settingsState = project.service<ProjectSettingsState>()
                            val configFile = File(settingsState.mainCssPath)
                            if (file.path == configFile.path) {
                                generateTailwindData()
                            }
                        }
                    }
                }
            }
        })
    }

    fun generateTailwindData() {
        tailwindClasses.clear()
        val workingDir = project.basePath
        val generateTailwind =
            File(
                JSLanguageServiceUtil.getPluginDirectory(javaClass, "tailwind"),
                "generateTailwind.js"
            ).absolutePath

        val tmpFile = createTempFile()

        ApplicationManager.getApplication().executeOnPooledThread {
            val settingsState = project.service<ProjectSettingsState>()
            try {
                // TODO: error handling maybe make it possible to select nodejs interpreter?
                val nodePath =
                    NodeJsLocalInterpreterManager.getInstance().interpreters[0].interpreterSystemDependentPath

                val command =
                    "$nodePath#$generateTailwind#${settingsState.mainCssPath}#${tmpFile.absolutePath}#$workingDir"

                command.runCommand(
                    File(
                        workingDir!!
                    )
                )

                val jsonNode = ObjectMapper().readTree(tmpFile)
                jsonNode["classNames"].fields().forEach { field ->
                    val restParts = field.key.split(":").toMutableList()
                    val firstPart = restParts.removeAt(0)
                    addTailwindClass(tailwindClasses, firstPart, null, restParts)
                }

                // Delete tmpfile
                Files.delete(tmpFile.toPath())
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }

    private fun addTailwindClass(
        tailwindClasses: MutableList<TailwindClass>,
        firstPart: String,
        parent: TailwindClass?,
        restParts: MutableList<String>
    ) {
        val foundClass = tailwindClasses.find { it.id == firstPart }
        if (foundClass != null) {
            val newFirstPart = restParts.removeAt(0)
            addTailwindClass(foundClass.children, newFirstPart, foundClass, restParts)
        } else {
            val children = arrayListOf<TailwindClass>()
            tailwindClasses.add(TailwindClass(firstPart, "Part", parent, children))

            if (restParts.isNotEmpty()) {
                val newFirstPart = restParts.removeAt(0)
                addTailwindClass(children, newFirstPart, parent, restParts)
            } else {
                return
            }
        }
    }

    fun String.runCommand(workingDir: File) {
        try {
            val command = this.split("#").toList()
            ProcessBuilder(command)
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor(60, TimeUnit.SECONDS)
        } catch (exception: Exception) {
            println(exception.message)
            throw exception
        }
    }
}

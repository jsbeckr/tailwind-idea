package com.github.jsbeckr.tailwindidea.services

import com.github.jsbeckr.tailwindidea.MyBundle
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.css.index.CssIndex
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.annotations.NotNull
import java.io.File
import java.util.concurrent.TimeUnit


class TailwindService(val project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))

        generateTailwindDummyCss()

        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(@NotNull events: List<VFileEvent?>) {
                events.forEach {
                    if (it != null && it.file != null) {
                        val file = it.file as VirtualFile
                        if (ProjectFileIndex.getInstance(project).isInContent(file)) {
                            if (file.name == "tailwind.js") {
                                generateTailwindDummyCss()
                                FileBasedIndex.getInstance().scheduleRebuild(CssIndex.CSS_INDEX, Throwable("CSS Index"))
                            }
                        }
                    }
                }
            }
        })
    }

    fun generateTailwindDummyCss() {
        val workingDir = project.basePath
        val cssFile = project.service<ProjectSettingsState>().mainCssPath
        "npx postcss $cssFile -o _tailwind.css".runCommand(File(workingDir!!))
    }

    fun String.runCommand(workingDir: File) {
        ProcessBuilder(*split(" ").toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor(60, TimeUnit.SECONDS)
    }
}

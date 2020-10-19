package com.github.jsbeckr.tailwindidea.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
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
        extractJSFiles()
        generateTailwindData()

//        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
//            override fun after(@NotNull events: List<VFileEvent?>) {
//                events.forEach {
//                    if (it != null && it.file != null) {
//                        val file = it.file as VirtualFile
//                        if (ProjectFileIndex.getInstance(project).isInContent(file)) {
//                            if (file.name == "tailwind.js") {
//                                FileBasedIndex.getInstance().scheduleRebuild(CssIndex.CSS_INDEX, Throwable("CSS Index"))
//                            }
//                        }
//                    }
//                }
//            }
//        })
    }

    private fun extractJSFiles() {
        val files = listOf("generateTailwind.js", "extractClassNames.js")
        for (file in files) {
            val src = javaClass.getResource("/tailwind/${file}")
            val dest = File(JSLanguageServiceUtil.getPluginDirectory(javaClass, "tailwind"), file)

            FileUtils.copyURLToFile(
                src,
                dest
            );
        }

        copyFromJar(
            "/tailwind/node_modules",
            Paths.get("${JSLanguageServiceUtil.getPluginDirectory(javaClass, "tailwind")}/node_modules")
        )
    }

//    fun generateTailwindClass(part: String, restParts: MutableList<String>): TailwindClass {
//        return if (restParts.count() != 0) {
//            val newPart = restParts.removeAt(0)
//            TailwindClass(part, "Group", arrayListOf(generateTailwindClass(newPart, restParts)))
//        } else {
//            TailwindClass(part, "Leaf", arrayListOf())
//        }
//    }

    fun generateTailwindData() {
        val workingDir = project.basePath
        val generateTailwind =
            File(JSLanguageServiceUtil.getPluginDirectory(javaClass, "tailwind"), "generateTailwind.js").absolutePath

        val tmpFile = createTempFile()

        "node ${generateTailwind} ${tmpFile.absolutePath}".runCommand(File(workingDir!!))

        println(tmpFile.path)
        val jsonNode = ObjectMapper().readTree(tmpFile)
        jsonNode["classNames"].fields().forEach { field ->
            val restParts = field.key.split(":").toMutableList()
            var firstPart = restParts.removeAt(0)

            // Do we have that thing already?
//            val index = tailwindClasses.indexOfFirst { twClass -> twClass.id == firstPart }

            // No it's a new thing
            addTailwindClass(tailwindClasses, firstPart, null, restParts)
            //tailwindClasses.add(generateTailwindClass(firstPart, parts))
            // Yes we have it already
//            } else {
            // remove next part, because the old firstPart is already in the classes
//                firstPart = restParts.removeAt(0)

            //tailwindClasses[index].children?.add(generateTailwindClass(firstPart, parts))
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
            ProcessBuilder(*split(" ").toTypedArray())
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

    @Throws(URISyntaxException::class, IOException::class)
    fun copyFromJar(source: String?, target: Path) {
        val resource = javaClass.getResource("").toURI()
        val fileSystem = FileSystems.newFileSystem(
            resource, emptyMap<String, String>()
        )
        val jarPath: Path = fileSystem.getPath(source)
        Files.walkFileTree(jarPath, object : SimpleFileVisitor<Path>() {
            private var currentTarget: Path? = null

            @Throws(IOException::class)
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                currentTarget = target.resolve(jarPath.relativize(dir).toString())
                Files.createDirectories(currentTarget)
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.copy(
                    file,
                    target.resolve(jarPath.relativize(file).toString()),
                    StandardCopyOption.REPLACE_EXISTING
                )
                return FileVisitResult.CONTINUE
            }
        })
    }
}

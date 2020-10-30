package com.github.jsbeckr.tailwindidea

import com.intellij.ide.ApplicationInitializedListener
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class TailwindAppStarter : ApplicationInitializedListener {
    override fun componentsInitialized() {
        extractJSFiles()
    }

    private fun extractJSFiles() {
        val tailwindPath = JSLanguageServiceUtil.getPluginDirectory(javaClass, "tailwind")
        tailwindPath.deleteRecursively()

        val files = listOf("generateTailwind.js", "extractClassNames.js")
        for (file in files) {
            val src = javaClass.getResource("/tailwind/${file}")
            val dest = File(JSLanguageServiceUtil.getPluginDirectory(javaClass, "tailwind"), file)

            FileUtils.copyURLToFile(
                src,
                dest
            )
        }

        copyFromJar(
            "/tailwind/node_modules",
        )
    }

    @Throws(URISyntaxException::class, IOException::class)
    fun copyFromJar(source: String) {
        val resource = javaClass.getResource("").toURI()
        try {
            val fileSystem = FileSystems.newFileSystem(
                resource, emptyMap<String, String>()
            )

            val jarPath: Path = fileSystem.getPath(source)
            Files.walkFileTree(jarPath, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    FileUtils.copyURLToFile(
                        file.toUri().toURL(),
                        File(JSLanguageServiceUtil.getPluginDirectory(javaClass, ""), file.toString())
                    )

                    return FileVisitResult.CONTINUE
                }
            })
        } catch (ex: Exception) {
            println(ex.stackTrace)
        }
    }
}

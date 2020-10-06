package com.github.jsbeckr.tailwindidea

import com.github.jsbeckr.tailwindidea.services.TailwindService
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.ProcessingContext


class TailwindCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val addColonInsertHandler = object : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, item: LookupElement) {
            if (context.completionChar == ':') return
            val editor = context.editor
            if (!isAtColon(context)) {
                EditorModificationUtil.insertStringAtCaret(editor, ":")
                context.commitDocument()
            }
        }

        private fun isAtColon(context: InsertionContext): Boolean {
            val startOffset = context.startOffset
            val document = context.document
            return document.textLength > startOffset && document.charsSequence[startOffset] == ':'
        }
    }

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        var activeProject: Project? = null
        for (project in ProjectManager.getInstance().openProjects) {
            val window = WindowManager.getInstance().suggestParentWindow(project)
            if (window != null && window.isActive()) {
                activeProject = project
            }
        }

        val tailwindService = activeProject!!.service<TailwindService>()
        val classes = tailwindService.tailwindClasses

        // TODO: make this recursive????
        // or just simple with idk 4 different regexes??

        val regex = Regex(".*\\s([^\\s]+):[^\\s]*IntellijIdeaRulezzz.*")
        val matchResult = regex.find(parameters.position.text)

        matchResult?.let { mr ->
            if (mr.groups.size > 1) {
                val groupClass = classes.find { it.id == mr.groups[1]?.value }
                groupClass?.children?.map { child ->
                    val newElement = LookupElementBuilder.create("${groupClass.id}:${child.id}")

                    if (child.children.isNullOrEmpty()) {
                        result.addElement(
                            newElement.withTypeText("Leaf")
                        )
                    } else {
                        newElement.withTailText(":")
                            .withInsertHandler(addColonInsertHandler).withTypeText("Group")
                    }
                }
            }
        }

        classes.map {
            it.children?.let { children ->
                if (children.size > 0) {
                    println("Adding Element: ${it.id}")
                    val element = LookupElementBuilder.create(it.id).withTailText(":")
                        .withTypeText(it.value).withInsertHandler(addColonInsertHandler)
                    result.addElement(element)
                }
            }
        }
//            it.children?.map {
//                val childElement = LookupElementBuilder.create(it.id)
//                    .withTypeText(it.value)
//                result.addElement(childElement)
//            }
    }
}

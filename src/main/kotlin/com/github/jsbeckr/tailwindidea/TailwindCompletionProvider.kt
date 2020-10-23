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

    var mProject: Project? = null
    var tailwindService: TailwindService? = null

    init {
        for (project in ProjectManager.getInstance().openProjects) {
            val window = WindowManager.getInstance().suggestParentWindow(project)
            if (window != null && window.isActive) {
                mProject = project
            }
        }

        mProject?.let {
            tailwindService = it.service<TailwindService>()
        }

    }

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

    @ExperimentalStdlibApi
    private fun computeCompletions(groups: MutableList<String>, result: CompletionResultSet) {
        fun foobar(completionText: String, group: String?, restGroups: MutableList<String>): Unit {
            val groupClass = tailwindService?.tailwindClasses?.find {
                it.id == group
            }
            val nextMatch = restGroups.removeFirstOrNull()

            // exit condition: no matchgroups anymore
            when {
                groupClass == null -> {
                    throw Exception("Shouldnt happen")
                }
                nextMatch == "" || tailwindService?.tailwindClasses?.find {
                    it.id == nextMatch
                } == null -> {
                    // fill all
                    groupClass.children.let { children ->
                        children.forEach {
                            val prefix =
                                if (completionText.isEmpty()) groupClass.id else "$completionText:${groupClass.id}"
                            val newElement = LookupElementBuilder.create("$prefix:${it.id}")

                            if (it.children.size > 0) {
                                result.addElement(
                                    newElement.withTailText(":")
                                        .withInsertHandler(addColonInsertHandler).withTypeText("GROUP")
                                )
                            } else {
                                result.addElement(
                                    newElement.withTypeText("LEAF")
                                )
                            }

                        }
                    }
                }
                else -> {
                    val prefix = if (completionText.isEmpty()) "" else "$completionText:${groupClass.id}:"
                    foobar("$prefix${groupClass.id}", nextMatch, restGroups)
                }
            }
        }

        // this is the first real match
        val firstMatch = groups.removeFirstOrNull()
        foobar("", firstMatch, groups)
    }

    @ExperimentalStdlibApi
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        var betterResultSet = result
        var activeProject: Project? = null
        for (project in ProjectManager.getInstance().openProjects) {
            val window = WindowManager.getInstance().suggestParentWindow(project)
            if (window != null && window.isActive) {
                activeProject = project
            }
        }

        val tailwindService = activeProject!!.service<TailwindService>()
        val classes = tailwindService.tailwindClasses

        val prefixes = betterResultSet.prefixMatcher.prefix.split(" ")
        val prefix = prefixes.last()
        betterResultSet = betterResultSet.withPrefixMatcher(prefix)

        val groups = prefix.split(":")

        if (groups.size == 1) {
            classes.map {
                it.children.let { children ->
                    if (children.size > 0) {
                        val element = LookupElementBuilder.create(it.id).withTailText(":")
                            .withTypeText(it.value).withInsertHandler(addColonInsertHandler)
                        val prioElement = PrioritizedLookupElement.withPriority(element, 2.0)
                        betterResultSet.addElement(prioElement)
                    } else {
                        val element = LookupElementBuilder.create(it.id)
                            .withTypeText(it.value)
                        betterResultSet.addElement(element)
                    }
                }
            }
        } else {
            computeCompletions(groups.toMutableList(), betterResultSet)
        }

        betterResultSet.addLookupAdvertisement("Tailwind ftw!")
    }
}

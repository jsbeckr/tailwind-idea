package com.github.jsbeckr.tailwindidea

import com.github.jsbeckr.tailwindidea.services.TailwindClass
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
    var tailwindClasses: ArrayList<TailwindClass>? = null

    init {
        for (project in ProjectManager.getInstance().openProjects) {
            val window = WindowManager.getInstance().suggestParentWindow(project)
            if (window != null && window.isActive) {
                mProject = project
            }
        }

        mProject?.let {
            val tailwindService = it.service<TailwindService>()
            tailwindClasses = tailwindService.tailwindClasses
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
    private fun computeCompletions(regex: Regex, text: String, result: CompletionResultSet, debug: String) {
        val matchResult = regex.find(text)

        fun foobar(completionText: String, match: MatchGroup?, restGroups: MutableList<MatchGroup?>): Unit {
            val groupClass = tailwindClasses?.find {
                it.id == match?.value
            }
            val nextMatch = restGroups.removeFirstOrNull()

            // exit condition: no matchgroups anymore
            when {
                groupClass == null -> {
                    return
                }
                nextMatch == null -> {
                    // fill all
                    groupClass.children.let { children ->
                        children.forEach {
                            val prefix =
                                if (completionText.isEmpty()) groupClass.id else "$completionText:${groupClass.id}"
                            val newElement = LookupElementBuilder.create("$prefix:${it.id}")

                            result.addElement(
                                newElement.withTypeText(debug)
                            )
                        }
                    }
                }
                else -> {
                    val prefix = if (completionText.isEmpty()) "" else "$completionText:${groupClass.id}:"
                    foobar("$prefix${groupClass.id}", nextMatch, restGroups)
                }
            }
        }

        matchResult?.let { mr ->
            val groups = mr.groups.toMutableList()
            if (groups.size > 1) {
                // we ignore the first group
                groups.removeFirst()

                // this is the first real match
                val firstMatch = groups.removeFirstOrNull()
                foobar("", firstMatch, groups)
            }
        }
    }

    @ExperimentalStdlibApi
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        var activeProject: Project? = null
        for (project in ProjectManager.getInstance().openProjects) {
            val window = WindowManager.getInstance().suggestParentWindow(project)
            if (window != null && window.isActive) {
                activeProject = project
            }
        }

        val tailwindService = activeProject!!.service<TailwindService>()
        val classes = tailwindService.tailwindClasses

        // TODO: make this recursive????
        // or just simple with idk 4 different regexes??

        val regex1 = Regex(""".*\s([\S]+):[\S]*IntellijIdeaRulezzz.*""")
        val regex2 = Regex(""".*\s([\S]+):([\S]+):[\S]*IntellijIdeaRulezzz.*""")
        val regex3 = Regex(""".*\s([\S]+):([\S]+):([\S]+):[\S]*IntellijIdeaRulezzz.*""")
        val regex4 = Regex(""".*\s([\S]+):([\S]+):([\S]+):([\S]+):[\S]*IntellijIdeaRulezzz.*""")

        val myText = parameters.position.text
        when {
            regex4.matches(myText) -> computeCompletions(regex4, myText, result, "regex4")
            regex3.matches(myText) -> computeCompletions(regex3, myText, result, "regex3")
            regex2.matches(myText) -> computeCompletions(regex2, myText, result, "regex2")
            regex1.matches(myText) -> computeCompletions(regex1, myText, result, "regex1")
        }

        classes.map {
            it.children.let { children ->
                if (children.size > 0) {
                    println("Adding Group Element: ${it.id}:")
                    val element = LookupElementBuilder.create(it.id).withTailText(":")
                        .withTypeText(it.value).withInsertHandler(addColonInsertHandler)
                    result.addElement(element)
                } else {
                    println("Adding Element: ${it.id}")
                    val element = LookupElementBuilder.create(it.id)
                        .withTypeText(it.value)
                    result.addElement(element)
                }
            }
        }
    }
}

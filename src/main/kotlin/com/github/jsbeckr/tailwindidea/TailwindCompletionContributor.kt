package com.github.jsbeckr.tailwindidea

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.patterns.PlatformPatterns

class TailwindCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(JSTokenTypes.STRING_TEMPLATE_PART), TailwindCompletionProvider())
    }
}

package com.github.jsbeckr.tailwindidea

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PlainTextTokenTypes

class TailwindCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(PlainTextTokenTypes.PLAIN_TEXT), TailwindCompletionProvider())
    }
}

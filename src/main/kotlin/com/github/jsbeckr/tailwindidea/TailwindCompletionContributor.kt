package com.github.jsbeckr.tailwindidea

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.xml.XmlElementType

class TailwindCompletionContributor : CompletionContributor() {
    init {
        // TODO: completion closes when caret in the middle of a string template
        // `bg-gray-100 sm:COMPLETION_HERE`
        extend(
            CompletionType.BASIC,
            psiElement(JSTokenTypes.STRING_TEMPLATE_PART),
            TailwindCompletionProvider()
        )

        // `bg-gray-100 ${sm:COMPLETION_HERE}`
        extend(
            CompletionType.BASIC,
            psiElement(JSTokenTypes.STRING_LITERAL),
            TailwindCompletionProvider()
        )

        // React JSX
        // className="sm:COMPLETION_HERE"
        extend(
            CompletionType.BASIC,
            psiElement(XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN).withParent(
                XmlPatterns.xmlAttributeValue()
            ).withSuperParent(2, XmlPatterns.xmlAttribute().withName("className")),
            TailwindCompletionProvider()
        )

        // HTML, etc.
        // class="sm:COMPLETION_HERE"
        extend(
            CompletionType.BASIC,
            psiElement(XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN).withParent(
                XmlPatterns.xmlAttributeValue()
            ).withSuperParent(2, XmlPatterns.xmlAttribute().withName("class")),
            TailwindCompletionProvider()
        )
    }
}

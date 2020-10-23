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
            psiElement(JSTokenTypes.STRING_TEMPLATE_PART).withSuperParent(
                5,
                XmlPatterns.xmlAttribute().withName("className")
            ),
            TailwindCompletionProvider()
        )

        // `bg-gray-100 ${sm:COMPLETION_HERE}`
        extend(
            CompletionType.BASIC,
            psiElement(JSTokenTypes.STRING_LITERAL).withSuperParent(
                7,
                XmlPatterns.xmlAttribute().withName("className")
            ),
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

    // for debug purposes
//    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
//        super.fillCompletionVariants(parameters, result)
//    }
}

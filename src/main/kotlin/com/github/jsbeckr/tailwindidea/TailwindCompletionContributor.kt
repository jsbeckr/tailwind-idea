package com.github.jsbeckr.tailwindidea

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlElementType

class TailwindCompletionContributor : CompletionContributor() {
    init {
        // TODO: completion closes when caret in the middle of a string template
//        extend(
//            CompletionType.BASIC,
//            psiElement(JSTokenTypes.STRING_TEMPLATE_PART),
//            TailwindCompletionProvider()
//        )
//
//        // TODO: does not work
//        extend(
//            CompletionType.BASIC,
//            psiElement(JSTokenTypes.STRING_LITERAL),
//            TailwindCompletionProvider()
//        )

        // react jsx
        extend(
            CompletionType.BASIC,
            psiElement(XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN).withParent(
                XmlPatterns.xmlAttributeValue()
            ).withSuperParent(2, XmlPatterns.xmlAttribute().withName("className")),
            TailwindCompletionProvider()
        )

//        extend(
//            CompletionType.BASIC,
//            psiElement(XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN).withParent(
//                XmlPatterns.xmlAttributeValue()
//            ).withSuperParent(2, XmlPatterns.xmlAttribute().withName("class")),
//            TailwindCompletionProvider()
//        )
    }
}

// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.kotlin.idea.quickfix.crossLanguage

import com.intellij.lang.jvm.JvmModifier
import com.intellij.lang.jvm.actions.CreateFieldRequest
import org.jetbrains.kotlin.idea.caches.resolve.getResolutionFacade
import org.jetbrains.kotlin.idea.core.appendModifier
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.callableBuilder.PropertyInfo
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.callableBuilder.TypeInfo
import org.jetbrains.kotlin.idea.quickfix.crossLanguage.KotlinElementActionsFactory.Companion.toKotlinTypeInfo
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.psi.KtElement

class AddFieldActionCreateCallableFromUsageFix(
    targetContainer: KtElement,
    classOrFileName: String?,
    val request: CreateFieldRequest,
    val lateinit: Boolean
) : AbstractPropertyActionCreateCallableFromUsageFix(targetContainer, classOrFileName) {
    init {
        init()
    }

    override val propertyInfo: PropertyInfo?
        get() = run {
            val targetContainer = element ?: return@run null
            val resolutionFacade = targetContainer.getResolutionFacade()
            val typeInfo = request.fieldType.toKotlinTypeInfo(resolutionFacade)
            val writable = JvmModifier.FINAL !in request.modifiers
            PropertyInfo(
                request.fieldName,
                TypeInfo.Empty,
                typeInfo,
                writable,
                listOf(targetContainer),
                isLateinitPreferred = false, // Dont set it to `lateinit` because it works via templates that brings issues in batch field adding
                isForCompanion = JvmModifier.STATIC in request.modifiers,
                modifierList = KotlinElementActionsFactory.ModifierBuilder(targetContainer, allowJvmStatic = false).apply {
                    addJvmModifiers(request.modifiers)
                    if (modifierList.children.none { it.node.elementType in KtTokens.VISIBILITY_MODIFIERS })
                        addJvmModifier(JvmModifier.PUBLIC)
                    if (lateinit)
                        modifierList.appendModifier(KtTokens.LATEINIT_KEYWORD)
                    if (!request.modifiers.contains(JvmModifier.PRIVATE) && !lateinit)
                        addAnnotation(JvmAbi.JVM_FIELD_ANNOTATION_FQ_NAME)
                }.modifierList,
                withInitializer = !lateinit
            )
        }
}
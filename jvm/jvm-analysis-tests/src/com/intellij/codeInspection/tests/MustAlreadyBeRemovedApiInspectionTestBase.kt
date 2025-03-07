package com.intellij.codeInspection.tests

import com.intellij.codeInspection.MustAlreadyBeRemovedApiInspection
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder
import com.intellij.util.PathUtil
import org.jetbrains.annotations.ApiStatus

abstract class MustAlreadyBeRemovedApiInspectionTestBase : UastInspectionTestBase(inspection) {
  override fun tuneFixture(moduleBuilder: JavaModuleFixtureBuilder<*>) {
    super.tuneFixture(moduleBuilder)
    moduleBuilder.addLibrary("util", PathUtil.getJarPathForClass(ApiStatus.ScheduledForRemoval::class.java))
  }

  companion object {
    private val inspection = MustAlreadyBeRemovedApiInspection().apply { currentVersion = "3.0" }
  }
}
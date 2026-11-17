package com.cmc.launcher.game.path

import java.io.File

private fun String.replaceSeparator(): String = this.replace("/", File.separator)

/** 当前选择的游戏目录 */
fun getGameHome(): String = GamePathManager.currentPath.value

/** 指定游戏目录下的 versions 文件夹 */
fun getVersionsHome(gameHome: String): String = "${gameHome}/versions".replaceSeparator()

fun getVersionsHome(): String = getVersionsHome(getGameHome())

/** 指定游戏目录下的 libraries 文件夹 */
fun getLibrariesHome(gameHome: String): String = "${gameHome}/libraries".replaceSeparator()

fun getLibrariesHome(): String = getLibrariesHome(getGameHome())

/** 指定游戏目录下的 assets 文件夹 */
fun getAssetsHome(gameHome: String): String = "${gameHome}/assets".replaceSeparator()

fun getAssetsHome(): String = getAssetsHome(getGameHome())

/** 指定游戏目录下的 resources 文件夹 */
fun getResourcesHome(gameHome: String): String = "${gameHome}/resources".replaceSeparator()

fun getResourcesHome(): String = getResourcesHome(getGameHome())
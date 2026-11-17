package com.cmc.launcher.game.download.addon.platform.curseforge.models

import com.cmc.launcher.game.download.addon.platform.Platform
import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import com.cmc.launcher.game.download.addon.platform.PlatformDisplayLabel
import com.cmc.launcher.game.download.addon.platform.PlatformFilterCode
import com.cmc.launcher.game.download.addon.platform.PlatformProject
import com.cmc.launcher.game.download.addon.platform.UnsupportedClassesException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CurseForgeProject(
    @SerialName("data")
    val data: CurseForgeData
): PlatformProject {
    override fun platform(): Platform = Platform.CURSEFORGE

    override fun platformId(): String = data.id.toString()

    override fun platformClasses(defaultClasses: PlatformClasses): PlatformClasses {
        return data.getPlatformClassesOrNull() ?: defaultClasses
    }

    override fun platformSlug(): String = data.slug

    override fun platformIconUrl(): String? = data.logo?.url

    override fun platformTitle(): String = data.name

    override fun platformSummary(): String = data.summary

    override fun platformAuthors(): List<String>? = data.platformAuthors()

    override fun platformDownloadCount(): Long = data.downloadCount

    override fun platformFollows(): Long? = null

    override fun platformAvailable(): Boolean = data.isApproved()

    override fun platformModLoaders(): List<PlatformDisplayLabel>? {
        return data.platformModLoaders()
    }

    override fun checkClasses() {
        val classId = data.classId
        if (CurseForgeClassID.entries.none { id -> classId == id.classID }) throw UnsupportedClassesException(classId)
    }

    override fun platformCategories(classes: PlatformClasses): List<PlatformFilterCode>? {
        return data.platformCategories(classes)
    }

    override fun platformUrls(defaultClasses: PlatformClasses): PlatformProject.Urls {
        val classes = data.getPlatformClassesOrNull() ?: defaultClasses
        return PlatformProject.Urls(
            projectUrl = "https://www.curseforge.com/minecraft/${classes.curseforge.slug}/${data.slug}",
            sourceUrl = data.links.sourceUrl,
            issuesUrl = data.links.issuesUrl,
            wikiUrl = data.links.wikiUrl
        )
    }

    override fun platformScreenshots(): List<PlatformProject.Screenshot> {
        return data.screenshots.map { asset ->
            PlatformProject.Screenshot(
                imageUrl = asset.url,
                title = asset.title,
                description = asset.description
            )
        }
    }
}

/**
 * @return 该项目是否可见
 */
fun CurseForgeProject.isApproved(): Boolean = this.data.isApproved()
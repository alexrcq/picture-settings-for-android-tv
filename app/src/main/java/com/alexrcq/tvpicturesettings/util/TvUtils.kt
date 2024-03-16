package com.alexrcq.tvpicturesettings.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

object TvUtils {
    fun isCurrentTvSupported(context: Context): Boolean = try {
        // just trying to take a random setting
        Settings.Global.getInt(context.contentResolver, MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL)
        true
    } catch (e: Settings.SettingNotFoundException) {
        false
    }

    fun isModel4kSmartTv(): Boolean = Build.MODEL.contains("4K SMART TV", true)

    fun isModelMssp(): Boolean = Build.MODEL.contains("MSSP", true)

    fun isOled(context: Context): Boolean = context.packageManager.hasSystemFeature("mitv.hardware.panel.oled")

    fun isLocalDimmingSupported(): Boolean = queryTvConfigByKey("support_local_dimming") == "YES"

    private fun queryTvConfigByKey(key: String): String? {
        val configLocations = listOf("/system/etc/mitv.config_product.xml", "/system/etc/mitv.config.xml")
        for (location in configLocations) {
            val configFile = File(location)
            if (!configFile.canRead()) continue
            var configValue: String? = null
            SAXParserFactory.newInstance().newSAXParser().parse(configFile, object: DefaultHandler() {
                override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
                    if (qName == "item" && attributes?.getValue("key") == key) {
                        configValue = attributes.getValue("value")
                    }
                }
            })
            if (configValue != null) {
                return configValue
            }
        }
        return null
    }
}
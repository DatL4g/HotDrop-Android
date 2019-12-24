package de.datlag.hotdrop

import androidx.multidex.MultiDexApplication
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath(this.getString(R.string.font_regular_path))
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())
    }
}
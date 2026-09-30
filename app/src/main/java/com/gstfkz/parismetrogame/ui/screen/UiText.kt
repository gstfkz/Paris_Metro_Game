package com.gstfkz.parismetrogame.ui.screen

import com.gstfkz.parismetrogame.data.local.GameLanguage

data class UiText(val fr:Boolean) {
    companion object { fun forLanguage(l:GameLanguage)=UiText(l==GameLanguage.FRENCH) }
    fun t(en:String, french:String)=if(fr) french else en
}

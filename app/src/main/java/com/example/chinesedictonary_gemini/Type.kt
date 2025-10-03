package com.example.chinesedictonary_gemini

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

// 1. 定義 Google Font Provider
// 這一步告訴 App 如何向 Google Fonts 請求字體
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// 2. 定義字體名稱
// 我們選用 Noto Serif TC 作為標楷體的優雅替代方案
val BiaoKaiStyleFont = GoogleFont("Noto Serif TC")

// 3. 建立字體家族 (FontFamily)
val BiaoKaiFontFamily = FontFamily(
    Font(googleFont = BiaoKaiStyleFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = BiaoKaiStyleFont, fontProvider = provider, weight = FontWeight.Bold)
)

// 4. 建立一個新的 Typography 物件，全面使用新字體
val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = BiaoKaiFontFamily, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = BiaoKaiFontFamily, fontWeight = FontWeight.Bold),
    displaySmall = TextStyle(fontFamily = BiaoKaiFontFamily, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = BiaoKaiFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = BiaoKaiFontFamily, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontFamily = BiaoKaiFontFamily, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = BiaoKaiFontFamily, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontFamily = BiaoKaiFontFamily, fontWeight = FontWeight.Bold),
    titleSmall = TextStyle(fontFamily = BiaoKaiFontFamily, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontFamily = BiaoKaiFontFamily, fontSize = 18.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontFamily = BiaoKaiFontFamily, fontSize = 16.sp),
    bodySmall = TextStyle(fontFamily = BiaoKaiFontFamily),
    labelLarge = TextStyle(fontFamily = BiaoKaiFontFamily),
    labelMedium = TextStyle(fontFamily = BiaoKaiFontFamily),
    labelSmall = TextStyle(fontFamily = BiaoKaiFontFamily)
)

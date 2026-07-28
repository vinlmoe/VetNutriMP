package fr.vetbrain.vetnutri_mp.Theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val VetNutriTypography =
        Typography(
                h1 =
                        TextStyle(
                                fontWeight = FontWeight.Light,
                                fontSize = AppSizes.fontSizeH1,
                                letterSpacing = (-1.5).sp
                        ),
                h2 =
                        TextStyle(
                                fontWeight = FontWeight.Light,
                                fontSize = AppSizes.fontSizeH2,
                                letterSpacing = (-0.5).sp
                        ),
                h3 =
                        TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = AppSizes.fontSizeH3,
                                letterSpacing = 0.sp
                        ),
                h4 =
                        TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = AppSizes.fontSizeH4,
                                letterSpacing = 0.25.sp
                        ),
                h5 =
                        TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = AppSizes.fontSizeH5,
                                letterSpacing = 0.sp
                        ),
                h6 =
                        TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = AppSizes.fontSizeH6,
                                letterSpacing = 0.15.sp
                        ),
                subtitle1 =
                        TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = AppSizes.fontSizeSubtitle1,
                                letterSpacing = 0.15.sp
                        ),
                subtitle2 =
                        TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = AppSizes.fontSizeSubtitle2,
                                letterSpacing = 0.1.sp
                        ),
                body1 =
                        TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = AppSizes.fontSizeBody1,
                                letterSpacing = 0.5.sp
                        ),
                body2 =
                        TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = AppSizes.fontSizeBody2,
                                letterSpacing = 0.25.sp
                        ),
                button =
                        TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = AppSizes.fontSizeBody2,
                                letterSpacing = 1.25.sp
                        ),
                caption =
                        TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = AppSizes.fontSizeCaption,
                                letterSpacing = 0.4.sp
                        ),
                overline =
                        TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = 10.sp,
                                letterSpacing = 1.5.sp
                        )
        )

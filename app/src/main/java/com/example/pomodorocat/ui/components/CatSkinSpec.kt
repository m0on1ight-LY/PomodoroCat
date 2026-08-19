package com.example.pomodorocat.ui.components

import androidx.compose.ui.graphics.Color

/**
 * 过程化猫咪皮肤与品种渲染规格
 */
data class CatSkinSpec(
    val id: String,
    val name: String,
    val furColor: Color,
    val furShadowColor: Color,
    val bellyColor: Color,
    val earInnerColor: Color = Color(0xFFFFB6C1),
    val stripeColor: Color,
    val eyeColor: Color,
    val noseColor: Color = Color(0xFFFF8DA1),
    val pawColor: Color = Color(0xFFFF8DA1),
    val isSiameseMask: Boolean = false,
    val isCalicoPatches: Boolean = false,
    val calicoPatchColor: Color = Color(0xFF37474F)
) {
    companion object {
        // 1. 元气橘猫
        val ORANGE_TABBY = CatSkinSpec(
            id = "orange_tabby",
            name = "元气橘橘",
            furColor = Color(0xFFFFB74D),
            furShadowColor = Color(0xFFFFA726),
            bellyColor = Color(0xFFFFF3E0),
            stripeColor = Color(0xFFF57C00),
            eyeColor = Color(0xFF43A047) // 翠绿眼
        )

        // 2. 软萌三花
        val CALICO = CatSkinSpec(
            id = "calico",
            name = "软萌三花",
            furColor = Color(0xFFFAFAFA),
            furShadowColor = Color(0xFFEEEEEE),
            bellyColor = Color(0xFFFFFDE7),
            stripeColor = Color(0xFFFFB74D), // 暖橘斑
            eyeColor = Color(0xFF2E7D32),
            isCalicoPatches = true,
            calicoPatchColor = Color(0xFF37474F) // 黛黑斑
        )

        // 3. 警长奶牛
        val TUXEDO = CatSkinSpec(
            id = "tuxedo",
            name = "警长奶牛",
            furColor = Color(0xFF263238), // 亮黑毛
            furShadowColor = Color(0xFF1B2428),
            bellyColor = Color(0xFFFFFFFF), // 纯白胸脯
            stripeColor = Color(0xFF1A237E),
            eyeColor = Color(0xFFFFCA28) // 金珀大眼
        )

        // 4. 学霸暹罗
        val SIAMESE = CatSkinSpec(
            id = "siamese",
            name = "学霸暹罗",
            furColor = Color(0xFFFFF8E1), // 奶油白身躯
            furShadowColor = Color(0xFFFFECB3),
            bellyColor = Color(0xFFFFFFFF),
            stripeColor = Color(0xFF4E342E), // 巧克力重点色
            eyeColor = Color(0xFF0288D1), // 湛蓝晶莹眼
            isSiameseMask = true
        )

        // 5. 贵族英短蓝猫
        val BRITISH_SHORTHAIR = CatSkinSpec(
            id = "british_shorthair",
            name = "贵族蓝宝",
            furColor = Color(0xFF78909C), // 高级蓝灰毛
            furShadowColor = Color(0xFF607D8B),
            bellyColor = Color(0xFF90A4AE),
            stripeColor = Color(0xFF546E7A),
            eyeColor = Color(0xFFFFB300) // 铜金明眸
        )

        fun getById(id: String): CatSkinSpec {
            return when (id) {
                "calico" -> CALICO
                "tuxedo" -> TUXEDO
                "siamese" -> SIAMESE
                "british_shorthair" -> BRITISH_SHORTHAIR
                else -> ORANGE_TABBY
            }
        }
    }
}

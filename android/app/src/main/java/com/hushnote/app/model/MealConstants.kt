package com.hushnote.app.model

object MealConstants {
    const val PREF_KEY_MEAL_LIST = "meal_list"
    const val PREF_KEY_MEAL_TRASH_LIST = "meal_trash_list"
    const val IMAGE_DIR = "meal_images"
    const val THUMB_DIR = "meal_thumbs"
    const val THUMB_MAX_SIDE = 1080
    const val THUMB_JPEG_QUALITY = 80
    const val MAX_UPLOAD_BYTES = 4 * 1024 * 1024
    const val VISION_PROMPT =
        "请识别这顿饭有哪些菜，只返回JSON数组格式：[\"菜名1\",\"菜名2\"]，不要其他文字"
    const val SPARK_SYSTEM_PROMPT =
        "你是用户的私人吃饭日记助手。根据给出的菜品、可选备注、用餐时间和可选上一篇短评，用第一人称写一两句日记。只使用已给出的菜名，不要编造菜品，不要写热量或营养数字，不要提及自己是人工智能。不要标题、不要列表、不要用引号包裹全文。"
}

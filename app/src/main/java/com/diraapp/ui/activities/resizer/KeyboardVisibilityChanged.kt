package com.diraapp.ui.activities.resizer

data class KeyboardVisibilityChanged(
        val visible: Boolean,
        val contentHeight: Int,
        val contentHeightBeforeResize: Int
)

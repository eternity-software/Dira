package com.diraapp.storage.images

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.Constraint
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import java.io.File

fun AppCompatActivity.compress(
        inputFile: File,
        callback: Callback,
        vararg constraints: Constraint
) {
    val context = this
    var outputFile: File? = null
    lifecycleScope.launch {
        try {
            outputFile = Compressor.compress(context, inputFile) {


                if (constraints.isEmpty()) {
                    default()
                } else {
                    for (con in constraints)
                        constraint(con)
                }
                quality(60)
                size(1_597_152) // 1 MB
            }.also {
                callback.onComplete(true, it)
            }
        } catch (e: Exception) {
            callback.onComplete(false, outputFile)
        }
    }
}

class ImageCompressor {
    companion object {
        @JvmStatic
        fun compress(activity: AppCompatActivity, inputFile: File,
                     callback: Callback,
                     vararg constraints: Constraint) {
            activity.compress(inputFile, callback, *constraints)
        }
    }
}

interface Callback {
    fun onComplete(status: Boolean, file: File?)
}
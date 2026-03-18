package com.tenebralis.dreamos.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * 客户端图片压缩工具
 *
 * - NPC 头像：512×512 px，WebP 80% 质量
 * - GIF 动图不做压缩，直接返回原始字节
 */
class ImageCompressor @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val contentResolver get() = context.contentResolver

    data class CompressedImage(
        val bytes: ByteArray,
        val mimeType: String,
        val extension: String
    )

    /**
     * 压缩图片为 NPC 头像规格
     */
    fun compressForNpcAvatar(uri: Uri): CompressedImage {
        val mime = contentResolver.getType(uri) ?: "image/webp"

        // GIF 动图不做压缩，直接返回原始字节
        if (mime == "image/gif") {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("无法读取图片")
            return CompressedImage(bytes, "image/gif", "gif")
        }

        // 解码原始 Bitmap
        val original = contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: throw IllegalStateException("无法解码图片")

        // 缩放到 512×512（保持比例，居中裁剪）
        val scaled = scaleCenterCrop(original, TARGET_SIZE, TARGET_SIZE)
        if (scaled !== original) original.recycle()

        // 压缩为 WebP
        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.WEBP_LOSSY, QUALITY, baos)
        scaled.recycle()

        return CompressedImage(baos.toByteArray(), "image/webp", "webp")
    }

    private fun scaleCenterCrop(source: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val srcW = source.width
        val srcH = source.height

        // 如果已经小于等于目标尺寸，直接返回
        if (srcW <= targetW && srcH <= targetH) return source

        val scale = maxOf(targetW.toFloat() / srcW, targetH.toFloat() / srcH)
        val scaledW = (srcW * scale).toInt()
        val scaledH = (srcH * scale).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(source, scaledW, scaledH, true)

        // 居中裁剪
        val x = (scaledW - targetW) / 2
        val y = (scaledH - targetH) / 2
        val cropped = Bitmap.createBitmap(scaledBitmap, x, y, targetW, targetH)
        if (cropped !== scaledBitmap) scaledBitmap.recycle()

        return cropped
    }

    private companion object {
        const val TARGET_SIZE = 512
        const val QUALITY = 80
    }
}

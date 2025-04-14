package com.intezya.solution.services

import org.springframework.stereotype.Service
import java.io.InputStream
import java.util.*

@Service
class ImageService(
	private val advertisementService: AdvertisementService,
	private val minioService: MinioService,
) {
	fun getAdvertisementImage(
		advertiserId: UUID,
	): InputStream {
		val advertisement = advertisementService.getForImage(advertiserId)

		advertisement.imageUrl ?: return InputStream.nullInputStream()

		return try {
			minioService.getImage(advertisement.imageUrl).use { stream ->
				stream.readBytes().inputStream()
			}
		} catch (_: Exception) {
			InputStream.nullInputStream()
		}
	}
}

package com.intezya.solution.services

import io.minio.*
import io.minio.errors.MinioException
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI

@Service
class MinioService(
	@Value("\${minio.url}")
	private val minioUrl: String,
	@Value("\${minio.accessKey}")
	private val accessKey: String,
	@Value("\${minio.secretKey}")
	private val secretKey: String,
	@Value("\${minio.bucket}")
	private val bucketName: String,
) {
	private val minioClient = MinioClient.builder().endpoint(minioUrl).credentials(accessKey, secretKey).build()

	init {
		createBucketIfNotExists()
	}

	private fun createBucketIfNotExists() {
		try {
			val bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build()
			if (!minioClient.bucketExists(bucketExistsArgs)) {
				val makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build()
				minioClient.makeBucket(makeBucketArgs)
			}
		} catch (e: MinioException) {
			throw RuntimeException("Error while creating bucket: ${e.message}")
		}
	}

	@Async
	fun asyncUploadImage(imageUrl: String) {
		val imageStream = downloadImage(imageUrl)

		uploadImage(imageUrl, imageStream, "image/jpeg")
	}

	private fun uploadImage(imageName: String, inputStream: InputStream, contentType: String): String {
		try {
			minioClient.putObject(
				PutObjectArgs.builder().bucket(bucketName).`object`(imageName).stream(inputStream, -1, 10485760)
					.contentType(contentType).build()
			)
			return "http://localhost:9000/$bucketName/$imageName"
		} catch (e: MinioException) {
			throw RuntimeException("Download error: ${e.message}")
		}
	}

	fun getImage(imageName: String): InputStream {
		return minioClient.getObject(
			GetObjectArgs.builder().bucket(bucketName).`object`(imageName).build()
		)
	}

	fun downloadImage(imageUrl: String): InputStream {
		val url = URI(imageUrl).toURL()
		val connection = url.openConnection() as HttpURLConnection
		connection.requestMethod = "GET"
		return connection.inputStream
	}
}

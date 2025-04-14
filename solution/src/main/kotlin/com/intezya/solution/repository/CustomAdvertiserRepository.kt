package com.intezya.solution.repository

import com.intezya.solution.entity.Advertiser

interface CustomAdvertiserRepository {
	fun bulkUpsert(advertisers: List<Advertiser>)
}

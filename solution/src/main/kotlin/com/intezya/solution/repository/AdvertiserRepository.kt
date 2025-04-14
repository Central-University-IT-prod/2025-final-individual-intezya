package com.intezya.solution.repository

import com.intezya.solution.entity.Advertiser
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AdvertiserRepository : CrudRepository<Advertiser, UUID>, CustomAdvertiserRepository

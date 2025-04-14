package com.intezya.solution.repository

import com.intezya.solution.entity.GlobalSettingsEntry
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GlobalSettingsRepository : CrudRepository<GlobalSettingsEntry, String>

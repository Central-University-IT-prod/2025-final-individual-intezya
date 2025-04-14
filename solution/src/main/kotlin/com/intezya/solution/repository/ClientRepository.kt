package com.intezya.solution.repository

import com.intezya.solution.entity.Client
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ClientRepository : CrudRepository<Client, UUID>, CustomClientRepository

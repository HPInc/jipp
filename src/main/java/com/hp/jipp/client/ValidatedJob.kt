package com.hp.jipp.client

import com.hp.jipp.model.Packet

data class ValidatedJob(val jobRequest: JobRequest, val packet: Packet)

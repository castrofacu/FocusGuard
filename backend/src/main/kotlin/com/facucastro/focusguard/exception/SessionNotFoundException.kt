package com.facucastro.focusguard.exception

class SessionNotFoundException(id: Long) :
    RuntimeException("Session with id $id not found")

package com.facucastro.focusguard.exception

class SessionAlreadyExistsException(id: Long) :
    RuntimeException("Session with id $id already exists")

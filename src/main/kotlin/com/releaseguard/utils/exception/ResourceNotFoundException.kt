package com.releaseguard.utils.exception

class ResourceNotFoundException : RuntimeException {

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
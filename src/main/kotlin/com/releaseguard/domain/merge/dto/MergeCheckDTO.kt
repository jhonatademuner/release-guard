package com.releaseguard.domain.merge.dto

data class MergeCheckDTO (

    var repo: String,
    var branch: String,
    var pullRequest: String,
    var mergeAvailability: Boolean = true,
    var reason: String = "Merge is available for this branch"

)
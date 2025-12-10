package com.akl.templatemobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
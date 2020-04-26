package com.york.bluetoothlibrary

class AutoReleaseLock(private var isRelease: Boolean) {

    private val waitLock = Object()

    fun waitFor(timeoutMillisecond: Long) {
        var sleepTime: Long = 0

        while (!isRelease) {
            Thread.sleep(20)
            sleepTime += 20
            if (sleepTime >= timeoutMillisecond) {
                break
            }
        }
        isRelease = false
        synchronized(waitLock) {
            waitLock.wait(timeoutMillisecond)
        }
    }

    fun release() {
        isRelease = true
        synchronized(waitLock) {
            waitLock.notify()
        }
    }

}
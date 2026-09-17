package io.github.lord1egypt.prootx.support

import android.system.Os

/** Injectable so symlink creation is unit-testable without the Android runtime. */
interface Symlinker {
    fun createSymlink(targetPath: String, linkPath: String)

    companion object {
        fun default(): Symlinker = OsSymlinker()
    }
}

private class OsSymlinker : Symlinker {
    override fun createSymlink(targetPath: String, linkPath: String) {
        Os.symlink(targetPath, linkPath)
    }
}

package com.example.sunday.p2pplayer.transfer

import java.io.File

/**
 * Created by sunday on 19-4-25.
 */
interface TransferItem {
    abstract fun getName(): String

    abstract fun getDisplayName(): String

    /**
     * Actual file in the file system to which the data is saved. Ideally it should be
     * inside the save path of the parent transfer.
     *
     * @return
     */
    abstract fun getFile(): File

    abstract fun getSize(): Long

    abstract fun isSkipped(): Boolean

    abstract fun getDownloaded(): Long

    /**
     * [0..100]
     *
     * @return
     */
    abstract fun getProgress(): Int

    abstract fun isComplete(): Boolean
}
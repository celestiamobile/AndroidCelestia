package space.celestia.celestiafoundation.resource.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class ResourceInstallationTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun installsStagedFiles() {
        val staging = temporaryFolder.newFolder("staging")
        File(staging, "description.json").writeText("metadata")
        val destination = File(temporaryFolder.root, "addon")

        commitResourceInstallation(staging, destination)

        assertEquals("metadata", File(destination, "description.json").readText())
        assertFalse(staging.exists())
    }

    @Test
    fun replacesOldFilesWithoutLeavingObsoleteContent() {
        val destination = temporaryFolder.newFolder("addon")
        File(destination, "old.txt").writeText("old")
        val staging = temporaryFolder.newFolder("staging")
        File(staging, "new.txt").writeText("new")

        commitResourceInstallation(staging, destination)

        assertEquals("new", File(destination, "new.txt").readText())
        assertFalse(File(destination, "old.txt").exists())
        assertEquals(listOf("addon"), temporaryFolder.root.list()?.toList())
    }

    @Test
    fun restoresExistingInstallationWhenCommitFails() {
        val destination = temporaryFolder.newFolder("addon")
        File(destination, "old.txt").writeText("old")
        val missingStaging = File(temporaryFolder.root, "missing")

        assertThrows(IOException::class.java) {
            commitResourceInstallation(missingStaging, destination)
        }

        assertEquals("old", File(destination, "old.txt").readText())
        assertEquals(listOf("addon"), temporaryFolder.root.list()?.toList())
    }
}

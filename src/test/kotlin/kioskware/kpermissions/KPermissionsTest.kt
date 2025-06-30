package kioskware.kpermissions

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KPermissionsTest {

    @Test
    fun `test allows with exact permission match`() {
        assertTrue(KPermissions.allows("users.read", "users.read"))
        assertTrue(KPermissions.allows("assets.files.write", "assets.files.write"))
    }

    @Test
    fun `test allows with domain wildcard`() {
        assertTrue(KPermissions.allows("users.read", "users.*"))
        assertTrue(KPermissions.allows("users.write", "users.*"))
        assertTrue(KPermissions.allows("assets.files.read", "assets.*"))
    }

    @Test
    fun `test allows with action wildcard`() {
        assertTrue(KPermissions.allows("users.read", "*.read"))
        assertTrue(KPermissions.allows("assets.files.read", "*.*.read"))
    }

    @Test
    fun `test allows with global wildcard`() {
        assertTrue(KPermissions.allows("users.read", "*"))
        assertTrue(KPermissions.allows("assets.files.write", "*"))
    }

    @Test
    fun `test allows with mixed wildcards`() {
        assertTrue(KPermissions.allows("assets.files.read", "assets.*.read"))
        assertTrue(KPermissions.allows("assets.stats.read", "assets.*.read"))
    }

    @Test
    fun `test disallows with non-matching permissions`() {
        assertFalse(KPermissions.allows("users.read", "users.write"))
        assertFalse(KPermissions.allows("assets.files.read", "assets.stats.read"))
        assertFalse(KPermissions.allows("users.read", "assets.*"))
    }

    @Test
    fun `test disallows with insufficient permission depth`() {
        assertFalse(KPermissions.allows("assets.files.read", "assets"))
        assertFalse(KPermissions.allows("assets.files.read", "assets.files"))
    }

    @Test
    fun `test allows with multiple subdomain levels`() {
        assertTrue(KPermissions.allows("assets.files.archive.read", "assets.files.archive.*"))
        assertTrue(KPermissions.allows("assets.files.archive.read", "assets.files.*"))
        assertTrue(KPermissions.allows("assets.files.archive.read", "assets.*"))
        assertTrue(KPermissions.allows("assets.files.archive.read", "*"))
        assertTrue(KPermissions.allows("assets.stats.history.monthly.view", "assets.stats.history.monthly.*"))
        assertTrue(KPermissions.allows("assets.stats.history.monthly.view", "assets.stats.history.*"))
        assertTrue(KPermissions.allows("assets.stats.history.monthly.view", "assets.stats.*"))
        assertTrue(KPermissions.allows("assets.stats.history.monthly.view", "assets.*"))
    }

    @Test
    fun `test allows with wildcards at different levels`() {
        assertTrue(KPermissions.allows("assets.files.archive.read", "assets.*.*.read"))
        assertTrue(KPermissions.allows("assets.files.archive.delete", "assets.*.archive.*"))
        assertTrue(KPermissions.allows("users.profile.settings.edit", "users.*.*.edit"))
        assertTrue(KPermissions.allows("users.profile.settings.edit", "*.profile.settings.*"))
    }

    @Test
    fun `test disallows with insufficient subdomain depth`() {
        assertFalse(KPermissions.allows("assets.files.archive.read", "assets"))
        assertFalse(KPermissions.allows("assets.files.archive.read", "assets.files"))
        assertFalse(KPermissions.allows("users.profile.settings.edit", "users.profile"))
        assertFalse(KPermissions.allows("users.profile.settings.edit", "users.profile.settings"))
    }

    @Test
    fun `test compress permissions`() {

        var cpr = KPermissions.compress(
            listOf(
                "users.read",
                "users.write",
                "assets.files.read",
                "assets.files.write",
                "assets.stats.read",
                "*"
            )
        )

        assert(cpr.size == 1)
        assert(cpr.contains("*"))


        cpr = KPermissions.compress(
            listOf(
                "users.*",
                "users.read",
                "users.write",
                "assets.*.read",
                "assets.files.write",
                "assets.stats.read",
                "assets.stats.write"
            )
        )

        println(cpr)

        assert(!cpr.contains("users.read"))
        assert(!cpr.contains("users.write"))
        assert(!cpr.contains("assets.stats.read"))
        assert(cpr.contains("assets.stats.write"))

    }
}

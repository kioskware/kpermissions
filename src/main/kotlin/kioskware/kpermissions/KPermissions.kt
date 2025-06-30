package kioskware.kpermissions

/**
 * # KPermissions Declaration Language Specification
 *
 * ## Syntax
 * The Permission Declaration Language is based on hierarchical dot-notation with wildcard support.
 *
 * ## Structure
 * Permissions are represented as dot-separated segments in the format: `domain.subdomain1.subdomain2...subdomainN.action`
 * - **domain**: Main category of permissions (e.g., `users`, `assets`)
 * - **subdomains**: Optional subcategories within the hierarchy (e.g., `files`, `stats`, `archive`, `personal`)
 * - **action**: Specific operation (e.g., `read`, `write`)
 *
 * The system supports an unlimited number of subdomain levels to allow for deep hierarchical organization.
 *
 * ## Special Symbols
 * - **`.`** (dot): Hierarchy separator
 * - **`*`** (asterisk): Wildcard symbol, matches any segment at its position
 *
 * ## Permission Types
 *
 * ### Required Permissions
 * Specific operations that a user wants to perform:
 *
 * - `users.read`
 * - `users.write`
 * - `assets.files.read`
 * - `assets.files.write`
 * - `assets.files.archive.read`
 * - `assets.stats.read`
 * - `assets.stats.history.monthly.view`
 *
 * ### Granted Permissions
 * Permissions that define access scopes:
 *
 * - `users.*` (allows all user-related permissions)
 * - `assets.files.*` (allows all file-related permissions under assets)
 * - `assets.files.archive.*` (allows all archive-related permissions under files)
 * - `assets.stats.*` (allows all stats-related permissions under assets)
 * - `assets.*` (allows all permissions under assets)
 * - `*` (allows all permissions)
 * - `assets.*.read` (allows all read permissions under assets regardless of subdomain level)
 * - `*.*.read` (allows all read permissions regardless of domain or subdomain)
 *
 * ## Matching Rules
 * A granted permission matches a required permission when:
 * 1. They are identical
 * 2. The granted permission contains wildcards that match the required permission's segments
 * 3. The hierarchical structure is maintained
 *
 * ## Examples
 * - `users.read` is allowed by: `users.*`, `*.read`, `*`
 * - `assets.files.write` is allowed by: `assets.files.*`, `assets.*`, `*.*.write`, `*`
 * - `assets.stats.read` is allowed by: `assets.*.read`, `assets.stats.*`, `assets.*`, `*`
 * - `assets.files.archive.read` is allowed by: `assets.files.archive.*`, `assets.files.*`, `assets.*`, `*`
 * - `assets.stats.history.monthly.view` is allowed by: `assets.stats.history.monthly.*`, `assets.stats.history.*`, `assets.stats.*`, `assets.*`, `*`
 */
object KPermissions {

    /**
     * Compresses the list of permissions by eliminating overlapping permissions.
     * This compressor finds all permissions that are already covered by other permissions with wildcards
     * and keeps only the most general ones.
     *
     * For example:
     * - If the list contains `users.*` and `users.read`, only `users.*` will be kept
     * - If the list contains `assets.files.archive.*`, `assets.files.*` and `assets.files.read`,
     *   only `assets.files.*` will be kept (as it covers both others)
     *
     * @param permissions The list of permissions to compress.
     * @return A compressed list of permissions without duplicates or redundant entries.
     */
    fun compress(permissions: List<String>): List<String> {
        if (permissions.isEmpty()) return emptyList()

        // Jeśli "*" istnieje w uprawnieniach, zwróć tylko to jako obejmujące wszystko
        if (permissions.contains("*")) return listOf("*")

        // Sortowanie uprawnień - najpierw te z gwiazdkami, potem przez liczbę segmentów malejąco
        // To zapewnia, że bardziej ogólne uprawnienia będą rozpatrywane przed szczegółowymi
        val sortedPermissions = permissions.sortedWith(
            compareByDescending<String> { it.contains("*") }
                .thenByDescending { it.count { char -> char == '.' } }
        )

        // Wynikowa lista uprawnień po kompresji
        val result = mutableListOf<String>()

        // Dodaj każde uprawnienie do wynikowej listy tylko jeśli nie jest pokryte przez inne
        for (permission in sortedPermissions) {
            // Sprawdź czy to uprawnienie nie jest już pokryte przez którekolwiek z dodanych wcześniej
            if (result.none { allows(permission, it) }) {
                result.add(permission)
            }
        }

        return result
    }

    /**
     * Checks if the required permission is allowed by the granted permission.
     *
     * @param required The permission that is required.
     * @param granted The permission that is granted.
     *
     * @return `true` if the required permission is allowed by the granted permission, `false` otherwise.
     */
    fun allows(required: String, granted: String): Boolean {
        // Wildcard permission allows everything
        if (granted == "*") return true

        val requiredSegments = required.split(".")
        val grantedSegments = granted.split(".")

        // Sprawdź przypadek "domain.*" uprawnienia
        if (grantedSegments.lastOrNull() == "*") {
            // Uprawnienie kończy się gwiazdką, sprawdź czy prefiksy pasują
            val prefixLength = grantedSegments.size - 1

            // Jeśli prefiks jest dłuższy niż wymagane uprawnienie, nie można dopasować
            if (prefixLength > requiredSegments.size) return false

            // Sprawdź, czy wszystkie segmenty prefiksu pasują
            for (i in 0 until prefixLength) {
                val requiredSegment = requiredSegments[i]
                val grantedSegment = grantedSegments[i]

                // Gwiazdka pasuje do każdego segmentu
                if (grantedSegment == "*") continue

                // Jeśli segmenty nie pasują, uprawnienie nie jest dozwolone
                if (grantedSegment != requiredSegment) return false
            }

            // Wszystkie segmenty prefiksu pasują
            return true
        }

        // Jeśli przyznane uprawnienie ma mniej segmentów niż wymagane, nie może pasować
        if (grantedSegments.size < requiredSegments.size) {
            return false
        }

        // Porównaj każdy segment
        for (i in requiredSegments.indices) {
            val requiredSegment = requiredSegments[i]
            val grantedSegment = grantedSegments[i]

            // Gwiazdka pasuje do każdego segmentu na tej pozycji
            if (grantedSegment == "*") continue

            // Jeśli segmenty nie pasują, uprawnienie nie jest dozwolone
            if (grantedSegment != requiredSegment) return false
        }

        // Jeśli przyznane uprawnienie ma więcej segmentów niż wymagane, muszą być dokładnie równe
        return grantedSegments.size <= requiredSegments.size
    }

}

/**
 * Checks if the list of granted permissions allows all required permissions.
 *
 * @param required The list of required permissions.
 * @return `true` if all required permissions are allowed by at least one granted permission, `false` otherwise.
 */
infix fun List<String>.allows(required: List<String>): Boolean {
    return required.all { req -> this.any { granted -> KPermissions.allows(req, granted) } }
}

/**
 * Checks if the list of granted permissions allows required permission.
 *
 * @param required The required permission.
 * @return `true` if the required permission is allowed by at least one granted permission, `false` otherwise.
 */
infix fun List<String>.allows(required: String): Boolean {
    return this.any { granted -> KPermissions.allows(required, granted) }
}

/**
 * Checks if the list of granted permissions allows any of the required permissions.
 *
 * @param required The list of required permissions.
 * @return `true` if at least one required permission is allowed by at least one granted permission, `false` otherwise.
 */
infix fun List<String>.allowsAny(required: List<String>): Boolean {
    return required.any { req -> this.any { granted -> KPermissions.allows(req, granted) } }
}


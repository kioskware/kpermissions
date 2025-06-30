# KPermissions

A lightweight, efficient Kotlin library for hierarchical permission management using dot-notation and wildcard patterns.

## Overview

KPermissions provides a flexible system for declaring, checking, and compressing permission structures in applications that require fine-grained access control. It uses an intuitive dot-notation syntax with wildcard support to represent permission hierarchies.

Developed by [kioskware.co](kioskware.co) - company delivering professional Android kiosk software and hardware. Based in Poland 🇵🇱
## Installation

Add JitPack repository to your build file:

### Gradle (Kotlin DSL)

```kotlin
repositories {
    // ...existing repositories...
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.kioskware:kpermissions:0.1.0")
}
```

### Gradle (Groovy)

```groovy
repositories {
    // ...existing repositories...
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.kioskware:kpermissions:0.1.0'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.kioskware</groupId>
        <artifactId>kpermissions</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

## Features

- **Hierarchical Structure**: Define permissions with unlimited depth using `domain.subdomain.action` format
- **Wildcard Support**: Use the `*` symbol to represent multiple permissions at once
- **Permission Compression**: Automatically eliminate redundant permissions for efficient storage
- **Simple Matching Rules**: Clear logic for determining if a required permission is satisfied by granted permissions

## Usage

```kotlin
// Check if a user has the required permission
val requiredPermission = "assets.files.archive.read"
val userPermissions = listOf("assets.files.*", "users.read")

if (KPermissions.allows(requiredPermission, userPermissions)) {
    // User can access the archive files
}

// Compress a list of permissions to remove redundancies
val permissions = listOf("users.*", "users.read", "assets.files.*", "assets.files.read")
val compressed = KPermissions.compress(permissions)
// Result: ["users.*", "assets.files.*"]
```

## Permission Syntax

Permissions are structured as: `domain.subdomain1.subdomain2...subdomainN.action`

Examples:
- `users.read` - Permission to read user data
- `assets.files.archive.*` - All permissions related to file archives
- `*.*.write` - Write permission across any domain and subdomain

## License

```
Copyright 2025 Kioskware

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

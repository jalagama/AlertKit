# AlertKit

Centralized, priority-based in-app popup queue for Android ([repository](https://github.com/jalagama/AlertKit)).

## Where is it published?

| Location | Is AlertKit here? |
|----------|-------------------|
| **Google Maven** (`google()`) | **No** — that hosts AndroidX / Play Services, not your GitHub project. |
| **Maven Central** (`mavenCentral()`) | **No** — unless you separately publish there (Sonatype). |
| **[JitPack](https://jitpack.io)** | **Yes** — add `maven { url = uri("https://jitpack.io") }` and use the dependency below. |

Gradle only downloads what you declare **and** which exists on a **repository you added**. Without JitPack in `repositories`, `implementation("…")` will never resolve.

## Consume from another app (JitPack)

JitPack publishes multi-module Android projects under **`com.github.<username>.<RepositoryName>`**, not `com.github.<username>` alone.

For this repo the **group** is **`com.github.jalagama.AlertKit`**.

1. **Tag a release** in Git (JitPack uses the tag as the version), e.g. `1.0.0`.
2. In the consumer app, add JitPack and depend on **`popup-ui`** (includes **`popup-core`** transitively):

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.github.jalagama.AlertKit:popup-ui:1.0.0")

    // Optional: Compose host
    // implementation("com.github.jalagama.AlertKit:popup-compose:1.0.0")
}
```

Replace `1.0.0` with your tag. Confirm the exact line on [JitPack — jalagama/AlertKit](https://jitpack.io/#jalagama/AlertKit) after a green build.

**Wrong (will not resolve):** `com.github.jalagama:popup-ui:…` or `com.github.jalagama:AlertKit:…`

3. **Install in `Application`:**

```kotlin
class MyApp : Application() {
    lateinit var popupManager: PopupManager
    override fun onCreate() {
        super.onCreate()
        popupManager = PopupUi.install(this)
    }
}
```

4. **Manifest:** `android:name=".MyApp"` and optionally `VIBRATE` if you use haptics.

5. **Enqueue:**

```kotlin
(application as MyApp).popupManager.enqueue(
    PopupRequest(id = "x", title = "Hi", message = "…"),
)
```

## Local verification

```bash
./gradlew :popup-ui:publishToMavenLocal
```

Then use `mavenLocal()` and:

`implementation("com.github.jalagama.AlertKit:popup-ui:<VERSION_NAME>")`

(`ALERTKIT_MAVEN_GROUP` / `VERSION_NAME` are in `gradle.properties`. After changing publish metadata, tag a **new** version on JitPack, e.g. `1.0.1`, and use that in `implementation(...)`.)

---

## Still “Failed to resolve” / cannot download?

The artifacts **are** on JitPack (e.g. [popup-ui POM for 1.0.0](https://jitpack.io/com/github/jalagama/AlertKit/popup-ui/1.0.0/popup-ui-1.0.0.pom)). If Gradle still fails, it is almost always the **consumer** project.

### 1. Put JitPack in `settings.gradle.kts` (not only in `app/build.gradle`)

If you use **`dependencyResolutionManagement`** with **`repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)`** (Android Studio default for new apps), Gradle **ignores** `repositories { }` inside `build.gradle`. JitPack **must** be listed next to `google()` and `mavenCentral()` in **settings**.

Use this shape (Kotlin DSL):

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "YourApp"
include(":app")
```

**Groovy** (`settings.gradle`):

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. Exact dependency (case-sensitive)

```kotlin
implementation("com.github.jalagama.AlertKit:popup-ui:1.0.0")
```

- Group is **`com.github.jalagama.AlertKit`** (capital **`A`** in `AlertKit`).
- Artifact is **`popup-ui`**, not `AlertKit`.
- Version must match a **Git tag** that [JitPack built successfully](https://jitpack.io/#jalagama/AlertKit).

### 3. Refresh and get a real error line

In the consumer project directory:

```bash
./gradlew :app:assembleDebug --refresh-dependencies --stacktrace
```

Or in Android Studio: **File → Invalidate Caches → Invalidate and Restart**, then **Sync Project with Gradle Files**.

### 4. Version catalog (`libs.versions.toml`)

```toml
[versions]
alertkit = "1.0.0"

[libraries]
alertkit-popup-ui = { group = "com.github.jalagama.AlertKit", name = "popup-ui", version.ref = "alertkit" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.alertkit.popup.ui)
```

### 5. Network

Corporate VPN / proxy sometimes blocks `jitpack.io`. Try a browser: open  
`https://jitpack.io/com/github/jalagama/AlertKit/popup-ui/1.0.0/popup-ui-1.0.0.pom`  
If that does not load, fix network/VPN first.

### 6. “Could not find” but the POM opens in a browser (Gradle metadata bug)

JitPack runs Gradle with `-Pgroup=com.github.jalagama`. If the library used a Gradle property named `GROUP`, that value was **overridden**, so the published **`.module`** (Gradle Module Metadata) could declare the wrong component id while the **POM** looked correct — Gradle then fails resolution.

This repo uses **`ALERTKIT_MAVEN_GROUP`** (not `GROUP`) so metadata matches `com.github.jalagama.AlertKit:popup-ui`. After pulling that fix, **tag a new release** (e.g. `1.0.1`), wait for a green [JitPack](https://jitpack.io/#jalagama/AlertKit) build, then depend on that tag.

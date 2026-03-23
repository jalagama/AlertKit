# AlertKit

Centralized, priority-based in-app popup queue for Android ([repository](https://github.com/jalagama/AlertKit)).

## Consume from another app (JitPack)

1. **Tag a release** in Git (JitPack uses the tag as the version), e.g. `v0.1.0`, matching `VERSION_NAME` in `gradle.properties` (e.g. `0.1.0` without `v`).
2. In the consumer app, add JitPack and the modules you need:

```kotlin
// settings.gradle.kts — keep your existing pluginManagement / dependencyResolutionManagement
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    // Material / View system (transitively includes popup-core)
    implementation("com.github.jalagama:popup-ui:VERSION")

    // Optional: Jetpack Compose host
    // implementation("com.github.jalagama:popup-compose:VERSION")
}
```

Replace `VERSION` with your Git tag (e.g. `0.1.0` or `v0.1.0` — use the exact string [JitPack](https://jitpack.io/#jalagama/AlertKit) shows after the first successful build).

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

Published Maven coordinates are **`com.github.jalagama`** + artifact id **`popup-core`**, **`popup-ui`**, or **`popup-compose`** (see `gradle.properties` `GROUP` / `VERSION_NAME`).

## Local verification

```bash
./gradlew :popup-ui:publishToMavenLocal
```

Then depend on `com.github.jalagama:popup-ui:0.1.0` from `mavenLocal()`.

[![](https://jitpack.io/v/jalagama/AlertKit.svg)](https://jitpack.io/#jalagama/AlertKit)


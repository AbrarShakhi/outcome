# Outcome

[![Kotlin/Gradle CI](https://github.com/abrarshakhi/outcome/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/abrarshakhi/outcome/actions/workflows/gradle.yml)
[![Qodana](https://github.com/abrarshakhi/outcome/actions/workflows/qodana_code_quality.yml/badge.svg?branch=main)](https://github.com/abrarshakhi/outcome/actions/workflows/qodana_code_quality.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue)](https://kotlinlang.org/)
[![JitPack](https://img.shields.io/badge/JitPack-enabled-brightgreen)](https://jitpack.io/#abrarshakhi/outcome)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)


A lightweight Kotlin library for **safe and expressive error handling**, inspired by Kotlin’s `Result`.
It provides an `Outcome` type representing success (`Ok`) or failure (`Err`), with rich extension functions for functional-style programming.

---

## Table of Contents

* [Features](#features)
* [Installation (JitPack)](#installation-jitpack)
* [Usage](#usage)

  * [Creating Outcomes](#creating-outcomes)
  * [Handling Success and Errors](#handling-success-and-errors)
  * [Transforming Outcomes](#transforming-outcomes)
* [Testing](#testing)
* [Contributing](#contributing)
* [License](#license)

---

## Features

* `Outcome` sealed interface with `Ok` (success) and `Err` (error).
* Safe construction via `Outcome.ofOk()`, `Outcome.ofErr()`, and `maybeThrows {}`.
* Functional extensions: `map`, `flatMap`, `mapError`, `recoverWith`, `fold`, `onOk`, `onErr`.
* `mapCatching` to safely transform values that may throw exceptions.
* Integration-friendly: works with Kotlin, Gradle, and coroutines (handles `CancellationException`).
* Tested: unit-tested and fully documented.

---

## Installation (JitPack)

### Step 1: Add JitPack repository

**Gradle (Kotlin DSL)**

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Or inside `settings.gradle.kts` if using older Gradle:

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

---

### Step 2: Add the dependency

Replace `<version>` with a release tag (e.g., `1.0-1`):

```kotlin
dependencies {
    implementation("com.github.abrarshakhi:outcome:<version>")
}
```

Example:

```kotlin
implementation("com.github.abrarshakhi:outcome:1.0-1")
```

---

## Usage

### Creating Outcomes

```kotlin
val success: Outcome<Int, String> = Outcome.ofOk(42)
val failure: Outcome<Int, String> = Outcome.ofErr("Something went wrong")

// Safely execute a block that may throw
val result = Outcome.maybeThrows { riskyComputation() }
```

---

### Handling Success and Errors

```kotlin
success.isOk()        // true
failure.isErr()       // true

val value = success.getOrElse { -1 }   // 42
val error = failure.errorOrNull()      // "Something went wrong"

success.onOk { println("Success: $it") }
failure.onErr { println("Error: $it") }
```

---

### Transforming Outcomes

```kotlin
val doubled = success.map { it * 2 }                     // Ok(84)
val recovered = failure.recoverWith { Outcome.ofOk(0) }  // Ok(0)

val flattened: Outcome<Int, String> =
    Outcome.ofOk(Outcome.ofOk(10)).flatten()             // Ok(10)

val safeTransform = success.mapCatching { riskyTransformation(it) }
```

---

## Testing

The library is tested using **JUnit 5** and Kotlin test libraries.

```bash
./gradlew test
```

Test reports are available in:

```
build/reports/tests/test/index.html
```

---

## Contributing

Contributions are welcome!

1. Fork the repository
2. Create a feature branch
3. Write tests for your changes
4. Submit a pull request

We follow **GitHub Actions CI** for automated testing and Qodana static analysis.

---

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.



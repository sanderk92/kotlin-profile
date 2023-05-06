# Kotlin Profile

A simple-to-use model for xy charts with the following characteristics:
- The x-axis supports any Temporal, including custom implementations
- The x-axis supports any linear scale defined by TemporalUnit, including custom implementations
- The y-axis supports any value

```kotlin
val start = Instant.now().truncatedTo(ChronoUnit.DAYS)
val end = start.plus(1, ChronoUnit.DAYS)

// An empty profile
val profileOne = Profile.empty<Instant, String>()

// A profile with a single value repeated between two times
val profileTwo = Profile.between(start, end, ChronoUnit.HOURS, "value")

// A profile with specific values starting at the given time
val profileThree = Profile.of(start, ChronoUnit.HOURS, listOf("value1", "value2"))

// Two profiles can be flattened
val zipped = profileOne.zip(profileTwo, String::plus)
val flattened = listOf(profileOne, profileTwo).zip(String::plus)
```
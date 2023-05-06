import Profile.Companion.empty
import java.time.temporal.Temporal
import java.time.temporal.TemporalUnit

data class Point<T : Temporal, U : Any>(val time: T, val value: U)

class Profile<T : Temporal, U : Any> private constructor(
    private val points: List<Point<T, U>>,
) : Iterable<Point<T, U>> {

    override fun iterator() = points.iterator()

    fun get(time: T): Point<T, U>? = points.find { it.time == time }

    /**
     * Zip the points of the given profile into this profile matched by timestamp, applying the given mutator on
     * matching points. If a point in either profile is not found in the other profile, it is appended.
     */
    fun zip(other: Profile<T, U>, mutator: (U, U) -> U): Profile<T, U> =
        points
            .plus(other.points)
            .groupBy(Point<T, U>::time)
            .map { (time, values) -> values.reduce { a, b -> Point(time, mutator(a.value, b.value)) } }
            .let(::Profile)

    /**
     * Zip the points of the given profile into this profile matched by timestamp, applying the given mutator on
     * matching points. If a point in either profile is not found in the other profile, it is discarded.
     */
    fun zipInner(other: Profile<T, U>, mutator: (U, U) -> U): Profile<T, U> =
        points
            .mapNotNull { point -> findAndMutate(point, other.points, mutator) }
            .let(::Profile)

    /**
     * Zip the points of the given profile into this profile matched by timestamp, applying the given mutator on
     * matching points. If a point in the other profile is not found in this profile, it is discarded.
     */
    fun zipLeft(other: Profile<T, U>, mutator: (U, U) -> U): Profile<T, U> =
        points
            .map { point -> findAndMutate(point, other.points, mutator) ?: point }
            .let(::Profile)

    private fun findAndMutate(point: Point<T, U>, other: List<Point<T, U>>, mutator: (U, U) -> U): Point<T, U>? =
        other
            .firstOrNull { point.time == it.time }
            ?.let { Point(point.time, mutator(point.value, it.value)) }

    companion object {

        /**
         * A new profile containing no values whatsoever
         */
        fun <T : Temporal, U : Any> empty(): Profile<T, U> = Profile(emptyList())

        /**
         * A new profile containing all values, starting at the specified start time and incremented by the unit
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Temporal, U : Any> of(start: T, unit: TemporalUnit, values: List<U>): Profile<T, U> =
            values
                .mapIndexed { i, value -> Point(start.plus(unit.duration.multipliedBy(i.toLong())) as T, value) }
                .let(::Profile)

        /**
         * A new profile containing a single value repeated from start until end time, incremented by the unit
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Temporal, U : Any> between(start: T, endExclusive: T, unit: TemporalUnit, value: U): Profile<T, U> =
            (0 until unit.between(start, endExclusive))
                .map { i -> Point(start.plus(i, unit) as T, value) }
                .let(::Profile)
    }
}

/**
 * Flatten using [Profile.zipLeft]
 */
fun <T : Temporal, U : Any> Iterable<Profile<T, U>>.flatten(mutator: (U, U) -> U) =
    fold(empty<T, U>()) { a, b -> a.zipLeft(b, mutator) }

/**
 * Flatten using  [Profile.zipInner]
 */
fun <T : Temporal, U : Any> Iterable<Profile<T, U>>.flattenInner(mutator: (U, U) -> U) =
    fold(empty<T, U>()) { a, b -> a.zipInner(b, mutator) }

/**
 * Flatten using  [Profile.zip]
 */
fun <T : Temporal, U : Any> Iterable<Profile<T, U>>.flattenOuter(mutator: (U, U) -> U) =
    fold(empty<T, U>()) { a, b -> a.zip(b, mutator) }
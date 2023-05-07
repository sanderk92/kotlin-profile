import Profile.Companion.empty
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalUnit

data class Point<T : Temporal, U : Any>(val time: T, val value: U)

class Profile<T : Temporal, U : Any> private constructor(
    private val points: List<Point<T, U>>,
    private val unit: TemporalUnit,
) : Iterable<Point<T, U>> {

    override fun iterator() = points.iterator()

    /**
     * Get the value related to the specified time. Looks for an exact match only.
     */
    fun get(time: T): U? =
        this
            .find { it.time == time }
            ?.value

    /**
     * Zip the points of the given profile into this profile matched by timestamp, applying the given mutator on
     * matching points. Correct order is guaranteed. Scale differences are ignored and zipping is performed on exact
     * timestamps.
     */
    fun zip(other: Profile<T, U>, mutator: (U, U) -> U): Profile<T, U> =
        this
            .plus(other.points)
            .groupBy(Point<T, U>::time)
            .map { (time, values) -> values.reduce { a, b -> Point(time, mutator(a.value, b.value)) } }
            .let { Profile(it, if (unit.duration < other.unit.duration) unit else other.unit) }

    companion object {

        /**
         * A new profile containing no values whatsoever. Defaults to the smallest supported linear scale; nanos.
         */
        fun <T : Temporal, U : Any> empty(): Profile<T, U> = Profile(emptyList(), ChronoUnit.NANOS)

        /**
         * A new profile containing all values, starting at the specified start time and a linear scale of unit.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Temporal, U : Any> of(start: T, unit: TemporalUnit, values: List<U>): Profile<T, U> =
            values
                .mapIndexed { i, value -> Point(start.plus(unit.duration.multipliedBy(i.toLong())) as T, value) }
                .let { Profile(it, unit) }

        /**
         * A new profile containing a single value repeated from start until end time, with a linear scale of unit.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Temporal, U : Any> between(start: T, endExclusive: T, unit: TemporalUnit, value: U): Profile<T, U> =
            (0 until unit.between(start, endExclusive))
                .map { i -> Point(start.plus(i, unit) as T, value) }
                .let { Profile(it, unit) }
    }
}

/**
 * Flatten using [Profile.zip]
 */
fun <T : Temporal, U : Any> Iterable<Profile<T, U>>.zip(mutator: (U, U) -> U) =
    fold(empty<T, U>()) { a, b -> a.zip(b, mutator) }

import java.time.temporal.ChronoUnit.NANOS
import java.time.temporal.Temporal
import kotlin.math.sign

data class Profile<T: Temporal, U : Any>(val points: List<Point<T, U>>) : Iterable<Point<T, U>> {

    override fun iterator() = points.iterator()

    fun append(temporal: T, value: U) = Profile(points.plus(Point(temporal, value)))

    companion object {
        fun <T : Temporal, U : Any> empty(): Profile<T, U> = Profile(emptyList())
    }

}

data class Point<T : Temporal, U : Any>(val time: T, val value: U): Comparable<Point<T, U>> {
    override fun compareTo(other: Point<T, U>): Int = time.until(other.time, NANOS).sign
}
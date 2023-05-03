import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.*
import java.time.temporal.ChronoField.MINUTE_OF_DAY
import java.time.temporal.ChronoUnit.*
import kotlin.math.sign

/**
 * A [Ptu] represents a [Temporal] containing an exact quarter of minutes indicated by an index
 */
class Ptu private constructor(val date: LocalDate, val index: Int, val zone: ZoneId) : Temporal, Comparable<Ptu> {

    val zonedDateTime: ZonedDateTime = Quarters.addTo(date.atStartOfDay().atZone(zone), index.toLong())

    /**
     * Use the specified [ZonedDateTime] to create a new [Ptu]. If the [ZonedDateTime] does not represent a
     * multiple of 15 minutes, any extra minutes and seconds will be ignored and the previous index obtained.
     */
    constructor(zonedDateTime: ZonedDateTime) : this(
        date = zonedDateTime.toLocalDate(),
        index = (zonedDateTime.truncatedTo(DAYS).until(zonedDateTime, MINUTES) / Quarters.duration.toMinutes()).toInt(),
        zone = zonedDateTime.zone,
    )

    override fun getLong(field: TemporalField): Long {
        return zonedDateTime.getLong(field)
    }

    override fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        return zonedDateTime.until(endExclusive, MINUTES) / unit.duration.toMinutes()
    }

    override fun plus(amountToAdd: Long, unit: TemporalUnit): Ptu {
        return Ptu(zonedDateTime.plus(amountToAdd, unit))
    }

    override fun with(field: TemporalField, newValue: Long): Ptu {
        return Ptu(zonedDateTime.with(field, newValue))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return zonedDateTime == (other as Ptu).zonedDateTime
    }

    override fun isSupported(unit: TemporalUnit): Boolean = true
    override fun isSupported(field: TemporalField): Boolean = true
    override fun hashCode(): Int = zonedDateTime.hashCode()
    override fun toString(): String = "Ptu(index=$index,time=$zonedDateTime)"
    override fun compareTo(other: Ptu): Int = zonedDateTime.until(other, NANOS).sign
}

@Suppress("UNCHECKED_CAST")
object Quarters : TemporalUnit {
    override fun isTimeBased(): Boolean = true
    override fun isDateBased(): Boolean = false
    override fun isDurationEstimated(): Boolean = false

    override fun getDuration(): Duration {
        return Duration.of(15, MINUTES)
    }

    override fun between(temporal: Temporal, other: Temporal): Long {
        return temporal.until(other, MINUTES) / duration.toMinutes()
    }

    override fun <R : Temporal> addTo(temporal: R, amount: Long): R {
        return temporal.plus(duration.multipliedBy(amount)) as R
    }
}

import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.*
import java.time.temporal.ChronoField.MINUTE_OF_DAY
import java.time.temporal.ChronoUnit.*
import kotlin.math.sign

data class PtuIndex(val date: LocalDate, val index: Int, val zone: ZoneId) : Temporal, Comparable<PtuIndex> {

    val zonedDateTime: ZonedDateTime = Ptu.addTo(date.atStartOfDay().atZone(zone), index.toLong())

    override fun isSupported(unit: TemporalUnit): Boolean = true
    override fun isSupported(field: TemporalField): Boolean = true

    override fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        return zonedDateTime.until(endExclusive, MINUTES) / unit.duration.toMinutes()
    }

    override fun getLong(field: TemporalField): Long {
        return zonedDateTime.getLong(field)
    }

    override fun compareTo(other: PtuIndex): Int {
        return zonedDateTime.until(other, NANOS).sign
    }

    /**
     * Add the amount of the specified [TemporalUnit] to this [PtuIndex]. If the [TemporalUnit] does not represent a
     * multiple of 15 minutes, any extra minutes and seconds will be ignored and the previous index obtained.
     */
    override fun plus(amountToAdd: Long, unit: TemporalUnit): PtuIndex {
        return PtuIndex(zonedDateTime.plus(amountToAdd, unit))
    }

    /**
     * Set the value of the specified [TemporalField] of this [PtuIndex]. If the [TemporalField] does not represent a
     * multiple of 15 minutes, any extra minutes and seconds will be ignored and the previous index obtained.
     */
    override fun with(field: TemporalField, newValue: Long): PtuIndex {
        return PtuIndex(zonedDateTime.with(field, newValue))
    }

    /**
     * Use the specified [ZonedDateTime] to create a new [PtuIndex]. If the [ZonedDateTime] does not represent a
     * multiple of 15 minutes, any extra minutes and seconds will be ignored and the previous index obtained.
     */
    constructor(zonedDateTime: ZonedDateTime) : this(
        date = zonedDateTime.toLocalDate(),
        index = (zonedDateTime.get(MINUTE_OF_DAY) / Ptu.duration.toMinutes()).toInt(),
        zone = zonedDateTime.zone,
    )
}

object Ptu : TemporalUnit {
    override fun isTimeBased(): Boolean = true
    override fun isDateBased(): Boolean = false
    override fun isDurationEstimated(): Boolean = false

    override fun getDuration(): Duration {
        return Duration.of(15, MINUTES)
    }

    override fun between(temporal: Temporal, other: Temporal): Long {
        return temporal.until(other, MINUTES) / duration.toMinutes()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : Temporal> addTo(temporal: R, amount: Long): R {
        return temporal.plus(duration.multipliedBy(amount)) as R
    }
}

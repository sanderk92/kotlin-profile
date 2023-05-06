import java.time.*
import java.time.LocalTime.MIDNIGHT
import java.time.temporal.*
import java.time.temporal.ChronoField.MINUTE_OF_DAY
import java.time.temporal.ChronoUnit.*
import kotlin.math.sign

/**
 * A [Ptu] represents a [Temporal] containing an exact quarter of minutes indicated by an index
 */
class Ptu private constructor(val date: LocalDate, val index: Int, val zone: ZoneId) : Temporal, Comparable<Ptu> {

    val zonedDateTime: ZonedDateTime = Quarters.addTo(date.atStartOfDay().atZone(zone), index.toLong())

    constructor(zonedDateTime: ZonedDateTime) : this(
        date = zonedDateTime.toLocalDate(),
        index = QuarterOfDay.getFrom(zonedDateTime).toInt(),
        zone = zonedDateTime.zone,
    )

    override fun isSupported(unit: TemporalUnit): Boolean {
        return true
    }

    override fun isSupported(field: TemporalField): Boolean {
        return true
    }

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

    override fun hashCode(): Int {
        return zonedDateTime.hashCode()
    }

    override fun toString(): String {
        return "Ptu(index=$index,time=$zonedDateTime)"
    }

    override fun compareTo(other: Ptu): Int {
        return zonedDateTime.until(other, NANOS).sign
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return zonedDateTime == (other as Ptu).zonedDateTime
    }
}

object Quarters : TemporalUnit {
    override fun isTimeBased(): Boolean {
        return true
    }

    override fun isDateBased(): Boolean {
        return false
    }

    override fun isDurationEstimated(): Boolean {
        return false
    }

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

object QuarterOfDay : TemporalField {
    override fun isTimeBased(): Boolean {
        return true
    }

    override fun isDateBased(): Boolean {
        return false
    }

    override fun getBaseUnit(): TemporalUnit {
        return MINUTES
    }

    override fun getRangeUnit(): TemporalUnit {
        return MINUTES
    }

    override fun range(): ValueRange {
        return ValueRange.of(0, 99)
    }

    override fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange {
        return ValueRange.of(0, 99) // TODO could be further refined
    }

    override fun isSupportedBy(temporal: TemporalAccessor): Boolean {
        return temporal is Instant || temporal is ZonedDateTime
    }

    override fun getFrom(temporal: TemporalAccessor): Long = when (temporal) {
        is Instant -> temporal.truncatedTo(DAYS).until(temporal, MINUTES) / Quarters.duration.toMinutes()
        is ZonedDateTime -> temporal.truncatedTo(DAYS).until(temporal, MINUTES) / Quarters.duration.toMinutes()
        else -> throw UnsupportedTemporalTypeException("$temporal is not supported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : Temporal> adjustInto(temporal: R, newValue: Long): R {
        return temporal.with(Quarters.addTo(MIDNIGHT, newValue)) as R
    }
}

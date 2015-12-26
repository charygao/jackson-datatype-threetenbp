/*
 * Copyright 2013 FasterXML.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package com.fasterxml.jackson.datatype.threetenbp.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.DateTimeUtils;
import com.fasterxml.jackson.datatype.threetenbp.DecimalUtils;
import com.fasterxml.jackson.datatype.threetenbp.function.BiFunction;
import com.fasterxml.jackson.datatype.threetenbp.function.Function;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAccessor;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Deserializer for ThreeTen temporal {@link Instant}s, {@link OffsetDateTime}, and {@link ZonedDateTime}s.
 *
 * @author Nick Williams
 * @since 2.2
 */
public class InstantDeserializer<T extends Temporal>
        extends ThreeTenDateTimeDeserializerBase<T> {
    private static final long serialVersionUID = 1L;

    public static final InstantDeserializer<Instant> INSTANT = new InstantDeserializer<Instant>(
            Instant.class, DateTimeFormatter.ISO_INSTANT,
            new Function<TemporalAccessor, Instant>() {
                @Override
                public Instant apply(TemporalAccessor temporalAccessor) {
                    return Instant.from(temporalAccessor);
                }
            },
            new Function<FromIntegerArguments, Instant>() {
                @Override
                public Instant apply(FromIntegerArguments a) {
                    return Instant.ofEpochMilli(a.value);
                }
            },
            new Function<FromDecimalArguments, Instant>() {
                @Override
                public Instant apply(FromDecimalArguments a) {
                    return Instant.ofEpochSecond(a.integer, a.fraction);
                }
            },
            null
    );

    public static final InstantDeserializer<OffsetDateTime> OFFSET_DATE_TIME = new InstantDeserializer<OffsetDateTime>(
            OffsetDateTime.class, DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            new Function<TemporalAccessor, OffsetDateTime>() {
                @Override
                public OffsetDateTime apply(TemporalAccessor temporalAccessor) {
                    return OffsetDateTime.from(temporalAccessor);
                }
            },
            new Function<FromIntegerArguments, OffsetDateTime>() {
                @Override
                public OffsetDateTime apply(FromIntegerArguments a) {
                    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId);
                }
            },
            new Function<FromDecimalArguments, OffsetDateTime>() {
                @Override
                public OffsetDateTime apply(FromDecimalArguments a) {
                    return OffsetDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId);
                }
            },
            new BiFunction<OffsetDateTime, ZoneId, OffsetDateTime>() {
                @Override
                public OffsetDateTime apply(OffsetDateTime d, ZoneId z) {
                    return d.withOffsetSameInstant(z.getRules().getOffset(d.toLocalDateTime()));
                }
            }
    );

    public static final InstantDeserializer<ZonedDateTime> ZONED_DATE_TIME = new InstantDeserializer<ZonedDateTime>(
            ZonedDateTime.class, DateTimeFormatter.ISO_ZONED_DATE_TIME,
            new Function<TemporalAccessor, ZonedDateTime>() {
                @Override
                public ZonedDateTime apply(TemporalAccessor temporalAccessor) {
                    return ZonedDateTime.from(temporalAccessor);
                }
            },
            new Function<FromIntegerArguments, ZonedDateTime>() {
                @Override
                public ZonedDateTime apply(FromIntegerArguments a) {
                    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId);
                }
            },
            new Function<FromDecimalArguments, ZonedDateTime>() {
                @Override
                public ZonedDateTime apply(FromDecimalArguments a) {
                    return ZonedDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId);
                }
            },
            new BiFunction<ZonedDateTime, ZoneId, ZonedDateTime>() {
                @Override
                public ZonedDateTime apply(ZonedDateTime zonedDateTime, ZoneId zoneId) {
                    return zonedDateTime.withZoneSameInstant(zoneId);
                }
            }
    );

    protected final Function<FromIntegerArguments, T> fromMilliseconds;

    protected final Function<FromDecimalArguments, T> fromNanoseconds;

    protected final Function<TemporalAccessor, T> parsedToValue;

    protected final BiFunction<T, ZoneId, T> adjust;

    protected InstantDeserializer(Class<T> supportedType,
                                  DateTimeFormatter parser,
                                  Function<TemporalAccessor, T> parsedToValue,
                                  Function<FromIntegerArguments, T> fromMilliseconds,
                                  Function<FromDecimalArguments, T> fromNanoseconds,
                                  BiFunction<T, ZoneId, T> adjust) {
        super(supportedType, parser);
        this.parsedToValue = parsedToValue;
        this.fromMilliseconds = fromMilliseconds;
        this.fromNanoseconds = fromNanoseconds;
        this.adjust = adjust == null ? new BiFunction<T, ZoneId, T>() {
            @Override
            public T apply(T t, ZoneId zoneId) {
                return t;
            }
        } : adjust;
    }

    @SuppressWarnings("unchecked")
    protected InstantDeserializer(InstantDeserializer<T> base, DateTimeFormatter f) {
        super((Class<T>) base.handledType(), f);
        parsedToValue = base.parsedToValue;
        fromMilliseconds = base.fromMilliseconds;
        fromNanoseconds = base.fromNanoseconds;
        adjust = base.adjust;
    }

    @Override
    protected JsonDeserializer<T> withDateFormat(DateTimeFormatter dtf) {
        if (dtf == _formatter) {
            return this;
        }
        return new InstantDeserializer<T>(this, dtf);
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        //NOTE: Timestamps contain no timezone info, and are always in configured TZ. Only
        //string values have to be adjusted to the configured TZ.
        switch (parser.getCurrentTokenId()) {
            case JsonTokenId.ID_NUMBER_FLOAT: {
                BigDecimal value = parser.getDecimalValue();
                long seconds = value.longValue();
                int nanoseconds = DecimalUtils.extractNanosecondDecimal(value, seconds);
                return fromNanoseconds.apply(new FromDecimalArguments(
                        seconds, nanoseconds, getZone(context)));
            }

            case JsonTokenId.ID_NUMBER_INT: {
                long timestamp = parser.getLongValue();
                if (context.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)) {
                    return this.fromNanoseconds.apply(new FromDecimalArguments(
                            timestamp, 0, this.getZone(context)
                    ));
                }
                return this.fromMilliseconds.apply(new FromIntegerArguments(
                        timestamp, this.getZone(context)
                ));
            }

            case JsonTokenId.ID_STRING: {
                String string = parser.getText().trim();
                if (string.length() == 0) {
                    return null;
                }
                T value;
                try {
                    TemporalAccessor acc = _formatter.parse(string);
                    value = parsedToValue.apply(acc);
                    if (context.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)) {
                        return adjust.apply(value, this.getZone(context));
                    }
                } catch (DateTimeException e) {
                    throw _peelDTE(e);
                }
                return value;
            }
        }
        throw context.mappingException("Expected type float, integer, or string.");
    }

    private ZoneId getZone(DeserializationContext context) {
        // Instants are always in UTC, so don't waste compute cycles
        return (_valueClass == Instant.class) ? null : DateTimeUtils.timeZoneToZoneId(context.getTimeZone());
    }

    private static class FromIntegerArguments {
        public final long value;
        public final ZoneId zoneId;

        private FromIntegerArguments(long value, ZoneId zoneId) {
            this.value = value;
            this.zoneId = zoneId;
        }
    }

    private static class FromDecimalArguments {
        public final long integer;
        public final int fraction;
        public final ZoneId zoneId;

        private FromDecimalArguments(long integer, int fraction, ZoneId zoneId) {
            this.integer = integer;
            this.fraction = fraction;
            this.zoneId = zoneId;
        }
    }
}
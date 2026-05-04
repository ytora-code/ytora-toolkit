package xyz.ytora.toolkit.convert.converters;

import xyz.ytora.toolkit.convert.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * created by yangtong on 2025/8/9 22:44:24
 * <br/>
 */
public class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {

    @Override
    public LocalDateTime convert(Date source) {
        if (source == null) return null;
        long millis = source.getTime();
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    @Override
    public Date reverseConvert(LocalDateTime source) {
        if (source == null) return null;
        Instant instant = source.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }
}

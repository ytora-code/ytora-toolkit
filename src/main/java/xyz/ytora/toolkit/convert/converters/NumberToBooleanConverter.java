package xyz.ytora.toolkit.convert.converters;

import xyz.ytora.toolkit.convert.Converter;

/**
 * created by yangtong on 2025/8/13 19:19:53
 * <br/>
 */
public class NumberToBooleanConverter implements Converter<Number, Boolean> {
    @Override
    public Boolean convert(Number source) {
        if (source == null) {
            return null;
        }
        return source.intValue() > 0;
    }

    @Override
    public Number reverseConvert(Boolean source) {
        if (source == null) {
            return null;
        }
        return source ? 1 : 0;
    }
}

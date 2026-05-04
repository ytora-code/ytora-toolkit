package xyz.ytora.toolkit.convert.converters;

import xyz.ytora.toolkit.convert.Converter;
import xyz.ytora.toolkit.text.Strs;

import java.math.BigDecimal;

/**
 * created by yangtong on 2025/4/4 下午5:36
 */
public class DoubleToStringConverter implements Converter<Double, String> {
    @Override
    public String convert(Double source) {
        if (source == null) return null;
        return BigDecimal.valueOf(source)
                .stripTrailingZeros()
                .toPlainString();
    }

    @Override
    public Double reverseConvert(String source) {
        if (Strs.isEmpty(source)) return null;
        return Double.valueOf(source);
    }
}

package xyz.ytora.toolkit.convert.converters;

import xyz.ytora.toolkit.convert.Converter;
import xyz.ytora.toolkit.text.Strs;

import java.math.BigInteger;

/**
 * created by yangtong on 2025/4/4 下午5:36
 */
public class StringToBigIntegerConverter implements Converter<String, BigInteger> {
    @Override
    public BigInteger convert(String source) {
        if (Strs.isEmpty(source)) return null;
        return new BigInteger(source);
    }

    @Override
    public String reverseConvert(BigInteger source) {
        if (source == null) return null;
        return String.valueOf(source);
    }
}

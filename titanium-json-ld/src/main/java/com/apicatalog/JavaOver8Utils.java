package com.apicatalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import kotlin.Pair;
import kotlin.text.StringsKt;

/**
 * Created by Anton Zhilenkov on 10/11/2020.
 */
public class JavaOver8Utils {
    static public boolean isBlank(String value) {
        return StringsKt.isBlank(value);
    }

    static public String strip(String value) {
        return StringsKt.trim(value).toString();
    }

    static public String stripTrailing(String value) {
        return StringsKt.trimStart(value).toString();
    }

    static public class OptionalU {
        static public boolean isEmpty(Optional value) {
            return !value.isPresent();
        }
    }

    static public class DataSet {
        static public <T> List<T> copyListOf(Iterable<T> iterable) {
            List<T> newList = new ArrayList<>();
            for (T t : iterable) newList.add(t);
            return newList;
        }

        static public <K, V> Map<K, V> mapOf(Pair<K, V>... pairs){
            Map<K, V> map = new HashMap<>();
            for (Pair<K, V> pair : pairs) {
                map.put(pair.getFirst(), pair.getSecond());
            }
            return map;
        }

    }

    static public class PredicateU {
        static public <T> Predicate<T> not(Predicate<? super T> target) {
            Objects.requireNonNull(target);
            return (Predicate<T>)target.negate();
        }
    }
}





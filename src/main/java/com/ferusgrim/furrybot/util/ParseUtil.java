package com.ferusgrim.furrybot.util;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ParseUtil {

    public static List<String> getList(final ConfigurationNode node, final String... path) {
        final List<String> list = Lists.newArrayList();

        list.addAll(node.getNode(path).getList(o -> o instanceof String ? (String) o : "", Lists.newArrayList()));

        list.removeAll(Collections.singletonList(""));

        return list;
    }

    public static String[] removeFirstElement(final String[] array) {
        return Arrays.copyOfRange(array, 1, array.length);
    }
}

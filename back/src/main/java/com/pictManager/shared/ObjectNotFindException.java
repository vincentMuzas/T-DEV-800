package com.pictManager.shared;

import java.text.MessageFormat;

public final class ObjectNotFindException extends RuntimeException {
    private static final String PATERN = "Cannot find Object of type [{0}] for criteria [{1}{2}]";

    @SuppressWarnings("rawtypes")
    public static ObjectNotFindException byId(Class clazz, Long id) {
        return new ObjectNotFindException(clazz.getName(), "id=", ""+id);
    }

    @SuppressWarnings("rawtypes")
    public static ObjectNotFindException byName(Class clazz, String name) {
        return new ObjectNotFindException(clazz.getName(), "name=", name);
    }

    @SuppressWarnings("all")
    public ObjectNotFindException(String ... params) {
        super(MessageFormat.format(PATERN, params));
    }
}

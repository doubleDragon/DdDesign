package com.wsl.library.design;

import android.support.annotation.IntRange;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by wsl on 16-9-2.
 */
@Retention(CLASS)
@Target({TYPE,METHOD,CONSTRUCTOR,FIELD})
public @interface RequiresApi {
    /**
     * The API level to require. Alias for {@link #api} which allows you to leave out the
     * {@code api=} part.
     */
    @IntRange(from=1)
    int value() default 1;

    /** The API level to require */
    @IntRange(from=1)
    int api() default 1;
}


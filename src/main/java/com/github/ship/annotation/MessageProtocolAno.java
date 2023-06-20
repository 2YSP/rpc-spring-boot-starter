package com.github.ship.annotation;

import java.lang.annotation.*;

/**
 * @author Ship
 * @date 2020/8/19 16:33
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageProtocolAno {

    String value() default "";
}

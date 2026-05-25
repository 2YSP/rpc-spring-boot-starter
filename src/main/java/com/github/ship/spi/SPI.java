package com.github.ship.spi;

import java.lang.annotation.*;

/**
 * SPI extension point annotation, similar to Dubbo's @SPI.
 * Indicates that an interface is an SPI extension point.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {

    /**
     * Default extension name.
     */
    String value() default "";
}
package cc.eamon.open.annotation.common.mapper;

import cc.eamon.open.annotation.common.AccessLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Eamon on 2018/9/29.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface MapperIgnore {

    String[] value() default {};

}

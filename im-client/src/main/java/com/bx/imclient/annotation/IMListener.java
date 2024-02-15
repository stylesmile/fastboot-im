package com.bx.imclient.annotation;

import com.bx.imcommon.enums.IMListenerType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IMListener {

    IMListenerType type();

}

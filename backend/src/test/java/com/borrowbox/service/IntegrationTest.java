package com.borrowbox.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * A test that runs against the real application and a real database.
 *
 * <p>Transactional so each test rolls back what it did. That keeps them
 * independent without restarting the context between them, which matters once
 * there are enough of these to notice.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest
@Transactional
public @interface IntegrationTest {
}

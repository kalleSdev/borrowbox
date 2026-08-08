package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Identifier generation for members and items.
 */
class AlphaNumericGenTest {

  private final AlphaNumericGen generator = new AlphaNumericGen();

  @Test
  @DisplayName("produces an id of the requested length")
  void producesIdOfRequestedLength() {
    assertThat(generator.generateAlphaNum(3)).hasSize(3);
    assertThat(generator.generateAlphaNum(6)).hasSize(6);
  }

  @Test
  @DisplayName("uses only letters and digits")
  void usesOnlyLettersAndDigits() {
    assertThat(generator.generateAlphaNum(64)).matches("[a-zA-Z0-9]+");
  }

  @Test
  @DisplayName("does not hand out the same id every time")
  void doesNotHandOutTheSameIdEveryTime() {
    Set<String> generated = new HashSet<>();
    for (int i = 0; i < 100; i++) {
      generated.add(generator.generateAlphaNum(6));
    }

    assertThat(generated).hasSizeGreaterThan(90);
  }
}

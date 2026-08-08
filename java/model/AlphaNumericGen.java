package model;

import java.util.Random;

/**
 * Class for generating a random id.
 */
public class AlphaNumericGen {
  private static final String letters = "abcdefghijklmnopqrstuvwxyz"; // Static field
  private final char[] alphanumeric = (letters + letters.toUpperCase() + "0123456789").toCharArray();
  private final Random random = new Random(); // Reusable Random object

  /**
   * Method generating the random id.
   */
  public String generateAlphaNum(int length) {
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < length; i++) {
      result.append(alphanumeric[random.nextInt(alphanumeric.length)]);
    }
    return result.toString();
  }
}

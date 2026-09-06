package io.github.HollowKnight.Model.npc;

import com.badlogic.gdx.math.RandomXS128;

public final class ZoteDialogue {

  private static final RandomXS128 RANDOM = new RandomXS128();

  public static final String[] FIRST_CONVERSATION = {
      "HI",
      "I am Zote the Mighty!",
      "You look tired of working on AP.",
      "One day you may understand the art of game development.",
      "THANK YOU FOR YOUR ATTENTION"
  };

  public static final String[] PRECEPTS = {
      "Precept One: Always Win your battles.",
      "Precept Twelve: Keep your clock dry.",
      "Precept Twenty: Speak only the truth.",
      "Precept Thirty-Three: Show your enemy never respect.",
      "Precept Forty-Five: One thing is not an other."
  };

  private ZoteDialogue() {}

  public static String randomPrecept() {
    return PRECEPTS[RANDOM.nextInt(PRECEPTS.length)];
  }
}

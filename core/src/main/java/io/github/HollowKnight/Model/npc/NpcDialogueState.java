package io.github.HollowKnight.Model.npc;

import java.util.HashSet;
import java.util.Set;

public class NpcDialogueState {

  private final Set<String> firstConversationCompleted = new HashSet<>();

  public boolean hasCompletedFirstConversation(String npcId) {
    return firstConversationCompleted.contains(npcId);
  }

  public void markFirstConversationComplete(String npcId) {
    firstConversationCompleted.add(npcId);
  }

  public String[] toSaveArray() {
    return firstConversationCompleted.toArray(new String[0]);
  }

  public void restoreFromSave(String[] completedNpcIds) {
    firstConversationCompleted.clear();
    if (completedNpcIds == null) {
      return;
    }
    for (String id : completedNpcIds) {
      if (id != null && !id.isEmpty()) {
        firstConversationCompleted.add(id);
      }
    }
  }
}

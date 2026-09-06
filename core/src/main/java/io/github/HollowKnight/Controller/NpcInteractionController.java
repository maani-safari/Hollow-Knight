package io.github.HollowKnight.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import io.github.HollowKnight.Model.Settings.AudioManager;
import io.github.HollowKnight.Model.Settings.KeyController;
import io.github.HollowKnight.Model.World.Room;
import io.github.HollowKnight.Model.entity.Knight;
import io.github.HollowKnight.Model.npc.Npc;
import io.github.HollowKnight.Model.npc.NpcDialogueState;
import io.github.HollowKnight.Model.npc.NpcState;
import io.github.HollowKnight.Model.npc.ZoteDialogue;
import io.github.HollowKnight.View.DialogueView;
import io.github.HollowKnight.View.InteractionHintView;

public class NpcInteractionController {

  private final Knight knight;
  private final KeyController keys;
  private final AudioManager audio;
  private final DialogueView dialogueView;
  private final InteractionHintView hintView;
  private final NpcDialogueState dialogueState;

  private Npc nearbyNpc;
  private Npc activeNpc;
  private String[] currentLines;
  private int lineIndex;
  private boolean dialogueOpen;

  public NpcInteractionController(
      Knight knight,
      KeyController keys,
      AudioManager audio,
      DialogueView dialogueView,
      InteractionHintView hintView,
      NpcDialogueState dialogueState
  ) {
    this.knight = knight;
    this.keys = keys;
    this.audio = audio;
    this.dialogueView = dialogueView;
    this.hintView = hintView;
    this.dialogueState = dialogueState;
  }

  public NpcDialogueState getDialogueState() {
    return dialogueState;
  }

  public boolean isDialogueOpen() {
    return dialogueOpen;
  }

  public void update(float delta, Room room) {
    for (Npc npc : room.getNpcs()) {
      npc.update(delta);
    }

    if (dialogueOpen) {
      hintView.setVisible(false);
      return;
    }

    nearbyNpc = findNearbyNpc(room);
    hintView.setVisible(nearbyNpc != null);
  }

  public void handleInput(Room room) {
    if (dialogueOpen) {
      handleDialogueInput();
      return;
    }

    if (nearbyNpc == null) {
      nearbyNpc = findNearbyNpc(room);
    }
    if (nearbyNpc == null) {
      return;
    }

    if (!Gdx.input.isKeyJustPressed(keys.getInteract())) {
      return;
    }
    if (Gdx.input.isKeyPressed(keys.getCheatMode())) {
      return;
    }

    startDialogue(nearbyNpc);
  }

  public boolean tryHitNpc(Rectangle nailHitbox, Room room) {
    if (dialogueOpen) {
      return false;
    }

    boolean hitAny = false;
    for (Npc npc : room.getNpcs()) {
      if (!nailHitbox.overlaps(npc.getHitbox())) {
        continue;
      }
      npc.onHit();
      audio.playZoteHurt();
      hitAny = true;
    }
    return hitAny;
  }

  private void startDialogue(Npc npc) {
    activeNpc = npc;
    dialogueOpen = true;
    knight.setDialogueLocked(true);
    knight.getVelocity().setZero();
    npc.setState(NpcState.TALK);

    if (dialogueState.hasCompletedFirstConversation(npc.getId())) {
      currentLines = new String[]{ZoteDialogue.randomPrecept()};
    } else {
      currentLines = ZoteDialogue.FIRST_CONVERSATION;
    }

    lineIndex = 0;
    dialogueView.open(npc.getDisplayName(), currentLines[lineIndex]);
    audio.playRandomZoteVoice();
  }

  private void handleDialogueInput() {
    if (!Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      return;
    }

    lineIndex++;
    if (lineIndex < currentLines.length) {
      dialogueView.setLine(currentLines[lineIndex]);
      audio.playRandomZoteVoice();
      return;
    }

    closeDialogue();
  }

  public void forceCloseDialogue() {
    if (!dialogueOpen) {
      return;
    }
    closeDialogue();
  }

  private void closeDialogue() {
    if (activeNpc != null) {
      if (!dialogueState.hasCompletedFirstConversation(activeNpc.getId())
          && currentLines == ZoteDialogue.FIRST_CONVERSATION) {
        dialogueState.markFirstConversationComplete(activeNpc.getId());
      }
      if (activeNpc.getState() == NpcState.TALK) {
        activeNpc.setState(NpcState.IDLE);
      }
      activeNpc = null;
    }

    dialogueOpen = false;
    currentLines = null;
    lineIndex = 0;
    dialogueView.close();
    knight.setDialogueLocked(false);
  }

  private Npc findNearbyNpc(Room room) {
    Rectangle knightHitbox = knight.getHitbox();
    for (Npc npc : room.getNpcs()) {
      if (npc.isInInteractionRange(knightHitbox)) {
        return npc;
      }
    }
    return null;
  }
}

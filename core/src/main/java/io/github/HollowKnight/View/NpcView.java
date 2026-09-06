package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.HollowKnight.Model.World.Room;
import io.github.HollowKnight.Model.npc.Npc;
import io.github.HollowKnight.Model.npc.NpcState;
import io.github.HollowKnight.Model.npc.NpcType;

import java.util.EnumMap;
import java.util.Map;

public class NpcView {

  private final Map<NpcType, TextureAtlas> atlases = new EnumMap<>(NpcType.class);
  private final Map<NpcType, Map<NpcState, Animation<TextureRegion>>> animations =
      new EnumMap<>(NpcType.class);

  public NpcView() {
    loadZoteAnimations();
  }

  private void loadZoteAnimations() {
    TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("atlas/Zote.atlas"));
    atlases.put(NpcType.ZOTE, atlas);

    Map<NpcState, Animation<TextureRegion>> zoteAnims = new EnumMap<>(NpcState.class);

    zoteAnims.put(
        NpcState.IDLE,
        new Animation<>(0.18f, atlas.findRegions("Idle"), Animation.PlayMode.LOOP)
    );
    zoteAnims.put(
        NpcState.TALK,
        new Animation<>(0.14f, atlas.findRegions("Talk"), Animation.PlayMode.LOOP)
    );
    zoteAnims.put(
        NpcState.HIT,
        new Animation<>(0.08f, atlas.findRegions("Fall"), Animation.PlayMode.NORMAL)
    );
    zoteAnims.put(
        NpcState.ANGRY,
        new Animation<>(0.12f, atlas.findRegions("Talk"), Animation.PlayMode.LOOP)
    );
    animations.put(NpcType.ZOTE, zoteAnims);
  }

  public void render(SpriteBatch batch, Room room, float delta) {
    for (Npc npc : room.getNpcs()) {
      renderNpc(batch, npc, delta);
    }
  }

  private void renderNpc(SpriteBatch batch, Npc npc, float delta) {
    Map<NpcState, Animation<TextureRegion>> typeAnims = animations.get(npc.getType());
    if (typeAnims == null) {
      return;
    }

    Animation<TextureRegion> animation = typeAnims.get(npc.getState());
    if (animation == null) {
      animation = typeAnims.get(NpcState.IDLE);
    }

    boolean looping = npc.getState() == NpcState.IDLE
        || npc.getState() == NpcState.TALK
        || npc.getState() == NpcState.ANGRY;
    TextureRegion frame = animation.getKeyFrame(npc.getStateTimer(), looping);

    float scaleX = 2.6f;
    float scaleY = 2.6f;
    float width = npc.getHitbox().width * scaleX;
    float height = npc.getHitbox().height * scaleY;
    float drawX = npc.getPosition().x - width / 2f;
    float drawY = npc.getPosition().y;

    if (!npc.isFacingRight()) {
      batch.draw(frame, drawX + width, drawY, -width, height);
    } else {
      batch.draw(frame, drawX, drawY, width, height);
    }
  }

  public void dispose() {
    for (TextureAtlas atlas : atlases.values()) {
      atlas.dispose();
    }
    atlases.clear();
    animations.clear();
  }
}

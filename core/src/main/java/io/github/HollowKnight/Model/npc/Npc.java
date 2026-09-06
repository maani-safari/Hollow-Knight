package io.github.HollowKnight.Model.npc;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Npc {

  public static final float DEFAULT_INTERACT_RADIUS = 160f;
  public static final float HIT_WIDTH = 100f;
  public static final float HIT_HEIGHT = 100f;
  public static final float ANGRY_DURATION = 1.2f;
  public static final float HIT_DURATION = 0.35f;

  private final String id;
  private final String displayName;
  private final NpcType type;
  private final Vector2 position;
  private final Rectangle hitbox;
  private final Circle interactionRadius;
  private final boolean facingRight;

  private NpcState state = NpcState.IDLE;
  private float stateTimer;

  public Npc(String id, String displayName, NpcType type, float x, float y, boolean facingRight) {
    this(id, displayName, type, x, y, facingRight, DEFAULT_INTERACT_RADIUS);
  }

  public Npc(
      String id,
      String displayName,
      NpcType type,
      float x,
      float y,
      boolean facingRight,
      float interactRadius
  ) {
    this.id = id;
    this.displayName = displayName;
    this.type = type;
    this.position = new Vector2(x, y);
    this.hitbox = new Rectangle(x - HIT_WIDTH / 2f, y, HIT_WIDTH, HIT_HEIGHT);
    this.interactionRadius = new Circle(x, y + HIT_HEIGHT / 2f, interactRadius);
    this.facingRight = facingRight;
  }

  public void update(float delta) {
    stateTimer += delta;
    if (state == NpcState.HIT && stateTimer >= HIT_DURATION) {
      setState(NpcState.ANGRY);
    } else if (state == NpcState.ANGRY && stateTimer >= ANGRY_DURATION) {
      setState(NpcState.IDLE);
    }
  }

  public void onHit() {
    if (state == NpcState.TALK) {
      return;
    }
    setState(NpcState.HIT);
  }

  public void setState(NpcState newState) {
    if (state == newState) {
      return;
    }
    state = newState;
    stateTimer = 0f;
  }

  public boolean isInInteractionRange(Rectangle knightHitbox) {
    float cx = knightHitbox.x + knightHitbox.width / 2f;
    float cy = knightHitbox.y + knightHitbox.height / 2f;
    return interactionRadius.contains(cx, cy);
  }

  public String getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public NpcType getType() {
    return type;
  }

  public Vector2 getPosition() {
    return position;
  }

  public Rectangle getHitbox() {
    return hitbox;
  }

  public Circle getInteractionRadius() {
    return interactionRadius;
  }

  public boolean isFacingRight() {
    return facingRight;
  }

  public NpcState getState() {
    return state;
  }

  public float getStateTimer() {
    return stateTimer;
  }
}

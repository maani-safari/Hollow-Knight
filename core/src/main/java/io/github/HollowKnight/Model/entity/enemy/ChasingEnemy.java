package io.github.HollowKnight.Model.entity.enemy;

public abstract class ChasingEnemy extends Enemy  {
    public static final float VISIBLE_RANGE = 150f;
    public static final float PATROL_SPEED = 40f;
    public static final float CHASE_SPEED = 100f;
    public static final float FORGET_TARGET_TIME = 2.5f;
    protected boolean movingRight = true;
    protected float forgetTargetTime ;
    protected float attackCollDown;

    public ChasingEnemy(float spawnX, float spawnY, float width, float height, int maxHp, int soulReward, int contactDamage) {
        super(spawnX, spawnY, width, height, maxHp, soulReward, contactDamage);
        this.enemyState = EnemyState.WALK;
    }

    @Override
    public void setEnemyState(EnemyState enemyState) {
        this.enemyState = enemyState;
        this.stateTimer = 0f;
    }

    @Override
    public void update(float delta, EnemyContext ctx) {
        if (attackCollDown > 0) attackCollDown -= delta;
        switch (enemyState){
            case ATTACKING -> updateAttacking(delta,ctx);
            case WALK -> updatePatrol(ctx);
            case ALERT ->  updateAlert(delta , ctx);
            case HURT -> updateHurt(delta);
            case DEATH -> velocity.set(0,0);
            default -> {}
        }
    }
    protected abstract void updateAttacking(float delta , EnemyContext ctx);
    protected abstract void  tryAttack(float dist , boolean knightSide);
    protected void updateHurt(float delta) {
        velocity.x = 0f;
        if (stateTimer >= hurtStunned) {
            setEnemyState(EnemyState.ALERT);
        }
    }
    protected void updateAlert(float delta , EnemyContext ctx){
        if (!ctx.knightAlive){
            setEnemyState(EnemyState.WALK);
        }
        if (!canSeeKnight(ctx)){
            forgetTargetTime += delta;
            if (forgetTargetTime >= FORGET_TARGET_TIME){
                forgetTargetTime = 0f;
                setEnemyState(EnemyState.WALK);
                return;
            }
        }else {
            forgetTargetTime  = 0f;
        }
        boolean knightLeft = ctx.knightIsLeftOf(position);
        float dist = ctx.distanceToKnight(position);
        tryAttack(dist , knightLeft);

    }
    protected void updatePatrol(EnemyContext ctx){
        velocity.x = movingRight ? PATROL_SPEED  : -PATROL_SPEED;
        facingRight = movingRight;
        if (ctx.knightAlive && canSeeKnight(ctx)){
            setEnemyState(EnemyState.ALERT);
        }
    }
    protected boolean canSeeKnight(EnemyContext ctx){
        float dist = ctx.distanceToKnight(position);
        if (dist > VISIBLE_RANGE) return false;
        boolean knightLeft = ctx.knightIsLeftOf(position);
        return facingRight ? !knightLeft : knightLeft;
    }
    public void hitWallOrCliff(){
        movingRight = !movingRight;
    }
    public boolean isMovingRight(){
        return movingRight;
    }
}

package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.assets.AnimationStrip;
import org.example.assets.SpriteSheet;

public class EnemyActor extends GameObject {

    private static final double DEFAULT_SLOW_MOVE_FACTOR = 0.55;

    private final String name;
    private int hp;
    private final int maxHp;
    private final double speed;
    private final Color color;
    private final boolean boss;
    private double attackCooldown;
    private double vy;
    private boolean onGround;
    private double jumpCooldown;
    private double slowTimer;
    private int slowStack;
    private int bleedTicks;
    private int bleedTickDamage;
    private double bleedTickTimer;
    private double bleedTickIntervalSec = 0.35;
    private final EnemyTuningState tuningState = new EnemyTuningState();
    private SpriteSheet spriteSheet;
    private AnimationStrip idleStrip;
    private AnimationStrip walkStrip;
    private AnimationStrip attackStrip;
    private AnimationStrip castingStrip;
    private AnimationStrip dashStrip;
    private double animationTime;
    private boolean moving;
    private boolean attacking;
    /** Keeps {@link #attackStrip} visible briefly after a melee hit so bosses do not flash attack for one frame. */
    private double bossMeleeAnimHoldSec;
    private int facing = -1;
    
    // Caesar Hunos boss-specific fields
    private boolean isCaesarHunos;
    private boolean isSirKhai;
    private boolean isKhaiBossForm;
    private boolean isVendor;
    private boolean isSecurityGuard;
    private double skillCooldown;
    private boolean castingSkill;
    private double castingTimer;
    private String currentSkillType;
    private int ultimatePhase = 1;
    private int khaiSkillIndex = 0;
    private double khaiAnimationTimer = 0;
    private double vendorSummonCooldown = 0;
    private double securityGuardDashCooldown = 0;
    private boolean isDashing = false;
    private double dashDuration = 0;
    private double dashSpeed = 0;

    public EnemyActor(String name, double x, double y, double width, double height,
                      int hp, int maxHp, double speed, Color color, boolean boss) {
        super(x, y, width, height);
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.speed = speed;
        this.color = color;
        this.boss = boss;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public double getSpeed() {
        return speed;
    }

    public Color getColor() {
        return color;
    }

    public boolean isBoss() {
        return boss;
    }

    public double getAttackCooldown() {
        return attackCooldown;
    }

    public void setAttackCooldown(double attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public double getSlowTimer() {
        return slowTimer;
    }

    public EnemyTuningState getTuningState() {
        return tuningState;
    }

    public void takeDamage(int damage) {
        hp -= damage;
    }

    public void setSpriteSheet(SpriteSheet spriteSheet, AnimationStrip idleStrip,
                               AnimationStrip walkStrip, AnimationStrip attackStrip) {
        this.spriteSheet = spriteSheet;
        this.idleStrip = idleStrip;
        this.walkStrip = walkStrip;
        this.attackStrip = attackStrip;
    }
    
    public void setCastingStrip(AnimationStrip castingStrip) {
        this.castingStrip = castingStrip;
    }
    
    public void setDashStrip(AnimationStrip dashStrip) {
        this.dashStrip = dashStrip;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public void setFacing(int facing) {
        this.facing = facing < 0 ? -1 : 1;
    }

    public void setAttacking(boolean attacking) {
        if (attacking && boss) {
            bossMeleeAnimHoldSec = 0.12;
        }
        this.attacking = attacking;
    }

    /** Holds {@link #attackStrip} visible for bosses (e.g. Khai boss skill windup) independent of melee flicker. */
    public void setBossAttackVisualHold(double seconds) {
        bossMeleeAnimHoldSec = Math.max(bossMeleeAnimHoldSec, seconds);
    }
    
    public void markAsCaesarHunos() {
        this.isCaesarHunos = true;
        this.skillCooldown = 2.0;
    }
    
    public void markAsSirKhai() {
        this.isSirKhai = true;
    }
    
    public void markAsSecurityGuard() {
        this.isSecurityGuard = true;
        this.securityGuardDashCooldown = 3.0;
    }
    
    public void markAsVendor() {
        this.isVendor = true;
        this.vendorSummonCooldown = 0.0; // Summon immediately
    }
    
    public void markAsKhaiBossForm() {
        this.isKhaiBossForm = true;
        this.skillCooldown = 1.5;
    }
    
    public boolean isCaesarHunos() {
        return isCaesarHunos;
    }
    
    public boolean isSirKhai() {
        return isSirKhai;
    }
    
    public boolean isKhaiBossForm() {
        return isKhaiBossForm;
    }
    
    public boolean isVendor() {
        return isVendor;
    }
    
    public boolean isSecurityGuard() {
        return isSecurityGuard;
    }
    
    public double getSecurityGuardDashCooldown() {
        return securityGuardDashCooldown;
    }
    
    public void setSecurityGuardDashCooldown(double cooldown) {
        this.securityGuardDashCooldown = cooldown;
    }
    
    public boolean isDashing() {
        return isDashing;
    }
    
    public void startDash(double speed, double duration) {
        this.isDashing = true;
        this.dashSpeed = speed;
        this.dashDuration = duration;
    }
    
    public void updateDash(double dt) {
        if (isDashing) {
            dashDuration -= dt;
            if (dashDuration <= 0) {
                isDashing = false;
                dashSpeed = 0;
            }
        }
    }
    
    public double getDashSpeed() {
        return dashSpeed;
    }
    
    public double getVendorSummonCooldown() {
        return vendorSummonCooldown;
    }
    
    public void setVendorSummonCooldown(double cooldown) {
        this.vendorSummonCooldown = cooldown;
    }
    
    public int getKhaiSkillIndex() {
        return khaiSkillIndex;
    }
    
    public void advanceKhaiSkill() {
        khaiSkillIndex = (khaiSkillIndex + 1) % 3;
    }
    
    public double getKhaiAnimationTimer() {
        return khaiAnimationTimer;
    }
    
    public void setKhaiAnimationTimer(double timer) {
        khaiAnimationTimer = timer;
    }
    
    public void updateKhaiAnimation(double dt) {
        if (khaiAnimationTimer > 0) {
            khaiAnimationTimer -= dt;
        }
    }
    
    public double getSkillCooldown() {
        return skillCooldown;
    }
    
    public void setSkillCooldown(double cooldown) {
        this.skillCooldown = cooldown;
    }
    
    public boolean isCastingSkill() {
        return castingSkill;
    }
    
    public void startCasting(String skillType) {
        this.castingSkill = true;
        this.castingTimer = 1.2;
        this.currentSkillType = skillType;
    }
    
    public void updateCasting(double dt) {
        if (castingSkill && castingTimer > 0) {
            castingTimer -= dt;
        }
    }
    
    public String getCurrentSkillType() {
        return currentSkillType;
    }
    
    public boolean isReadyToCast() {
        return castingSkill && castingTimer <= 0;
    }
    
    public void finishCasting() {
        castingSkill = false;
        castingTimer = 0;
    }
    
    public int getUltimatePhase() {
        return ultimatePhase;
    }
    
    public void toggleUltimatePhase() {
        ultimatePhase = ultimatePhase == 1 ? 2 : 1;
    }

    public void stepVertical(double dt) {
        moveBy(0, vy * dt);
    }

    public void landOn(double surfaceY) {
        setY(surfaceY - getHeight());
        vy = 0;
        onGround = true;
    }

    public boolean canJump() {
        return onGround && jumpCooldown <= 0;
    }

    public void jump(double jumpVelocity) {
        vy = jumpVelocity;
        onGround = false;
        jumpCooldown = 0.9;
    }

    public void applySlow(double baseDuration, SlowConfig config) {
        if (slowTimer <= 0) {
            slowStack = 0;
        }
        slowStack++;
        double d = baseDuration;
        if (slowStack > config.diminishAfterStacks()) {
            d *= config.diminishFactor();
        }
        slowTimer = Math.max(slowTimer, d);
    }

    public void applyBleed(int totalDamage, BleedConfig config) {
        bleedTicks += config.tickCount();
        int perTick = Math.max(1, totalDamage / config.tickCount());
        perTick = Math.min(config.tickDamageCap(), perTick);
        bleedTickDamage = Math.max(bleedTickDamage, perTick);
        bleedTickIntervalSec = config.tickIntervalSec();
        if (bleedTickTimer <= 0) {
            bleedTickTimer = bleedTickIntervalSec;
        }
    }

    public void applyHitEffects(HitPayload payload, BleedConfig bleedCfg, SlowConfig slowCfg) {
        takeDamage(payload.getDirectDamage());
        for (StatusApplication s : payload.getStatuses()) {
            switch (s.type()) {
                case BLEED -> {
                    applyBleed((int) Math.round(s.magnitude()), bleedCfg);
                    tuningState.onBleedApplied();
                }
                case SLOW -> {
                    applySlow(s.magnitude(), slowCfg);
                    tuningState.onSlowApplied();
                }
            }
        }
    }

    public boolean isDefeated() {
        return hp <= 0;
    }

    public void updateStatusEffects(double dt) {
        animationTime += dt;
        tuningState.update(dt);

        double prevSlow = slowTimer;
        slowTimer = Math.max(0, slowTimer - dt);
        if (prevSlow > 0 && slowTimer == 0) {
            slowStack = 0;
        }

        jumpCooldown = Math.max(0, jumpCooldown - dt);
        bossMeleeAnimHoldSec = Math.max(0, bossMeleeAnimHoldSec - dt);

        if (bleedTicks <= 0) {
            return;
        }

        bleedTickTimer -= dt;
        if (bleedTickTimer <= 0) {
            takeDamage(bleedTickDamage);
            bleedTicks--;
            bleedTickTimer = bleedTickIntervalSec;
            if (bleedTicks <= 0) {
                bleedTickDamage = 0;
            }
        }
    }

    public void chase(PlayerActor player, double dt, double minX, double maxX, double slowMoveFactor) {
        // Caesar Hunos and Khai Boss Form don't chase - they stay in place
        if (isCaesarHunos || isKhaiBossForm) {
            moving = false;
            return;
        }
        
        double cx = player.getCenterX() - getCenterX();
        double direction = Math.signum(cx);
        double tuningMult = tuningState.moveMultiplier();
        double effectiveSpeed = slowTimer > 0 ? speed * slowMoveFactor * tuningMult : speed * tuningMult;
        moving = Math.abs(cx) > 0.5;
        if (boss) {
            // Avoid flip-flopping facing when the player sits near the boss centerline (reduces mirror-smear feel).
            if (cx < -10) {
                facing = -1;
            } else if (cx > 10) {
                facing = 1;
            }
        } else if (direction != 0) {
            facing = direction < 0 ? -1 : 1;
        }
        moveBy(direction * effectiveSpeed * dt, 0);
        setX(Math.max(minX, Math.min(maxX, getX())));
    }

    /** Legacy chase using default slow factor (matches profile enemy slow). */
    public void chase(PlayerActor player, double dt, double minX, double maxX) {
        chase(player, dt, minX, maxX, DEFAULT_SLOW_MOVE_FACTOR);
    }

    @Override
    public void render(GraphicsContext gc) {
        double x = Math.round(getX());
        double y = Math.round(getY());
        double pixel = boss ? 6 : 4;
        
        // Different padding for different boss sizes
        double paddingX = boss ? 6 : 4;
        double paddingY = boss ? 10 : 6;
        if (isCaesarHunos) {
            paddingX = 18;
            paddingY = 30;
        } else if (isKhaiBossForm) {
            paddingX = 16;
            paddingY = 28;
        } else if (isSirKhai) {
            paddingX = 12;
            paddingY = 20;
        }

        if (spriteSheet != null) {
            AnimationStrip strip = resolveStrip();
            int row = strip == null ? 0 : strip.row();
            int column = strip == null ? 0 : strip.frameAt(animationTime);
            double drawW = getWidth() + paddingX * 2;
            double drawH = getHeight() + paddingY * 2;
            double dx = x - paddingX;
            double dy = y - paddingY;
            boolean flip = facing > 0;
            // CaesarHunos_Idle cells are 120×120 but opaque art only reaches ~y=107 — omit dead band so the boss fills the silhouette.
            if (isCaesarHunos && spriteSheet.frameWidth() == 120 && spriteSheet.frameHeight() == 120) {
                spriteSheet.drawFramePartial(gc, row, column, dx, dy, drawW, drawH, flip,
                        0, 0, 120, 107);
            } else if (isKhaiBossForm && spriteSheet.frameWidth() == 128 && spriteSheet.frameHeight() == 160) {
                // Asymmetric art: never mirror (flipX), or limbs smear at the edges.
                // Khai's sheet uses 128×160 cells. Treat the logical box as the floor anchor and draw each frame
                // bottom-centered so transparent padding and attack poses cannot move the hitbox/health bar.
                flip = false;
                final double srcW = 128;
                final double srcH = 160;
                double scale = Math.min(drawW / srcW, drawH / srcH);
                int destW = Math.max(1, (int) Math.round(srcW * scale));
                int destH = Math.max(1, (int) Math.round(srcH * scale));
                double rdx = Math.rint(x + (getWidth() - destW) / 2.0);
                double rdy = Math.rint(y + getHeight() - destH);
                gc.save();
                gc.beginPath();
                gc.rect(rdx, rdy, destW, destH);
                gc.clip();
                spriteSheet.drawFrame(gc, row, column, rdx, rdy, destW, destH, flip);
                gc.restore();
            } else {
                spriteSheet.drawFrame(gc, row, column, dx, dy, drawW, drawH, flip);
            }
        } else {
            gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.18));
            gc.fillRect(x - pixel * 2, y - pixel * 2, getWidth() + pixel * 4, getHeight() + pixel * 4);

            gc.setFill(color);
            gc.fillRect(x + pixel, y, getWidth() - pixel * 2, pixel * 3);
            gc.fillRect(x, y + pixel * 3, getWidth(), getHeight() - pixel * 3);

            gc.setFill(Color.color(0.08, 0.08, 0.10));
            gc.fillRect(x + pixel * 2, y + pixel * 4, pixel * 2, pixel * 2);
            gc.fillRect(x + getWidth() - pixel * 4, y + pixel * 4, pixel * 2, pixel * 2);
        }

        gc.setFill(Color.color(0.1, 0.1, 0.12, 0.95));
        gc.fillRect(x, y - pixel * 2, getWidth(), pixel);
        gc.setFill(Color.color(0.9, 0.16, 0.16, 0.95));
        gc.fillRect(x, y - pixel * 2, getWidth() * (hp / (double) maxHp), pixel);

        if (slowTimer > 0) {
            gc.setFill(Color.color(0.25, 0.7, 1.0, 0.3));
            gc.fillRect(x - pixel, y - pixel, getWidth() + pixel * 2, getHeight() + pixel * 2);
        }

        if (bleedTicks > 0) {
            gc.setFill(Color.color(1.0, 0.12, 0.18, 0.24));
            gc.fillRect(x - pixel * 2, y - pixel * 2, getWidth() + pixel * 4, getHeight() + pixel * 4);
        }

        if (tuningState.isAdrenaline()) {
            gc.setFill(Color.color(1.0, 0.65, 0.2, 0.22));
            gc.fillRect(x - pixel * 2, y - pixel * 3, getWidth() + pixel * 4, pixel * 2);
        }

        if (!boss && (slowTimer > 0 || bleedTicks > 0)) {
            gc.setFont(javafx.scene.text.Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 9));
            gc.setFill(Color.color(0.9, 0.95, 0.9));
            String tag = (slowTimer > 0 ? "S" + slowStack : "") + (bleedTicks > 0 ? " B" + bleedTicks : "");
            gc.fillText(tag.trim(), x + 2, y - pixel * 3);
        }

        if (boss) {
            var font = javafx.scene.text.Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 12);
            gc.setFont(font);
            gc.setFill(Color.WHITE);
            double labelW = name.length() * 7.0;
            gc.fillText(name, x + (getWidth() - labelW) / 2.0, y - 16);
        }
    }

    private AnimationStrip resolveStrip() {
        // Caesar Hunos casting animation
        if (isCaesarHunos && castingSkill && castingStrip != null) {
            return castingStrip;
        }
        
        // Security Guard dashing animation
        if (isSecurityGuard && isDashing && dashStrip != null) {
            return dashStrip;
        }
        
        if (boss && attackStrip != null && bossMeleeAnimHoldSec > 0) {
            return attackStrip;
        }
        if (attacking && attackStrip != null) {
            return attackStrip;
        }
        // Major bosses chase every frame; `moving` can flicker near the player line and swap idle vs walk
        // one frame apart (e.g. Caesar idle = 1 cell vs 6-frame walk) which reads as constant blinking.
        if (boss && walkStrip != null && !isCaesarHunos) {
            return walkStrip;
        }
        if (moving && walkStrip != null) {
            return walkStrip;
        }
        if (idleStrip != null) {
            return idleStrip;
        }
        return walkStrip != null ? walkStrip : attackStrip;
    }
}

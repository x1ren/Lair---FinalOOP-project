package org.example.scenes;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.example.Main;
import org.example.assets.AnimationStrip;
import org.example.assets.AssetRegistry;
import org.example.assets.SpriteSheet;
import org.example.assets.SpriteSet;
import org.example.engines.CollisionManager;
import org.example.engines.GameLoop;
import org.example.engines.InputHandler;
import org.example.gameplay.BleedConfig;
import org.example.gameplay.CombatScaling;
import org.example.gameplay.EnemyActor;
import org.example.gameplay.EntityManager;
import org.example.gameplay.HitPayload;
import org.example.gameplay.PlatformTile;
import org.example.gameplay.PlayerActor;
import org.example.gameplay.Projectile;
import org.example.gameplay.SlowConfig;
import org.example.gameplay.StageCatalog;
import org.example.gameplay.StageDefinition;
import org.example.player.CharacterCombatProfile;
import org.example.player.CharacterType;
import org.example.runtime.GameContext;
import org.example.weapons.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScene {

    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;
    private static final double GROUND_Y = 620;
    private static final double GRAVITY = 1500;
    private static final double PIXEL = 4;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final InputHandler input = new InputHandler();
    private final Random random = new Random();
    private final AssetRegistry assets = GameContext.assets();

    private final CharacterType character;
    private final CharacterCombatProfile combatProfile;
    private final Weapon weapon;
    private final PlayerActor player;
    private final EntityManager<EnemyActor> enemies = new EntityManager<>();
    private final EntityManager<Projectile> projectiles = new EntityManager<>();
    private final List<StageDefinition> stages = StageCatalog.buildStoryStages();
    private final StageArena arena = new StageArena(W, GROUND_Y);
    private final GameVisualRenderer visualRenderer = new GameVisualRenderer(gc, assets, W, H, GROUND_Y);
    private final GameHudRenderer hudRenderer = new GameHudRenderer(gc, W, H, PIXEL);

    private final GameLoop loop;

    private int stageIndex;
    private int ammo;
    private int hp;
    private final int maxHp;

    private double shootCooldown;
    private double reloadTimer;
    private double abilityCooldown;
    private boolean reloading;
    private double stageIntroTimer = 4.0;
    private double statusTimer;
    private String statusText = "";

    private double hemorrhageTimer;
    private double suppressTimer;
    private double overdriveTimer;
    private double focusTimer;
    private double invulnerableTimer;
    private int focusShots;
    private int overloadShots;
    private boolean stageBossSpawned;
    private double footstepTimer;
    private boolean deathSoundPlayed;

    /** Seconds, used for HUD skill strip animation. */
    private double hudAnimTime;

    private double muzzleFlashTimer;
    private double muzzleFlashX;
    private double muzzleFlashY;
    private double muzzleFlashAngle;

    private boolean finished;
    private boolean victory;

    public GameScene(CharacterType character) {
        this.character = character;
        this.combatProfile = character.getCombatProfile();
        this.weapon = character.createWeapon();
        this.player = new PlayerActor(120, GROUND_Y - 58, 42, 58);
        this.player.setAuraColor(weapon.getProjectileColor());
        this.player.setSpriteSet(loadPlayerSprites(character));
        this.maxHp = combatProfile.profileBaseMaxHp();
        this.hp = maxHp;
        this.ammo = weapon.getMagazineSize();

        Pane root = new Pane(canvas);
        this.scene = new Scene(root, W, H);
        input.attachTo(scene);

        startStage(0);

        this.loop = new GameLoop() {
            @Override
            protected void update(double deltaTime) {
                updateGame(deltaTime);
            }

            @Override
            protected void render() {
                renderGame();
            }
        };
        loop.start();
    }

    private SpriteSet loadPlayerSprites(CharacterType character) {
        var sheet = assets.sheet(character.getSpriteAssetId(), 32, 32);
        return sheet == null ? null : SpriteSet.player(sheet);
    }

    private void updateGame(double dt) {
        player.updateAnimation(dt);
        shootCooldown = Math.max(0, shootCooldown - dt);
        reloadTimer = Math.max(0, reloadTimer - dt);
        abilityCooldown = Math.max(0, abilityCooldown - dt);
        stageIntroTimer = Math.max(0, stageIntroTimer - dt);
        statusTimer = Math.max(0, statusTimer - dt);
        hemorrhageTimer = Math.max(0, hemorrhageTimer - dt);
        suppressTimer = Math.max(0, suppressTimer - dt);
        overdriveTimer = Math.max(0, overdriveTimer - dt);
        focusTimer = Math.max(0, focusTimer - dt);
        invulnerableTimer = Math.max(0, invulnerableTimer - dt);
        muzzleFlashTimer = Math.max(0, muzzleFlashTimer - dt);

        if (focusTimer == 0) {
            focusShots = 0;
        }

        if (input.isJustPressed(KeyCode.ESCAPE)) {
            exitToCharacterSelect();
            return;
        }

        hudAnimTime += dt;

        if (finished) {
            if (input.isJustPressed(KeyCode.ENTER) || input.isJustPressed(KeyCode.SPACE)) {
                exitToCharacterSelect();
                return;
            }
            input.endFrame();
            return;
        }

        StageDefinition stage = stages.get(stageIndex);

        if (reloading && reloadTimer == 0) {
            ammo = weapon.getMagazineSize();
            reloading = false;
            setStatus("Reload complete.");
        }

        handlePlayerInput();
        updatePlayerPhysics(dt);
        updateProjectiles(dt);
        updateEnemies(dt);
        updateStageExit();
        updateCamera();

        if (hp <= 0) {
            finished = true;
            victory = false;
            player.markDefeated();
            if (!deathSoundPlayed) {
                GameContext.audio().playDeath();
                deathSoundPlayed = true;
            }
        }

        if (!finished && enemies.isEmpty() && !arena.exitMarker().isActive()) {
            handleStageCleared(stage);
        }

        input.endFrame();
    }

    private void handlePlayerInput() {
        double moveSpeed = character.getMovementSpeedPx();
        if (overdriveTimer > 0 && character == CharacterType.ILDE_JAN_FIGUERAS) {
            moveSpeed *= combatProfile.overdriveMoveMultiplier();
        }

        player.setVx(0);
        if (input.isDown(KeyCode.A) || input.isDown(KeyCode.LEFT)) {
            player.setVx(-moveSpeed);
            player.setFacing(-1);
        }
        if (input.isDown(KeyCode.D) || input.isDown(KeyCode.RIGHT)) {
            player.setVx(moveSpeed);
            player.setFacing(1);
        }

        if (input.isJustPressed(KeyCode.SPACE) && player.isOnGround()) {
            player.setVy(CharacterType.BASE_JUMP);
            player.setOnGround(false);
            GameContext.audio().playJump(character);
        }

        if (input.isJustPressed(KeyCode.R) && ammo < weapon.getMagazineSize() && reloadTimer <= 0) {
            startReload();
        }

        if (input.isJustPressed(KeyCode.Q)) {
            activateAbility();
        }

        if (input.isMouseLeftDown() && shootCooldown <= 0 && reloadTimer <= 0) {
            fireWeapon();
        }
    }

    private void updatePlayerPhysics(double dt) {
        boolean wasOnGround = player.isOnGround();
        double previousBottom = player.getY() + player.getHeight();

        player.setVy(player.getVy() + GRAVITY * dt);
        player.step(dt);
        player.setOnGround(false);

        // First check ground collision
        if (player.getY() + player.getHeight() >= GROUND_Y) {
            player.landOn(GROUND_Y);
        }

        // Then check platform collisions
        if (!player.isOnGround()) {
            for (PlatformTile platform : arena.platforms()) {
                if (player.getVy() >= 0 && CollisionManager.landsOnTop(player, platform, previousBottom)) {
                    player.landOn(platform.getY());
                    break;
                }
            }
        }

        // Additional check: if player is very close to a platform surface, snap them to it
        // This handles the case where player walks along a platform
        if (!player.isOnGround() && player.getVy() >= 0) {
            for (PlatformTile platform : arena.platforms()) {
                double currentBottom = player.getY() + player.getHeight();
                boolean horizontalOverlap = player.getX() + player.getWidth() > platform.getX()
                        && player.getX() < platform.getX() + platform.getWidth();
                // If player is within a small range of the platform top, snap them to it
                if (horizontalOverlap && currentBottom >= platform.getY() && currentBottom <= platform.getY() + 10) {
                    player.landOn(platform.getY());
                    break;
                }
            }
        }

        if (!wasOnGround && player.isOnGround()) {
            GameContext.audio().playLand();
        }

        if (player.isOnGround() && Math.abs(player.getVx()) > 10) {
            footstepTimer -= dt;
            if (footstepTimer <= 0) {
                GameContext.audio().playFootstep();
                footstepTimer = 0.28;
            }
        } else {
            footstepTimer = 0;
        }

        arena.clampPlayer(player);
    }

    private void updateProjectiles(double dt) {
        for (var iterator = projectiles.iterator(); iterator.hasNext();) {
            Projectile projectile = iterator.next();
            projectile.update(dt);

            boolean remove = projectile.isExpired(arena.worldWidth(), H);
            if (!remove) {
                for (EnemyActor enemy : enemies) {
                    if (CollisionManager.circleHitsRect(projectile, enemy)) {
                        HitPayload payload = projectile.getHitPayload();
                        int direct = payload.getDirectDamage();
                        if (character == CharacterType.IBEN_ANOOS && suppressTimer > 0 && enemy.getSlowTimer() > 0) {
                            direct = (int) Math.round(direct * combatProfile.suppressDamageVsSlowedMultiplier());
                        }
                        HitPayload adjusted = new HitPayload(direct, payload.getStatuses());
                        enemy.applyHitEffects(adjusted, projectile.getBleedConfig(), projectile.getSlowConfig());
                        remove = true;
                        break;
                    }
                }
            }

            if (remove) {
                iterator.remove();
            }
        }

        enemies.removeIf(EnemyActor::isDefeated);
    }

    private void updateEnemies(double dt) {
        for (EnemyActor enemy : enemies) {
            enemy.setAttacking(false);
            enemy.updateStatusEffects(dt);
            enemy.setAttackCooldown(Math.max(0, enemy.getAttackCooldown() - dt));
            updateEnemyPhysics(enemy, dt);
            enemy.chase(player, dt, 20, arena.worldWidth() - enemy.getWidth() - 20,
                    combatProfile.enemySlowMoveFactor());

            if (enemy.getAttackCooldown() <= 0 && CollisionManager.intersects(enemy, player)) {
                applyDamage(enemy.isBoss() ? 22 : 12);
                double baseCd = enemy.isBoss() ? 0.8 : 1.1;
                enemy.setAttackCooldown(baseCd * enemy.getTuningState().attackCooldownMultiplier());
                enemy.setAttacking(true);
            }
        }
    }

    private void updateEnemyPhysics(EnemyActor enemy, double dt) {
        double previousBottom = enemy.getY() + enemy.getHeight();
        enemy.setVy(enemy.getVy() + GRAVITY * dt);

        if (shouldEnemyJump(enemy)) {
            enemy.jump(CharacterType.BASE_JUMP * 0.88);
        }

        enemy.stepVertical(dt);
        enemy.setOnGround(false);

        // Check ground collision
        if (enemy.getY() + enemy.getHeight() >= GROUND_Y) {
            enemy.landOn(GROUND_Y);
        }

        // Check platform collisions
        if (!enemy.isOnGround()) {
            for (PlatformTile platform : arena.platforms()) {
                if (enemy.getVy() >= 0 && CollisionManager.landsOnTop(enemy, platform, previousBottom)) {
                    enemy.landOn(platform.getY());
                    break;
                }
            }
        }

        // Additional check: if enemy is very close to a platform surface, snap them to it
        if (!enemy.isOnGround() && enemy.getVy() >= 0) {
            for (PlatformTile platform : arena.platforms()) {
                double currentBottom = enemy.getY() + enemy.getHeight();
                boolean horizontalOverlap = enemy.getX() + enemy.getWidth() > platform.getX()
                        && enemy.getX() < platform.getX() + platform.getWidth();
                if (horizontalOverlap && currentBottom >= platform.getY() && currentBottom <= platform.getY() + 10) {
                    enemy.landOn(platform.getY());
                    break;
                }
            }
        }
    }

    private boolean shouldEnemyJump(EnemyActor enemy) {
        if (!enemy.canJump()) {
            return false;
        }

        double dx = player.getCenterX() - enemy.getCenterX();
        double dy = enemy.getCenterY() - player.getCenterY();

        if (dy > 46 && Math.abs(dx) < 260) {
            return true;
        }

        for (PlatformTile platform : arena.platforms()) {
            boolean ahead = dx > 0
                    ? platform.getX() > enemy.getX() && platform.getX() - enemy.getX() < 120
                    : enemy.getX() > platform.getX() && enemy.getX() - platform.getX() < 120;
            boolean reachableHeight = platform.getY() < enemy.getY() && enemy.getY() - platform.getY() < 150;

            if (ahead && reachableHeight && player.getY() + player.getHeight() <= platform.getY() + 16) {
                return true;
            }
        }

        return false;
    }

    private void fireWeapon() {
        if (ammo <= 0) {
            startReload();
            return;
        }

        ammo--;
        shootCooldown = weapon.getFireRate();
        if (overdriveTimer > 0 && character == CharacterType.ILDE_JAN_FIGUERAS) {
            shootCooldown *= combatProfile.overdriveFireRateCooldownMultiplier();
        }

        double originX = player.getCenterX();
        double originY = player.getY() + player.getHeight() * 0.35;
        double targetX = input.getMouseX() + arena.cameraX();
        double targetY = input.getMouseY();
        double angle = Math.atan2(targetY - originY, targetX - originX);

        muzzleFlashTimer = 0.08;
        muzzleFlashX = originX;
        muzzleFlashY = originY;
        muzzleFlashAngle = angle;

        int projectileCount = weapon.getPelletsPerShot();
        if (overloadShots > 0 && character == CharacterType.GAILE_AMOLONG) {
            projectileCount *= combatProfile.overloadPelletMultiplier();
        }
        int totalDamage = getCurrentShotDamage();
        int baseDamage = Math.max(1, totalDamage / projectileCount);
        int remainder = Math.max(0, totalDamage % projectileCount);

        int bleedPerProjectile = 0;
        if (hemorrhageTimer > 0 && character == CharacterType.JOSEPH_JIMENEZ) {
            bleedPerProjectile = (int) Math.round(totalDamage * combatProfile.hemorrhageBleedPercentOfShotDamage());
        }
        double slowDuration = suppressTimer > 0 && character == CharacterType.IBEN_ANOOS
                ? combatProfile.suppressSlowDurationSec()
                : 0;

        BleedConfig bleedConfig = new BleedConfig(
                combatProfile.hemorrhageBleedTickCount(),
                combatProfile.hemorrhageBleedTickIntervalSec(),
                combatProfile.hemorrhageBleedTickDamageCap());
        SlowConfig slowConfig = character == CharacterType.IBEN_ANOOS
                ? new SlowConfig(combatProfile.suppressSlowDiminishAfterStacks(), combatProfile.suppressSlowDiminishFactor())
                : SlowConfig.none();

        for (int i = 0; i < projectileCount; i++) {
            double spread = (random.nextDouble() - 0.5) * weapon.getSpread();
            double shotAngle = angle + spread;
            int damage = baseDamage + (i < remainder ? 1 : 0);

            HitPayload payload = HitPayload.withBleedSlow(damage, bleedPerProjectile, slowDuration);
            projectiles.add(new Projectile(
                    originX,
                    originY,
                    Math.cos(shotAngle) * weapon.getProjectileSpeed(),
                    Math.sin(shotAngle) * weapon.getProjectileSpeed(),
                    payload,
                    weapon.getProjectileColor(),
                    bleedConfig,
                    slowConfig
            ));
        }

        if (overloadShots > 0 && character == CharacterType.GAILE_AMOLONG) {
            overloadShots--;
        }
        if (focusShots > 0 && character == CharacterType.JAMUEL_BACUS) {
            focusShots--;
        }
    }

    private void activateAbility() {
        if (abilityCooldown > 0) {
            setStatus("Skill cooling down.");
            return;
        }

        switch (character) {
            case JOSEPH_JIMENEZ -> {
                hemorrhageTimer = combatProfile.hemorrhageDurationSec();
                abilityCooldown = character.getSkillCooldown();
                setStatus("Hemorrhage active. Hits inflict bleed.");
            }
            case IBEN_ANOOS -> {
                suppressTimer = combatProfile.suppressDurationSec();
                abilityCooldown = character.getSkillCooldown();
                setStatus("Suppress active. Hits slow enemies.");
            }
            case ILDE_JAN_FIGUERAS -> {
                overdriveTimer = combatProfile.overdriveDurationSec();
                abilityCooldown = character.getSkillCooldown();
                setStatus("Overdrive increased speed and fire rate.");
            }
            case GAILE_AMOLONG -> {
                overloadShots = Math.max(overloadShots, combatProfile.overloadChargeBudget());
                abilityCooldown = character.getSkillCooldown();
                setStatus("Overload primed the next 2 blasts.");
            }
            case JAMUEL_BACUS -> {
                focusTimer = combatProfile.focusDurationSec();
                focusShots = combatProfile.focusShotBudget();
                abilityCooldown = character.getSkillCooldown();
                setStatus("Focus primed the next sniper shot.");
            }
        }
    }

    private int getCurrentShotDamage() {
        double damage = character.getDamage();
        if (focusShots > 0 && focusTimer > 0 && character == CharacterType.JAMUEL_BACUS) {
            double t = focusTimer / combatProfile.focusDurationSec();
            double focusMult = combatProfile.focusDamageMultiplierEarly()
                    + (combatProfile.focusDamageMultiplierLate() - combatProfile.focusDamageMultiplierEarly()) * (1.0 - t);
            damage *= focusMult;
        }
        if (overdriveTimer > 0 && character == CharacterType.ILDE_JAN_FIGUERAS) {
            damage *= combatProfile.overdriveBonusDamageMultiplier();
        }
        damage *= CombatScaling.playerDamageStageMultiplier(stageIndex);
        return (int) Math.round(damage);
    }

    private void applyDamage(int damage) {
        if (invulnerableTimer > 0) {
            return;
        }
        if (character == CharacterType.GAILE_AMOLONG && overloadShots > 0) {
            damage = (int) Math.round(damage * combatProfile.overloadDamageTakenMultiplier());
        }
        hp -= damage;
        invulnerableTimer = 0.25;
        player.triggerHit();
    }

    private void startReload() {
        if (reloadTimer <= 0) {
            reloadTimer = weapon.getReloadTime();
            reloading = true;
            setStatus("Reloading " + weapon.getName() + "...");
        }
    }

    private void startStage(int newIndex) {
        stageIndex = newIndex;
        projectiles.clear();
        enemies.clear();
        stageIntroTimer = 4.5;
        stageBossSpawned = false;
        arena.exitMarker().setActive(false);
        arena.exitMarker().setLabel(newIndex == stages.size() - 1 ? "FINAL" : "NEXT");

        StageDefinition stage = stages.get(stageIndex);
        arena.prepareStage(stage, stageIndex, player, loadBackdrop(stage));

        if (stage.hasMobs()) {
            spawnMobWave(stage);
        } else if (stage.hasBoss()) {
            spawnBoss(stage);
        }

        GameContext.audio().setStoryBackgroundMusic(stageIndex == stages.size() - 1);
    }

    private void spawnMobWave(StageDefinition stage) {
        for (int i = 0; i < stage.enemyCount(); i++) {
            double x = arena.mobSpawnX(i, stage.enemyCount());
            // Spawn enemies high above so they fall and land on platforms
            EnemyActor enemy = new EnemyActor(stage.enemyName(), x, 50, 42, 54,
                    stage.enemyHealth(), stage.enemyHealth(), stage.enemySpeed(), stage.tint(), false);
            if (!stage.enemySpriteIds().isEmpty()) {
                applyEnemySprite(enemy, stage.enemySpriteIds().get(i % stage.enemySpriteIds().size()));
            }
            enemies.add(enemy);
        }
    }

    private void spawnBoss(StageDefinition stage) {
        stageBossSpawned = true;
        double x = arena.bossSpawnX(stageIndex == stages.size() - 1);
        // Spawn boss high above so they fall and land on platforms
        EnemyActor enemy = new EnemyActor(stage.bossName(), x, 50, 74, 96,
                stage.bossHealth(), stage.bossHealth(), stage.bossSpeed(), stage.tint(), true);
        if (stage.bossSpriteId() != null) {
            applyEnemySprite(enemy, stage.bossSpriteId());
        }
        enemies.add(enemy);
    }

    private Image loadBackdrop(StageDefinition stage) {
        return assets.image(stage.backdropAssetId());
    }

    private void applyEnemySprite(EnemyActor enemy, String spriteId) {
        SpriteSheet sheet;
        AnimationStrip idle;
        AnimationStrip walk;
        AnimationStrip attack;

        switch (spriteId) {
            case "enemy.librarian", "enemy.janitor" -> {
                sheet = assets.sheet(spriteId, 32, 32);
                idle = new AnimationStrip(0, 0, 12, 5);
                walk = new AnimationStrip(1, 0, 6, 8);
                attack = null;
            }
            case "enemy.student_f", "enemy.student_m" -> {
                sheet = assets.sheet(spriteId, 32, 32);
                idle = new AnimationStrip(0, 0, 8, 5);
                walk = new AnimationStrip(1, 0, 4, 8);
                attack = null;
            }
            case "enemy.security_guard" -> {
                sheet = assets.sheet(spriteId, 96, 128);
                idle = new AnimationStrip(0, 0, 8, 5);
                walk = new AnimationStrip(0, 0, 8, 7);
                attack = new AnimationStrip(1, 0, 18, 10);
            }
            case "enemy.vendor" -> {
                sheet = assets.sheet(spriteId, 64, 64);
                idle = new AnimationStrip(0, 0, 12, 5);
                walk = new AnimationStrip(1, 0, 8, 8);
                attack = new AnimationStrip(1, 0, 8, 8);
            }
            case "enemy.caesar_hunos" -> {
                sheet = assets.sheet(spriteId, 120, 120);
                idle = new AnimationStrip(0, 0, 25, 5);
                walk = new AnimationStrip(0, 0, 25, 10);
                attack = new AnimationStrip(0, 0, 25, 14);
            }
            case "enemy.khai_mimic" -> {
                // 2560×640 sheet: 20×128px columns × 4 rows of 160px (12 / 14 / 15 / 17 frames per row).
                sheet = assets.sheet(spriteId, 128, 160);
                idle = new AnimationStrip(0, 0, 12, 5);
                walk = new AnimationStrip(1, 0, 14, 8);
                attack = new AnimationStrip(3, 0, 17, 10);
            }
            default -> {
                sheet = assets.sheet(spriteId, 32, 32);
                idle = new AnimationStrip(0, 0, 1, 1);
                walk = idle;
                attack = idle;
            }
        }

        enemy.setSpriteSheet(sheet, idle, walk, attack);
    }

    private void handleStageCleared(StageDefinition stage) {
        if (stage.hasBoss() && !stageBossSpawned) {
            spawnBoss(stage);
            stageIntroTimer = 2.8;
            setStatus(stage.bossName() + " has appeared.");
            return;
        }

        if (stageIndex == stages.size() - 1) {
            finished = true;
            victory = true;
            setStatus(getStageClearMessage(stage));
            return;
        }

        arena.exitMarker().setActive(true);
        arena.exitMarker().setLabel("NEXT");
        setStatus(getStageClearMessage(stage));
    }

    private String getStageClearMessage(StageDefinition stage) {
        if (stageIndex == stages.size() - 1) {
            return "The mimic collapses.";
        }
        if ("Caesar Hunos".equals(stage.bossName())) {
            return "Caesar dropped the stabilized LAIR vial.";
        }
        return stage.name() + " cleared.";
    }

    private void updateStageExit() {
        if (!arena.exitMarker().isActive()) {
            return;
        }

        if (CollisionManager.intersects(player, arena.exitMarker())) {
            startStage(stageIndex + 1);
        }
    }

    private void updateCamera() {
        arena.updateCamera(player);
    }

    private void renderGame() {
        StageDefinition stage = stages.get(stageIndex);

        visualRenderer.renderBackground(stage, loadBackdrop(stage), arena.cameraX(), arena.worldWidth());
        gc.save();
        gc.translate(-arena.cameraX(), 0);
        
        // Render platform bases (shadows and legs) first
        for (PlatformTile platform : arena.platforms()) {
            platform.renderBase(gc);
        }
        
        // Render characters and objects on top of platform bases
        arena.exitMarker().render(gc);
        enemies.renderAll(gc);
        player.render(gc);
        visualRenderer.renderPlayerWeapon(player, weapon, finished, victory, getAimAngle(), muzzleFlashTimer);
        visualRenderer.renderMuzzleFlash(muzzleFlashTimer, muzzleFlashX, muzzleFlashY, muzzleFlashAngle);
        projectiles.renderAll(gc);
        
        gc.restore();
        hudRenderer.renderHud(stage, character, weapon, hp, maxHp, ammo,
                character.getSkillName(), character.getSkillEffectSummary(), "[Q]",
                getAbilityMeterFill(), getAbilityStatusText(), getReloadStatusText(),
                getActiveEffectLines(),
                CombatScaling.playerDamageStageMultiplier(stageIndex),
                arena.exitMarker().isActive(),
                hudAnimTime);

        if (stageIntroTimer > 0) {
            hudRenderer.renderStageIntro(stage);
        }

        if (statusTimer > 0) {
            hudRenderer.renderStatusBanner(statusText);
        }

        if (finished) {
            hudRenderer.renderEndOverlay(victory);
        }
    }

    private double getAimAngle() {
        double originX = player.getCenterX();
        double originY = player.getY() + player.getHeight() * 0.35;
        double targetX = input.getMouseX() + arena.cameraX();
        double targetY = input.getMouseY();

        if (targetX == 0 && targetY == 0) {
            return player.getFacing() >= 0 ? 0 : Math.PI;
        }

        return Math.atan2(targetY - originY, targetX - originX);
    }

    private double getAbilityMeterFill() {
        if (abilityCooldown <= 0) {
            return 1.0;
        }
        return 1.0 - (abilityCooldown / character.getSkillCooldown());
    }

    private String getAbilityStatusText() {
        if (abilityCooldown <= 0) {
            return "READY";
        }
        return String.format("%.1fs", abilityCooldown);
    }

    private String getReloadStatusText() {
        return reloadTimer > 0 ? String.format("%.1fs", reloadTimer) : "READY";
    }

    private List<String> getActiveEffectLines() {
        List<String> lines = new ArrayList<>();
        if (hemorrhageTimer > 0) {
            lines.add(String.format("Hemorrhage  %.1fs  — hits apply bleed (DoT)", hemorrhageTimer));
        }
        if (suppressTimer > 0) {
            lines.add(String.format("Suppress  %.1fs  — hits slow enemies", suppressTimer));
        }
        if (overdriveTimer > 0) {
            lines.add(String.format("Overdrive  %.1fs  — move & fire rate up, bonus damage", overdriveTimer));
        }
        if (overloadShots > 0) {
            lines.add(String.format("Overload  x%d charges  — next blasts: extra pellets, damage resist", overloadShots));
        }
        if (focusShots > 0) {
            lines.add(String.format("Focus  %.1fs window / %d shot(s)  — sniper damage buff (wait longer = stronger)", focusTimer, focusShots));
        }
        return lines;
    }

    private void setStatus(String message) {
        statusText = message;
        statusTimer = 1.8;
    }

    private void exitToCharacterSelect() {
        GameContext.audio().stopBackgroundMusic();
        loop.stop();
        GameContext.showCharacterSelect();
    }

    public Scene getScene() {
        return scene;
    }
}

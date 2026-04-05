package org.example.scenes;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.Main;
import org.example.assets.AnimationStrip;
import org.example.assets.AssetRegistry;
import org.example.assets.SpriteSheet;
import org.example.assets.SpriteSet;
import org.example.engines.CollisionManager;
import org.example.engines.GameLoop;
import org.example.engines.InputHandler;
import org.example.gameplay.EnemyActor;
import org.example.gameplay.EntityManager;
import org.example.gameplay.PlatformTile;
import org.example.gameplay.PlayerActor;
import org.example.gameplay.Projectile;
import org.example.gameplay.StageCatalog;
import org.example.gameplay.StageDefinition;
import org.example.gameplay.StageExitMarker;
import org.example.player.CharacterType;
import org.example.runtime.GameContext;
import org.example.weapons.Weapon;
import org.example.weapons.WeaponType;

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
    private final Weapon weapon;
    private final PlayerActor player;
    private final EntityManager<EnemyActor> enemies = new EntityManager<>();
    private final EntityManager<Projectile> projectiles = new EntityManager<>();
    private final StageExitMarker stageExit = new StageExitMarker(W - 92, GROUND_Y - 84, 44, 64);
    private final List<PlatformTile> platforms = new ArrayList<>();
    private final List<StageDefinition> stages = StageCatalog.buildStoryStages();

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

    private double muzzleFlashTimer;
    private double muzzleFlashX;
    private double muzzleFlashY;
    private double muzzleFlashAngle;
    private double cameraX;
    private double worldWidth;

    private boolean finished;
    private boolean victory;

    public GameScene(CharacterType character) {
        this.character = character;
        this.weapon = character.createWeapon();
        this.player = new PlayerActor(120, GROUND_Y - 58, 42, 58);
        this.player.setAuraColor(weapon.getProjectileColor());
        this.player.setSpriteSet(loadPlayerSprites(character));
        this.maxHp = character.getHealth();
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

        if (!finished && enemies.isEmpty() && !stageExit.isActive()) {
            handleStageCleared(stage);
        }

        input.endFrame();
    }

    private void handlePlayerInput() {
        double moveSpeed = character.getMovementSpeedPx();
        if (overdriveTimer > 0 && character == CharacterType.ILDE_JAN_FIGUERAS) {
            moveSpeed *= 1.35;
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

        if (player.getY() + player.getHeight() >= GROUND_Y) {
            player.landOn(GROUND_Y);
        }

        if (!player.isOnGround()) {
            for (PlatformTile platform : platforms) {
                if (player.getVy() >= 0 && CollisionManager.landsOnTop(player, platform, previousBottom)) {
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

        player.clampX(0, worldWidth - player.getWidth());
    }

    private void updateProjectiles(double dt) {
        for (var iterator = projectiles.iterator(); iterator.hasNext();) {
            Projectile projectile = iterator.next();
            projectile.update(dt);

            boolean remove = projectile.isExpired(worldWidth, H);
            if (!remove) {
                for (EnemyActor enemy : enemies) {
                    if (CollisionManager.circleHitsRect(projectile, enemy)) {
                        enemy.takeDamage(projectile.getDamage());
                        if (projectile.getBleedDamage() > 0) {
                            enemy.applyBleed(projectile.getBleedDamage());
                        }
                        if (projectile.getSlowDuration() > 0) {
                            enemy.applySlow(projectile.getSlowDuration());
                        }
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
            enemy.chase(player, dt, 20, worldWidth - enemy.getWidth() - 20);

            if (enemy.getAttackCooldown() <= 0 && CollisionManager.intersects(enemy, player)) {
                applyDamage(enemy.isBoss() ? 22 : 12);
                enemy.setAttackCooldown(enemy.isBoss() ? 0.8 : 1.1);
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

        if (enemy.getY() + enemy.getHeight() >= GROUND_Y) {
            enemy.landOn(GROUND_Y);
        }

        if (!enemy.isOnGround()) {
            for (PlatformTile platform : platforms) {
                if (enemy.getVy() >= 0 && CollisionManager.landsOnTop(enemy, platform, previousBottom)) {
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

        for (PlatformTile platform : platforms) {
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
            shootCooldown *= 0.55;
        }

        double originX = player.getCenterX();
        double originY = player.getY() + player.getHeight() * 0.35;
        double targetX = input.getMouseX() + cameraX;
        double targetY = input.getMouseY();
        double angle = Math.atan2(targetY - originY, targetX - originX);

        muzzleFlashTimer = 0.08;
        muzzleFlashX = originX;
        muzzleFlashY = originY;
        muzzleFlashAngle = angle;

        int projectileCount = weapon.getPelletsPerShot();
        if (overloadShots > 0 && character == CharacterType.GAILE_AMOLONG) {
            projectileCount *= 2;
        }
        int totalDamage = getCurrentShotDamage();
        int baseDamage = Math.max(1, totalDamage / projectileCount);
        int remainder = Math.max(0, totalDamage % projectileCount);
        int bleedDamage = hemorrhageTimer > 0 && character == CharacterType.JOSEPH_JIMENEZ ? 24 : 0;
        double slowDuration = suppressTimer > 0 && character == CharacterType.IBEN_ANOOS ? 1.8 : 0;

        for (int i = 0; i < projectileCount; i++) {
            double spread = (random.nextDouble() - 0.5) * weapon.getSpread();
            double shotAngle = angle + spread;
            int damage = baseDamage + (i < remainder ? 1 : 0);

            projectiles.add(new Projectile(
                    originX,
                    originY,
                    Math.cos(shotAngle) * weapon.getProjectileSpeed(),
                    Math.sin(shotAngle) * weapon.getProjectileSpeed(),
                    damage,
                    weapon.getProjectileColor(),
                    bleedDamage,
                    slowDuration
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
                hemorrhageTimer = 5.0;
                abilityCooldown = character.getSkillCooldown();
                setStatus("Hemorrhage active. Hits inflict bleed.");
            }
            case IBEN_ANOOS -> {
                suppressTimer = 5.0;
                abilityCooldown = character.getSkillCooldown();
                setStatus("Suppress active. Hits slow enemies.");
            }
            case ILDE_JAN_FIGUERAS -> {
                overdriveTimer = 5.0;
                abilityCooldown = character.getSkillCooldown();
                setStatus("Overdrive increased speed and fire rate.");
            }
            case GAILE_AMOLONG -> {
                overloadShots = Math.max(overloadShots, 2);
                abilityCooldown = character.getSkillCooldown();
                setStatus("Overload primed the next 2 blasts.");
            }
            case JAMUEL_BACUS -> {
                focusTimer = 5.0;
                focusShots = 1;
                abilityCooldown = character.getSkillCooldown();
                setStatus("Focus primed the next sniper shot.");
            }
        }
    }

    private int getCurrentShotDamage() {
        double damage = character.getDamage();
        if (focusShots > 0 && focusTimer > 0 && character == CharacterType.JAMUEL_BACUS) {
            damage *= 3.0;
        }
        return (int) Math.round(damage);
    }

    private void applyDamage(int damage) {
        if (invulnerableTimer > 0) {
            return;
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
        platforms.clear();
        stageIntroTimer = 4.5;
        stageBossSpawned = false;
        stageExit.setActive(false);
        stageExit.setLabel(newIndex == stages.size() - 1 ? "FINAL" : "NEXT");

        StageDefinition stage = stages.get(stageIndex);
        worldWidth = computeWorldWidth(stage);
        cameraX = 0;
        buildStageLayout(stage);
        player.setX(120);
        player.setY(GROUND_Y - player.getHeight());
        player.setVx(0);
        player.setVy(0);
        player.setOnGround(true);

        if (stage.hasMobs()) {
            spawnMobWave(stage);
        } else if (stage.hasBoss()) {
            spawnBoss(stage);
        }
    }

    private void spawnMobWave(StageDefinition stage) {
        for (int i = 0; i < stage.enemyCount(); i++) {
            double laneStart = Math.max(520, worldWidth * 0.40);
            double laneWidth = Math.max(280, worldWidth * 0.45);
            double spacing = laneWidth / Math.max(1, stage.enemyCount() - 1);
            double x = laneStart + i * spacing;
            EnemyActor enemy = new EnemyActor(stage.enemyName(), x, GROUND_Y - 54, 42, 54,
                    stage.enemyHealth(), stage.enemyHealth(), stage.enemySpeed(), stage.tint(), false);
            if (!stage.enemySpriteIds().isEmpty()) {
                applyEnemySprite(enemy, stage.enemySpriteIds().get(i % stage.enemySpriteIds().size()));
            }
            enemies.add(enemy);
        }
    }

    private void spawnBoss(StageDefinition stage) {
        stageBossSpawned = true;
        double x = worldWidth - (stageIndex == stages.size() - 1 ? 420 : 320);
        EnemyActor enemy = new EnemyActor(stage.bossName(), x, GROUND_Y - 96, 74, 96,
                stage.bossHealth(), stage.bossHealth(), stage.bossSpeed(), stage.tint(), true);
        if (stage.bossSpriteId() != null) {
            applyEnemySprite(enemy, stage.bossSpriteId());
        }
        enemies.add(enemy);
    }

    private javafx.scene.image.Image loadBackdrop(StageDefinition stage) {
        return assets.image(stage.backdropAssetId());
    }

    private double computeWorldWidth(StageDefinition stage) {
        Image backdrop = loadBackdrop(stage);
        double targetH = GROUND_Y - 24 - 64;
        if (backdrop == null || backdrop.getHeight() <= 0) {
            return W * 1.75;
        }
        return Math.max(W, targetH * (backdrop.getWidth() / backdrop.getHeight()));
    }

    private void buildStageLayout(StageDefinition stage) {
        double w = worldWidth;
        double lowY = 520;
        double midY = 456;
        double highY = 392;

        platforms.add(new PlatformTile(140, lowY, 240, 18));
        platforms.add(new PlatformTile(w * 0.28, midY, 210, 18));
        platforms.add(new PlatformTile(w * 0.48, highY, 210, 18));
        platforms.add(new PlatformTile(w * 0.68, midY + 24, 200, 18));

        if (stageIndex == 0) {
            platforms.add(new PlatformTile(w * 0.84, 420, 170, 18));
        } else if (stageIndex == 1) {
            platforms.add(new PlatformTile(w * 0.80, 370, 180, 18));
            platforms.add(new PlatformTile(w * 0.58, 512, 170, 18));
        } else if (stageIndex == 2) {
            platforms.add(new PlatformTile(w * 0.75, 338, 200, 18));
        } else {
            platforms.add(new PlatformTile(w * 0.55, 350, 190, 18));
            platforms.add(new PlatformTile(w * 0.86, 460, 180, 18));
        }

        stageExit.setX(worldWidth - 92);
        stageExit.setY(GROUND_Y - 84);
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

        stageExit.setActive(true);
        stageExit.setLabel("NEXT");
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
        if (!stageExit.isActive()) {
            return;
        }

        if (CollisionManager.intersects(player, stageExit)) {
            startStage(stageIndex + 1);
        }
    }

    private void updateCamera() {
        double target = player.getCenterX() - W / 2.0;
        cameraX += (target - cameraX) * 0.12;
        cameraX = clamp(cameraX, 0, Math.max(0, worldWidth - W));
    }

    private void renderGame() {
        StageDefinition stage = stages.get(stageIndex);

        renderBackground(stage);
        gc.save();
        gc.translate(-cameraX, 0);
        for (PlatformTile platform : platforms) {
            platform.render(gc);
        }
        stageExit.render(gc);
        player.render(gc);
        renderPlayerWeapon();
        renderMuzzleFlash();
        projectiles.renderAll(gc);
        enemies.renderAll(gc);
        gc.restore();
        renderHud(stage);

        if (stageIntroTimer > 0) {
            renderStageIntro(stage);
        }

        if (statusTimer > 0) {
            renderStatusBanner();
        }

        if (finished) {
            renderEndOverlay();
        }
    }

    private void renderBackground(StageDefinition stage) {
        gc.setFill(Color.color(0.01, 0.02, 0.03));
        gc.fillRect(0, 0, W, H);

        Image backdrop = loadBackdrop(stage);
        if (backdrop != null) {
            drawBackdropAsRoom(backdrop);
        } else {
            gc.setFill(Color.color(0.03, 0.08, 0.11));
            gc.fillRect(0, 0, W, H - 96);
        }

        gc.setFill(Color.color(stage.tint().getRed(), stage.tint().getGreen(), stage.tint().getBlue(), 0.08));
        gc.fillRect(0, 72, W, GROUND_Y - 72);

        gc.setFill(Color.color(0, 0, 0, 0.10));
        gc.fillRect(0, 0, W, 68);

        gc.setFill(Color.color(0.01, 0.02, 0.03, 0.18));
        gc.fillRect(0, GROUND_Y - 36, W, 36);

        gc.setFill(Color.color(0.03, 0.05, 0.06, 0.55));
        gc.fillRect(0, GROUND_Y, W, H - GROUND_Y);
    }

    private void drawBackdropAsRoom(Image backdrop) {
        gc.setImageSmoothing(false);

        double targetX = 0;
        double targetY = 64;
        double targetW = W;
        double targetH = GROUND_Y - 24 - targetY;
        double scaledWorldWidth = targetH * (backdrop.getWidth() / backdrop.getHeight());
        double scrollRatio = scaledWorldWidth <= targetW ? 0 : cameraX / (scaledWorldWidth - targetW);
        double sourceW = backdrop.getWidth() * (targetW / scaledWorldWidth);
        double sourceX = scrollRatio * (backdrop.getWidth() - sourceW);
        double sourceY = 0;
        double sourceH = backdrop.getHeight();

        gc.drawImage(backdrop, sourceX, sourceY, sourceW, sourceH, targetX, targetY, targetW, targetH);
        gc.setFill(Color.color(0, 0, 0, 0.08));
        gc.fillRect(targetX, targetY, targetW, targetH);
    }

    private void renderMuzzleFlash() {
        if (muzzleFlashTimer <= 0) {
            return;
        }

        Image flashSheet = assets.image("effect.muzzle_flash");
        if (flashSheet == null) {
            return;
        }

        int frameWidth = 32;
        int frameHeight = 64;
        int columns = (int) flashSheet.getWidth() / frameWidth;
        int column = Math.min(columns - 1, (int) ((0.08 - muzzleFlashTimer) / 0.08 * columns));

        gc.save();
        gc.translate(muzzleFlashX, muzzleFlashY);
        gc.rotate(Math.toDegrees(muzzleFlashAngle));
        gc.setImageSmoothing(false);
        gc.drawImage(flashSheet, column * frameWidth, 0, frameWidth, frameHeight, 0, -14, 48, 36);
        gc.restore();
    }

    private void renderPlayerWeapon() {
        if (finished && !victory) {
            return;
        }

        double aimAngle = getAimAngle();
        boolean aimingRight = Math.cos(aimAngle) >= 0;
        double shoulderX = player.getCenterX() + (aimingRight ? 10 : -10);
        double shoulderY = player.getY() + player.getHeight() * 0.38;

        gc.save();
        gc.translate(shoulderX, shoulderY);
        gc.rotate(Math.toDegrees(aimAngle));

        gc.setFill(Color.color(0, 0, 0, 0.18));
        gc.fillRect(4, -2, 26, 6);

        if (weapon.getType() == WeaponType.SMG) {
            renderSmgWeaponSprite();
        } else {
            renderProceduralWeapon();
        }

        gc.restore();
    }

    private void renderSmgWeaponSprite() {
        Image smgSheet = assets.image("weapon.smg");
        if (smgSheet == null) {
            renderProceduralWeapon();
            return;
        }

        int frameWidth = 32;
        int frameHeight = 32;
        int column = muzzleFlashTimer > 0 ? 1 : 0;
        int row = 1;

        gc.setImageSmoothing(false);
        gc.drawImage(smgSheet, column * frameWidth, row * frameHeight, frameWidth, frameHeight,
                -4, -18, 44, 24);
    }

    private void renderProceduralWeapon() {
        Color body = switch (weapon.getType()) {
            case ASSAULT_RIFLE -> Color.color(0.34, 0.40, 0.46);
            case SHOTGUN -> Color.color(0.56, 0.34, 0.16);
            case SNIPER -> Color.color(0.74, 0.74, 0.78);
            case LMG -> Color.color(0.44, 0.22, 0.22);
            case SMG -> Color.color(0.34, 0.48, 0.26);
        };

        gc.setFill(body);
        switch (weapon.getType()) {
            case ASSAULT_RIFLE -> {
                gc.fillRect(0, -5, 30, 8);
                gc.fillRect(12, -9, 10, 4);
                gc.fillRect(8, 3, 5, 8);
                gc.fillRect(26, -3, 8, 3);
            }
            case SHOTGUN -> {
                gc.fillRect(0, -4, 26, 7);
                gc.fillRect(18, -2, 15, 2);
                gc.fillRect(4, 3, 6, 11);
            }
            case SNIPER -> {
                gc.fillRect(0, -4, 36, 6);
                gc.fillRect(10, -9, 12, 3);
                gc.fillRect(6, 2, 5, 10);
                gc.fillRect(32, -2, 12, 2);
            }
            case LMG -> {
                gc.fillRect(0, -5, 32, 8);
                gc.fillRect(10, -9, 12, 4);
                gc.fillRect(6, 3, 6, 10);
                gc.fillRect(14, 5, 12, 3);
                gc.fillRect(28, -3, 10, 3);
            }
            case SMG -> {
                gc.fillRect(0, -4, 24, 7);
                gc.fillRect(6, 3, 4, 8);
                gc.fillRect(20, -2, 8, 2);
            }
        }

        gc.setFill(Color.color(0.92, 0.96, 1.0, 0.22));
        gc.fillRect(2, -3, 8, 2);
    }

    private double getAimAngle() {
        double originX = player.getCenterX();
        double originY = player.getY() + player.getHeight() * 0.35;
        double targetX = input.getMouseX() + cameraX;
        double targetY = input.getMouseY();

        if (targetX == 0 && targetY == 0) {
            return player.getFacing() >= 0 ? 0 : Math.PI;
        }

        return Math.atan2(targetY - originY, targetX - originX);
    }

    private void renderHud(StageDefinition stage) {
        double panelY = 18;
        double accentR = stage.tint().getRed();
        double accentG = stage.tint().getGreen();
        double accentB = stage.tint().getBlue();

        drawPixelPanel(20, panelY, 320, 104, Color.color(0.01, 0.03, 0.05, 0.84),
                Color.color(0.10, 0.76, 0.42, 0.72));
        drawPixelPanel(W / 2.0 - 190, panelY, 380, 52, Color.color(0.01, 0.03, 0.05, 0.78),
                Color.color(accentR, accentG, accentB, 0.70));
        drawPixelPanel(W - 224, panelY, 204, 104, Color.color(0.01, 0.03, 0.05, 0.84),
                Color.color(0.10, 0.76, 0.42, 0.72));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gc.setFill(Color.WHITE);
        gc.fillText(character.getName(), 36, panelY + 28);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.74, 0.86, 0.80));
        gc.fillText(character.getTitle() + " | " + weapon.getName(), 36, panelY + 48);

        gc.setFill(Color.WHITE);
        gc.fillText("HP " + hp + "/" + maxHp, 36, panelY + 68);
        drawBar(36, panelY + 74, 288, 10, hp / (double) maxHp, Color.color(0.88, 0.2, 0.2));

        gc.setFill(Color.WHITE);
        gc.fillText("SKILL " + getAbilityStatusText(), 36, panelY + 96);
        drawBar(36, panelY + 102, 288, 10, getAbilityMeterFill(), Color.color(0.22, 0.7, 0.92));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.color(0.74, 0.86, 0.80));
        gc.fillText(stage.name().toUpperCase(), W / 2.0 - textWidth(stage.name(), 12) / 2, panelY + 20);

        gc.setFont(Font.font("Monospaced", 11));
        gc.setFill(Color.WHITE);
        String objective = stage.objective().toUpperCase();
        gc.fillText(objective, W / 2.0 - textWidth(objective, 11) / 2, panelY + 38);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.WHITE);
        gc.fillText("AMMO", W - 204, panelY + 28);
        gc.fillText(ammo + "/" + weapon.getMagazineSize(), W - 204, panelY + 48);
        gc.fillText("RELOAD", W - 204, panelY + 72);
        gc.fillText(getReloadStatusText(), W - 204, panelY + 92);

        String buffText = getBuffStatusText();
        if (buffText != null) {
            drawPixelPanel(20, H - 74, 260, 38, Color.color(0.01, 0.03, 0.05, 0.82),
                    Color.color(0.10, 0.76, 0.42, 0.70));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
            gc.setFill(Color.color(0.15, 0.9, 0.4));
            gc.fillText(buffText, 34, H - 50);
        }

        if (stageExit.isActive()) {
            drawPixelPanel(W - 220, H - 74, 200, 38, Color.color(0.01, 0.03, 0.05, 0.82),
                    Color.color(0.10, 0.76, 0.42, 0.70));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
            gc.setFill(Color.color(0.15, 0.9, 0.4));
            gc.fillText("EXIT OPEN  MOVE INTO MARKER", W - 204, H - 50);
        }
    }

    private void renderStageIntro(StageDefinition stage) {
        gc.setFill(Color.color(0, 0, 0, 0.54));
        gc.fillRect(0, 0, W, H);

        drawPixelPanel(180, 210, W - 360, 190, Color.color(0.03, 0.04, 0.06, 0.96),
                Color.color(stage.tint().getRed(), stage.tint().getGreen(), stage.tint().getBlue(), 0.95));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        gc.setFill(Color.WHITE);
        gc.fillText(stage.name(), W / 2.0 - textWidth(stage.name(), 24) / 2, 265);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        gc.setFill(Color.color(0.7, 0.75, 0.72));
        gc.fillText(stage.objective().toUpperCase(), W / 2.0 - textWidth(stage.objective(), 15) / 2, 304);

        gc.setFont(Font.font("Monospaced", 13));
        gc.setFill(Color.color(0.86, 0.86, 0.86));
        wrapText(stage.description(), 220, 338, W - 440, 20);
    }

    private void renderStatusBanner() {
        drawPixelPanel(W / 2.0 - 180, H - 94, 360, 42, Color.color(0.02, 0.03, 0.04, 0.92),
                Color.color(0.18, 0.8, 0.32, 0.85));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.setFill(Color.color(0.9, 0.95, 0.9));
        gc.fillText(statusText, W / 2.0 - textWidth(statusText, 16) / 2, H - 66);
    }

    private void renderEndOverlay() {
        gc.setFill(Color.color(0, 0, 0, 0.76));
        gc.fillRect(0, 0, W, H);

        drawPixelPanel(120, 120, W - 240, H - 240, Color.color(0.04, 0.05, 0.06, 0.98),
                Color.color(0.18, 0.82, 0.34, 0.85));

        String title = victory ? "THE LAIR" : "Synchronization Failed";
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 34));
        gc.setFill(Color.WHITE);
        gc.fillText(title, W / 2.0 - textWidth(title, 34) / 2, 190);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        gc.setFill(Color.color(0.75, 0.8, 0.76));
        String subtitle = victory
                ? "Caesar was only the first host. The real trap was waiting in the courtyard."
                : "The campus falls silent as LAIR keeps learning.";
        gc.fillText(subtitle, W / 2.0 - textWidth(subtitle, 15) / 2, 230);

        gc.setFont(Font.font("Monospaced", 13));
        gc.setFill(Color.color(0.9, 0.9, 0.9));
        if (victory) {
            wrapText("Caesar falls in the gym and drops a stabilized LAIR vial, but the real Sir Khai was already dead. "
                            + "The creature guiding you was LAIR wearing his face, learning trust before it fed. "
                            + "In the courtyard, the mimic reveals itself and the final fight ends the night's worst lie.",
                    170, 290, W - 340, 28);
        } else {
            wrapText("You were close, but the synchronized aura failed before the school could be reclaimed. "
                            + "Caesar remains the first host, and the false Sir Khai keeps feeding the campus to LAIR.",
                    170, 300, W - 340, 28);
        }

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(Color.color(0.18, 0.85, 0.32));
        String prompt = "Press ENTER or SPACE to return to character select";
        gc.fillText(prompt, W / 2.0 - textWidth(prompt, 14) / 2, H - 150);
    }

    private void drawBar(double x, double y, double width, double height, double fill, Color color) {
        x = snap(x);
        y = snap(y);
        width = snap(width);
        height = snap(height);

        gc.setFill(Color.color(0.12, 0.14, 0.16));
        gc.fillRect(x, y, width, height);
        gc.setFill(Color.color(0.05, 0.06, 0.08));
        gc.fillRect(x + PIXEL, y + PIXEL, width - PIXEL * 2, height - PIXEL * 2);
        gc.setFill(color);
        gc.fillRect(x + PIXEL, y + PIXEL, (width - PIXEL * 2) * clamp(fill, 0, 1), height - PIXEL * 2);
    }

    private void drawPixelPanel(double x, double y, double width, double height, Color bg, Color border) {
        x = snap(x);
        y = snap(y);
        width = snap(width);
        height = snap(height);

        gc.setFill(border);
        gc.fillRect(x, y, width, height);
        gc.setFill(Color.color(0.02, 0.03, 0.03));
        gc.fillRect(x + PIXEL, y + PIXEL, width - PIXEL * 2, height - PIXEL * 2);
        gc.setFill(bg);
        gc.fillRect(x + PIXEL * 2, y + PIXEL * 2, width - PIXEL * 4, height - PIXEL * 4);
    }

    private double snap(double value) {
        return Math.round(value / PIXEL) * PIXEL;
    }

    private void wrapText(String text, double x, double y, double maxWidth, double lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        double currentY = y;

        for (String word : words) {
            String nextLine = line.isEmpty() ? word : line + " " + word;
            if (textWidth(nextLine, 16) > maxWidth) {
                gc.fillText(line.toString(), x, currentY);
                currentY += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(nextLine);
            }
        }

        if (!line.isEmpty()) {
            gc.fillText(line.toString(), x, currentY);
        }
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

    private String getBuffStatusText() {
        if (hemorrhageTimer <= 0 && suppressTimer <= 0 && overdriveTimer <= 0
                && overloadShots <= 0 && focusShots <= 0) {
            return null;
        }

        StringBuilder line = new StringBuilder("BUFFS ");
        if (hemorrhageTimer > 0) line.append("HEMORRHAGE ");
        if (suppressTimer > 0) line.append("SUPPRESS ");
        if (overdriveTimer > 0) line.append("OVERDRIVE ");
        if (overloadShots > 0) line.append("OVERLOAD x").append(overloadShots).append(" ");
        if (focusShots > 0) line.append("FOCUS ");
        return line.toString().trim();
    }

    private void setStatus(String message) {
        statusText = message;
        statusTimer = 1.8;
    }

    private void exitToCharacterSelect() {
        loop.stop();
        GameContext.showCharacterSelect();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double textWidth(String text, double size) {
        return text.length() * size * 0.52;
    }

    public Scene getScene() {
        return scene;
    }
}

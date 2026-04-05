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
import org.example.assets.AssetRegistry;
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
    private final List<PlatformTile> platforms = List.of(
            new PlatformTile(170, 520, 220, 16),
            new PlatformTile(520, 430, 240, 16),
            new PlatformTile(900, 340, 180, 16)
    );
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

        player.clampX(0, W - player.getWidth());
    }

    private void updateProjectiles(double dt) {
        for (var iterator = projectiles.iterator(); iterator.hasNext();) {
            Projectile projectile = iterator.next();
            projectile.update(dt);

            boolean remove = projectile.isExpired(W, H);
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
            enemy.chase(player, dt, 20, W - enemy.getWidth() - 20);

            if (enemy.getAttackCooldown() <= 0 && CollisionManager.intersects(enemy, player)) {
                applyDamage(enemy.isBoss() ? 22 : 12);
                enemy.setAttackCooldown(enemy.isBoss() ? 0.8 : 1.1);
                enemy.setAttacking(true);
            }
        }
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
        double angle = Math.atan2(input.getMouseY() - originY, input.getMouseX() - originX);

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
        stageIntroTimer = 4.5;
        stageBossSpawned = false;
        stageExit.setActive(false);
        stageExit.setLabel(newIndex == stages.size() - 1 ? "FINAL" : "NEXT");

        StageDefinition stage = stages.get(stageIndex);
        if (stage.hasMobs()) {
            spawnMobWave(stage);
        } else if (stage.hasBoss()) {
            spawnBoss(stage);
        }
    }

    private void spawnMobWave(StageDefinition stage) {
        for (int i = 0; i < stage.enemyCount(); i++) {
            double x = 620 + i * 84;
            EnemyActor enemy = new EnemyActor(stage.enemyName(), x, GROUND_Y - 54, 42, 54,
                    stage.enemyHealth(), stage.enemyHealth(), stage.enemySpeed(), stage.tint(), false);
            if (!stage.enemySpriteIds().isEmpty()) {
                enemy.setSpriteSheet(loadEnemySheet(stage.enemySpriteIds().get(i % stage.enemySpriteIds().size())));
            }
            enemies.add(enemy);
        }
    }

    private void spawnBoss(StageDefinition stage) {
        stageBossSpawned = true;
        double x = stageIndex == stages.size() - 1 ? 980 : 920;
        EnemyActor enemy = new EnemyActor(stage.bossName(), x, GROUND_Y - 96, 74, 96,
                stage.bossHealth(), stage.bossHealth(), stage.bossSpeed(), stage.tint(), true);
        if (stage.bossSpriteId() != null) {
            enemy.setSpriteSheet(loadEnemySheet(stage.bossSpriteId()));
        }
        enemies.add(enemy);
    }

    private javafx.scene.image.Image loadBackdrop(StageDefinition stage) {
        return assets.image(stage.backdropAssetId());
    }

    private org.example.assets.SpriteSheet loadEnemySheet(String spriteId) {
        return switch (spriteId) {
            case "enemy.security_guard" -> assets.sheet(spriteId, 96, 128);
            case "enemy.vendor" -> assets.sheet(spriteId, 128, 64);
            default -> assets.sheet(spriteId, 32, 32);
        };
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

    private void renderGame() {
        StageDefinition stage = stages.get(stageIndex);

        renderBackground(stage);
        for (PlatformTile platform : platforms) {
            platform.render(gc);
        }
        stageExit.render(gc);
        player.render(gc);
        renderMuzzleFlash();
        projectiles.renderAll(gc);
        enemies.renderAll(gc);
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
            gc.setImageSmoothing(false);
            gc.drawImage(backdrop, 0, 120, W, 320);
            gc.setFill(Color.color(0, 0, 0, 0.18));
            gc.fillRect(0, 120, W, 320);
        } else {
            gc.setFill(Color.color(0.03, 0.08, 0.11));
            gc.fillRect(0, 0, W, H - 96);
        }

        gc.setFill(Color.color(stage.tint().getRed(), stage.tint().getGreen(), stage.tint().getBlue(), 0.16));
        gc.fillRect(0, 96, W, 380);

        gc.setFill(Color.color(0.02, 0.04, 0.05));
        for (int i = 0; i < 9; i++) {
            double sx = 70 + i * 150;
            gc.fillRect(sx, 0, 6, H);
        }

        gc.setFill(Color.color(0.05, 0.10, 0.07));
        gc.fillRect(0, GROUND_Y, W, H - GROUND_Y);
        gc.setFill(Color.color(0.10, 0.18, 0.12));
        for (int x = 0; x < W; x += 32) {
            gc.fillRect(x, GROUND_Y + 24, 16, 12);
        }
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

    private void renderHud(StageDefinition stage) {
        double panelY = 18;
        double panelH = 160;

        drawPixelPanel(20, panelY, 460, panelH, Color.color(0.01, 0.03, 0.05, 0.95),
                Color.color(0.10, 0.76, 0.42));
        drawPixelPanel(W - 400, panelY, 380, panelH, Color.color(0.01, 0.03, 0.05, 0.95),
                Color.color(0.10, 0.76, 0.42));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gc.setFill(Color.WHITE);
        gc.fillText(character.getName(), 34, 46);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.color(0.74, 0.86, 0.80));
        gc.fillText(character.getTitle() + " | " + weapon.getName(), 34, 68);
        gc.fillText("CHAPTER: " + stage.name().toUpperCase(), 34, 88);
        gc.fillText("OBJECTIVE: " + stage.objective().toUpperCase(), 34, 108);
        gc.fillText("SKILL: " + character.getSkillName().toUpperCase(), 34, 128);
        gc.fillText(stage.moodText().toUpperCase(), 34, 148);

        drawBar(250, 132, 190, 12, hp / (double) maxHp, Color.color(0.88, 0.2, 0.2));
        drawBar(250, 152, 190, 12, getAbilityMeterFill(), Color.color(0.22, 0.7, 0.92));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.WHITE);
        gc.fillText("HP " + hp + "/" + maxHp, 250, 126);
        gc.fillText(getAbilityStatusText(), 250, 146);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.WHITE);
        gc.fillText("Damage " + getCurrentShotDamage(), W - 360, 48);
        gc.fillText("Move " + character.getMovementSpeed() + " (" + (int) character.getMovementSpeedPx() + " px/s)", W - 360, 70);
        gc.fillText("Ammo " + ammo + "/" + weapon.getMagazineSize(), W - 360, 92);
        gc.fillText(reloadTimer > 0 ? "Reload " + String.format("%.1fs", reloadTimer) : "Reload ready", W - 360, 114);
        gc.fillText("Controls: A/D or Arrows  |  SPACE  |  Mouse  |  Q  |  R", W - 360, 136);

        if (hemorrhageTimer > 0 || suppressTimer > 0 || overdriveTimer > 0
                || overloadShots > 0 || focusShots > 0) {
            gc.setFill(Color.color(0.15, 0.9, 0.4));
            String buffLine = "Buffs:";
            if (hemorrhageTimer > 0) buffLine += " Hemorrhage";
            if (suppressTimer > 0) buffLine += " Suppress";
            if (overdriveTimer > 0) buffLine += " Overdrive";
            if (overloadShots > 0) buffLine += " Overload x" + overloadShots;
            if (focusShots > 0) buffLine += " Focus";
            gc.fillText(buffLine, W - 360, 158);
        }

        if (stageExit.isActive()) {
            gc.setFill(Color.color(0.15, 0.9, 0.4));
            gc.fillText("Exit open: move right into the marker", W - 360, 178);
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
            return "Skill READY";
        }
        return "Skill " + String.format("%.1fs", abilityCooldown);
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

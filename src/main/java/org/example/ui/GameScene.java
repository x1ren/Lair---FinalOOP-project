package org.example.ui;

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
import org.example.engine.CollisionManager;
import org.example.engine.GameLoop;
import org.example.engine.InputHandler;
import org.example.gameplay.BleedConfig;
import org.example.gameplay.BossSkill;
import org.example.gameplay.CombatScaling;
import org.example.gameplay.EnemyActor;
import org.example.gameplay.EntityManager;
import org.example.gameplay.HitPayload;
import org.example.gameplay.PlatformTile;
import org.example.gameplay.PlayerActor;
import org.example.gameplay.Projectile;
import org.example.gameplay.SlowConfig;
import org.example.gameplay.SpikeEffect;
import org.example.gameplay.SplittingSpike;
import org.example.gameplay.StageCatalog;
import org.example.gameplay.StageDefinition;
import org.example.player.CharacterCombatProfile;
import org.example.player.CharacterType;
import org.example.app.GameContext;
import org.example.leaderboard.LeaderboardEntry;
import org.example.leaderboard.LeaderboardManager;
import org.example.leaderboard.RunTimer;
import org.example.weapons.Weapon;
import org.example.weapons.WeaponType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScene {

    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;
    private static final double GROUND_Y = 620;
    private static final double GRAVITY = 1500;
    private static final double PIXEL = 4;
    private static final double PAUSE_MENU_BUTTON_W = 288;
    private static final double PAUSE_MENU_BUTTON_H = 42;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final InputHandler input = new InputHandler();
    private final Random random = new Random();
    private final AssetRegistry assets = GameContext.assets();

    private final String playerName;
    private final RunTimer runTimer = new RunTimer();
    private LeaderboardEntry completedRun;

    private final CharacterType character;
    private final CharacterCombatProfile combatProfile;
    private final Weapon weapon;
    private final PlayerActor player;
    private final EntityManager<EnemyActor> enemies = new EntityManager<>();
    private final EntityManager<Projectile> projectiles = new EntityManager<>();
    private final EntityManager<BossSkill> bossSkills = new EntityManager<>();
    private final EntityManager<SpikeEffect> spikeEffects = new EntityManager<>();
    private final EntityManager<SplittingSpike> splittingSpikes = new EntityManager<>();
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
    
    // Batch spawning fields
    private static final int BATCH_SIZE = 10;
    private int totalMobsToSpawn;
    private int mobsSpawned;
    private boolean batchSpawningActive;

    /** Seconds, used for HUD skill strip animation. */
    private double hudAnimTime;

    private double muzzleFlashTimer;
    /** Length of the current muzzle burst; matches weapon cadence so SMG visuals stay in sync with fire rate. */
    private double muzzleFlashDuration = 0.08;
    private double muzzleFlashX;
    private double muzzleFlashY;
    private double muzzleFlashAngle;

    private boolean finished;
    private boolean victory;
    private boolean paused;
    
    private double victoryOverlayTimer;
    private boolean victoryShakeTriggered;

    /** False Sir Khai appears human first; morphs to the zombified host sheet after enough damage. */
    private boolean khaiMimicMorphTriggered;
    
    private double screenShakeTimer;
    private double screenShakeIntensity;

    public GameScene(CharacterType character, String playerName) {
        this.character = character;
        this.playerName = playerName;
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
        if (!finished && input.isJustPressed(KeyCode.ESCAPE)) {
            paused = !paused;
            input.endFrame();
            return;
        }

        if (paused) {
            if (input.isJustPressed(KeyCode.ENTER) || input.isJustPressed(KeyCode.SPACE)) {
                paused = false;
            } else if (input.isMouseLeftJustClicked() && isPauseMenuButtonHit()) {
                exitToMainMenu();
                return;
            }
            input.endFrame();
            return;
        }

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

        if (!finished && !paused) {
            if (stageIndex == 0 && stageIntroTimer <= 0) {
                runTimer.start();
            }
            if (runTimer.isStarted() && !runTimer.isStopped()) {
                runTimer.accumulate(dt);
            }
        }

        hudAnimTime += dt;

        if (finished) {
            if (victory) {
                // Victory sequence: show overlay for 5 seconds, shake, then transition to ending
                victoryOverlayTimer += dt;
                
                if (victoryOverlayTimer >= 5.0 && !victoryShakeTriggered) {
                    // Start screen shake
                    screenShakeTimer = 2.0;
                    screenShakeIntensity = 18;
                    victoryShakeTriggered = true;
                }
                
                if (victoryOverlayTimer >= 7.0) {
                    // Transition to ending scene
                    GameContext.audio().stopBackgroundMusic();
                    loop.stop();
                    GameContext.showEnding(completedRun);
                    return;
                }
            } else {
                // Defeat: allow return to character select
                if (input.isJustPressed(KeyCode.ESCAPE)) {
                    exitToCharacterSelect();
                    return;
                }
                if (input.isJustPressed(KeyCode.ENTER) || input.isJustPressed(KeyCode.SPACE)) {
                    exitToCharacterSelect();
                    return;
                }
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
        updateBossSkills(dt);
        updateSpikeEffects(dt);
        updateSplittingSpikes(dt);
        updateEnemies(dt);
        updateStageExit();
        updateCamera();
        updateScreenShake(dt);

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
        double cameraX = arena.cameraX();
        double screenLeft = cameraX - 40;
        double screenRight = cameraX + W + 40;
        double screenBottom = GROUND_Y;
        
        for (var iterator = projectiles.iterator(); iterator.hasNext();) {
            Projectile projectile = iterator.next();
            projectile.update(dt);

            boolean remove = projectile.isExpired(arena.worldWidth(), H);
            
            // Remove bullets that go below the visible platform area
            if (!remove && projectile.getCenterY() > screenBottom) {
                remove = true;
            }
            
            if (!remove) {
                double bulletX = projectile.getCenterX();
                double bulletY = projectile.getCenterY();
                boolean bulletOnScreen = bulletX >= screenLeft && bulletX <= screenRight && bulletY <= screenBottom;
                
                for (EnemyActor enemy : enemies) {
                    double enemyX = enemy.getCenterX();
                    boolean enemyOnScreen = enemyX >= screenLeft && enemyX <= screenRight;
                    
                    if (bulletOnScreen && enemyOnScreen && CollisionManager.circleHitsRect(projectile, enemy)) {
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
            
            // Caesar Hunos special behavior
            if (enemy.isCaesarHunos()) {
                updateCaesarHunosBehavior(enemy, dt);
            } else if (enemy.isKhaiBossForm()) {
                updateKhaiBossBehavior(enemy, dt);
            } else if (enemy.isVendor()) {
                updateVendorBehavior(enemy, dt);
            } else if (enemy.isSecurityGuard()) {
                updateSecurityGuardBehavior(enemy, dt);
            } else {
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
        maybeMorphKhaiMimicBoss();
    }

    private static final String KHAI_MIMIC_BOSS_NAME = "LAIR Mimic (False Sir Khai)";
    /** Only triggers when a stage uses {@code LAIR Mimic (False Sir Khai)} at this index (story catalog may omit it). */
    private static final int KHAI_MIMIC_STAGE_INDEX = 3; // 0-based stage index

    private void maybeMorphKhaiMimicBoss() {
        if (khaiMimicMorphTriggered || finished || stageIndex != KHAI_MIMIC_STAGE_INDEX) {
            return;
        }
        for (EnemyActor enemy : enemies) {
            if (!enemy.isBoss() || !KHAI_MIMIC_BOSS_NAME.equals(enemy.getName())) {
                continue;
            }
            double hpRatio = enemy.getHp() / (double) Math.max(1, enemy.getMaxHp());
            if (hpRatio > 0.50) {
                continue;
            }
            applyEnemySprite(enemy, "enemy.khai_mimic");
            khaiMimicMorphTriggered = true;
            setStatus("The mask drops — LAIR sheds the human disguise.");
            break;
        }
    }
    
    private void updateSecurityGuardBehavior(EnemyActor guard, double dt) {
        // Update dash state
        guard.updateDash(dt);
        
        // Physics
        updateEnemyPhysics(guard, dt);
        
        // Update dash cooldown
        double currentCooldown = guard.getSecurityGuardDashCooldown();
        guard.setSecurityGuardDashCooldown(Math.max(0, currentCooldown - dt));
        
        // Check if ready to dash
        double distanceToPlayer = Math.abs(guard.getCenterX() - player.getCenterX());
        if (!guard.isDashing() && guard.getSecurityGuardDashCooldown() <= 0 && distanceToPlayer > 100 && distanceToPlayer < 600) {
            // Start dash towards player
            double direction = player.getCenterX() > guard.getCenterX() ? 1 : -1;
            guard.startDash(450 * direction, 0.6); // Fast dash for 0.6 seconds
            guard.setSecurityGuardDashCooldown(3.5); // Dash every 3.5 seconds
            setStatus("Security Guard dashes!");
        }
        
        if (guard.isDashing()) {
            // Move at dash speed
            double dashSpeed = guard.getDashSpeed();
            guard.moveBy(dashSpeed * dt, 0);
            guard.setX(Math.max(20, Math.min(arena.worldWidth() - guard.getWidth() - 20, guard.getX())));
            
            // Check collision during dash - deals damage
            if (CollisionManager.intersects(guard, player)) {
                applyDamage(30); // Higher damage during dash
                guard.setAttackCooldown(0.5);
                guard.setAttacking(true);
            }
        } else {
            // Normal chase behavior with increased speed
            double normalSpeed = guard.getSpeed() * 1.5; // 50% faster than normal
            double cx = player.getCenterX() - guard.getCenterX();
            double direction = Math.signum(cx);
            guard.setMoving(Math.abs(cx) > 0.5);
            guard.moveBy(direction * normalSpeed * dt, 0);
            guard.setX(Math.max(20, Math.min(arena.worldWidth() - guard.getWidth() - 20, guard.getX())));
            
            // Normal melee attack
            if (guard.getAttackCooldown() <= 0 && CollisionManager.intersects(guard, player)) {
                applyDamage(22);
                guard.setAttackCooldown(0.8 * guard.getTuningState().attackCooldownMultiplier());
                guard.setAttacking(true);
            }
        }
    }
    
    private void updateVendorBehavior(EnemyActor vendor, double dt) {
        // Vendor chases player like normal boss
        updateEnemyPhysics(vendor, dt);
        vendor.chase(player, dt, 20, arena.worldWidth() - vendor.getWidth() - 20,
                combatProfile.enemySlowMoveFactor());
        
        // Update summon cooldown
        double currentCooldown = vendor.getVendorSummonCooldown();
        vendor.setVendorSummonCooldown(Math.max(0, currentCooldown - dt));
        
        // Summon students when cooldown is ready
        if (vendor.getVendorSummonCooldown() <= 0) {
            summonStudents(vendor);
            vendor.setVendorSummonCooldown(12.0); // Summon every 12 seconds
        }
        
        // Normal melee attack
        if (vendor.getAttackCooldown() <= 0 && CollisionManager.intersects(vendor, player)) {
            applyDamage(22);
            vendor.setAttackCooldown(0.8 * vendor.getTuningState().attackCooldownMultiplier());
            vendor.setAttacking(true);
        }
    }
    
    private void summonStudents(EnemyActor vendor) {
        int summonCount = 30; // 30 students
        double vendorX = vendor.getCenterX();
        StageDefinition stage = stages.get(stageIndex);
        
        for (int i = 0; i < summonCount; i++) {
            // Spawn students around the vendor in a spread pattern
            double offsetX = (random.nextDouble() - 0.5) * 400; // Spread across 400px
            double x = Math.max(50, Math.min(arena.worldWidth() - 92, vendorX + offsetX));
            
            // Alternate between male and female students
            String spriteId = random.nextBoolean() ? "enemy.student_m" : "enemy.student_f";
            
            // Spawn high above so they fall down
            EnemyActor student = new EnemyActor("Student", x, 50, 42, 54,
                    stage.enemyHealth(), stage.enemyHealth(), stage.enemySpeed(), stage.tint(), false);
            applyEnemySprite(student, spriteId);
            enemies.add(student);
        }
        
        setStatus("Mutated Vendor summons a horde of students!");
    }
    
    private void updateCaesarHunosBehavior(EnemyActor caesar, double dt) {
        // Caesar stays in place, only uses physics for gravity
        updateEnemyPhysics(caesar, dt);
        
        // Update casting state
        caesar.updateCasting(dt);
        
        // Update skill cooldown
        double currentCooldown = caesar.getSkillCooldown();
        caesar.setSkillCooldown(Math.max(0, currentCooldown - dt));
        
        // Check if ready to cast a new skill
        if (caesar.getSkillCooldown() <= 0 && !caesar.isCastingSkill()) {
            double hpRatio = caesar.getHp() / (double) Math.max(1, caesar.getMaxHp());
            
            if (hpRatio < 0.4) {
                // Use ultimate abilities - more frequent attacks
                String ultimateType = "ultimate" + caesar.getUltimatePhase();
                caesar.startCasting(ultimateType);
                caesar.setSkillCooldown(2.5);
            } else {
                // Use regular skills (randomize between skill1 and skill2)
                String skillType = random.nextBoolean() ? "skill1" : "skill2";
                caesar.startCasting(skillType);
                caesar.setSkillCooldown(1.8);
            }
        }
        
        // Check if casting is complete and skill should be fired
        if (caesar.isReadyToCast()) {
            fireCaesarSkill(caesar);
            caesar.finishCasting();
        }
    }
    
    private void fireCaesarSkill(EnemyActor caesar) {
        String skillType = caesar.getCurrentSkillType();
        double centerX = caesar.getCenterX();
        double centerY = caesar.getCenterY();
        
        // Stage 3 (index 2): Use varied purple bullet patterns instead of skill animations
        if (stageIndex == 2) {
            fireCaesarBulletPattern(centerX, centerY, skillType);
            return;
        }
        
        // Other stages: Use original skill-based attacks
        if ("skill1".equals(skillType)) {
            // Wave Attack - multiple horizontal waves with slight delays
            int waveCount = 15;
            double waveSpacing = 35;
            double startX = centerX - (waveCount / 2.0 * waveSpacing);
            for (int i = 0; i < waveCount; i++) {
                double x = startX + (i * waveSpacing);
                double vx = 0;
                double vy = 280 + (i % 3) * 30; // Varying speeds
                bossSkills.add(new BossSkill("Wave", x, centerY, vx, vy, 15, "wave"));
            }
            setStatus("Caesar Hunos unleashes a Wave Attack!");
        } else if ("skill2".equals(skillType)) {
            // Rain Attack - heavy projectile rain from above
            int rainCount = 30;
            double screenWidth = 1280;
            for (int i = 0; i < rainCount; i++) {
                double x = random.nextDouble() * screenWidth;
                double y = -50 - (random.nextDouble() * 100); // Staggered heights
                double vx = (random.nextDouble() - 0.5) * 120;
                double vy = 250 + random.nextDouble() * 150;
                bossSkills.add(new BossSkill("Rain", x, y, vx, vy, 12, "rain"));
            }
            setStatus("Caesar Hunos summons a Rain of Projectiles!");
        } else if ("ultimate1".equals(skillType)) {
            // Random Barrage - chaotic projectiles in all directions
            int barrageCount = 35;
            for (int i = 0; i < barrageCount; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double speed = 200 + random.nextDouble() * 200;
                double vx = Math.cos(angle) * speed;
                double vy = Math.sin(angle) * speed;
                bossSkills.add(new BossSkill("Barrage", centerX, centerY, vx, vy, 18, "barrage"));
            }
            caesar.toggleUltimatePhase();
            setStatus("Caesar Hunos unleashes a Random Barrage!");
        } else if ("ultimate2".equals(skillType)) {
            // Spiral Storm - massive multi-layered spiral
            int spiralLayers = 4;
            int projectilesPerLayer = 12;
            double spiralSpeed = 280;
            for (int layer = 0; layer < spiralLayers; layer++) {
                for (int i = 0; i < projectilesPerLayer; i++) {
                    double angle = (i * Math.PI * 2) / projectilesPerLayer + (layer * 0.4);
                    double speed = spiralSpeed + (layer * 40);
                    double vx = Math.cos(angle) * speed;
                    double vy = Math.sin(angle) * speed;
                    bossSkills.add(new BossSkill("Spiral", centerX, centerY, vx, vy, 20, "spiral"));
                }
            }
            caesar.toggleUltimatePhase();
            setStatus("Caesar Hunos creates a Spiral Storm!");
        }
    }
    
    private void fireCaesarBulletPattern(double centerX, double centerY, String skillType) {
        // Randomly select one of several varied bullet patterns
        int patternChoice = random.nextInt(8);
        
        switch (patternChoice) {
            case 0 -> fireScatteredBurst(centerX, centerY);
            case 1 -> fireCircularSpread(centerX, centerY);
            case 2 -> fireRandomChaos(centerX, centerY);
            case 3 -> fireTightCluster(centerX, centerY);
            case 4 -> fireWideArc(centerX, centerY);
            case 5 -> fireAsymmetricSpray(centerX, centerY);
            case 6 -> fireDoubleHelix(centerX, centerY);
            case 7 -> fireRandomBursts(centerX, centerY);
        }
    }
    
    // Pattern 1: Scattered burst with random angles and speeds
    private void fireScatteredBurst(double centerX, double centerY) {
        int count = 12 + random.nextInt(8);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 150 + random.nextDouble() * 180;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bossSkills.add(new BossSkill("Bullet", centerX, centerY, vx, vy, 12, "bullet"));
        }
        setStatus("Caesar fires a scattered burst!");
    }
    
    // Pattern 2: Circular spread with even distribution
    private void fireCircularSpread(double centerX, double centerY) {
        int count = 16 + random.nextInt(8);
        double baseSpeed = 200 + random.nextDouble() * 100;
        for (int i = 0; i < count; i++) {
            double angle = (i * Math.PI * 2) / count;
            double speedVariation = 0.8 + random.nextDouble() * 0.4;
            double vx = Math.cos(angle) * baseSpeed * speedVariation;
            double vy = Math.sin(angle) * baseSpeed * speedVariation;
            bossSkills.add(new BossSkill("Bullet", centerX, centerY, vx, vy, 12, "bullet"));
        }
        setStatus("Caesar unleashes a circular spread!");
    }
    
    // Pattern 3: Random chaos - completely unpredictable
    private void fireRandomChaos(double centerX, double centerY) {
        int count = 20 + random.nextInt(15);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 100 + random.nextDouble() * 250;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            // Random spawn offset for extra chaos
            double offsetX = (random.nextDouble() - 0.5) * 60;
            double offsetY = (random.nextDouble() - 0.5) * 60;
            bossSkills.add(new BossSkill("Bullet", centerX + offsetX, centerY + offsetY, vx, vy, 12, "bullet"));
        }
        setStatus("Caesar creates chaotic bullet spray!");
    }
    
    // Pattern 4: Tight cluster aimed at player
    private void fireTightCluster(double centerX, double centerY) {
        int count = 8 + random.nextInt(5);
        double playerX = player.getCenterX();
        double playerY = player.getCenterY();
        double baseAngle = Math.atan2(playerY - centerY, playerX - centerX);
        
        for (int i = 0; i < count; i++) {
            double angleSpread = (random.nextDouble() - 0.5) * 0.6; // Tight spread
            double angle = baseAngle + angleSpread;
            double speed = 220 + random.nextDouble() * 80;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bossSkills.add(new BossSkill("Bullet", centerX, centerY, vx, vy, 14, "bullet"));
        }
        setStatus("Caesar fires a focused cluster!");
    }
    
    // Pattern 5: Wide arc covering large area
    private void fireWideArc(double centerX, double centerY) {
        int count = 18 + random.nextInt(10);
        double startAngle = random.nextDouble() * Math.PI * 2;
        double arcSize = Math.PI * 1.2 + random.nextDouble() * 0.8;
        
        for (int i = 0; i < count; i++) {
            double angle = startAngle + (i * arcSize / count);
            double speed = 180 + random.nextDouble() * 120;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bossSkills.add(new BossSkill("Bullet", centerX, centerY, vx, vy, 12, "bullet"));
        }
        setStatus("Caesar sweeps a wide arc!");
    }
    
    // Pattern 6: Asymmetric spray - more bullets in one direction
    private void fireAsymmetricSpray(double centerX, double centerY) {
        int mainCount = 15 + random.nextInt(8);
        int sideCount = 5 + random.nextInt(4);
        double mainAngle = random.nextDouble() * Math.PI * 2;
        
        // Main direction
        for (int i = 0; i < mainCount; i++) {
            double angleSpread = (random.nextDouble() - 0.5) * 1.2;
            double angle = mainAngle + angleSpread;
            double speed = 200 + random.nextDouble() * 100;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bossSkills.add(new BossSkill("Bullet", centerX, centerY, vx, vy, 12, "bullet"));
        }
        
        // Opposite side (fewer bullets)
        for (int i = 0; i < sideCount; i++) {
            double angleSpread = (random.nextDouble() - 0.5) * 0.8;
            double angle = mainAngle + Math.PI + angleSpread;
            double speed = 150 + random.nextDouble() * 80;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bossSkills.add(new BossSkill("Bullet", centerX, centerY, vx, vy, 12, "bullet"));
        }
        setStatus("Caesar fires an asymmetric spray!");
    }
    
    // Pattern 7: Double helix pattern
    private void fireDoubleHelix(double centerX, double centerY) {
        int count = 12;
        double baseSpeed = 220;
        
        for (int i = 0; i < count; i++) {
            double angle1 = (i * Math.PI * 2) / count;
            double angle2 = angle1 + Math.PI;
            
            double vx1 = Math.cos(angle1) * baseSpeed;
            double vy1 = Math.sin(angle1) * baseSpeed;
            double vx2 = Math.cos(angle2) * baseSpeed;
            double vy2 = Math.sin(angle2) * baseSpeed;
            
            bossSkills.add(new BossSkill("Bullet", centerX, centerY, vx1, vy1, 12, "bullet"));
            bossSkills.add(new BossSkill("Bullet", centerX, centerY, vx2, vy2, 12, "bullet"));
        }
        setStatus("Caesar creates a double helix!");
    }
    
    // Pattern 8: Multiple random bursts from different positions
    private void fireRandomBursts(double centerX, double centerY) {
        int burstCount = 3 + random.nextInt(2);
        
        for (int burst = 0; burst < burstCount; burst++) {
            double burstX = centerX + (random.nextDouble() - 0.5) * 100;
            double burstY = centerY + (random.nextDouble() - 0.5) * 80;
            int bulletsPerBurst = 5 + random.nextInt(4);
            
            for (int i = 0; i < bulletsPerBurst; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double speed = 180 + random.nextDouble() * 120;
                double vx = Math.cos(angle) * speed;
                double vy = Math.sin(angle) * speed;
                bossSkills.add(new BossSkill("Bullet", burstX, burstY, vx, vy, 12, "bullet"));
            }
        }
        setStatus("Caesar fires multiple bursts!");
    }
    
    private void updateBossSkills(double dt) {
        for (var iterator = bossSkills.iterator(); iterator.hasNext();) {
            BossSkill skill = iterator.next();
            skill.update(dt);
            
            boolean remove = skill.isExpired();
            
            // Check collision with player
            if (!remove && CollisionManager.intersects(skill, player)) {
                applyDamage(skill.getDamage());
                remove = true;
            }
            
            if (remove) {
                iterator.remove();
            }
        }
    }
    
    private static final double KHAI_BOSS_SKILL_WINDUP_SEC = 0.65;

    private static AnimationStrip khaiBossIdleLoopStrip() {
        return new AnimationStrip(0, 0, 14, 14 / 1.45);
    }

    private static AnimationStrip khaiBossSkillStrip(int skillIndex) {
        int row = skillIndex + 1;
        int frames = switch (skillIndex) {
            case 0 -> 16;
            case 1 -> 18;
            case 2 -> 19;
            default -> 1;
        };
        return new AnimationStrip(row, 0, frames, frames / KHAI_BOSS_SKILL_WINDUP_SEC);
    }

    private void updateKhaiBossBehavior(EnemyActor khai, double dt) {
        // Khai stays stationary, only uses physics for gravity
        updateEnemyPhysics(khai, dt);
        
        // Update animation timer
        khai.updateKhaiAnimation(dt);
        
        // Update skill cooldown
        double currentCooldown = khai.getSkillCooldown();
        khai.setSkillCooldown(Math.max(0, currentCooldown - dt));
        
        // Check if animation is playing and should fire skill
        if (khai.getKhaiAnimationTimer() > 0 && khai.getKhaiAnimationTimer() <= dt) {
            // Animation just finished, fire the skill
            int skillIndex = khai.getKhaiSkillIndex();
            fireKhaiSkill(khai, skillIndex);
            
            SpriteSheet sheet = assets.sheet("enemy.khai_boss_form", 128, 128);
            if (sheet != null) {
                AnimationStrip idle = khaiBossIdleLoopStrip();
                khai.setSpriteSheet(sheet, idle, idle, idle);
            }
            
            khai.advanceKhaiSkill();
        }
        
        // Check if ready to start next skill animation
        if (khai.getSkillCooldown() <= 0 && khai.getKhaiAnimationTimer() <= 0) {
            int skillIndex = khai.getKhaiSkillIndex();
            
            SpriteSheet sheet = assets.sheet("enemy.khai_boss_form", 128, 128);
            if (sheet != null) {
                AnimationStrip idle = khaiBossIdleLoopStrip();
                AnimationStrip skill = khaiBossSkillStrip(skillIndex);
                khai.setSpriteSheet(sheet, idle, idle, skill);
                khai.setBossAttackVisualHold(KHAI_BOSS_SKILL_WINDUP_SEC);
            }
            
            // Set animation timer (skill fires when this reaches 0)
            khai.setKhaiAnimationTimer(KHAI_BOSS_SKILL_WINDUP_SEC);
        }
    }
    
    private void fireKhaiSkill(EnemyActor khai, int skillIndex) {
        double centerX = khai.getCenterX();
        double centerY = khai.getCenterY();
        double worldWidth = arena.worldWidth();
        
        switch (skillIndex) {
            case 0 -> {
                // Stomp - ground shake + spikes from below (cap at 100 total spike effects)
                if (spikeEffects.view().size() < 100) {
                    screenShakeTimer = 0.8;
                    screenShakeIntensity = 12;
                    
                    // Spawn ground spikes across the arena
                    for (int i = 0; i < 8; i++) {
                        double x = 100 + (i * (worldWidth - 200) / 7);
                        spikeEffects.add(new SpikeEffect("ground", x, GROUND_Y, 20));
                    }
                    setStatus("Khai stomps! Spikes erupt from below!");
                }
                khai.setSkillCooldown(1.5);  // Reduced from 2.0 to 1.5
            }
            case 1 -> {
                // Scream - spikes rain from above (cap at 100 total spike effects)
                if (spikeEffects.view().size() < 100) {
                    for (int i = 0; i < 10; i++) {
                        double x = 80 + random.nextDouble() * (worldWidth - 160);
                        spikeEffects.add(new SpikeEffect("rain", x, -50, 18));
                    }
                    setStatus("Khai screams! Spikes rain from above!");
                }
                khai.setSkillCooldown(1.6);  // Reduced from 2.2 to 1.6
            }
            case 2 -> {
                // Throw - splitting spike projectile (cap at 50 total splitting spikes)
                if (splittingSpikes.view().size() < 50) {
                    double dx = player.getCenterX() - centerX;
                    double dy = player.getCenterY() - centerY;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    double vx = (dx / distance) * 450;  // Increased from 300 to 450
                    double vy = (dy / distance) * 450;  // Increased from 300 to 450
                    splittingSpikes.add(new SplittingSpike(centerX, centerY, vx, vy, 22));
                    setStatus("Khai throws a splitting spike!");
                }
                khai.setSkillCooldown(1.8);  // Reduced from 2.5 to 1.8
            }
        }
    }
    
    private void updateSpikeEffects(double dt) {
        for (var iterator = spikeEffects.iterator(); iterator.hasNext();) {
            SpikeEffect spike = iterator.next();
            spike.update(dt);
            
            boolean remove = spike.isExpired();
            
            // Check collision with player
            if (!remove && !spike.hasHit() && CollisionManager.intersects(spike, player)) {
                applyDamage(spike.getDamage());
                spike.markHit();
            }
            
            if (remove) {
                iterator.remove();
            }
        }
    }
    
    private void updateSplittingSpikes(double dt) {
        List<SplittingSpike> newSpikes = new ArrayList<>();
        
        for (var iterator = splittingSpikes.iterator(); iterator.hasNext();) {
            SplittingSpike spike = iterator.next();
            spike.update(dt);
            
            boolean remove = spike.isExpired();
            
            // Check if should split into three (only if we won't exceed reasonable limit)
            if (spike.shouldSplit() && splittingSpikes.view().size() + newSpikes.size() < 50) {
                spike.markSplit();
                double x = spike.getCenterX();
                double y = spike.getCenterY();
                
                // Create three spikes in different directions - faster speeds
                newSpikes.add(new SplittingSpike(x, y, -280, -150, spike.getDamage()));  // Increased from -200, -100
                newSpikes.add(new SplittingSpike(x, y, 0, -350, spike.getDamage()));     // Increased from 0, -250
                newSpikes.add(new SplittingSpike(x, y, 280, -150, spike.getDamage()));   // Increased from 200, -100
            }
            
            // Check collision with player
            if (!remove && CollisionManager.intersects(spike, player)) {
                applyDamage(spike.getDamage());
                remove = true;
            }
            
            if (remove) {
                iterator.remove();
            }
        }
        
        // Add new spikes after iteration is complete to avoid concurrent modification
        for (SplittingSpike spike : newSpikes) {
            splittingSpikes.add(spike);
        }
    }
    
    private void updateScreenShake(double dt) {
        screenShakeTimer = Math.max(0, screenShakeTimer - dt);
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
        // Muzzle + SMG strip were hardcoded at 0.08s while SMG fire rate is 0.06s — flashes restarted before finishing
        // and felt out of sync with shots. Tie burst length to this shot's cooldown (capped for slow weapons).
        muzzleFlashDuration = Math.min(0.14, Math.max(0.035, shootCooldown * 0.92));

        GunfireGeometry geo = computeGunfireGeometry();
        double angle = geo.aimAngle();
        double originX = geo.muzzleX();
        double originY = geo.muzzleY();

        muzzleFlashTimer = muzzleFlashDuration;
        muzzleFlashX = originX;
        muzzleFlashY = originY;
        muzzleFlashAngle = angle;
        GameContext.audio().playWeaponFire(character);

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
        bossSkills.clear();
        spikeEffects.clear();
        splittingSpikes.clear();
        stageIntroTimer = 4.5;
        stageBossSpawned = false;
        khaiMimicMorphTriggered = false;
        arena.exitMarker().setActive(false);
        arena.exitMarker().setLabel(newIndex == stages.size() - 1 ? "FINAL" : "NEXT");
        
        // Reset batch spawning state
        batchSpawningActive = false;
        totalMobsToSpawn = 0;
        mobsSpawned = 0;

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
        // Initialize batch spawning
        totalMobsToSpawn = stage.enemyCount();
        mobsSpawned = 0;
        batchSpawningActive = true;
        
        // Spawn first batch
        spawnNextBatch(stage);
    }
    
    private void spawnNextBatch(StageDefinition stage) {
        int batchStart = mobsSpawned;
        int batchEnd = Math.min(mobsSpawned + BATCH_SIZE, totalMobsToSpawn);
        
        for (int i = batchStart; i < batchEnd; i++) {
            double x = arena.randomMobSpawnX(random, 42);
            double spawnY = 40 + random.nextDouble() * 75;
            EnemyActor enemy = new EnemyActor(stage.enemyName(), x, spawnY, 42, 54,
                    stage.enemyHealth(), stage.enemyHealth(), stage.enemySpeed(), stage.tint(), false);
            if (!stage.enemySpriteIds().isEmpty()) {
                applyEnemySprite(enemy, stage.enemySpriteIds().get(i % stage.enemySpriteIds().size()));
            }
            enemies.add(enemy);
        }
        
        mobsSpawned = batchEnd;
        
        if (mobsSpawned >= totalMobsToSpawn) {
            batchSpawningActive = false;
        }
        
        setStatus("Wave " + ((mobsSpawned / BATCH_SIZE)) + " / " + ((totalMobsToSpawn + BATCH_SIZE - 1) / BATCH_SIZE) + " spawned!");
    }

    private void spawnBoss(StageDefinition stage) {
        stageBossSpawned = true;
        double x = arena.bossSpawnX(stageIndex == stages.size() - 1);
        
        // Different boss sizes
        double width = 74;
        double height = 96;
        
        if ("Caesar Hunos".equals(stage.bossName())) {
            // Caesar Hunos is 3x bigger
            width = 74 * 3;
            height = 96 * 3;
        } else if ("Khai (Boss Form)".equals(stage.bossName())) {
            // Khai Boss Form is 3x bigger (128x128 sprite scaled up)
            width = 128 * 2.5;
            height = 128 * 2.5;
        } else if ("LAIR Mimic (False Sir Khai)".equals(stage.bossName()) ||
                   "Security Guard".equals(stage.bossName()) ||
                   "Mutated Vendor".equals(stage.bossName())) {
            // Sir Khai, Security Guard, and Mutated Vendor are 2x bigger
            width = 74 * 2;
            height = 96 * 2;
        }
        
        // Spawn boss high above so they fall and land on platforms
        EnemyActor enemy = new EnemyActor(stage.bossName(), x, 50, width, height,
                stage.bossHealth(), stage.bossHealth(), stage.bossSpeed(), stage.tint(), true);
        if (stage.bossSpriteId() != null) {
            applyEnemySprite(enemy, stage.bossSpriteId());
        }
        
        // Mark Caesar Hunos for special behavior
        if ("Caesar Hunos".equals(stage.bossName())) {
            enemy.markAsCaesarHunos();
        } else if ("LAIR Mimic (False Sir Khai)".equals(stage.bossName())) {
            enemy.markAsSirKhai();
        } else if ("Security Guard".equals(stage.bossName())) {
            enemy.markAsSecurityGuard();
        } else if ("Mutated Vendor".equals(stage.bossName())) {
            enemy.markAsVendor();
        } else if ("Khai (Boss Form)".equals(stage.bossName())) {
            enemy.markAsKhaiBossForm();
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
                // 2304×640 → 18×5 grid of 128×128 (96×128 mis-tiles the sheet and scrambles frames).
                sheet = assets.sheet(spriteId, 128, 128);
                idle = new AnimationStrip(0, 0, 8, 5);
                walk = new AnimationStrip(1, 0, 8, 7);
                attack = new AnimationStrip(2, 0, 12, 10);
                enemy.setSpriteSheet(sheet, idle, walk, attack);
                // Set dash animation (row 1 for dash)
                AnimationStrip dash = new AnimationStrip(1, 0, 8, 12);
                enemy.setDashStrip(dash);
                return;
            }
            case "enemy.vendor" -> {
                sheet = assets.sheet(spriteId, 64, 64);
                idle = new AnimationStrip(0, 0, 12, 5);
                walk = new AnimationStrip(1, 0, 8, 8);
                attack = new AnimationStrip(1, 0, 8, 8);
            }
            case "enemy.caesar_hunos" -> {
                // CaesarHunos_Idle.png is 3000×120 → 25× grid of 120×120 (was incorrectly capped at 8 frames).
                sheet = assets.sheet("enemy.caesar_hunos", 120, 120);
                idle = new AnimationStrip(0, 0, 25, 9);
                walk = idle;
                attack = idle;
            }
            case "enemy.khai_mimic_human" -> {
                // khai_with_zombified.png 256×128 @ 32×32: row 0 has 8 opaque cells; row 1 cols 4–7 are fully empty.
                // Bosses always pick walk — use row 0 for walk so the disguise never samples transparent tiles.
                sheet = assets.sheet("character.sir_khai", 32, 32);
                idle = new AnimationStrip(0, 0, 8, 4);
                walk = new AnimationStrip(0, 0, 8, 5);
                attack = new AnimationStrip(0, 0, 4, 6);
            }
            case "enemy.khai_mimic" -> {
                // khai_boss_mimic 2560×640: 20 cols × 128px × 4 rows × 160px — rows 0 idle, 1 walk, 2 braced, 3 slam.
                sheet = assets.sheet(spriteId, 128, 160);
                idle = new AnimationStrip(0, 0, 12, 3);
                walk = new AnimationStrip(1, 0, 14, 4);
                attack = new AnimationStrip(3, 0, 17, 7);
            }
            case "enemy.khai_boss_form" -> {
                // khai_boss_form.png is 2560×640 → 20×5 grid @ 128×128. Row 0 = idle (14 cells), rows 1–3 = stomp / scream / throw.
                sheet = assets.sheet(spriteId, 128, 128);
                AnimationStrip idleLoop = khaiBossIdleLoopStrip();
                idle = idleLoop;
                walk = idleLoop;
                attack = idleLoop;
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
        // Check if we need to spawn the next batch of mobs
        if (batchSpawningActive && mobsSpawned < totalMobsToSpawn) {
            spawnNextBatch(stage);
            return;
        }
        
        if (stage.hasBoss() && !stageBossSpawned) {
            spawnBoss(stage);
            stageIntroTimer = 2.8;
            setStatus(stage.bossName() + " has appeared.");
            return;
        }

        if (stageIndex == stages.size() - 1) {
            finished = true;
            victory = true;
            runTimer.stop();
            completedRun = new LeaderboardEntry(playerName, runTimer.getElapsedMillis(), Instant.now());
            LeaderboardManager.get().submitEntryAsync(completedRun, null);
            setStatus(getStageClearMessage(stage));
            return;
        }

        arena.exitMarker().setActive(true);
        arena.exitMarker().setLabel("NEXT");
        setStatus(getStageClearMessage(stage));
    }

    private String getStageClearMessage(StageDefinition stage) {
        if (stageIndex == stages.size() - 1) {
            return "Khai's monstrous form collapses. The nightmare is over.";
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
        
        // Apply screen shake if active
        gc.save();
        if (screenShakeTimer > 0 && !paused) {
            double shakeX = (random.nextDouble() - 0.5) * screenShakeIntensity;
            double shakeY = (random.nextDouble() - 0.5) * screenShakeIntensity;
            gc.translate(shakeX, shakeY);
        }
        
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
        GunfireGeometry gunAim = computeGunfireGeometry();
        visualRenderer.renderPlayerWeapon(player, weapon, finished, victory, gunAim.aimAngle(),
                gunAim.shoulderOnRight(), muzzleFlashTimer, muzzleFlashDuration);
        visualRenderer.renderMuzzleFlash(muzzleFlashTimer, muzzleFlashDuration,
                muzzleFlashX, muzzleFlashY, muzzleFlashAngle);
        projectiles.renderAll(gc);
        bossSkills.renderAll(gc);
        spikeEffects.renderAll(gc);
        splittingSpikes.renderAll(gc);
        
        gc.restore();
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
            // Only show overlay if victory shake hasn't started yet
            // For defeat, always show overlay
            if (!victory || !victoryShakeTriggered) {
                hudRenderer.renderEndOverlay(victory);
            }
        } else if (paused) {
            hudRenderer.renderPauseOverlay();
        }
    }

    /**
     * Shoulder pivot and aim match {@link GameVisualRenderer#renderPlayerWeapon}; muzzle offsets match barrel tips in
     * {@link GameVisualRenderer#renderProceduralWeapon} / SMG sprite placement.
     */
    private GunfireGeometry computeGunfireGeometry() {
        double cx = player.getCenterX();
        double torsoY = player.getY() + player.getHeight() * 0.35;
        double targetX = input.getMouseX() + arena.cameraX();
        double targetY = input.getMouseY();

        double aimAngle;
        boolean aimingRight;
        if (targetX == 0 && targetY == 0) {
            aimAngle = player.getFacing() >= 0 ? 0 : Math.PI;
            aimingRight = Math.cos(aimAngle) >= 0;
        } else {
            double prelim = Math.atan2(targetY - torsoY, targetX - cx);
            aimingRight = Math.cos(prelim) >= 0;
            double sx = cx + (aimingRight ? 10 : -10);
            double sy = player.getY() + player.getHeight() * 0.38;
            aimAngle = Math.atan2(targetY - sy, targetX - sx);
        }

        double shoulderX = cx + (aimingRight ? 10 : -10);
        double shoulderY = player.getY() + player.getHeight() * 0.38;

        WeaponType wt = weapon.getType();
        double mlx = wt.muzzleTipLocalX();
        double mly = wt.muzzleTipLocalY();
        double cos = Math.cos(aimAngle);
        double sin = Math.sin(aimAngle);
        double muzzleX = shoulderX + cos * mlx - sin * mly;
        double muzzleY = shoulderY + sin * mlx + cos * mly;
        return new GunfireGeometry(muzzleX, muzzleY, aimAngle, aimingRight);
    }

    private record GunfireGeometry(double muzzleX, double muzzleY, double aimAngle, boolean shoulderOnRight) {}

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

    private void exitToMainMenu() {
        GameContext.audio().stopBackgroundMusic();
        loop.stop();
        GameContext.showMainMenu();
    }

    private boolean isPauseMenuButtonHit() {
        double buttonX = W / 2.0 - PAUSE_MENU_BUTTON_W / 2.0;
        double buttonY = H / 2.0 + 10;
        return input.getMouseX() >= buttonX
                && input.getMouseX() <= buttonX + PAUSE_MENU_BUTTON_W
                && input.getMouseY() >= buttonY
                && input.getMouseY() <= buttonY + PAUSE_MENU_BUTTON_H;
    }

    public Scene getScene() {
        return scene;
    }
}

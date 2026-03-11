package org.example.scenes;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.example.Main;
import org.example.engines.CollisionManager;
import org.example.engines.GameLoop;
import org.example.engines.InputHandler;
import org.example.gameplay.EnemyActor;
import org.example.gameplay.EntityManager;
import org.example.gameplay.PlatformTile;
import org.example.gameplay.PlayerActor;
import org.example.gameplay.Projectile;
import org.example.gameplay.StageDefinition;
import org.example.player.CharacterType;
import org.example.weapons.Weapon;

import java.util.List;
import java.util.Random;

/**
 * Gameplay scene kept intentionally small.
 *
 * OOP usage in this scene:
 *   - Encapsulation: gameplay state is hidden behind actor classes
 *   - Inheritance: PlayerActor / EnemyActor / Projectile extend GameObject
 *   - Abstraction: GameObject and GameLoop define shared contracts
 *   - Polymorphism: EntityManager renders concrete entities through the base type
 *   - Generics: EntityManager<T extends GameObject>
 */
public class GameScene {

    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;
    private static final double GROUND_Y = 620;
    private static final double GRAVITY = 1500;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final InputHandler input = new InputHandler();
    private final Random random = new Random();

    private final CharacterType character;
    private final Weapon weapon;
    private final PlayerActor player;
    private final EntityManager<EnemyActor> enemies = new EntityManager<>();
    private final EntityManager<Projectile> projectiles = new EntityManager<>();
    private final List<PlatformTile> platforms = List.of(
            new PlatformTile(170, 520, 220, 16),
            new PlatformTile(520, 430, 240, 16),
            new PlatformTile(900, 340, 180, 16)
    );
    private final List<StageDefinition> stages = List.of(
            new StageDefinition("Awakening Wing", "Clear the infected classrooms.",
                    "The synchronized survivors wake inside a school that is no longer human.",
                    5, 60, 72, Color.color(0.18, 0.45, 0.18), false, ""),
            new StageDefinition("Laboratory Block", "Survive the toxic mutations.",
                    "LAIR thickens in the labs. The air feels heavier with every breath.",
                    6, 72, 82, Color.color(0.25, 0.5, 0.16), false, ""),
            new StageDefinition("Silent Library", "Hunt the stalkers between the shelves.",
                    "It does not only infect. It observes, imitates, and waits.",
                    6, 78, 88, Color.color(0.36, 0.36, 0.38), false, ""),
            new StageDefinition("Gymnasium Nest", "Break through the brute swarm.",
                    "The gym became a nest. Something large kept feeding here.",
                    7, 92, 98, Color.color(0.5, 0.28, 0.12), false, ""),
            new StageDefinition("Main Building Core", "Defeat Caesar, the primary host.",
                    "\"You should have left me outside.\"",
                    1, 450, 86, Color.color(0.68, 0.16, 0.16), true, "Caesar Hunos"),
            new StageDefinition("Outer Grounds", "Defeat the false Sir Khai.",
                    "The jacket by the tree is torn and soaked in blood. The cure was bait.",
                    1, 520, 102, Color.color(0.58, 0.72, 0.2), true, "False Sir Khai")
    );

    private final GameLoop loop;

    private int stageIndex = 0;
    private int ammo;
    private int hp;
    private double mana;
    private final int maxHp;
    private final int maxMana;

    private double shootCooldown = 0;
    private double reloadTimer = 0;
    private boolean reloading = false;
    private double stageIntroTimer = 4.0;
    private double stageAdvanceTimer = -1;
    private double statusTimer = 0;
    private String statusText = "";

    private double logicBuffTimer = 0;
    private double dashTimer = 0;
    private double invulnerableTimer = 0;
    private int shield = 0;
    private int overclockShots = 0;

    private boolean finished = false;
    private boolean victory = false;

    public GameScene(CharacterType character, Weapon weapon) {
        this.character = character;
        this.weapon = weapon;
        this.player = new PlayerActor(120, GROUND_Y - 58, 42, 58);
        this.player.setAuraColor(weapon.getProjectileColor());
        this.maxHp = character.getHealth();
        this.hp = maxHp;
        this.maxMana = character.getMana();
        this.mana = maxMana;
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

    private void updateGame(double dt) {
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

        shootCooldown = Math.max(0, shootCooldown - dt);
        reloadTimer = Math.max(0, reloadTimer - dt);
        stageIntroTimer = Math.max(0, stageIntroTimer - dt);
        stageAdvanceTimer = Math.max(-1, stageAdvanceTimer - dt);
        statusTimer = Math.max(0, statusTimer - dt);
        logicBuffTimer = Math.max(0, logicBuffTimer - dt);
        dashTimer = Math.max(0, dashTimer - dt);
        invulnerableTimer = Math.max(0, invulnerableTimer - dt);
        mana = Math.min(maxMana, mana + dt * 8);

        if (reloading && reloadTimer == 0) {
            ammo = weapon.getMagazineSize();
            reloading = false;
            setStatus("Reload complete.");
        }

        if (stageAdvanceTimer == 0) {
            if (stageIndex == stages.size() - 1) {
                finished = true;
                victory = true;
            } else {
                startStage(stageIndex + 1);
            }
            stageAdvanceTimer = -1;
        }

        handlePlayerInput();
        updatePlayerPhysics(dt);
        updateProjectiles(dt);
        updateEnemies(dt, stage);

        if (hp <= 0) {
            finished = true;
            victory = false;
        }

        if (!finished && enemies.isEmpty() && stageAdvanceTimer < 0) {
            stageAdvanceTimer = 2.6;
            setStatus(stageIndex == stages.size() - 1 ? "The imitation collapses." : "Sector cleared.");
        }

        input.endFrame();
    }

    private void handlePlayerInput() {
        double moveSpeed = character.getFinalSpeed(weapon);
        if (dashTimer > 0) {
            moveSpeed *= 2.3;
        }

        player.setVx(0);
        if (input.isDown(KeyCode.A)) {
            player.setVx(-moveSpeed);
            player.setFacing(-1);
        }
        if (input.isDown(KeyCode.D)) {
            player.setVx(moveSpeed);
            player.setFacing(1);
        }

        if (input.isJustPressed(KeyCode.SPACE) && player.isOnGround()) {
            player.setVy(character.getFinalJump(weapon));
            player.setOnGround(false);
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

    private void updateEnemies(double dt, StageDefinition stage) {
        for (EnemyActor enemy : enemies) {
            enemy.setAttackCooldown(Math.max(0, enemy.getAttackCooldown() - dt));
            enemy.chase(player, dt, 20, W - enemy.getWidth() - 20);

            if (enemy.isBoss() && random.nextDouble() < dt * 0.7) {
                enemy.setY(GROUND_Y - enemy.getHeight() - random.nextInt(10));
            }

            if (enemy.getAttackCooldown() <= 0 && CollisionManager.intersects(enemy, player)) {
                applyDamage(stage.boss() ? 22 : 12);
                enemy.setAttackCooldown(stage.boss() ? 0.8 : 1.1);
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

        double originX = player.getCenterX();
        double originY = player.getY() + player.getHeight() * 0.35;
        double angle = Math.atan2(input.getMouseY() - originY, input.getMouseX() - originX);

        int projectileCount = weapon.getPelletsPerShot();
        for (int i = 0; i < projectileCount; i++) {
            double spread = (random.nextDouble() - 0.5) * weapon.getSpread();
            double shotAngle = angle + spread;
            int damage = weapon.getDamage() + getCurrentBasicAttack();
            if (overclockShots > 0) {
                damage = (int) Math.round(damage * 1.4);
            }

            projectiles.add(new Projectile(
                    originX,
                    originY,
                    Math.cos(shotAngle) * weapon.getProjectileSpeed(),
                    Math.sin(shotAngle) * weapon.getProjectileSpeed(),
                    damage,
                    weapon.getProjectileColor()
            ));
        }

        if (overclockShots > 0) {
            overclockShots--;
        }
    }

    private void activateAbility() {
        switch (character) {
            case JOSEPH_JIMENEZ -> {
                if (mana >= 30 && logicBuffTimer <= 0) {
                    mana -= 30;
                    logicBuffTimer = 5;
                    setStatus("Adrenal Lock activated.");
                }
            }
            case IBEN_ANOOS -> {
                if (mana >= 20 && dashTimer <= 0) {
                    mana -= 20;
                    dashTimer = 0.18;
                    invulnerableTimer = 0.7;
                    setStatus("Phase Dash triggered.");
                }
            }
            case ILDE_JAN_FIGUERAS -> {
                if (mana >= 20) {
                    mana -= 20;
                    shield += 40;
                    setStatus("Barrier Pulse absorbed 40 damage.");
                }
            }
            case GAILE_AMOLONG -> {
                if (mana >= 30 && overclockShots == 0) {
                    mana -= 30;
                    overclockShots = 3;
                    setStatus("Overclock primed the next 3 shots.");
                }
            }
            case JAMUEL_BACUS -> {
                if (mana >= 20) {
                    mana -= 20;
                    hp = Math.min(maxHp, hp + 15);
                    setStatus("Reserve Conversion restored 15 HP.");
                }
            }
        }
    }

    private int getCurrentBasicAttack() {
        int logic = character.getLogic() + (logicBuffTimer > 0 ? 20 : 0);
        return (int) Math.round(logic * 0.35);
    }

    private void applyDamage(int damage) {
        if (invulnerableTimer > 0) {
            return;
        }

        if (shield > 0) {
            int absorbed = Math.min(shield, damage);
            shield -= absorbed;
            damage -= absorbed;
        }

        hp -= damage;
        invulnerableTimer = 0.25;
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
        stageAdvanceTimer = -1;

        StageDefinition stage = stages.get(stageIndex);
        if (stage.boss()) {
            double x = stageIndex == 4 ? 920 : 980;
            enemies.add(new EnemyActor(stage.bossName(), x, GROUND_Y - 96, 74, 96,
                    stage.enemyHealth(), stage.enemyHealth(), stage.enemySpeed(), stage.tint(), true));
        } else {
            for (int i = 0; i < stage.enemyCount(); i++) {
                double x = 620 + i * 80;
                enemies.add(new EnemyActor("Infected", x, GROUND_Y - 54, 42, 54,
                        stage.enemyHealth(), stage.enemyHealth(), stage.enemySpeed(), stage.tint(), false));
            }
        }
    }

    private void renderGame() {
        StageDefinition stage = stages.get(stageIndex);

        renderBackground(stage);
        for (PlatformTile platform : platforms) {
            platform.render(gc);
        }
        player.render(gc);
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
        gc.setFill(Color.color(0.035, 0.04, 0.05));
        gc.fillRect(0, 0, W, H);

        gc.setFill(Color.color(stage.tint().getRed() * 0.35, stage.tint().getGreen() * 0.35,
                stage.tint().getBlue() * 0.35, 0.18));
        gc.fillOval(-140, -120, 520, 320);
        gc.fillOval(W - 360, -80, 440, 280);

        gc.setFill(Color.color(0.08, 0.1, 0.12));
        gc.fillRect(90, 130, W - 180, 300);

        gc.setFill(Color.color(stage.tint().getRed(), stage.tint().getGreen(), stage.tint().getBlue(), 0.22));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 10; col++) {
                gc.fillRect(130 + col * 100, 160 + row * 70, 48, 28);
            }
        }

        gc.setStroke(Color.color(stage.tint().getRed(), stage.tint().getGreen(), stage.tint().getBlue(), 0.12));
        gc.setLineWidth(2);
        for (int i = 0; i < 7; i++) {
            double sx = 40 + i * 190;
            gc.strokeLine(sx, 0, sx + 120, H);
        }

        gc.setFill(Color.color(0.06, 0.08, 0.07));
        gc.fillRect(0, GROUND_Y, W, H - GROUND_Y);
    }

    private void renderHud(StageDefinition stage) {
        double panelY = 18;
        double panelH = 148;

        gc.setFill(Color.color(0.03, 0.04, 0.05, 0.92));
        gc.fillRoundRect(20, panelY, 430, panelH, 12, 12);
        gc.fillRoundRect(W - 380, panelY, 360, panelH, 12, 12);

        gc.setStroke(Color.color(0.2, 0.65, 0.28, 0.45));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(20, panelY, 430, panelH, 12, 12);
        gc.strokeRoundRect(W - 380, panelY, 360, panelH, 12, 12);

        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        gc.setFill(Color.WHITE);
        gc.fillText(character.name, 34, 46);

        gc.setFont(Font.font("Georgia", FontPosture.ITALIC, 13));
        gc.setFill(Color.color(0.68, 0.72, 0.7));
        gc.fillText(character.title + "  •  " + weapon.getName(), 34, 68);
        gc.fillText("Chapter: " + stage.name(), 34, 90);
        gc.fillText("Objective: " + stage.objective(), 34, 112);
        gc.fillText("Q Skill: " + getActiveSkillName(), 34, 134);

        drawBar(34, 120, 180, 12, hp / (double) maxHp, Color.color(0.88, 0.2, 0.2));
        drawBar(226, 120, 180, 12, mana / maxMana, Color.color(0.22, 0.7, 0.92));

        gc.setFont(Font.font("Courier New", 12));
        gc.setFill(Color.WHITE);
        gc.fillText("HP " + hp + "/" + maxHp, 34, 145);
        gc.fillText("Mana " + (int) mana + "/" + maxMana, 226, 145);

        gc.setFont(Font.font("Courier New", 12));
        gc.setFill(Color.WHITE);
        gc.fillText("Logic " + character.getLogic(), W - 360, 48);
        gc.fillText("Basic " + getCurrentBasicAttack(), W - 360, 70);
        gc.fillText("Ammo " + ammo + "/" + weapon.getMagazineSize(), W - 360, 92);
        gc.fillText(reloadTimer > 0 ? "Reload " + String.format("%.1fs", reloadTimer) : "Reload ready", W - 360, 114);
        gc.fillText("Controls: A D SPACE  |  Mouse  |  Q  |  R", W - 360, 136);

        if (logicBuffTimer > 0 || shield > 0 || overclockShots > 0) {
            gc.setFill(Color.color(0.15, 0.9, 0.4));
            String buffLine = "Buffs:";
            if (logicBuffTimer > 0) buffLine += " Logic+20";
            if (shield > 0) buffLine += " Shield " + shield;
            if (overclockShots > 0) buffLine += " Overclock x" + overclockShots;
            gc.fillText(buffLine, W - 360, 156);
        }
    }

    private void renderStageIntro(StageDefinition stage) {
        gc.setFill(Color.color(0, 0, 0, 0.54));
        gc.fillRect(0, 0, W, H);

        gc.setFill(Color.color(0.04, 0.05, 0.06, 0.94));
        gc.fillRoundRect(200, 210, W - 400, 180, 16, 16);
        gc.setStroke(Color.color(stage.tint().getRed(), stage.tint().getGreen(), stage.tint().getBlue(), 0.8));
        gc.setLineWidth(2);
        gc.strokeRoundRect(200, 210, W - 400, 180, 16, 16);

        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        gc.setFill(Color.WHITE);
        gc.fillText(stage.name(), W / 2.0 - textWidth(stage.name(), 32) / 2, 265);

        gc.setFont(Font.font("Georgia", FontPosture.ITALIC, 18));
        gc.setFill(Color.color(0.7, 0.75, 0.72));
        gc.fillText(stage.objective(), W / 2.0 - textWidth(stage.objective(), 18) / 2, 304);

        gc.setFont(Font.font("Georgia", 15));
        gc.setFill(Color.color(0.86, 0.86, 0.86));
        wrapText(stage.description(), 240, 338, W - 480, 24);
    }

    private void renderStatusBanner() {
        gc.setFill(Color.color(0.02, 0.03, 0.04, 0.88));
        gc.fillRoundRect(W / 2.0 - 180, H - 94, 360, 42, 12, 12);
        gc.setStroke(Color.color(0.18, 0.8, 0.32, 0.65));
        gc.strokeRoundRect(W / 2.0 - 180, H - 94, 360, 42, 12, 12);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        gc.setFill(Color.color(0.9, 0.95, 0.9));
        gc.fillText(statusText, W / 2.0 - textWidth(statusText, 16) / 2, H - 66);
    }

    private void renderEndOverlay() {
        gc.setFill(Color.color(0, 0, 0, 0.76));
        gc.fillRect(0, 0, W, H);

        gc.setFill(Color.color(0.04, 0.05, 0.06, 0.96));
        gc.fillRoundRect(120, 120, W - 240, H - 240, 18, 18);
        gc.setStroke(Color.color(0.18, 0.82, 0.34, 0.7));
        gc.setLineWidth(2);
        gc.strokeRoundRect(120, 120, W - 240, H - 240, 18, 18);

        String title = victory ? "THE LAIR" : "Synchronization Failed";
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 38));
        gc.setFill(Color.WHITE);
        gc.fillText(title, W / 2.0 - textWidth(title, 38) / 2, 190);

        gc.setFont(Font.font("Georgia", FontPosture.ITALIC, 18));
        gc.setFill(Color.color(0.75, 0.8, 0.76));
        String subtitle = victory
                ? "The cure was never a cure. Sir Khai was never Sir Khai."
                : "The campus falls silent as LAIR keeps learning.";
        gc.fillText(subtitle, W / 2.0 - textWidth(subtitle, 18) / 2, 230);

        gc.setFont(Font.font("Georgia", 16));
        gc.setFill(Color.color(0.9, 0.9, 0.9));
        if (victory) {
            wrapText("Caesar drops the stabilized vial, but the real twist waits outside. "
                    + "Sir Khai's torn jacket lies in the grass. The thing guiding you was LAIR wearing his face. "
                    + "It learned trust first, then hunger. The final mimic falls, but the infection is still out there.",
                    170, 290, W - 340, 28);
        } else {
            wrapText("You were close, but the synchronized aura failed before the school could be reclaimed. "
                    + "Caesar remains the first host, and the false Sir Khai keeps feeding the campus to LAIR.",
                    170, 300, W - 340, 28);
        }

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        gc.setFill(Color.color(0.18, 0.85, 0.32));
        String prompt = "Press ENTER or SPACE to return to character select";
        gc.fillText(prompt, W / 2.0 - textWidth(prompt, 14) / 2, H - 150);
    }

    private void drawBar(double x, double y, double width, double height, double fill, Color color) {
        gc.setFill(Color.color(0.12, 0.14, 0.16));
        gc.fillRoundRect(x, y, width, height, 6, 6);
        gc.setFill(color);
        gc.fillRoundRect(x, y, width * clamp(fill, 0, 1), height, 6, 6);
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

    private String getActiveSkillName() {
        return switch (character) {
            case JOSEPH_JIMENEZ -> character.getSkillThree();
            case IBEN_ANOOS -> character.getSkillOne();
            case ILDE_JAN_FIGUERAS -> character.getSkillOne();
            case GAILE_AMOLONG -> character.getSkillThree();
            case JAMUEL_BACUS -> character.getSkillThree();
        };
    }

    private void setStatus(String message) {
        statusText = message;
        statusTimer = 1.8;
    }

    private void exitToCharacterSelect() {
        loop.stop();
        Main.setScene(new CharacterSelectScene().getScene());
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

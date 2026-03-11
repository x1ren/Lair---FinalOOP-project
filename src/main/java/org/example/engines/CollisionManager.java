package org.example.engines;

import org.example.gameplay.GameObject;
import org.example.gameplay.Projectile;

/**
 * Small collision utility shared by gameplay classes.
 */
public final class CollisionManager {

    private CollisionManager() {
    }

    public static boolean intersects(GameObject a, GameObject b) {
        return a.getX() < b.getX() + b.getWidth()
                && a.getX() + a.getWidth() > b.getX()
                && a.getY() < b.getY() + b.getHeight()
                && a.getY() + a.getHeight() > b.getY();
    }

    public static boolean circleHitsRect(Projectile projectile, GameObject target) {
        double nearestX = clamp(projectile.getDrawX(), target.getX(), target.getX() + target.getWidth());
        double nearestY = clamp(projectile.getDrawY(), target.getY(), target.getY() + target.getHeight());
        double dx = projectile.getDrawX() - nearestX;
        double dy = projectile.getDrawY() - nearestY;
        return dx * dx + dy * dy <= projectile.getRadius() * projectile.getRadius();
    }

    public static boolean landsOnTop(GameObject actor, GameObject surface, double previousBottom) {
        return actor.getX() + actor.getWidth() > surface.getX()
                && actor.getX() < surface.getX() + surface.getWidth()
                && actor.getY() + actor.getHeight() >= surface.getY()
                && previousBottom <= surface.getY();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

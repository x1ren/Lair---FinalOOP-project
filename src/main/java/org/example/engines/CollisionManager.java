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
        // Check if there's horizontal overlap between actor and surface
        boolean horizontalOverlap = actor.getX() + actor.getWidth() > surface.getX()
                && actor.getX() < surface.getX() + surface.getWidth();
        
        if (!horizontalOverlap) {
            return false;
        }
        
        // Current bottom position of the actor
        double currentBottom = actor.getY() + actor.getHeight();
        
        // Actor should land if:
        // 1. They were above or at the surface in the previous frame
        // 2. They are now at or below the surface
        // This catches actors falling onto platforms from above
        if (previousBottom <= surface.getY() && currentBottom >= surface.getY()) {
            return true;
        }
        
        // Also land if actor is already very close to being on the surface
        // This handles the case where actor is standing on the platform
        // Allow a small penetration tolerance
        if (Math.abs(currentBottom - surface.getY()) <= 2) {
            return true;
        }
        
        return false;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

package bot.botimplementations;

import bot.heuristics.*;
import datastorage.Terrain;

public class BotFactory {
    public enum BotImplementations {
        RULE,
        RANDOM,
        HILL_CLIMBING,
        GRADIENT_DESCENT,
        PARTICLE_SWARM,
    }

    static Terrain terrain;

    public static void setTerrain(Terrain setTerrain) {
        terrain = setTerrain;
    }

    public static IBot getBot(BotImplementations implementation) {
        if (implementation == BotImplementations.PARTICLE_SWARM) {
            return new ParticleSwarmBot(new FinalAStarDistanceHeuristic(terrain), 0.5, 0.5, 0.5, 100, 10);
        }
        if (implementation == BotImplementations.RULE) {
            return new RuleBasedBot();
        }
        if (implementation == BotImplementations.HILL_CLIMBING) {
            //return new HillClimbingBot(new FinalClosestEuclidianDistanceHeuristic(), 0.01, 4,
            //        getBot(BotImplementations.PARTICLE_SWARM));
            return new AdaptiveHillClimbingBot(
                    new FinalAStarDistanceHeuristic(terrain),
                    4,
                    0.01,
                    2,
                    4,
                    null
            );
        }
        if (implementation == BotImplementations.GRADIENT_DESCENT) {
            return new GradientDescentBot(new FinalClosestEuclidianDistanceHeuristic(), 0.01,
                    getBot(BotImplementations.PARTICLE_SWARM));
        }
        return new RandomBot(new FinalEuclidianDistanceHeuristic(), 1000);
    }
}

package reader;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import bot.botimplementations.IBot;
import bot.botimplementations.BotFactory;
import datastorage.*;
import datastorage.obstacles.IObstacle;
import datastorage.obstacles.ObstacleBox;
import datastorage.obstacles.ObstacleTree;
import datastorage.obstacles.ObstacleWall;
import physics.collisionsystems.*;
import physics.*;
import physics.solvers.*;
import physics.stoppingconditions.*;
import utility.UtilityClass;
import utility.math.Vector2;

public class GameStateLoader {
   public static String OS;

   private static final String delimiter = ";";
   private static Scanner scanner;

   // region private Variables
   // Singular values
   private static double solverStep;

   private static IODESolver ODEsolver;
   private static IStoppingCondition stoppingCondition;
   private static ICollisionSystem collisionSystem;

   private static Vector2 terrainBottomRight;
   private static Vector2 terrainTopLeft;
   private static double greenKineticFriction;
   private static double greenStaticFriction;
   private static String terrainFunction;

   private static Vector2 ballStartPoint;
   private static double ballRadius;

   private static double targetRadius;
   private static Vector2 targetPosition;

   // ArrayList values
   private static ArrayList<Vector2> sandZoneBottomLeftCorner = new ArrayList<Vector2>();
   private static ArrayList<Vector2> sandZoneTopRightCorner = new ArrayList<Vector2>();
   private static ArrayList<Double> sandKineticFriction = new ArrayList<Double>();
   private static ArrayList<Double> sandStaticFriction = new ArrayList<Double>();

   private static ArrayList<Vector2> treePosition = new ArrayList<Vector2>();
   private static ArrayList<Double> treeRadius = new ArrayList<Double>();
   private static ArrayList<Double> treeBounciness = new ArrayList<Double>();

   private static ArrayList<Vector2> boxBottomLeftCorner = new ArrayList<Vector2>();
   private static ArrayList<Vector2> boxTopRightCorner = new ArrayList<Vector2>();
   private static ArrayList<Double> boxBounciness = new ArrayList<Double>();
   
   private static ArrayList<Vector2> wallFirstPosition = new ArrayList<Vector2>();
   private static ArrayList<Vector2> wallSecondPosition = new ArrayList<Vector2>();
   private static ArrayList<Double> wallBounciness = new ArrayList<Double>();

   private static Terrain terrain; // The generated terrain that will be returned
   // endregion

   // region default variable values
   private final static double defsolverStep = 0.01;

   private final static IODESolver defODEsolver = new RungeKutta4Solver(defsolverStep);
   private final static IStoppingCondition defstoppingCondition = new SmallVelocityStoppingCondition();
   private final static ICollisionSystem defcollisionSystem = new BounceCollisionSystem();

   private final static Vector2 defterrainTopLeft = new Vector2(-50, -50);
   private final static Vector2 defterrainBottomRight = new Vector2(50, 50);
   private final static double defgreenKineticFriction = 0.05;
   private final static double defgreenStaticFriction = 0.1;
   private final static String defterrainFunction = "sin(x+y)"; // ask Niko for his implementation and leave it as a
                                                                // String for now

   private final static Vector2 defballStartPoint = Vector2.zeroVector();

   private final static double deftargetRadius = 0.1;
   private final static Vector2 deftargetPosition = new Vector2(4, 4);

   // private static final IBot defbot = BotFactory.getBot(BotFactory.BotImplementations.GRADIENT_DESCENT);
   private static final IBot defbot = null;

   // ArrayList values
   private final static Vector2 defsandZoneTopRightCorner = new Vector2(-5, -10);
   private final static Vector2 defsandZoneBottomLeftCorner = new Vector2(-10, -5);
   private final static double defsandKineticFriction = 0.25;
   private final static double defsandStaticFriction = 0.4;

   private final static Vector2 deftreePosition = new Vector2(1,1);
   private final static double deftreeRadius = 0.5;
   private final static double deftreeBounciness = 1;

   private final static Vector2 defboxTopRightCorner = new Vector2(5, 0);
   private final static Vector2 defboxBottomLeftCorner = new Vector2(0, 5);
   private final static double defBoxBounciness = 1;
   
   private final static Vector2 defwallFirst = new Vector2(1, 1);
   private final static Vector2 defwallSecond = new Vector2(3, 3);
   private final static double defWallBounciness = 1;

   public static GameState readFile() {
      createScanner();

      resetVariables();
      String[] allLinesSplit = splitLines();
      readVariables(allLinesSplit);

      GameState gameState = createGameStateUsingGeneratedData();
      BotFactory.setTerrain(gameState.getTerrain());
      return gameState;
   }

   private static void resetVariables() {
      solverStep = 0;
      ODEsolver = null;
      stoppingCondition = null;
      collisionSystem = null;

      terrainBottomRight = null;
      terrainTopLeft = null;

      greenKineticFriction = 0;
      greenStaticFriction = 0;
      terrainFunction = null;

      ballStartPoint = Vector2.zeroVector();

      targetRadius = 0;
      targetPosition = new Vector2(4, 4);

      sandZoneBottomLeftCorner = new ArrayList<>();
      sandZoneTopRightCorner = new ArrayList<>();
      sandKineticFriction = new ArrayList<>();
      sandStaticFriction = new ArrayList<>();

      treePosition = new ArrayList<>();
      treeBounciness = new ArrayList<>();
      treeRadius = new ArrayList<>();

      boxBottomLeftCorner = new ArrayList<>();
      boxTopRightCorner = new ArrayList<>();
      boxBounciness = new ArrayList<>();
   }

   private static GameState createGameStateUsingGeneratedData() {
      Terrain generatedTerrain = createTerrain();
      Ball startingBall = createBall();
      PhysicsEngine engine = createEngine();

      GameState gameState = new GameState(generatedTerrain, startingBall, engine);
      return gameState;
   }

   /**
    * Tries to create a buffered reader
    * 
    * @return true, if the reader has been successfully created
    */
   private static void createScanner() {
      try {
         scanner = new Scanner(
               new FileReader(getPath()));
      } catch (FileNotFoundException e) {
         throw new NullPointerException("File not found - the path to the save file is wrong, see comment above");
      } catch (NullPointerException e) {
         throw new NullPointerException("The path to the save file itself was null");
      }
   }

   private static String getPath() {
      // The top line does not work on my computer, so I put the one at the bottom -
      // comment it out and switch.
      // I was not able to come up with a line of code that would work on everyone's
      // computer
      String dir = System.getProperty("user.dir");
      String separator = System.getProperty("file.separator");
      OS = System.getProperty("os.name");

      boolean stansLaptop = dir.contains("Phase 1");
      if (stansLaptop) {
         return dir + "\\src\\reader\\UserInput.csv";
      }
      if (OS.contains("Windows")) {
         return dir + separator + "Phase 1" + separator + "src" + separator + "reader" + separator + "UserInput_Best.csv";
      } else {
         return dir + separator + "Phase 1" + separator + "src" + separator + "reader" + separator + "UserInput_Best.csv";
      }
   }

   private static String[] splitLines() {
      String wholeLine = "";
      while (scanner.hasNextLine()) {
         wholeLine += scanner.nextLine();
      }
      scanner.close();
      return wholeLine.split(delimiter);
   }

   private static void readVariables(String[] allLinesSplit) {
      for (String line : allLinesSplit) {
         checkLineForVariables(line);
      }
   }

   private static void checkLineForVariables(String line) {
      // Physics engine
      if (lineContainsKeywordAndEqualSign(line, "solverStep")) {
         solverStep = readDouble(line);
      }
      if (lineContainsKeywordAndEqualSign(line, "ODEsolver")) {
         ODEsolver = readSolver(line);
      }
      if (lineContainsKeywordAndEqualSign(line, "stoppingCondition")) {
         stoppingCondition = readStoppingCondition(line);
      }
      if (lineContainsKeywordAndEqualSign(line, "collisionSystem")) {
         collisionSystem = readCollisionSystem(line);
      }
      // Green
      if (lineContainsKeywordAndEqualSign(line, "terrainBottomRight")) {
         terrainBottomRight = readPoint(line);
      }
      if (lineContainsKeywordAndEqualSign(line, "terrainTopLeft")) {
         terrainTopLeft = readPoint(line);
      }
      if (lineContainsKeywordAndEqualSign(line, "greenKineticFriction")) {
         double friction = readDouble(line);
         friction = UtilityClass.clamp(friction, 0.01, 1);
         greenKineticFriction = friction;
      }
      if (lineContainsKeywordAndEqualSign(line, "greenStaticFriction")) {
         double friction = readDouble(line);
         friction = UtilityClass.clamp(friction, 0.01, 1);
         greenStaticFriction = friction;
      }
      if (lineContainsKeywordAndEqualSign(line, "terrainFunction")) {
         terrainFunction = readString(line);
      }
      // Ball
      if (lineContainsKeywordAndEqualSign(line, "ballStartPoint")) {
         ballStartPoint = readPoint(line);
      }
      if (lineContainsKeywordAndEqualSign(line, "ballRadius")) {
         ballRadius = readDouble(line);
      }
      // Target
      if (lineContainsKeywordAndEqualSign(line, "targetRadius")) {
         targetRadius = readDouble(line);
      }
      if (lineContainsKeywordAndEqualSign(line, "targetPosition")) {
         targetPosition = readPoint(line);
      }
      // Sand zone
      if (lineContainsKeywordAndEqualSign(line, "sandZoneBottomLeft")) {
         sandZoneBottomLeftCorner.add(readPoint(line));
      }
      if (lineContainsKeywordAndEqualSign(line, "sandZoneTopRight")) {
         sandZoneTopRightCorner.add(readPoint(line));
      }
      if (lineContainsKeywordAndEqualSign(line, "sandKineticFriction")) {
         double friction = readDouble(line);
         friction = UtilityClass.clamp(friction, 0.01, 1);
         sandKineticFriction.add(friction);
      }
      if (lineContainsKeywordAndEqualSign(line, "sandStaticFriction")) {
         double friction = readDouble(line);
         friction = UtilityClass.clamp(friction, 0.01, 1);
         sandStaticFriction.add(friction);
      }
      // Tree
      if (lineContainsKeywordAndEqualSign(line, "treePosition")) {
         treePosition.add(readPoint(line));
      }
      if (lineContainsKeywordAndEqualSign(line, "treeRadius")) {
         treeRadius.add(readDouble(line));
      }
      if (lineContainsKeywordAndEqualSign(line, "treeBounciness")) {
         double bounciness = readDouble(line);
         bounciness = UtilityClass.clamp(bounciness, 0.01, 2);
         treeBounciness.add(bounciness);
      }
      // Box
      if (lineContainsKeywordAndEqualSign(line, "boxBottomLeft")) {
         boxBottomLeftCorner.add(readPoint(line));
      }
      if (lineContainsKeywordAndEqualSign(line, "boxTopRight")) {
         boxTopRightCorner.add(readPoint(line));
      }
      if (lineContainsKeywordAndEqualSign(line, "boxBounciness")) {
         double bounciness = readDouble(line);
         bounciness = UtilityClass.clamp(bounciness, 0.01, 2);
         boxBounciness.add(bounciness);
      }
      // Wall
      if (lineContainsKeywordAndEqualSign(line, "wallFirst")) {
         wallFirstPosition.add(readPoint(line));
      }
      if (lineContainsKeywordAndEqualSign(line, "wallSecond")) {
         wallSecondPosition.add(readPoint(line));
      }
      if (lineContainsKeywordAndEqualSign(line, "wallBounciness")) {
         double bounciness = readDouble(line);
         bounciness = UtilityClass.clamp(bounciness, 0.01, 2);
         wallBounciness.add(bounciness);
      }
   }

   private static boolean lineContainsKeywordAndEqualSign(String line, String keyword) {
      return line.contains(keyword) && line.contains("=");
   }

   // region Read Objects
   private static IODESolver readSolver(String line) {
      try {
         String name = line.substring(line.lastIndexOf("=") + 1);
         return getSolverFromName(name);
      } catch (IndexOutOfBoundsException e) {
         System.out.println("There was nothing after the = sign");
         return defODEsolver;
      }
   }

   private static IODESolver getSolverFromName(String name) {
      if (solverStep == 0) {
         solverStep = defsolverStep;
      }

      if (name.contains("Euler")) {
         return new EulerSolver(solverStep);
      }
      if (name.contains("RK2")) {
         return new RungeKutta2Solver(solverStep);
      }
      if (name.contains("RK4")) {
         return new RungeKutta4Solver(solverStep);
      } else {
         return defODEsolver;
      }
   }

   private static IStoppingCondition readStoppingCondition(String line) {
      try {
         String name = line.substring(line.lastIndexOf("=") + 1);
         return getStoppingConditionFromName(name);
      } catch (IndexOutOfBoundsException e) {
         System.out.println("There was nothing after the = sign");
         return defstoppingCondition;
      }
   }

   private static IStoppingCondition getStoppingConditionFromName(String name) {
      if (name.contains("smallV")) {
         return new SmallVelocityStoppingCondition();
      } else {
         return defstoppingCondition;
      }
   }

   private static ICollisionSystem readCollisionSystem(String line) {
      try {
         String name = line.substring(line.lastIndexOf("=") + 1);
         return getCollisionSystemFromName(name);
      } catch (IndexOutOfBoundsException e) {
         System.out.println("There was nothing after the = sign");
         return defcollisionSystem;
      }
   }

   private static ICollisionSystem getCollisionSystemFromName(String name) {
      if (name.contains("bounce")) {
         return new BounceCollisionSystem();
      }
      if (name.contains("stop")) {
         return new StopCollisionSystem();
      } else {
         return defcollisionSystem;
      }
   }

   private static IBot readBotImplementation(String line) {
      try {
         String name = line.substring(line.lastIndexOf("=") + 1);
         return getBotImplementationFromName(name);
      } catch (IndexOutOfBoundsException e) {
         System.out.println("There was nothing after the = sign");
         return defbot;
      }
   }

   private static IBot getBotImplementationFromName(String name) {
      if (name.contains("hillClimbing")) {
         return BotFactory.getBot(BotFactory.BotImplementations.HILL_CLIMBING);
      }
      if (name.contains("particleSwarm")) {
         return BotFactory.getBot(BotFactory.BotImplementations.PARTICLE_SWARM);
      }
      if (name.contains("random")) {
         return BotFactory.getBot(BotFactory.BotImplementations.RANDOM);
      }
      if (name.contains("rule")) {
         return BotFactory.getBot(BotFactory.BotImplementations.RULE);
      }
      if (name.contains("gradientDescent")) {
         return BotFactory.getBot(BotFactory.BotImplementations.GRADIENT_DESCENT);
      } else {
         return defbot;
      }
   }
   // endregion

   // region Read Values
   private static double readDouble(String line) {
      try {
         if (line.contains("=")) {
            String temp = (line.substring(line.lastIndexOf("=") + 1));
            return Double.parseDouble(temp);
         } else {
            return Double.parseDouble(line);
         }
      } catch (NullPointerException e) {
         System.out.println("String after = was null");
         return 0;
      } catch (NumberFormatException e) {
         System.out.println("String after = was not parsable into a double");
         return 0;
      } catch (IndexOutOfBoundsException e) {
         System.out.println("There was nothing after the = sign");
         return 0;
      }
   }

   private static String readString(String line) {
      try {
         return (line.substring(line.lastIndexOf("=") + 1));
      } catch (IndexOutOfBoundsException e) {
         System.out.println("There was nothing after the = sign");
         return null;
      }
   }

   private static double[] readRange(String line) {
      try {
         String temp = line.substring(line.lastIndexOf("=") + 1);
         String[] split = temp.split("<");
         double[] range = new double[2];
         range[0] = readDouble(split[0]);
         range[1] = readDouble(split[1]);
         return range;

      } catch (IndexOutOfBoundsException e) {
         System.out.println("There was nothing after the = sign");
         return null;
      } catch (Exception e) {
         System.out.println("Pattern syntax was invalid");
         return null;
      }
   }

   private static Vector2 readPoint(String line) {
      try {
         String temp = line.substring(line.lastIndexOf("=") + 1);
         String[] split = temp.split(",");
         return new Vector2(readDouble(split[0]), readDouble(split[1]));

      } catch (IndexOutOfBoundsException e) {
         System.out.println("There was nothing after the = sign");
         return null;
      } catch (Exception e) {
         System.out.println("Pattern syntax was invalid");
         return null;
      }
   }
   // endregion

   // region Create Terrain
   private static Terrain createTerrain() {
      terrain = new Terrain();
      // Singular values
      defineGreen();
      defineTarget();
      defineStartingPoint();

      // Multiple values
      defineObstacles();
      defineZones();

      return terrain;
   }

   private static void defineGreen() {
      if (terrainBottomRight == null) {
         terrain.bottomRightCorner = defterrainBottomRight.copy();
      } else {
         terrain.bottomRightCorner = terrainBottomRight;
      }
      if (terrainTopLeft == null) {
         terrain.topLeftCorner = defterrainTopLeft.copy();
      } else {
         terrain.topLeftCorner = terrainTopLeft;
      }

      if (greenKineticFriction == 0) {
         terrain.kineticFriction = defgreenKineticFriction;
      } else {
         terrain.kineticFriction = greenKineticFriction;
      }
      if (greenStaticFriction == 0) {
         terrain.staticFriction = defgreenStaticFriction;
      } else {
         terrain.staticFriction = greenStaticFriction;
      }
      TerrainHeightFunction decodedFunction;
      if (terrainFunction == null) {
         decodedFunction = new TerrainHeightFunction(defterrainFunction);
      } else {
         decodedFunction = new TerrainHeightFunction(terrainFunction);
      }
      terrain.setTerrainFunction(decodedFunction);

   }

   private static void defineTarget() {
      Target target = new Target();
      if (targetRadius == 0) {
         target.radius = deftargetRadius;
      } else {
         target.radius = targetRadius;
      }
      if (targetPosition == null) {
         target.position = deftargetPosition;
      } else {
         target.position = targetPosition;
      }
      terrain.target = target;
   }

   private static void defineStartingPoint() {
      if (ballStartPoint == null) {
         terrain.ballStartingPosition = defballStartPoint;
      } else {
         terrain.ballStartingPosition = ballStartPoint;
      }
   }

   // region Obstacles
   private static void defineObstacles() {
      ArrayList<IObstacle> obstacles = new ArrayList<>();

      obstacles.addAll(createTrees());
      obstacles.addAll(createBoxes());
      obstacles.addAll(createWalls());

      terrain.obstacles = obstacles;
   }

   //region Trees
   private static ArrayList<IObstacle> createTrees() {
      ArrayList<IObstacle> trees = new ArrayList<>();
      while (hasTree()) {
         trees.add(createTree());
      }
      return trees;
   }

   private static boolean hasTree() {
      return treePosition.size() > 0 || treeRadius.size() > 0 || treeBounciness.size() > 0;
   }

   private static IObstacle createTree() {
      ObstacleTree tree = new ObstacleTree();
      Vector2 position = new Vector2();
      if (treePosition.size() > 0) {
         position = treePosition.get(0);
         treePosition.remove(0);
      } else {
         position = deftreePosition;
      }
      if (treeRadius.size() > 0) {
         tree.radius = treeRadius.get(0);
         treeRadius.remove(0);
      } else {
         tree.radius = deftreeRadius;
      }
      if (treeBounciness.size() > 0) {
         tree.bounciness = treeBounciness.get(0);
         treeBounciness.remove(0);
      } else {
         tree.bounciness = deftreeBounciness;
      }
      tree.originPosition = position;
      return tree;
   }

   private static ArrayList<IObstacle> createBoxes() {
      ArrayList<IObstacle> boxes = new ArrayList<>();
      while (hasBox()) {
         boxes.add(createBox());
      }
      return boxes;
   }

   private static boolean hasBox() {
      return boxBottomLeftCorner.size() > 0 || boxTopRightCorner.size() > 0 || boxBounciness.size() > 0;
   }

   private static IObstacle createBox() {
      // Position
      Vector2 bottomLeftCorner = new Vector2();
      Vector2 topRightCorner = new Vector2();
      if (boxBottomLeftCorner.size() > 0) {
         bottomLeftCorner = boxBottomLeftCorner.get(0);
         boxBottomLeftCorner.remove(0);
      } else {
         bottomLeftCorner = defboxBottomLeftCorner.copy();
      }
      if (boxTopRightCorner.size() > 0) {
         topRightCorner = boxTopRightCorner.get(0);
         boxTopRightCorner.remove(0);
      } else {
         topRightCorner = defboxTopRightCorner.copy();
      }
      ObstacleBox box = new ObstacleBox(bottomLeftCorner, topRightCorner);
      // Bounciness
      if (boxBounciness.size() > 0) {
         box.bounciness = boxBounciness.get(0);
         boxBounciness.remove(0);
      } else {
         box.bounciness = defBoxBounciness;
      }

      return box;
   }
   // endregion

   // region Zones
   private static void defineZones() {
      ArrayList<Zone> zones = new ArrayList<>();

      zones.addAll(createZones());

      terrain.zones = zones.toArray(new Zone[0]);
   }

   private static ArrayList<Zone> createZones() {
      ArrayList<Zone> sandZones = new ArrayList<>();
      while (hasSandZone()) {
         sandZones.add(createSandZone());
      }
      return sandZones;
   }

   private static boolean hasSandZone() {
      return sandKineticFriction.size() > 0 || sandStaticFriction.size() > 0 || sandZoneBottomLeftCorner.size() > 0
            || sandZoneTopRightCorner.size() > 0;
   }

   private static Zone createSandZone() {
      // Position
      Vector2 bottomLeftCorner = new Vector2();
      Vector2 topRightCorner = new Vector2();
      if (sandZoneBottomLeftCorner.size() > 0) {
         bottomLeftCorner = sandZoneBottomLeftCorner.get(0);
         sandZoneBottomLeftCorner.remove(0);
      } else {
         bottomLeftCorner = defsandZoneBottomLeftCorner.copy();
      }
      if (sandZoneTopRightCorner.size() > 0) {
         topRightCorner = sandZoneTopRightCorner.get(0);
         sandZoneTopRightCorner.remove(0);
      } else {
         topRightCorner = defsandZoneTopRightCorner.copy();
      }
      Zone zone = new Zone(bottomLeftCorner, topRightCorner);
      // Friction
      if (sandKineticFriction.size() > 0) {
         zone.kineticFriction = sandKineticFriction.get(0);
         sandKineticFriction.remove(0);
      } else {
         zone.kineticFriction = defsandKineticFriction;
      }
      if (sandStaticFriction.size() > 0) {
         zone.staticFriction = sandStaticFriction.get(0);
         sandStaticFriction.remove(0);
      } else {
         zone.staticFriction = defsandStaticFriction;
      }
      return zone;
   }
   // endregion
   
   //region Walls
   private static ArrayList<IObstacle> createWalls() {
      ArrayList<IObstacle> walls = new ArrayList<>();
      while (hasWall()) {
         walls.add(createWall());
      }
      return walls;
   }

   private static boolean hasWall() {
      return wallFirstPosition.size() > 0 || wallSecondPosition.size() > 0 || wallBounciness.size() > 0;
   }

   private static IObstacle createWall() {
      // Position
      Vector2 firstPosition = new Vector2();
      Vector2 secondPosition = new Vector2();
      if (wallFirstPosition.size() > 0) {
         firstPosition = wallFirstPosition.get(0);
         wallFirstPosition.remove(0);
      } else {
         firstPosition = defwallFirst.copy();
      }
      if (wallSecondPosition.size() > 0) {
         secondPosition = wallSecondPosition.get(0);
         wallSecondPosition.remove(0);
      } else {
         secondPosition = defwallSecond.copy();
      }
      ObstacleWall wall = new ObstacleWall(firstPosition, secondPosition);
      // Bounciness
      if (wallBounciness.size() > 0) {
         wall.setBounciness(wallBounciness.get(0));
         wallBounciness.remove(0);
      } else {
         wall.setBounciness(defWallBounciness);
      }

      return wall;
   }
   // endregion
   // endregion

   // region Create Engine
   private static PhysicsEngine createEngine() {
      IODESolver savedSolver;
      IStoppingCondition savedCondition;
      ICollisionSystem savedCollisionSystem;

      if (ODEsolver == null) {
         savedSolver = defODEsolver;
      } else {
         savedSolver = ODEsolver;
      }
      if (stoppingCondition == null) {
         savedCondition = defstoppingCondition;
      } else {
         savedCondition = stoppingCondition;
      }
      if (collisionSystem == null) {
         savedCollisionSystem = defcollisionSystem;
      } else {
         savedCollisionSystem = collisionSystem;
      }

      return new PhysicsEngine2(savedSolver, savedCondition, savedCollisionSystem);
   }

   private static Ball createBall() {
      Ball newBall = new Ball(terrain.ballStartingPosition, Vector2.zeroVector());
      newBall.radius = ballRadius;
      return newBall;
   }
   // endregion
}
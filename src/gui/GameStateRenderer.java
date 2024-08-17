package gui;

import datastorage.*;
import datastorage.obstacles.*;
import visualization.gameengine.Camera;
import visualization.gameengine.Game;
import utility.UtilityClass;
import utility.math.*;
import utility.math.Rectangle;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class GameStateRenderer {
    /**
     * Only contains the green. Displays the entire map.
     */
    private final GameState gameState;
    private final Terrain terrain;
    public static final int PIXELS_PER_GAME_UNIT = 40;
    public static final double MAX_ARROW_LENGTH = 5;
    /**
     * Contains the green with all obstacles, zones and the target added. Displays
     * the entire map.
     */
    private BufferedImage STATIC_TERRAIN_IMAGE;
    private boolean displayMouseCoordinates = false;

    public GameStateRenderer(GameState gameState) {
        this.gameState = gameState;
        this.terrain = gameState.getTerrain();
        updateTerrain();
    }

    // region Startup
    public void updateTerrain() {
        STATIC_TERRAIN_IMAGE = getGreenWithObstacles();
    }

    private BufferedImage getGreenWithObstacles() {
        BufferedImage imageOfGreenWithObstacles = getImageOfGreen();
        Graphics2D g2 = (Graphics2D) imageOfGreenWithObstacles.getGraphics();

        drawTarget(g2, getTerrainHeightRange());
        drawObstacles(g2);
        return imageOfGreenWithObstacles;
    }

    // region getGreenWithObstacles helper methods
    private void drawTarget(Graphics2D g2, double heightRange) {
        double radius = terrain.target.radius;
        drawCircle(g2, terrain.target.position, radius, Color.BLACK, false);

    }

    private void drawObstacles(Graphics2D g2) {
        Color treeColor = new Color(28, 140, 3);
        Color treeOutlineColor = new Color(16, 87, 3);
        Color boxColor = new Color(125, 111, 79);
        Color boxOutlineColor = new Color(79, 70, 51);
        Color wallColor = new Color(20, 90, 151);

        for (IObstacle o : terrain.obstacles) {
            // Trees
            if (o instanceof ObstacleTree) {
                ObstacleTree t = (ObstacleTree) o;
                drawCircle(g2, t.originPosition, t.radius, treeColor, true);
                drawCircle(g2, t.originPosition, t.radius, treeOutlineColor, false);
            } else if (o instanceof ObstacleBox) {
                ObstacleBox b = (ObstacleBox) o;
                drawRectangle(g2, b, boxColor, true);
                drawRectangle(g2, b, boxOutlineColor, false);
            } else if (o instanceof ObstacleWall) {
                ObstacleWall w = (ObstacleWall) o;
                drawLine(g2, w, wallColor, (float) w.getWallThickness());
            }
        }
    }
    // endregion

    public BufferedImage getImageOfGreen() {
        BufferedImage imageOfGreen = new BufferedImage(getTerrainWidthInPixels(), getTerrainHeightInPixels(),
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = (Graphics2D) imageOfGreen.getGraphics();
        // Number of units in colored space
        // Clear screen
        Color brownBackground = new Color(75, 47, 26);
        g2.setColor(brownBackground);
        g2.fillRect(0, 0, imageOfGreen.getWidth(), imageOfGreen.getHeight());
        // Render the terrain
        int numVertices = (int) Math.sqrt(terrain.heightmap.length);
        double xStep = (terrain.bottomRightCorner.x - terrain.topLeftCorner.x) / numVertices;
        double yStep = (terrain.bottomRightCorner.y - terrain.topLeftCorner.y) / numVertices;
        // Find the max and min height in the terrain
        double[] minMax = getMaxAndMinTerrainHeight();
        double totalMinHeight = minMax[0];
        double totalMaxHeight = minMax[1];

        // Make sure that the deepest part isn't pitch black
        totalMinHeight -= 1.5;
        // Find the rendering coordinates
        for (int yy = 0; yy < numVertices - 1; yy++) {
            for (int xx = 0; xx < numVertices - 1; xx++) {
                // First point
                double x1 = terrain.topLeftCorner.x + xx * xStep;
                double y1 = terrain.topLeftCorner.y + yy * yStep;
                double h1 = UtilityClass.clamp(terrain.getTerrainFunction().valueAt(x1, y1), -10, 10);
                // Second point
                double x2 = terrain.topLeftCorner.x + (xx + 1) * xStep;
                double y2 = terrain.topLeftCorner.y + yy * yStep;
                double h2 = UtilityClass.clamp(terrain.getTerrainFunction().valueAt(x2, y2), -10, 10);
                // Third point
                double x3 = terrain.topLeftCorner.x + xx * xStep;
                double y3 = terrain.topLeftCorner.y + (yy + 1) * yStep;
                double h3 = UtilityClass.clamp(terrain.getTerrainFunction().valueAt(x3, y3), -10, 10);

                // Fourth point
                double x4 = terrain.topLeftCorner.x + (xx + 1) * xStep;
                double y4 = terrain.topLeftCorner.y + (yy + 1) * yStep;
                double h4 = UtilityClass.clamp(terrain.getTerrainFunction().valueAt(x4, y4), -10, 10);

                double heightRange = getTerrainHeightRange();
                double maxHeight = UtilityClass.getMaxValue(new double[] { h1, h2, h3, h4 });

                int renderPixelX1 = (int) ((x1 - terrain.topLeftCorner.x) * PIXELS_PER_GAME_UNIT);
                int renderPixelY1 = (int) ((y1 - h1 - terrain.topLeftCorner.y + heightRange / 2)
                        * PIXELS_PER_GAME_UNIT);

                int renderPixelX2 = (int) ((x2 - terrain.topLeftCorner.x) * PIXELS_PER_GAME_UNIT);
                int renderPixelY2 = (int) ((y2 - h2 - terrain.topLeftCorner.y + heightRange / 2)
                        * PIXELS_PER_GAME_UNIT);

                int renderPixelX3 = (int) ((x3 - terrain.topLeftCorner.x) * PIXELS_PER_GAME_UNIT);
                int renderPixelY3 = (int) ((y3 - h3 - terrain.topLeftCorner.y + heightRange / 2)
                        * PIXELS_PER_GAME_UNIT);

                int renderPixelX4 = (int) ((x4 - terrain.topLeftCorner.x) * PIXELS_PER_GAME_UNIT);
                int renderPixelY4 = (int) ((y4 - h4 - terrain.topLeftCorner.y + heightRange / 2)
                        * PIXELS_PER_GAME_UNIT);

                double lightLevel = (maxHeight - totalMinHeight) / (totalMaxHeight - totalMinHeight);

                Square square = new Square();
                square.lightLevel = lightLevel;
                square.maxHeight = maxHeight;
                square.squarePosition = new Vector2(x1, y1);

                square.pixel1X = renderPixelX1;
                square.pixel1Y = renderPixelY1;
                square.pixel2X = renderPixelX2;
                square.pixel2Y = renderPixelY2;
                square.pixel3X = renderPixelX3;
                square.pixel3Y = renderPixelY3;
                square.pixel4X = renderPixelX4;
                square.pixel4Y = renderPixelY4;

                drawSquare(g2, square);
            }
        }
        return imageOfGreen;
    }

    // region getImageOfGreen helper methods
    private void drawSquare(Graphics2D g2, Square square) {
        g2.setColor(getSquareColor(square));
        g2.fillPolygon(new int[] { square.pixel1X, square.pixel2X, square.pixel4X, square.pixel3X },
                new int[] { square.pixel1Y, square.pixel2Y, square.pixel4Y, square.pixel3Y }, 4);
    }

    private Color getSquareColor(Square square) {
        Vector2 squarePosition = square.squarePosition;

        if (square.maxHeight < 0) {
            return calculateWaterColor(square.lightLevel);
        }
        if (terrain.isPointInZone(squarePosition.x, squarePosition.y)) {
            return calculateSandColor(square);
        }
        return calculateGrassColor(square);
    }

    private Color calculateSandColor(Square square) {
        Vector2 squarePosition = square.squarePosition;
        double lightLevel = square.lightLevel;

        if (isPixelTinted(squarePosition.x, squarePosition.y)) {
            return new Color((int) (150 * lightLevel), (int) (150 * lightLevel), 0);
        } else {
            return new Color((int) (100 * lightLevel), (int) (100 * lightLevel), 0);
        }
    }

    private Color calculateGrassColor(Square square) {
        Vector2 squarePosition = square.squarePosition;
        double lightLevel = square.lightLevel;

        if (isPixelTinted(squarePosition.x, squarePosition.y)) {
            return new Color(0, (int) (200 * lightLevel), 0);
        } else {
            return new Color(0, (int) (180 * lightLevel), 0);
        }
    }

    private Color calculateWaterColor(double lightLevel) {
        return new Color((int) (34 * lightLevel), (int) (124 * lightLevel), (int) (176 * lightLevel));
    }

    private boolean isPixelTinted(double xPos, double yPos) {
        // The lenfth of one of the sides of a tinted square in game units
        int TINTED_SQUARE_SIZE = 4;

        int numBlockX = (int) ((xPos - terrain.topLeftCorner.x) / TINTED_SQUARE_SIZE);
        int numBlockY = (int) ((yPos - terrain.topLeftCorner.y) / TINTED_SQUARE_SIZE);
        return (numBlockX + numBlockY) % 2 == 0;
    }

    private double[] getMaxAndMinTerrainHeight() {
        // First value is min height, second is max height
        double[] minMax = new double[2];

        int numVertices = (int) Math.sqrt(terrain.heightmap.length);
        double xStep = (terrain.bottomRightCorner.x - terrain.topLeftCorner.x) / numVertices;
        double yStep = (terrain.bottomRightCorner.y - terrain.topLeftCorner.y) / numVertices;
        // Find the max and min height in the terrain
        double totalMaxHeight = -10;
        double totalMinHeight = 10;
        for (int yy = 0; yy < numVertices; yy++) {
            for (int xx = 0; xx < numVertices; xx++) {
                double x = terrain.topLeftCorner.x + xx * xStep;
                double y = terrain.topLeftCorner.y + yy * yStep;
                double h = terrain.getTerrainFunction().valueAt(x, y);
                if (h > 10) {
                    h = 10;
                }
                if (h < -10) {
                    h = -10;
                }
                if (h > totalMaxHeight) {
                    totalMaxHeight = h;
                }
                if (h < totalMinHeight) {
                    totalMinHeight = h;
                }
            }
        }
        minMax[0] = totalMinHeight;
        minMax[1] = totalMaxHeight;
        return minMax;
    }
    // endregion
    // endregion

    /**
     * Returns a round picture that is a circle
     * 
     * @param camera
     * @return
     */
    public BufferedImage getMinimap(Camera camera) {
        BufferedImage subimage = getSubimage(camera, false, false);

        int radius = Math.min(subimage.getWidth(), subimage.getHeight());
        BufferedImage circleBuffer = new BufferedImage(radius, radius, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circleBuffer.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, radius, radius));
        g2.drawImage(subimage, 0, 0, radius, radius, null);

        subimage.flush();
        return circleBuffer;
    }

    public BufferedImage getSubimage(Camera camera, boolean drawArrow, boolean drawText) {
        BufferedImage image = getEmptyTerrainImage(camera);
        fillImageWithBrown(image, camera);
        Graphics2D g2 = (Graphics2D) image.getGraphics();

        renderCroppedImage(g2, camera);

        renderChangingElements(g2, camera, drawArrow, drawText);
        return image;
    }

    // region getSubimage helper methods
    public BufferedImage getEmptyTerrainImage(Camera camera) {
        return new BufferedImage((int) (camera.WIDTH * PIXELS_PER_GAME_UNIT),
                (int) (camera.HEIGHT * PIXELS_PER_GAME_UNIT),
                BufferedImage.TYPE_4BYTE_ABGR);
    }

    private void fillImageWithBrown(BufferedImage image, Camera camera) {
        Graphics2D imageG2 = (Graphics2D) image.getGraphics();
        // Fill the given part of the Graphics2D object in brown
        imageG2.setColor(new Color(75, 47, 26));
        imageG2.fillRect(0, 0, (int) (camera.WIDTH * PIXELS_PER_GAME_UNIT),
                (int) (camera.HEIGHT * PIXELS_PER_GAME_UNIT));
    }

    private void renderCroppedImage(Graphics2D g2, Camera camera) {
        BufferedImage subImage = cropImage(STATIC_TERRAIN_IMAGE, calculateVisibleTerrainArea(camera,
                STATIC_TERRAIN_IMAGE));
        g2.drawImage(subImage, null, 0, 0);
        subImage.flush();
    }

    private BufferedImage cropImage(BufferedImage image, Canvas visibleTerrainArea) {
        return image.getSubimage(visibleTerrainArea.topLeftX,
                visibleTerrainArea.topLeftY, visibleTerrainArea.width, visibleTerrainArea.height);
    }

    /**
     * Renders everything that changes from frame to frame.
     * Currently: ball, flag, input direction arrow and text
     */
    private void renderChangingElements(Graphics2D g2, Camera camera, boolean drawArrow, boolean drawText) {
        if (drawArrow) {
            Vector2 ballPosition = getBallPositionOnScreen();
            renderArrow(g2, camera, ballPosition, ballPosition.translated(getMousePositionOnScreen()));
        }
        renderBallAndFlag(g2, camera);
        if (drawText) {
            drawText(g2);
        }
        g2.dispose();
    }

    private void drawText(Graphics2D g2) {
        setupFont(g2);
        drawNumberOfShots(g2);
        drawBallPosition(g2);
        if (displayMouseCoordinates) {
            drawMousePosition(g2);
        }
    }

    private void setupFont(Graphics2D g2) {
        final int FONT_SIZE = 20;
        g2.setFont(new Font("TimesRoman", Font.BOLD, FONT_SIZE));
        g2.setColor(Color.WHITE);
    }

    private void drawNumberOfShots(Graphics2D g2) {
        g2.drawString("Shots: " + Game.game.getNumShots(), PIXELS_PER_GAME_UNIT, PIXELS_PER_GAME_UNIT);
    }

    private void drawBallPosition(Graphics2D g2) {
        BigDecimal xx = new BigDecimal(gameState.getBall().state.position.x);
        xx = xx.setScale(5, RoundingMode.HALF_UP);
        BigDecimal yy = new BigDecimal(gameState.getBall().state.position.y);
        yy = yy.setScale(5, RoundingMode.HALF_UP);
        g2.drawString("x = " + xx, PIXELS_PER_GAME_UNIT, 2 * PIXELS_PER_GAME_UNIT);
        g2.drawString("y = " + yy, PIXELS_PER_GAME_UNIT, 3 * PIXELS_PER_GAME_UNIT);
    }

    private void drawMousePosition(Graphics2D g2) {
        Vector2 mousePosition = Game.getMiddleMousePosition().translated(gameState.getBall().state.position);
        BigDecimal xx = new BigDecimal(mousePosition.x);
        xx = xx.setScale(5, RoundingMode.HALF_UP);
        BigDecimal yy = new BigDecimal(mousePosition.y);
        yy = yy.setScale(5, RoundingMode.HALF_UP);

        g2.drawString("x = " + xx, PIXELS_PER_GAME_UNIT, 4 * PIXELS_PER_GAME_UNIT);
        g2.drawString("y = " + yy, PIXELS_PER_GAME_UNIT, 5 * PIXELS_PER_GAME_UNIT);
    }

    private Canvas calculateVisibleTerrainArea(Camera camera, BufferedImage image) {
        // Render the terrain
        double camTLx = camera.xPos - camera.WIDTH / 2;
        double camTLy = camera.yPos - camera.HEIGHT / 2;
        double camBRx = camera.xPos + camera.WIDTH / 2;
        double camBRy = camera.yPos + camera.HEIGHT / 2;

        int xTL = -1, yTL = -1, xBR = -1, yBR = -1;

        double heightRange = terrain.maxVal - terrain.minVal;

        // Check if the terrain is on-screen and find the range of the drawing part
        if (camTLx < terrain.bottomRightCorner.x && camTLy < terrain.bottomRightCorner.y
                &&
                camBRx > terrain.topLeftCorner.x && camBRy > terrain.topLeftCorner.y) {

            if (camTLx < terrain.topLeftCorner.x) {
                xTL = 0;
            } else {
                // Calculate x top left
                xTL = (int) ((camTLx - terrain.topLeftCorner.x) * PIXELS_PER_GAME_UNIT);
            }

            if (camTLy < terrain.topLeftCorner.y - heightRange / 2) {
                yTL = 0;
            } else {
                // Calculate y top left
                yTL = (int) ((camTLy - (terrain.topLeftCorner.y - heightRange / 2)) * PIXELS_PER_GAME_UNIT);
            }

            if (camBRx > terrain.bottomRightCorner.x) {
                xBR = STATIC_TERRAIN_IMAGE.getWidth();
            } else {
                // Calculate x bottom right
                xBR = (int) ((camBRx - terrain.topLeftCorner.x) * PIXELS_PER_GAME_UNIT);
            }

            if (camBRy > terrain.bottomRightCorner.y + heightRange / 2) {
                yBR = STATIC_TERRAIN_IMAGE.getHeight();
            } else {
                // Calculate y bottom right
                yBR = (int) ((camBRy - (terrain.topLeftCorner.y - heightRange / 2)) * PIXELS_PER_GAME_UNIT);
            }
        }

        Canvas visibleTerrainArea = new Canvas(image);
        visibleTerrainArea.topLeftX = xTL;
        visibleTerrainArea.topLeftY = yTL;
        visibleTerrainArea.width = xBR - xTL;
        visibleTerrainArea.height = yBR - yTL;

        // visibleTerrainArea.clampCanvas();

        return visibleTerrainArea;
    }

    private void renderBallAndFlag(Graphics2D g2, Camera camera) {
        boolean ballIsBelowFlag = terrain.target.position.y > gameState.getBall().state.position.y;
        // boolean ballIsBelowFlag = true;
        if (ballIsBelowFlag) {
            renderBall(g2, camera);
            renderFlag(g2, camera);
        } else {
            renderFlag(g2, camera);
            renderBall(g2, camera);
        }
    }

    private void renderFlag(Graphics2D g2, Camera camera) {
        Target target = gameState.getTerrain().target;
        double z = gameState.getTerrain().getTerrainFunction().valueAt(target.position.x, target.position.y);
        int x = (int) ((target.position.x - camera.xPos + camera.WIDTH / 2) * PIXELS_PER_GAME_UNIT);
        int y = (int) ((target.position.y - z - camera.yPos + camera.HEIGHT / 2 - 2) * PIXELS_PER_GAME_UNIT);
        g2.setColor(Color.WHITE);
        g2.drawLine(x, y, x, y + 2 * PIXELS_PER_GAME_UNIT);
        g2.setColor(Color.RED);
        g2.fillPolygon(new int[] { x, x + PIXELS_PER_GAME_UNIT / 2, x },
                new int[] { y, y + PIXELS_PER_GAME_UNIT / 4, y + PIXELS_PER_GAME_UNIT / 2 }, 3);
    }

    private void renderBall(Graphics2D g2, Camera camera) {
        Ball ball = gameState.getBall();
        double z = ball.state.getZCoordinate(terrain);
        int x = (int) ((ball.state.position.x - camera.xPos + camera.WIDTH / 2 - ball.radius) * PIXELS_PER_GAME_UNIT);
        int y = (int) ((ball.state.position.y - z - camera.yPos + camera.HEIGHT / 2 - ball.radius)
                * PIXELS_PER_GAME_UNIT);
        g2.setColor(Color.WHITE);
        g2.fillArc(x, y, (int) (ball.radius * PIXELS_PER_GAME_UNIT * 2), (int) (ball.radius * PIXELS_PER_GAME_UNIT * 2),
                0,
                360);
    }

    // region Render Arrow
    private Vector2 getBallPositionOnScreen() {
        double ballZOffset = gameState.getBall().state.getZCoordinate(gameState.getTerrain());
        Vector2 ballPosition = gameState.getBall().state.position.translated(0, -ballZOffset);
        return ballPosition;
    }

    private Vector2 getMousePositionOnScreen() {
        Vector2 mousePositionOnScreen = Game.getMiddleMousePosition();
        if (mousePositionOnScreen.length() > MAX_ARROW_LENGTH) {
            mousePositionOnScreen.normalize().scale(MAX_ARROW_LENGTH);
        }
        return mousePositionOnScreen;
    }

    /**
     * Documentation
     * https://drive.google.com/file/d/16GReA5Fd6xL5yreBKhzWaXLGMchfEYjl/view?usp=sharing
     * 
     * @param g2
     * @param camera
     * @param startingPos the position given in game units where the arrow starts
     * @param targetPos   the position given in game units where the arrow ends
     */
    private void renderArrow(Graphics2D g2, Camera camera, Vector2 startingPos, Vector2 targetPos) {
        // The maximum length of the arrow in game units. This is the maximum distance
        // to the mouse cursor that will affect the arrow
        double arrowLength = startingPos.distanceTo(targetPos);
        double arrowBaseWidth = arrowLength * 0.05d; // In game units
        double triangleSideSize = arrowLength * 0.35d; // In game units

        Polygon arrowBase = getArrowBase(camera, startingPos.copy(), targetPos.copy(), arrowBaseWidth,
                triangleSideSize);
        Polygon arrowTriangle = getArrowTriangle(camera, startingPos.copy(), targetPos.copy(), triangleSideSize);
        // Draw the colored shape
        g2.setColor(getArrowColor(startingPos, targetPos, MAX_ARROW_LENGTH));
        g2.fillPolygon(arrowBase);
        g2.fillPolygon(arrowTriangle);
        // Make a black outline
        g2.setColor(Color.BLACK);
        g2.drawPolygon(arrowBase);
        g2.drawPolygon(arrowTriangle);
    }

    private Polygon getArrowBase(Camera camera, Vector2 startPos, Vector2 targetPos, double arrowBaseWidth,
            double triangleSideSize) {
        translatePositionsByCamera(camera, startPos);
        translatePositionsByCamera(camera, targetPos);

        InfLine2D arrowLine = new InfLine2D(startPos, targetPos);
        InfLine2D startingPosLine = arrowLine.getPerpendicularLineAtPoint(startPos);
        Vector2 arrowBaseEndPos = calculateArrowBaseEndPosition(startPos, targetPos, triangleSideSize);
        InfLine2D targetPosLine = arrowLine.getPerpendicularLineAtPoint(arrowBaseEndPos);

        Vector2 startingPosLineDirection = startingPosLine.getDirectionVector().normalize().scale(arrowBaseWidth);
        Vector2 targetPosLineDirection = targetPosLine.getDirectionVector().normalize().scale(arrowBaseWidth);

        // Make the direction line always point to the right from the arrow line
        if (startPos.y > targetPos.y) {
            startingPosLineDirection.reverse();
            targetPosLineDirection.reverse();
        }

        ArrowBase arrowBase = new ArrowBase();
        arrowBase.startRightPosition = gameUnitToPixels(startPos.translated(startingPosLineDirection));
        arrowBase.startLeftPosition = gameUnitToPixels(startPos.translated(startingPosLineDirection.reversed()));
        arrowBase.targetRightPosition = gameUnitToPixels(arrowBaseEndPos.translated(targetPosLineDirection));
        arrowBase.targetLeftPosition = gameUnitToPixels(arrowBaseEndPos.translated(targetPosLineDirection.reversed()));
        return new Polygon(arrowBase.getXPoints(), arrowBase.getYPoints(), 4);
    }

    private Polygon getArrowTriangle(Camera camera, Vector2 startPos, Vector2 targetPos, double triangleSideSize) {
        translatePositionsByCamera(camera, startPos);
        translatePositionsByCamera(camera, targetPos);

        InfLine2D arrowLine = new InfLine2D(startPos, targetPos);
        InfLine2D startPosLine = arrowLine.getPerpendicularLineAtPoint(startPos);
        Vector2 arrowBaseEndPos = calculateArrowBaseEndPosition(startPos, targetPos, triangleSideSize);

        Vector2 startPosLineDirection = startPosLine.getDirectionVector().normalize().scale(triangleSideSize / 2);

        // Make the direction line always point to the right from the arrow line
        if (startPos.y >= targetPos.y) {
            startPosLineDirection.reverse();
        }

        ArrowTriangle arrowTriangle = new ArrowTriangle();
        arrowTriangle.rightPosition = gameUnitToPixels(arrowBaseEndPos.translated(startPosLineDirection));
        arrowTriangle.leftPosition = gameUnitToPixels(arrowBaseEndPos.translated(startPosLineDirection.reversed()));
        arrowTriangle.topPosition = gameUnitToPixels(targetPos);
        return new Polygon(arrowTriangle.getXPoints(), arrowTriangle.getYPoints(), 3);
    }

    private void translatePositionsByCamera(Camera camera, Vector2 position) {
        double xOffset = -camera.xPos + camera.WIDTH / 2;
        double yOffset = -camera.yPos + camera.HEIGHT / 2;
        position.translate(xOffset, yOffset);
    }

    private Color getArrowColor(Vector2 startingPos, Vector2 targetPos, double MAX_LENGTH) {
        double arrowLength = startingPos.distanceTo(targetPos);
        Color yellowColor = new Color(247, 238, 52);
        Color redColor = new Color(255, 17, 0);

        double maxLengthPercentage = arrowLength / MAX_LENGTH;
        return UtilityClass.lerp(yellowColor, redColor, maxLengthPercentage);
    }

    private Vector2 calculateArrowBaseEndPosition(Vector2 startPos, Vector2 targetPos, double triangleSideSize) {
        Vector2 deltaPosition = startPos.deltaPositionTo(targetPos);
        Vector2 triangleHeightVector = deltaPosition.modifyLength(-getTriangleHeight(triangleSideSize));
        return startPos.translated(triangleHeightVector);
    }

    private double getTriangleHeight(double triangleSideSize) {
        return triangleSideSize / 2 * Math.sqrt(3);
    }

    private class ArrowBase {
        // The vectors are translated from game units into pixels
        public int[] startRightPosition;
        public int[] startLeftPosition;
        public int[] targetRightPosition;
        public int[] targetLeftPosition;

        public int[] getXPoints() {

            return new int[] { targetLeftPosition[0], targetRightPosition[0], startRightPosition[0],
                    startLeftPosition[0] };
        }

        public int[] getYPoints() {
            return new int[] { targetLeftPosition[1], targetRightPosition[1], startRightPosition[1],
                    startLeftPosition[1] };
        }
    }

    private class ArrowTriangle {
        public int[] leftPosition;
        public int[] rightPosition;
        public int[] topPosition;

        public int[] getXPoints() {
            return new int[] { leftPosition[0], rightPosition[0], topPosition[0] };
        }

        public int[] getYPoints() {
            return new int[] { leftPosition[1], rightPosition[1], topPosition[1] };
        }
    }
    // endregion
    // endregion

    // region Draw shapes
    /**
     * Draws a circle onto the function terrain
     * 
     * @param g2     The Graphics2D object that the function is being drawn to
     * @param x      The x coordinate of the center of the circle
     * @param y      The y coordinate of the center of the circle
     * @param radius The radius of the circle
     * @param color  The color of the circle
     * @param filled Whether it's a filled circle or just an outline
     */
    private void drawCircle(Graphics2D g2, Vector2 position, double radius, Color color, boolean filled) {
        double heightRange = terrain.maxVal - terrain.minVal;
        int[] xPoints = new int[361];
        int[] yPoints = new int[361];
        g2.setColor(color);
        int firstPointX = -1, firstPointY = -1;
        // Loop through 360 degrees and add the points
        for (int deg = 0; deg <= 360; deg++) {
            double xx = position.x + radius * Math.cos(deg / (2 * Math.PI));
            double yy = position.y + radius * Math.sin(deg / (2 * Math.PI));
            double h = terrain.getTerrainFunction().valueAt(xx, yy);
            if (h > 10) {
                h = 10;
            }
            if (h < -10) {
                h = -10;
            }
            int renderX = (int) ((xx - terrain.topLeftCorner.x) * PIXELS_PER_GAME_UNIT);
            int renderY = (int) ((yy - h - terrain.topLeftCorner.y + heightRange / 2) * PIXELS_PER_GAME_UNIT);
            xPoints[deg] = renderX;
            yPoints[deg] = renderY;
            if (firstPointX == -1) {
                firstPointX = renderX;
                firstPointY = renderY;
            }
        }
        // Add last point
        xPoints[360] = firstPointX;
        yPoints[360] = firstPointY;
        // Draw the polygon
        if (filled) {
            g2.fillPolygon(xPoints, yPoints, 361);
        } else {
            g2.drawPolygon(xPoints, yPoints, 361);
        }
    }

    // region Rectangle
    private void drawRectangle(Graphics2D g2, Rectangle rectangle, Color color,
            boolean filled) {
        int[] xPoints = new int[4];
        int[] yPoints = new int[4];
        g2.setColor(color);

        fillListsWithRectangleCorners(xPoints, yPoints, rectangle);

        if (filled) {
            g2.fillPolygon(xPoints, yPoints, 4);
        } else {
            g2.drawPolygon(xPoints, yPoints, 4);
        }
    }

    private void fillListsWithRectangleCorners(int[] xPoints, int[] yPoints, Rectangle rectangle) {
        Vector2 bottomLeftCorner = rectangle.bottomLeftCorner;
        Vector2 topRightCorner = rectangle.topRightCorner;

        Vector2 bottomRightCornerTranslated = getPointValue(new Vector2(topRightCorner.x, bottomLeftCorner.y));
        assignTranslatedPoint(xPoints, yPoints, 0, bottomRightCornerTranslated);

        Vector2 bottomLeftCornerTranslated = getPointValue(new Vector2(bottomLeftCorner.x, bottomLeftCorner.y));
        assignTranslatedPoint(xPoints, yPoints, 1, bottomLeftCornerTranslated);

        Vector2 topLeftCornerTranslated = getPointValue(new Vector2(bottomLeftCorner.x, topRightCorner.y));
        assignTranslatedPoint(xPoints, yPoints, 2, topLeftCornerTranslated);

        Vector2 topRightCornerTranslated = getPointValue(new Vector2(topRightCorner.x, topRightCorner.y));
        assignTranslatedPoint(xPoints, yPoints, 3, topRightCornerTranslated);
    }

    private void assignTranslatedPoint(int[] xPoints, int[] yPoints, int index, Vector2 point) {
        xPoints[index] = (int) point.x;
        yPoints[index] = (int) point.y;
    }

    private Vector2 getPointValue(Vector2 point) {
        double heightRange = terrain.maxVal - terrain.minVal;
        double xx = point.x;
        double yy = point.y;
        double h = terrain.getTerrainFunction().valueAt(xx, yy);
        int renderX = (int) ((xx - terrain.topLeftCorner.x) * PIXELS_PER_GAME_UNIT);
        int renderY = (int) ((yy - h - terrain.topLeftCorner.y + heightRange / 2) * PIXELS_PER_GAME_UNIT);
        return new Vector2(renderX, renderY);
    }

    // endregion

    // region Wall
    private void drawLine(Graphics2D g2, Episode episode, Color color, float width) {
        g2.setColor(color);
        Stroke savedStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.draw(getLineToDraw(episode));
        g2.setStroke(savedStroke);
    }

    private Line2D getLineToDraw(Episode episode) {
        Vector2 firstPositionTranslated = getPointValue(episode.firstPosition);

        Vector2 secondPositionTranslated = getPointValue(episode.secondPosition);
        int[] xPoints = { (int) firstPositionTranslated.x, (int) secondPositionTranslated.x };
        int[] yPoints = { (int) firstPositionTranslated.y, (int) secondPositionTranslated.y };
        return new Line2D.Float(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
    }
    // endregion
    // endregion

    // region Translation from game units to pixels
    private int getTerrainWidthInPixels() {
        return (int) (terrain.bottomRightCorner.x - terrain.topLeftCorner.x) * PIXELS_PER_GAME_UNIT;
    }

    private int getTerrainHeightInPixels() {
        return (int) (terrain.bottomRightCorner.y - terrain.topLeftCorner.y) * PIXELS_PER_GAME_UNIT;
    }

    private double getTerrainHeightRange() {
        return terrain.maxVal - terrain.minVal;

    }

    /**
     * @param gameUnitPosition
     * @return array with [xPos, yPos]
     */
    private int[] gameUnitToPixels(Vector2 gameUnitPosition) {
        // Vector2 translatedVector =
        // terrain.topLeftCorner.deltaPositionTo(gameUnitPosition).scale(PIXELS_PER_GAME_UNIT);
        Vector2 translatedVector = gameUnitPosition.scaled(PIXELS_PER_GAME_UNIT);
        int[] pixelsVector = new int[] { (int) translatedVector.x, (int) translatedVector.y };
        return pixelsVector;
    }
    // endregion

    // region Helper classes
    public class Square {
        /**
         * Game units
         */
        public Vector2 squarePosition;

        // Pixel position in pixel units
        public int pixel1X;
        public int pixel1Y;
        public int pixel2X;
        public int pixel2Y;
        public int pixel3X;
        public int pixel3Y;
        public int pixel4X;
        public int pixel4Y;

        /**
         * The highest value of one of the corners
         */
        public double maxHeight;

        public double lightLevel;
    }

    public class Canvas {

        public Canvas(BufferedImage image) {
            this.image = image;
        }

        BufferedImage image;

        public int topLeftX;
        public int topLeftY;
        public int width;
        public int height;

        public void clampCanvas() {
            if (topLeftX < width) {
                topLeftX = width;
            }
            if (topLeftX > image.getWidth() - width) {
                topLeftX = image.getWidth() - width;
            }
            if (topLeftY < height) {
                topLeftY = height;
            }
            if (topLeftY > image.getHeight() - height) {
                topLeftY = image.getHeight() - height;
            }
        }
    }
    // endregion

}

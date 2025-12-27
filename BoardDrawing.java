import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import javax.swing.*;

public class BoardDrawing extends JComponent {
    private GameState state;
    private RoundRectangle2D.Double board;
    private ArrayList<Ellipse2D.Double> holes;
    
    // Modern Color Palette
    private final Color COLOR_BOARD = new Color(44, 62, 80);    // Dark Blue-Grey
    private final Color COLOR_BG = new Color(236, 240, 241);    // Soft Grey-White
    private final Color COLOR_RED = new Color(231, 76, 60);     // Flat Red
    private final Color COLOR_YELLOW = new Color(241, 196, 15); // Flat Yellow
    private final Color COLOR_HOLE = new Color(255, 255, 255);  // White
    
    // Layout Constants
    final int MARGIN_TOP = 100;
    final int MARGIN_LEFT = 50; // Increased margin for centering
    final int HOLE_DIAMETER = 50; // Larger holes
    final int HOLE_GAP = 15;
    final int PADDING = 20;

    // Derived dimensions
    final int BOARD_WIDTH = (HOLE_DIAMETER * 7) + (HOLE_GAP * 6) + (PADDING * 2);
    final int BOARD_HEIGHT = (HOLE_DIAMETER * 6) + (HOLE_GAP * 5) + (PADDING * 2);
    
    public BoardDrawing(GameState gs) {
        state = gs;
        this.setPreferredSize(new Dimension(BOARD_WIDTH + 100, BOARD_HEIGHT + 150)); 
        
        // Define board shape relative to (0,0) of the board coordinates
        // We will translate this into position during paint logic
        // MARGIN_TOP is now internal to the translated context or handled via translation
        
        // Let's treat (0,0) as the top-left of the BOARD itself.
        // So the board rectangle starts at 0,0 locally.
        board = new RoundRectangle2D.Double(0, 0, BOARD_WIDTH, BOARD_HEIGHT, 30, 30);
        
        // Initialize holes positions relative to the board's (0,0)
        holes = new ArrayList<>();
        double holeStartX = PADDING;
        
        for (int i = 0; i < 7; i++) { // Columns
            for (int j = 0; j < 6; j++) { // Rows
                // j=0 is bottom
                double x = holeStartX + i * (HOLE_DIAMETER + HOLE_GAP);
                double y = (BOARD_HEIGHT - PADDING - HOLE_DIAMETER) - (j * (HOLE_DIAMETER + HOLE_GAP));
                
                Ellipse2D.Double hole = new Ellipse2D.Double(x, y, HOLE_DIAMETER, HOLE_DIAMETER);
                holes.add(hole);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2 = (Graphics2D) g;
        
        // Enable Anti-Aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fill Background
        g2.setColor(COLOR_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw Status Message (Centered at top, fixed position relative to panel)
        drawStatusMessage(g2);

        // Calculate centering offset
        int translateX = (getWidth() - BOARD_WIDTH) / 2;
        int translateY = MARGIN_TOP; // Fixed top margin for the board
        
        // Save old transform
        AffineTransform oldTx = g2.getTransform();
        
        // Apply Translation
        g2.translate(translateX, translateY);

        // Draw Board Body with Shadow/Gradient
        drawBoard(g2);

        // Draw Pieces
        drawPieces(g2);
        
        // Draw Row Labels
        drawRowLabels(g2);
        
        // Restore transform for elements that shouldn't be translated (if any future ones)
        // OR just keeping it clean
        g2.setTransform(oldTx);
        
        // Draw Error Message (at bottom, centered in panel)
        if (state.getError() != null) {
            drawErrorMessage(g2, state.getError());
        }
    }

    private void drawStatusMessage(Graphics2D g2) {
        String message = "";
        Color msgColor = Color.DARK_GRAY;

        if (state.getRedWins()) {
            message = "RED WINS!";
            msgColor = COLOR_RED;
        } else if (state.getYellowWins()) {
            message = "YELLOW WINS!";
            msgColor = COLOR_YELLOW;
        } else if (state.getGameOver()) {
            message = "IT'S A TIE!";
            msgColor = Color.BLACK;
        } else if (state.getRedsTurn()) {
            message = "Red's Turn";
            msgColor = COLOR_RED;
        } else {
            message = "Yellow's Turn";
            msgColor = COLOR_YELLOW.darker(); // Darker to be readable on light BG
        }
        
        g2.setFont(new Font("SansSerif", Font.BOLD, 30));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(message);
        
        // Draw centered at top
        g2.setColor(msgColor);
        g2.drawString(message, (getWidth() - w) / 2, 60);
    }

    private void drawBoard(Graphics2D g2) {
        // Shadow for depth
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRoundRect((int)board.getX() + 5, (int)board.getY() + 5, (int)board.getWidth(), (int)board.getHeight(), 30, 30);

        // Gradient for board
        GradientPaint gp = new GradientPaint(
                (float)board.getX(), (float)board.getY(), COLOR_BOARD.brighter(),
                (float)board.getX(), (float)(board.getY() + board.getHeight()), COLOR_BOARD
        );
        g2.setPaint(gp);
        g2.fill(board);

        // Draw empty holes (white)
        g2.setColor(COLOR_HOLE);
        for (Ellipse2D.Double hole : holes) {
            g2.fill(hole);
            // Inner shadow for hole "depth"
            g2.setColor(new Color(0, 0, 0, 30));
            g2.drawOval((int)hole.x, (int)hole.y, (int)hole.width, (int)hole.height);
            g2.setColor(COLOR_HOLE); // reset
        }
    }

    private void drawPieces(Graphics2D g2) {
        Boolean[][] pieces = state.getPieces();
        // iterate mapping to our holes array logic
        // Game state: pieces[col][row] where row=0 is bottom
        
        for (int i = 0; i < 7; i++) { // col
            for (int j = 0; j < 6; j++) { // row
                Boolean isRed = pieces[i][j];
                if (isRed != null) {
                    // Calculate visual position (same logic as hole creation)
                    // We can actually just find the matching hole index
                    // hole index = (i * 6) + j
                    // wait, creating holes:
                    // for i=0..7 { for j=0..6 } -> add
                    // so holes.get(i*6 + j) IS the correct hole
                    
                    Ellipse2D.Double targetHole = holes.get(i * 6 + j);
                    
                    Color baseColor = isRed ? COLOR_RED : COLOR_YELLOW;
                    
                    // Radial Gradient for 3D sphere look
                    Point2D center = new Point2D.Double(targetHole.getCenterX() - 10, targetHole.getCenterY() - 10);
                    float radius = (float) targetHole.width;
                    float[] dist = {0.0f, 1.0f};
                    Color[] colors = {Color.WHITE, baseColor};
                    RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
                    
                    // Or simpler linear gradient
                    GradientPaint gp = new GradientPaint(
                            (float)targetHole.x, (float)targetHole.y, baseColor.brighter(),
                            (float)targetHole.x + (float)targetHole.width, (float)targetHole.y + (float)targetHole.height, baseColor.darker()
                    );
                    
                    g2.setPaint(gp);
                    g2.fill(new Ellipse2D.Double(targetHole.x + 4, targetHole.y + 4, targetHole.width - 8, targetHole.height - 8));
                    
                    // Add a border highlight
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(new Color(255,255,255,100));
                    g2.drawOval((int)targetHole.x + 6, (int)targetHole.y + 6, (int)targetHole.width - 12, (int)targetHole.height - 12);
                }
            }
        }
    }
    
    private void drawRowLabels(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.setColor(Color.GRAY);
        
        // We need the x center of each column (i: 0-6)
        // first hole of col i is at holes.get(i*6)
        
        for(int i = 0; i < 7; i++) {
            Ellipse2D.Double bottomHole = holes.get(i * 6 + 0); // row 0 is bottom
            // Wait, we generate holes j=0 as bottom? 
            // In init: y = (MARGIN_TOP ...) - (j * ...). Yes j=0 is bottom, j=5 is top.
            // Wait, holes list order:
            // i=0 loop: j=0..5 additions.
            // so index = i*6 + j.
            
            double cx = bottomHole.getCenterX();
            double y = board.getMaxY() + 25;
            
            String label = Integer.toString(i + 1);
            int w = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, (int)(cx - w/2), (int)y);
        }
    }

    private void drawErrorMessage(Graphics2D g2, String error) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(new Color(231, 76, 60)); // Red error
        // Draw at bottom
        // Since we restored translation, we can use absolute Y relative to panel
        // Board is at MARGIN_TOP to MARGIN_TOP + BOARD_HEIGHT
        
        int y = MARGIN_TOP + BOARD_HEIGHT + 47;
        int w = g2.getFontMetrics().stringWidth("Oops! " + error);
        g2.drawString("Oops! " + error, (getWidth() - w) / 2, y);
    }
}

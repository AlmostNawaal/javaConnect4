import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Connect4UI {
    
    // Theme Colors (matching BoardDrawing)
    private static final Color COLOR_BG = new Color(236, 240, 241);
    private static final Color COLOR_BTN = new Color(52, 152, 219); // Blue button
    private static final Color COLOR_BTN_TEXT = Color.WHITE;
    private static final Color COLOR_ACCENT = new Color(44, 62, 80);

    public static void main(String[] args) {
        // Run on EDT
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Connect 4 - Modern Edition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(COLOR_BG);
        
        // Main Container Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);
        frame.setContentPane(mainPanel);

        final GameState state = new GameState();
        final BoardDrawing board = new BoardDrawing(state);
        
        // Center the board
        JPanel boardPanel = new JPanel(new GridBagLayout());
        boardPanel.setBackground(COLOR_BG);
        boardPanel.add(board);
        mainPanel.add(boardPanel, BorderLayout.CENTER);

        // Control Panel (Buttons)
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBackground(COLOR_BG);
        controlsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Column Selection Buttons
        JPanel dropButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        dropButtonsPanel.setBackground(COLOR_BG);
        
        for (int i = 1; i <= 7; i++) {
            final int col = i;
            JButton btn = createStyledButton(String.valueOf(i));
            btn.setPreferredSize(new Dimension(50, 40));
            btn.addActionListener(e -> {
                state.move(col);
                board.repaint();
            });
            dropButtonsPanel.add(btn);
        }

        // Game Control Buttons (Undo, Restart)
        JPanel gameActionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        gameActionPanel.setBackground(COLOR_BG);
        gameActionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton btnUndo = createStyledButton("Undo");
        btnUndo.setBackground(new Color(149, 165, 166)); // Grey
        btnUndo.addActionListener(e -> {
            state.undo();
            board.repaint();
        });

        JButton btnRestart = createStyledButton("Restart");
        btnRestart.setBackground(new Color(46, 204, 113)); // Green
        btnRestart.addActionListener(e -> {
            state.restart();
            board.repaint();
        });

        gameActionPanel.add(btnUndo);
        gameActionPanel.add(btnRestart);

        // Add button panels to controls
        controlsPanel.add(dropButtonsPanel);
        controlsPanel.add(gameActionPanel);

        mainPanel.add(controlsPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
    }

    private static JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };
        
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(COLOR_BTN_TEXT);
        btn.setBackground(COLOR_BTN);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Graphics2D;

public class Tests extends JPanel{
    public static final int LENGTH = 1200;
    public static final int HEIGHT = 600;
    public static byte[][] board = new byte [255][255];

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        fillGrid(board, g);
    }
    public Dimension getPreferredSize() {
        return new Dimension(LENGTH, HEIGHT);
    }
    private static void createGUI(Tests mainPanel){
        JFrame frame = new JFrame("CPU Memory");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
    private void fillGrid(byte[][] board, Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                int tileX = (LENGTH / board.length) * j;
                int tileY = (HEIGHT / board.length) * i;
                Color color = new Color(board[i][j] & 0xFF, 0, 0);
                g2d.setColor(color);
                g2d.fillRect(tileX, tileY, LENGTH / 5, HEIGHT / 5);
            }
        }
    }
    public void updateBoard(byte[][] newBoard){
        board = newBoard;
        repaint();
    }
    public void loadBoard(byte[] memory){
        int p = 0;
        for (int i = 0; i < 255; i++){
            for (int j = 0; j < 255; j++){
                board[i][j] = memory[p];
                p++;
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        Tests panel = new Tests();
        byte[] memory = new byte[65536];
        CPU cpu = new CPU(memory);

        // load program
        int[] program = {
                0xA0, 0x00, 0x84, 0x32, 0xB1, 0x30, 0xAA, 0xC8,
                0xCA, 0xB1, 0x30, 0xC8, 0xD1, 0x30, 0x90, 0x10,
                0xF0, 0x0E, 0x48, 0xB1, 0x30, 0x88, 0x91, 0x30,
                0x68, 0xC8, 0x91, 0x30, 0xA9, 0xFF, 0x85, 0x32,
                0xCA, 0xD0, 0xE6, 0x24, 0x32, 0x30, 0xD9, 0x60,
        };
//        int[] program = {0xA2, 0xFF, 0xA0, 0x00, 0x96, 0xFF, 0xC8, 0xD0, 0xFB, 0x00};
        for (int i = 0; i < program.length; i++){
            memory[i] = (byte) program[i];
        }
        memory[0x30] = 0x7D;
        memory[0x31] = 0x00;
        memory[0x7D00 & 0xFFFF] = 8;
        int start = 0x7D00;
        int inc = 255;
        for (int i = 0; i < 8; i++){
            memory[start + i] = (byte) (inc);
            inc -= 31;
        }
        // load board with memory
        panel.loadBoard(memory);

        SwingUtilities.invokeLater(() -> {
            createGUI(panel);
            panel.updateBoard(board); // Update the display with the initial board
        });
        while (true){
            cpu.execute();
            panel.loadBoard(memory);
            panel.updateBoard(board);
            Thread.sleep(100);
            System.out.println("SP: " + (cpu.SP & 0xFF));
        }
    }
}



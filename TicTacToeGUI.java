/**
 * TicTacToeGUI is a graphical user interface implementation of the Tic Tac Toe game.
 * It allows players to play against each other, keeps track of their wins,
 * and provides a menu for starting a new game or quitting.
 * 
 * @author Adam Rebello 101258440
 * @version April 9, 2024
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class TicTacToeGUI {
    private JFrame frame;
    private JButton[][] buttons;
    private JLabel statusLabel;
    private JLabel playerXStatsLabel;
    private JLabel playerOStatsLabel;
    private JMenuBar menuBar;
    private JMenu gameMenu;
    private JMenuItem newGameMenuItem;
    private JMenuItem quitMenuItem;

    private static final int BOARD_SIZE = 3;
    private static final int WINNING_LENGTH = 3;
    private static final String EMPTY_CELL = "";
    private static final String PLAYER_X = "X";
    private static final String PLAYER_O = "O";

    private String currentPlayer;
    private String[][] board;
    private int playerXWins;
    private int playerOWins;
    private int[] playerWins = {0, 0}; // Index 0 for Player X, Index 1 for Player O
    private int ties;
    private boolean gameOver;

    /**
     * Constructs a new TicTacToeGUI object.
     */
    public TicTacToeGUI() {
        frame = new JFrame("Tic Tac Toe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);

        buttons = new JButton[BOARD_SIZE][BOARD_SIZE];
        board = new String[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = PLAYER_X;
        playerXWins = 0;
        playerOWins = 0;
        ties = 0;

        initializeBoard();
        initializeStatusLabel();
        initializePlayerStatsLabel();
        initializeMenuBar();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createBoardPanel(), BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        mainPanel.add(createPlayerStatsPanel(), BorderLayout.EAST);

        frame.add(mainPanel);
        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }
    
    /**
     * Initializes the game board with empty cells.
     */
    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY_CELL;
            }
        }
    }
    
    /**
     * Initializes the menu bar with options for starting a new game and quitting.
     */
    private void initializeMenuBar() {
        menuBar = new JMenuBar();
        gameMenu = new JMenu("Game");

        // New Game option
        newGameMenuItem = new JMenuItem("New Game");
        newGameMenuItem.addActionListener(e -> startNewGame());

        // Quit option
        quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.addActionListener(e -> System.exit(0));
        
        // Add options to menu
        gameMenu.add(newGameMenuItem);
        gameMenu.add(quitMenuItem);
        menuBar.add(gameMenu);
    }

    /**
     * Initializes the status label for displaying game messages.
     */
    private void initializeStatusLabel() {
        statusLabel = new JLabel("Game In Progress: Player X's turn");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Initializes the player statistics labels for displaying wins.
     */
    private void initializePlayerStatsLabel() {
        playerXStatsLabel = new JLabel("Player X: Wins - " + playerWins[0]);
        playerOStatsLabel = new JLabel("Player O: Wins - " + playerWins[1]);
    }
    
    /**
     * Updates the player statistics labels with the current win counts.
     */
    private void updatePlayerStatsLabels() {
        playerXStatsLabel.setText("Player X Wins: " + playerXWins);
        playerOStatsLabel.setText("Player O Wins: " + playerOWins);
    }
    
    /**
     * Creates and returns a panel containing the player statistics labels.
     * 
     * @return The player statistics panel.
     */
    private JPanel createPlayerStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 1));
        statsPanel.add(playerXStatsLabel);
        statsPanel.add(playerOStatsLabel);
        return statsPanel;
    }
    
    /**
     * Creates and returns a panel containing the game board buttons.
     * 
     * @return The game board panel.
     */
    private JPanel createBoardPanel() {
        JPanel boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                JButton button = new JButton();
                button.setFont(new Font("Arial", Font.PLAIN, 40));
                button.addActionListener(new ButtonClickListener(i, j));
                buttons[i][j] = button;
                boardPanel.add(button);
            }
        }

        return boardPanel;
    }

    /**
     * Starts a new game by resetting the board, player turns, and game status.
     */
    private void startNewGame() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                buttons[i][j].setText("");
                board[i][j] = EMPTY_CELL;
            }
        }
        currentPlayer = PLAYER_X;
        gameOver = false;
        // Clear the icons from all buttons
        clearButtonIcons();
        statusLabel.setText("Game In Progress: Player X's turn");
    }
    
    /**
     * Clears the icons from all buttons on the game board.
     */
    private void clearButtonIcons() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                buttons[row][col].setIcon(null); // Clear the icon from the button
            }
        }
    }

    /**
     * Checks for a winner after a move is made on the game board.
     * 
     * @param row The row of the last move.
     * @param col The column of the last move.
     */
    private void checkForWinner(int row, int col) {
        if (checkRow(row) || checkColumn(col) || checkDiagonals(row, col)) {
            gameOver = true;
            statusLabel.setText("Player " + currentPlayer + " wins!");
            if (currentPlayer.equals(PLAYER_X)) {
                playerXWins++;
            } else {
                playerOWins++;
            }
            updatePlayerStatsLabels();
        } else if (isBoardFull()) {
            gameOver = true;
            statusLabel.setText("It's a tie!");
            ties++;
            updatePlayerStatsLabels();
        } else {
            currentPlayer = (currentPlayer.equals(PLAYER_X)) ? PLAYER_O : PLAYER_X;
            statusLabel.setText("Player " + currentPlayer + "'s turn");
        }
    }

    /**
     * Checks if the specified row contains a winning combination for the current player.
     * 
     * @param row The row to check.
     * @return True if the row contains a winning combination, false otherwise.
     */
    private void updatePlayerStats(String player, String outcome) {
        int playerIndex = (player.equals(PLAYER_X)) ? 0 : 1;
        if (outcome.equals("win")) {
            playerWins[playerIndex]++;
        }
        
        playerXStatsLabel.setText("Player X: Wins - " + playerWins[0]);
    }

    //lab 10 implementation broken down
    private boolean checkRow(int row) {
        return board[row][0].equals(currentPlayer) &&
                board[row][1].equals(currentPlayer) &&
                board[row][2].equals(currentPlayer);
    }

    private boolean checkColumn(int col) {
        return board[0][col].equals(currentPlayer) &&
                board[1][col].equals(currentPlayer) &&
                board[2][col].equals(currentPlayer);
    }

    private boolean checkDiagonals(int row, int col) {
        if (row == col) {
            return board[0][0].equals(currentPlayer) &&
                    board[1][1].equals(currentPlayer) &&
                    board[2][2].equals(currentPlayer);
        } else if (row + col == BOARD_SIZE - 1) {
            return board[0][2].equals(currentPlayer) &&
                    board[1][1].equals(currentPlayer) &&
                    board[2][0].equals(currentPlayer);
        }
        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].equals(EMPTY_CELL)) {
                    return false;
                }
            }
        }
        return true;
    }

    private class ButtonClickListener implements ActionListener {
        private int row;
        private int col;
    
        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!gameOver && board[row][col].equals(EMPTY_CELL)) {
                // load the appropriate image based on the current player
                ImageIcon icon = (currentPlayer.equals(PLAYER_X)) ? new ImageIcon("x_tic.png") : new ImageIcon("o_tic.png");
                
                // Create the icon of the button
                buttons[row][col].setIcon(icon);
                
                // Update the game state
                board[row][col] = currentPlayer;
                checkForWinner(row, col);
            }
            
            playClickSound();
        }
        
        public void mouseClicked(MouseEvent e) {
            playClickSound();
        }
        
        private void playClickSound() {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("clicking_sound.wav"));
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

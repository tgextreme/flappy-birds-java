package com.infogonzalez.tomas.FlappyBirds;

/**
 * Hello world!
 *
 */

import java.util.Random;


import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class FlappyBird implements  Runnable {
    static final int WIDTH = 30;
    static final int HEIGHT = 15;
    static final int NUM_PIPES = 5; // Número de tubos en el juego
    volatile int birdY = HEIGHT / 2;
    int[][] pipes; // Representa las posiciones de los tubos
    int score = 0;
    volatile boolean gameOver = false;
    volatile boolean upPressed = false;
    Thread gameThread;
    
    volatile int upTime = 0;

    public FlappyBird() {
        // Inicializar el array de tubos
        pipes = new int[NUM_PIPES][2];
        Random rand = new Random();
        for (int i = 0; i < NUM_PIPES; i++) {
            pipes[i][0] = WIDTH + i * 15;
            pipes[i][1] = rand.nextInt(HEIGHT - 3) + 1;
        }
    }

    public void startGame() {
        gameThread = new Thread(this);
        gameThread.start();

        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            terminal.enterRawMode();

            while (!gameOver) {
                int code = terminal.reader().read();
                if (code == ' ') {
                    synchronized (this) {
                        if (birdY > 0) {
                            birdY--;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!gameOver) {
            updateGame();
            drawGame();
            checkCollision();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Game Over! Your score: " + score);
    }

    public void updateGame() {
        // Mueve los tubos hacia la izquierda
        for (int i = 0; i < pipes.length; i++) {
            synchronized (this) {
                pipes[i][0]--; // Decrementa la posición x del tubo
                if (pipes[i][0] < 0) {
                    pipes[i][0] = WIDTH;
                    pipes[i][1] = 15; // Nueva altura aleatoria para el tubo
                }
            }
        }

        // Mueve el pájaro hacia arriba
        synchronized (this) {
            if (upTime > 0) {
                birdY--;
                upTime--;
            } else {
                birdY++; // Si no hay impulso, el pájaro desciende
            }
        }

        // Verifique que el pájaro no salga de los límites
        if (birdY < 0) {
            birdY = 0;
        }
        if (birdY >= HEIGHT) {
            birdY = HEIGHT - 1;
        }
    }


    void drawGame() {
        // Limpiar la consola
        System.out.print("\033[H\033[2J");
        System.out.flush();

        // Dibujar el juego
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                boolean isPipe = false;
                for (int[] pipe : pipes) {
                    if (j == pipe[0] && i != pipe[1]) {
                        System.out.print("#");
                        isPipe = true;
                        break;
                    }
                }
                if (!isPipe) {
                    if (j == 2 && i == birdY) {
                        System.out.print("->");
                    } else {
                        System.out.print(" ");
                    }
                }
            }
            System.out.println();
        }
    }

    void checkCollision() {
        for (int[] pipe : pipes) {
            if (birdY != pipe[1] && pipe[0] == 2) {
                gameOver = true;
                return;
            }
        }
    }

    public static void main(String[] args) {
        new FlappyBird().startGame();
    }

}


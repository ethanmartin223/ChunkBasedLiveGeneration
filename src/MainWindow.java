import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

public class MainWindow extends JFrame {

    public MainWindow() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 1000);
        setTitle("");
        setLocationRelativeTo(null);

        setVisible(true);
    }


    public static void main(String[] args) {
        MainWindow window = new MainWindow();

        ChunkGenerator cgn = new ChunkGenerator();
        WorldRenderer renderer = new WorldRenderer(cgn);
        window.add(renderer);

        window.setSize(1000, 1001);
        window.setSize(1000, 1000);


    }

}
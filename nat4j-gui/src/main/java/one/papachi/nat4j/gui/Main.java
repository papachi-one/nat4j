package one.papachi.nat4j.gui;

import com.formdev.flatlaf.FlatDarkLaf;

public class Main {

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        JMain main = new JMain();
        main.pack();
        main.setVisible(true);
        main.init();
    }

}

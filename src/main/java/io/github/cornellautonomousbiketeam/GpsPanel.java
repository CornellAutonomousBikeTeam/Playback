package io.github.cornellautonomousbiketeam;

import java.awt.Graphics;
import java.util.Map;
import javax.swing.JPanel;

import io.github.cornellautonomousbiketeam.BikeState;

/**
 * Paints the GPS data.
 */
public class GpsPanel extends JPanel {

    // Maps Floats (timestamps) to BikeStates
    private Map<Float, BikeState> points;

    // The current time, as a float
    public GpsPanel( Map<Float, BikeState> p ) {
        points = p;
    }

    public void paintComponent( Graphics g ) {}
}


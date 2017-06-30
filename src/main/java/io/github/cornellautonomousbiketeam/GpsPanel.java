package io.github.cornellautonomousbiketeam;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import javax.swing.JPanel;

import io.github.cornellautonomousbiketeam.TimedBikeState;

/**
 * Paints the GPS data.
 */
public class GpsPanel extends JPanel {
    private List<TimedBikeState> points;

    // The current time, as a float
    public GpsPanel( List<TimedBikeState> p ) {
        points = p;
    }

    public void paintComponent( Graphics g ) {
        super.paintComponent( g );
        g.setColor( Color.BLUE );
        for( TimedBikeState state : points ) {
            System.out.println( state );
            System.out.println( String.format( "(%d, %d)", (int)( 100000F * ( state.xB - 42.44814F ) ) + 500, (int)( 100000F * ( state.yB + 76.48489F ) ) + 100 ) );
            g.fillRect( (int)( 100000F * ( state.xB - 42.44814F ) ) + 500, (int)( 100000F * ( state.yB + 76.48489F ) ) + 100, 5, 5 );
        }
    }
}


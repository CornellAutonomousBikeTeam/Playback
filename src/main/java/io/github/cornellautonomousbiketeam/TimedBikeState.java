package io.github.cornellautonomousbiketeam;

import java.util.Date;

import io.github.cornellautonomousbiketeam.BikeState;

/**
 * BikeState with a timestamp.
 */
public class TimedBikeState extends BikeState {
    public Date date;

    public TimedBikeState( Date date, float x, float y, float psi, float v ) {
        super( x, y, psi, v );
        this.date = date;
    }

    public String toString() {
        return super.toString() + " at " + date.toString();
    }
}

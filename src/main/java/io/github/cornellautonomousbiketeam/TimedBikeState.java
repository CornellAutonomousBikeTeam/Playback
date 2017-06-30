package io.github.cornellautonomousbiketeam;

import java.util.Date;

import io.github.cornellautonomousbiketeam.BikeState;

/**
 * BikeState with a timestamp.
 */
public class TimedBikeState extends BikeState {
    public Date date;

    public TimedBikeState( Date date, float x, float y ) {
        super( x, y );
        this.date = date;
    }

    public String toString() {
        return super.toString() + " at " + date.toString();
    }
}

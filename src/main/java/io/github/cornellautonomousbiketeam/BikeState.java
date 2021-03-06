package io.github.cornellautonomousbiketeam;

public class BikeState {
    public float xB;
    public float yB;

    /** Lean angle */
    public float phi;

    /** Heading, in radians */
    public float psi;

    /** Steer angle */
    public float delta;

    /** Lean rate */
    public float w_r;

    /** Speed */
    public float v;

    public BikeState( float x, float y, float psi, float v ) {
        this.xB = x;
        this.yB = y;
        this.psi = psi;
        this.v = v;
    }

    public String toString() {
        return String.format( "(%.10f, %.10f)", xB, yB );
    }
}

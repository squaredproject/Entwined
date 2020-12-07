package com.charlesgadeken.entwined.patterns.contributors.markLottor;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.model.Cube;
import com.charlesgadeken.entwined.model.ShrubCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.triggers.ParameterTriggerableAdapter;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Mark Lottor")
public class MarkLottor extends EntwinedTriggerablePattern {

    // These parameters will be knobs on the UI
    final BoundedParameter p1 = new BoundedParameter("SIZ", 0.25);
    final BoundedParameter p2 = new BoundedParameter("NUM", 0.75);
    final BoundedParameter p3 = new BoundedParameter("SPD", 0.5);
    final BoundedParameter p4 = new BoundedParameter("DIM", 0.17);

    // This is an example modulator
    final SinLFO verticalPosition = new SinLFO(model.yMin, model.yMax, 5000);

    // This is an example of using cube theta
    final SawLFO anglePosition = new SawLFO(0, 360, 2000);

    int n;
    int MAXYINCHES = (50 * 12); // max sculpture height in inches??
    int BALLS = 100;
    int maxballs = BALLS;
    MovObj[] balls;

    boolean isFresh = true;

    public MarkLottor(LX lx) {
        super(lx);

        // Makes the parameters have knobs in the UI
        addParameter("markLottor/markLottor/p1", p1);
        addParameter("markLottor/markLottor/p2", p2);
        addParameter("markLottor/markLottor/p3", p3);
        addParameter("markLottor/markLottor/p4", p4);

        // Starts the modulators
        addModulator(verticalPosition).start();
        addModulator(anglePosition).start();

        balls = new MovObj[BALLS];
        for (n = 0; n < BALLS; n++) balls[n] = new MovObj(-1, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) {
            if (!isFresh) {
                for (n = 0; n < BALLS; n++) balls[n] = new MovObj(-1, 0, 0, 0, 0, 0, 0);
                clearColors();
            }
            return;
        }

        isFresh = false;

        int n;
        float theta, ntheta;
        float y, ny;

        // These are the values of your knobs
        float p1v = p1.getValuef();
        float p2v = p2.getValuef();
        float p3v = p3.getValuef();
        float p4v = p4.getValuef();

        // These are the values of the LFOs
        float vpf = verticalPosition.getValuef();
        float apf = anglePosition.getValuef();

        maxballs = (int) (BALLS * p2v);
        // dim everything already on cube
        for (Cube cube : model.cubes) {
            colors[cube.index] =
                    LX.hsb(
                            LXColor.h(colors[cube.index]),
                            LXColor.s(colors[cube.index]),
                            LXColor.b(colors[cube.index]) * (1.0f - (p4v * p4v)));
        }
        for (ShrubCube cube : model.shrubCubes) {
            colors[cube.index] =
                    LX.hsb(
                            LXColor.h(colors[cube.index]),
                            LXColor.s(colors[cube.index]),
                            LXColor.b(colors[cube.index]) * (1.0f - (p4v * p4v)));
        }
        /*
        // dim everything already on cube
          for (Cube cube : model.cubes) {
            colors[cube.index] = lx.hsb(0,0,0);
          }
          */

        // add new balls if free slot and random interval
        for (n = 0; n < maxballs; n++) {
            if (balls[n].getposx() != -1) continue;
            if (Utilities.random(100) < 95) continue;

            // init new ball
            balls[n].setcolor(LX.hsb(lx.engine.palette.getHuef() % 360, 100, 100));
            balls[n].setpos(Utilities.random(0, 360), 0, 0); // theta,y,n/a
            balls[n].setvel(0, Utilities.random(0.1f, 0.5f), 0); // up speed
        }

        // update all ball positions
        for (n = 0; n < maxballs; n++) {
            if (balls[n].getposx() == -1) continue;

            // update positions
            ntheta = (balls[n].getposx() + balls[n].getvelx()) % 360;
            ny = balls[n].getposy() + (20 * p3v * balls[n].getvely());
            if (ny > MAXYINCHES) {
                balls[n].setpos(-1, 0, 0); // ball over
                continue;
            }
            balls[n].setpos(ntheta, ny, 0); // new position
        }

        // display all balls
        for (n = 0; n < maxballs; n++) {
            if (balls[n].getposx() == -1) continue;

            theta = balls[n].getposx();
            y = balls[n].getposy();

            // light up any cubes "near" this ball
            for (BaseCube cube : model.baseCubes) {
                if ((Utilities.abs(theta - cube.getTransformedTheta()) < (50 * p1v))
                        && (Utilities.abs(y - cube.transformedY) < (50 * p1v)))
                    colors[cube.index] = balls[n].getcolor();
            }
        }
    }

    ParameterTriggerableAdapter getParameterTriggerableAdapter() {
        return new ParameterTriggerableAdapter(lx, getChannel().fader) {
            public void onTriggered(float strength) {
                if (!isFresh) {
                    for (n = 0; n < BALLS; n++) balls[n] = new MovObj(-1, 0, 0, 0, 0, 0, 0);
                    clearColors();
                }
                super.onTriggered(strength);
            }
        };
    }

    static class MovObj {
        float posx, posy, posz;
        float velx, vely, velz;
        int co;

        MovObj(float x, float y, float z, float vx, float vy, float vz, int c) {
            setpos(x, y, z);
            setvel(vx, vy, vz);
            setcolor(c);
        }

        void setpos(float x, float y, float z) {
            posx = x;
            posy = y;
            posz = z;
        }

        void setvel(float x, float y, float z) {
            velx = x;
            vely = y;
            velz = z;
        }

        void setcolor(int c) {
            co = c;
        }

        float getposx() {
            return (posx);
        }

        float getposy() {
            return (posy);
        }

        float getposz() {
            return (posz);
        }

        float getvelx() {
            return (velx);
        }

        float getvely() {
            return (vely);
        }

        float getvelz() {
            return (velz);
        }

        int getcolor() {
            return (co);
        }
    }
}

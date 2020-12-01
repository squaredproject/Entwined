package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import com.charlesgadeken.entwined.patterns.contributors.colinHunt.BeachBall;
import com.charlesgadeken.entwined.patterns.contributors.colinHunt.Breath;
import com.charlesgadeken.entwined.patterns.contributors.colinHunt.ColorWave;
import com.charlesgadeken.entwined.patterns.contributors.geoffSchmidt.Parallax;
import com.charlesgadeken.entwined.patterns.contributors.geoffSchmidt.Pixels;
import com.charlesgadeken.entwined.patterns.contributors.geoffSchmidt.Wedges;
import com.charlesgadeken.entwined.patterns.contributors.grantPatterson.Planes;
import com.charlesgadeken.entwined.patterns.contributors.grantPatterson.Pond;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Bubbles;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Cells;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Fire;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Fireflies;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Fumes;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Lattice;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Pulleys;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Springs;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Voronoi;
import com.charlesgadeken.entwined.patterns.contributors.jackLampack.AcidTrip;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.BassSlam;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.CandyCloud;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.ColorStrobe;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.Explosions;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.Fade;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.NoPattern;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.Rain;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.RandomColor;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.SolidColor;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.Strobe;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.Wisps;
import com.charlesgadeken.entwined.patterns.contributors.markLottor.MarkLottor;
import com.charlesgadeken.entwined.patterns.contributors.maryWang.Twinkle;
import com.charlesgadeken.entwined.patterns.contributors.maryWang.VerticalSweep;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.IceCrystals;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.Lightning;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.MultiSine;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.Ripple;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.SparkleHelix;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.SparkleTakeOver;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.Stripes;
import com.charlesgadeken.entwined.patterns.original.ColoredLeaves;
import com.charlesgadeken.entwined.patterns.original.SeeSaw;
import com.charlesgadeken.entwined.patterns.original.SweepPattern;
import com.charlesgadeken.entwined.patterns.original.Twister;
import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;
import java.util.ArrayList;
import java.util.List;

public class EntwinedPatterns {
    public static void registerPatternController(
            List<EntwinedBasePattern> patterns, String name, EntwinedBasePattern pattern) {
        pattern.readableName = name;
        patterns.add(pattern);
    }

    public static List<EntwinedBasePattern> registerIPadPatterns(LX lx) {
        List<EntwinedBasePattern> patterns = new ArrayList<>();
        registerPatternController(patterns, "None", new NoPattern(lx));
        registerPatternController(patterns, "Twister", new Twister(lx));
        registerPatternController(patterns, "Lottor", new MarkLottor(lx));
        registerPatternController(patterns, "Ripple", new Ripple(lx));
        registerPatternController(patterns, "Stripes", new Stripes(lx));
        registerPatternController(patterns, "Lattice", new Lattice(lx));
        registerPatternController(patterns, "Fumes", new Fumes(lx));
        registerPatternController(patterns, "Voronoi", new Voronoi(lx));
        registerPatternController(patterns, "Candy Cloud", new CandyCloud(lx));
        // registerPatternController("Galaxy Cloud", new GalaxyCloud(lx));

        registerPatternController(patterns, "Color Strobe", new ColorStrobe(lx));
        registerPatternController(patterns, "Strobe", new Strobe(lx));
        registerPatternController(patterns, "Sparkle Takeover", new SparkleTakeOver(lx));
        registerPatternController(patterns, "Multi-Sine", new MultiSine(lx));
        registerPatternController(patterns, "Seesaw", new SeeSaw(lx));
        registerPatternController(patterns, "Cells", new Cells(lx));
        registerPatternController(patterns, "Fade", new Fade(lx));

        registerPatternController(patterns, "Ice Crystals", new IceCrystals(lx));
        registerPatternController(patterns, "Fire", new Fire(lx));

        registerPatternController(patterns, "Acid Trip", new AcidTrip(lx));
        registerPatternController(patterns, "Rain", new Rain(lx));
        registerPatternController(patterns, "Bass Slam", new BassSlam(lx));

        registerPatternController(patterns, "Fireflies", new Fireflies(lx));
        registerPatternController(patterns, "Bubbles", new Bubbles(lx));
        registerPatternController(patterns, "Lightning", new Lightning(lx));
        registerPatternController(patterns, "Wisps", new Wisps(lx));
        registerPatternController(patterns, "Fireworks", new Explosions(lx));

        registerPatternController(patterns, "ColorWave", new ColorWave(lx));

        registerPatternController(patterns, "Pond", new Pond(lx));
        registerPatternController(patterns, "Planes", new Twister(lx));

        return patterns;
    }

    public static List<EntwinedBasePattern> addPatterns(LX lx) {
        List<EntwinedBasePattern> patterns = new ArrayList<>();
        // Add patterns here.
        // The order here is the order it shows up in the patterns list
        // patterns.add(new SolidColor(lx));
        // patterns.add(new ClusterLineTest(lx));
        // patterns.add(new OrderTest(lx));
        patterns.add(new Twister(lx));
        patterns.add(new CandyCloud(lx));
        patterns.add(new MarkLottor(lx));
        patterns.add(new SolidColor(lx));
        // patterns.add(new DoubleHelix(lx));
        patterns.add(new SparkleHelix(lx));
        patterns.add(new Lightning(lx));
        patterns.add(new SparkleTakeOver(lx));
        patterns.add(new MultiSine(lx));
        patterns.add(new Ripple(lx));
        patterns.add(new SeeSaw(lx));
        patterns.add(new SweepPattern(lx));
        patterns.add(new IceCrystals(lx));
        patterns.add(new ColoredLeaves(lx));
        patterns.add(new Stripes(lx));
        patterns.add(new AcidTrip(lx));
        patterns.add(new Springs(lx));
        patterns.add(new Lattice(lx));
        patterns.add(new Fire(lx));
        patterns.add(new Fireflies(lx));
        patterns.add(new Fumes(lx));
        patterns.add(new Voronoi(lx));
        patterns.add(new Cells(lx));
        patterns.add(new Bubbles(lx));
        patterns.add(new Pulleys(lx));

        patterns.add(new Wisps(lx));
        patterns.add(new Explosions(lx));
        patterns.add(new BassSlam(lx));
        patterns.add(new Rain(lx));
        patterns.add(new Fade(lx));
        patterns.add(new Strobe(lx));
        patterns.add(new Twinkle(lx));
        patterns.add(new VerticalSweep(lx));
        patterns.add(new RandomColor(lx));
        patterns.add(new ColorStrobe(lx));
        patterns.add(new Pixels(lx));
        patterns.add(new Wedges(lx));
        patterns.add(new Parallax(lx));

        // Colin Hunt Patterns
        patterns.add(new ColorWave(lx));
        patterns.add(new BeachBall(lx));
        patterns.add(new Breath(lx));

        // Grant Patterson Patterns
        patterns.add(new Pond(lx));
        patterns.add(new Planes(lx));

        return patterns;
    }

    static LXPattern[] getPatternListForChannels(LX lx) {
        return addPatterns(lx).toArray(new LXPattern[0]);
    }
}

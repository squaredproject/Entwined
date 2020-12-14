package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import com.charlesgadeken.entwined.patterns.contributors.colinHunt.ColorWave;
import com.charlesgadeken.entwined.patterns.contributors.grantPatterson.Pond;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Bubbles;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Cells;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Fire;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Fireflies;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Fumes;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Lattice;
import com.charlesgadeken.entwined.patterns.contributors.ireneZhou.Voronoi;
import com.charlesgadeken.entwined.patterns.contributors.jackLampack.AcidTrip;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.BassSlam;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.CandyCloud;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.ColorStrobe;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.Explosions;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.Fade;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.NoPattern;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.Rain;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.Strobe;
import com.charlesgadeken.entwined.patterns.contributors.kyleFleming.Wisps;
import com.charlesgadeken.entwined.patterns.contributors.markLottor.MarkLottor;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.IceCrystals;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.Lightning;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.MultiSine;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.Ripple;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.SparkleTakeOver;
import com.charlesgadeken.entwined.patterns.contributors.raySykes.Stripes;
import com.charlesgadeken.entwined.patterns.original.SeeSaw;
import com.charlesgadeken.entwined.patterns.original.Twister;
import heronarts.lx.LX;
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
}

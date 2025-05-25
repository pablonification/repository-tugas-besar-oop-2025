package com.spakborhills.model.Map;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
// import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.Object.DeployedObject;

/**
 * TODO: either make this an abstract class or remove this interface entirely.
 * some of the method below, mostly doesn't need some specific-map behavior.
 *
 * */
public interface MapArea {
    public String getName();
    public Dimension getSize();
    public Tile getTile(int x, int y);
    public boolean isOccupied(int x, int y);
    public boolean isWithinBounds(int x, int y);
    public boolean placeObject(DeployedObject obj, int x, int y);
    public DeployedObject getObjectAt(int x, int y);
    public List<Point> getEntryPoints();
}

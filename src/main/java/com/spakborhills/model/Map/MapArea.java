package com.spakborhills.model.Map;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import com.spakborhills.model.Object.DeployedObject;

public interface MapArea {
    public String getName();
    public Dimension getSize();
    public Tile getTile(int x, int y);
    public Tile[][] getTiles();
    public boolean isOccupied(int x, int y);
    public boolean isWithinBounds(int x, int y);
    public boolean placeObject(DeployedObject obj, int x, int y);
    public DeployedObject getObjectAt(int x, int y);
    public List<Point> getEntryPoints();
}

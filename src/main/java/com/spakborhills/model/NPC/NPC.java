/*
 *   abstract class NPC {
    # name: String
    # heartPoints: int
    # maxHeartPoints: int
    # lovedItems: List<String>
    # likedItems: List<String>
    # hatedItems: List<String>
    # relationshipStatus: RelationshipStatus
    # homeLocation: LocationType ' Changed to LocationType
    # isBachelor: boolean ' Added from previous good version
    + getName(): String
    + getHeartPoints(): int
    + addHeartPoints(amt: int): void
    + getRelationshipStatus(): RelationshipStatus
    + setRelationshipStatus(s: RelationshipStatus): void
    + checkGiftPreference(item: Item): int
    + interact(player: Player): void
  }
 */

package com.spakborhills.model.NPC;

import java.util.List;
import java.util.ArrayList;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Enum.RelationshipStatus;
import com.spakborhills.model.Player;

public abstract class NPC {
    protected String name;
    protected int heartPoints;
    protected int maxHeartPoints;
    protected List<String> lovedItems;
    protected List<String> likedItems;
    protected List<String> hatedItems;
    protected RelationshipStatus relationshipStatus;
    protected LocationType homeLocation;
    protected boolean isBachelor;

    // Konstruktor untuk NPC
    protected NPC(String name, LocationType homeLocation, boolean isBachelor) {
        this.name = name;
        this.homeLocation = homeLocation;
        this.isBachelor = isBachelor;

        this.heartPoints = 0;
        this.relationshipStatus = RelationshipStatus.SINGLE;

        this.maxHeartPoints = isBachelor? 150 : 100;

        this.lovedItems = new ArrayList<>();
        this.likedItems = new ArrayList<>();
        this.hatedItems = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getHeartPoints() {
        return heartPoints;
    }
    
    public void addHeartPoints(int amt){
        heartPoints += amt;
        if (heartPoints > maxHeartPoints) {
            heartPoints = maxHeartPoints;
        }

        if (heartPoints < 0) {
            heartPoints = 0;
        }
    }

    public RelationshipStatus getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(RelationshipStatus status) {
        relationshipStatus = status;
    }

    public int checkGiftPreference(Item item) {
        if (lovedItems.contains(item.getName())) {
            return 25;
        } else if (likedItems.contains(item.getName())) {
            return 20;
        } else if (hatedItems.contains(item.getName())) {
            return -25;
        }
        return 0;
    }

    public boolean isBachelor() {
        return isBachelor;
    }

    public LocationType getHomeLocation() {
        return homeLocation;
    }

    public int getMaxHeartPoints() {
        return maxHeartPoints;
    }    


    public abstract void interact(Player player);
}

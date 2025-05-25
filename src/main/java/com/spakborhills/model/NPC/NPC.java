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
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
// import java.io.File;
import java.io.IOException;

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
    protected int currentTileX;
    protected int currentTileY;

    // Untuk spritesheets
    protected String spritesheetPath;
    protected transient BufferedImage fullSpritesheet;

    // Dimensi dan koordinat untuk frame sprite default di peta
    public int defaultSpriteX, defaultSpriteY, spriteWidth, spriteHeight;
    // Dimensi dan koordinat untuk potret default di dialog
    public int defaultPortraitX, defaultPortraitY, portraitWidth, portraitHeight;


    protected NPC(String name, LocationType homeLocation, boolean isBachelor, String spritesheetPath,
                  int defaultSpriteX, int defaultSpriteY, int spriteWidth, int spriteHeight,
                  int defaultPortraitX, int defaultPortraitY, int portraitWidth, int portraitHeight) {
        this.name = name;
        this.homeLocation = homeLocation;
        this.isBachelor = isBachelor;
        this.spritesheetPath = spritesheetPath;

        this.heartPoints = 0;
        this.relationshipStatus = RelationshipStatus.SINGLE;
        this.maxHeartPoints = isBachelor ? 150 : 100;

        this.lovedItems = new ArrayList<>();
        this.likedItems = new ArrayList<>();
        this.hatedItems = new ArrayList<>();

        this.currentTileX = 5;
        this.currentTileY = 5;

        // Simpan info frame/potret default
        this.defaultSpriteX = defaultSpriteX;
        this.defaultSpriteY = defaultSpriteY;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.defaultPortraitX = defaultPortraitX;
        this.defaultPortraitY = defaultPortraitY;
        this.portraitWidth = portraitWidth;
        this.portraitHeight = portraitHeight;

        loadSpritesheet();
    }

    private void loadSpritesheet() {
        try {
            if (this.spritesheetPath != null && !this.spritesheetPath.isEmpty()) {
                this.fullSpritesheet = ImageIO.read(getClass().getResourceAsStream(this.spritesheetPath));
                if (this.fullSpritesheet == null) {
                    System.err.println("Gagal memuat spritesheet NPC: " + this.name + " dari path: " + this.spritesheetPath);
                }
            }
        } catch (IOException e) {
            System.err.println("Error saat memuat spritesheet untuk NPC " + this.name + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error path spritesheet tidak valid untuk NPC " + this.name + ": " + e.getMessage());
        }
    }

    // Mendapatkan frame sprite default untuk di peta
    public Image getCurrentSpriteFrame() {
        if (this.fullSpritesheet == null) {
            if (this.spritesheetPath != null && !this.spritesheetPath.isEmpty()) {
                loadSpritesheet(); // Coba muat ulang jika belum ada
            }
            if (this.fullSpritesheet == null) return null; // Tetap null jika gagal muat
        }
        // Potong sub-gambar dari spritesheet untuk frame sprite default
        // Pastikan koordinat dan dimensi tidak melebihi batas gambar
        if (defaultSpriteX + spriteWidth > fullSpritesheet.getWidth() || defaultSpriteY + spriteHeight > fullSpritesheet.getHeight()) {
            System.err.println("Koordinat atau dimensi sprite default untuk " + name + " di luar batas spritesheet.");
            return null; 
        }
        return this.fullSpritesheet.getSubimage(defaultSpriteX, defaultSpriteY, spriteWidth, spriteHeight);
    }

    // Mendapatkan potret default untuk dialog
    public Image getDefaultPortrait() {
        if (this.fullSpritesheet == null) {
            if (this.spritesheetPath != null && !this.spritesheetPath.isEmpty()) {
                loadSpritesheet();
            }
            if (this.fullSpritesheet == null) return null;
        }
        // Potong sub-gambar dari spritesheet untuk potret default
        // Pastikan koordinat dan dimensi tidak melebihi batas gambar
        if (defaultPortraitX + portraitWidth > fullSpritesheet.getWidth() || defaultPortraitY + portraitHeight > fullSpritesheet.getHeight()) {
            System.err.println("Koordinat atau dimensi potret default untuk " + name + " di luar batas spritesheet.");
            return null;
        }
        return this.fullSpritesheet.getSubimage(defaultPortraitX, defaultPortraitY, portraitWidth, portraitHeight);
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

    public int getCurrentTileX() {
        return currentTileX;
    }

    public void setCurrentTileX(int currentTileX) {
        this.currentTileX = currentTileX;
    }

    public int getCurrentTileY() {
        return currentTileY;
    }

    public void setCurrentTileY(int currentTileY) {
        this.currentTileY = currentTileY;
    }

    public abstract String getDialogue(Player player);

    public abstract String reactToGift(Item item, Player player);
}

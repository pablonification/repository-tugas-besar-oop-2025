package com.spakborhills.model.NPC;

import java.util.List;
import java.util.ArrayList;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Enum.RelationshipStatus;
import com.spakborhills.model.Enum.Direction;
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
    protected String dialoguePortraitPath;
    protected transient BufferedImage portraitSpritesheetForDialogue; 
    protected transient BufferedImage dialoguePortraitImage; 

    // Dimensi dan koordinat untuk frame sprite default di peta
    public int defaultSpriteX, defaultSpriteY, spriteWidth, spriteHeight;
    // Dimensi dan koordinat untuk potret default di dialog
    public int defaultPortraitX, defaultPortraitY, portraitWidth, portraitHeight;

    protected Direction currentDirection;
    protected boolean isMoving;
    protected int animationFrame;
    protected int animationCounter;
    protected static final int ANIMATION_SPEED = 3; 
    protected static final int WALKING_FRAMES = 4; 


    // Layout spritesheet NPC 
    protected int spriteSheetRowDown = 0;   
    protected int spriteSheetRowUp = 2;       
    protected int spriteSheetRowLeft = 3;   
    protected int spriteSheetRowRight = 1;  


    protected NPC(String name, LocationType homeLocation, boolean isBachelor, String spritesheetPath,
    int defaultSpriteX, int defaultSpriteY, int spriteWidth, int spriteHeight,
    int defaultPortraitX, int defaultPortraitY, int portraitWidth, int portraitHeight, String dialoguePortraitPath) {
        this.name = name;
        this.homeLocation = homeLocation;
        this.isBachelor = isBachelor;
        this.spritesheetPath = spritesheetPath;
        this.dialoguePortraitPath = dialoguePortraitPath; 

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

        // Inisialisasi animasi
        this.currentDirection = Direction.SOUTH; 
        this.isMoving = false;
        this.animationFrame = 0; 
        this.animationCounter = 0;

        loadSpritesheet();
        loadDialoguePortrait(); 
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

    // Method to load the dialogue portrait image
    private void loadDialoguePortrait() {
        try {
            if (this.dialoguePortraitPath != null && !this.dialoguePortraitPath.isEmpty()) {
                this.portraitSpritesheetForDialogue = ImageIO.read(getClass().getResourceAsStream(this.dialoguePortraitPath));

                if (this.portraitSpritesheetForDialogue != null) {
                    try {
                        this.dialoguePortraitImage = this.portraitSpritesheetForDialogue.getSubimage(
                            this.defaultPortraitX, 
                            this.defaultPortraitY, 
                            this.portraitWidth,     
                            this.portraitHeight     
                        );
                    } catch (Exception e) {
                        System.err.println("Gagal memotong potret dialog dari spritesheet potret khusus untuk NPC: " + this.name +
                                           " (Path: " + this.dialoguePortraitPath + 
                                           ", X:" + this.defaultPortraitX + ", Y:" + this.defaultPortraitY +
                                           ", W:" + this.portraitWidth + ", H:" + this.portraitHeight +
                                           "): " + e.getMessage());
                        this.dialoguePortraitImage = null;
                    }
                } else {
                    System.err.println("Gagal memuat spritesheet potret dialog khusus (dari dialoguePortraitPath): " + this.name + " dari path: " + this.dialoguePortraitPath);
                    this.dialoguePortraitImage = null; 
                }
            } else {
                System.err.println("Tidak ada dialoguePortraitPath yang disediakan untuk NPC: " + this.name + ". Potret dialog akan null.");
                this.dialoguePortraitImage = null;
            }
        } catch (IOException e) {
            System.err.println("Error I/O saat memuat spritesheet potret dialog (dari dialoguePortraitPath) untuk NPC " + this.name + " (" + this.dialoguePortraitPath + "): " + e.getMessage());
            this.dialoguePortraitImage = null;
        } catch (IllegalArgumentException e) {
            System.err.println("Error path spritesheet potret dialog (dari dialoguePortraitPath) tidak valid untuk NPC " + this.name + " (" + this.dialoguePortraitPath + "): " + e.getMessage());
            this.dialoguePortraitImage = null;
        } catch (Exception e) {
            System.err.println("Exception umum saat memuat spritesheet potret dialog (dari dialoguePortraitPath) untuk NPC " + this.name + " (" + this.dialoguePortraitPath + "): " + e.getMessage());
            this.dialoguePortraitImage = null;
        }
    }

    // Mendapatkan frame sprite default untuk di peta
    public Image getCurrentSpriteFrame() {
        if (this.fullSpritesheet == null) {
            if (this.spritesheetPath != null && !this.spritesheetPath.isEmpty()) {
                loadSpritesheet();
            }
            if (this.fullSpritesheet == null) {
                System.err.println("NPC " + name + ": fullSpritesheet null di getCurrentSpriteFrame.");
                return null;
            }
        }

        int spriteSheetRowPixelY; 
        int spriteSheetColPixelX; 

        // Tentukan baris berdasarkan arah hadap
        switch (currentDirection) {
            case NORTH:
                spriteSheetRowPixelY = spriteSheetRowUp * spriteHeight;
                break;
            case SOUTH:
            default: 
                spriteSheetRowPixelY = spriteSheetRowDown * spriteHeight;
                break;
            case WEST:
                spriteSheetRowPixelY = spriteSheetRowLeft * spriteHeight;
                break;
            case EAST:
                spriteSheetRowPixelY = spriteSheetRowRight * spriteHeight;
                break;
        }

        // Tentukan kolom berdasarkan status bergerak dan frame animasi
        if (isMoving) {
            switch (animationFrame) { 
                case 0: // Kaki Kiri
                    spriteSheetColPixelX = 1 * spriteWidth;
                    break;
                case 1: // Diam
                    spriteSheetColPixelX = 0 * spriteWidth;
                    break;
                case 2: // Kaki Kanan
                    spriteSheetColPixelX = 3 * spriteWidth;
                    break;
                case 3: // Diam lagi
                default: 
                    spriteSheetColPixelX = 0 * spriteWidth;
                    break;
            }
        } else {
            // NPC diam, gunakan frame idle 
            spriteSheetColPixelX = 0 * spriteWidth;
        }
        
        // Validasi batas
        if (spriteSheetColPixelX < 0 || spriteSheetRowPixelY < 0 ||
            spriteSheetColPixelX + spriteWidth > fullSpritesheet.getWidth() ||
            spriteSheetRowPixelY + spriteHeight > fullSpritesheet.getHeight()) {
            System.err.println("NPC " + name + ": Koordinat/dimensi sprite di luar batas!" +
                               " Dir: " + currentDirection + ", Moving: " + isMoving + ", AnimFrameIdx: " + animationFrame +
                               ", Xpx: " + spriteSheetColPixelX + ", Ypx: " + spriteSheetRowPixelY +
                               ", W: " + spriteWidth + ", H: " + spriteHeight +
                               ", Sheet: " + fullSpritesheet.getWidth() + "x" + fullSpritesheet.getHeight());
            return this.fullSpritesheet.getSubimage(defaultSpriteX, defaultSpriteY, spriteWidth, spriteHeight);
        }

        try {
            return this.fullSpritesheet.getSubimage(spriteSheetColPixelX, spriteSheetRowPixelY, spriteWidth, spriteHeight);
        } catch (Exception e) {
            System.err.println("NPC " + name + ": Exception saat getSubimage: " + e.getMessage());
            e.printStackTrace();
            return this.fullSpritesheet.getSubimage(defaultSpriteX, defaultSpriteY, spriteWidth, spriteHeight);
        }
    }

    // Method untuk update animasi NPC
    public void updateAnimation() {
        animationFrame = 0; 
        animationCounter = 0; 
        isMoving = false;
    }

    // Getter dan Setter untuk animasi
    public void setMoving(boolean moving) {
        this.isMoving = false;
        this.animationFrame = 0;
        this.animationCounter = 0;
    }

    public void setCurrentDirection(Direction direction) {
        if (this.currentDirection != direction) {
            this.currentDirection = direction;
            this.animationFrame = 0; 
            this.animationCounter = 0;
        } else if (!isMoving) { 
            this.animationFrame = 0;
            this.animationCounter = 0;
        }
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    // Untuk testing, NPC tidak bisa bergerak
    public boolean move(Direction direction) {
        this.isMoving = false;
        this.animationFrame = 0;
        this.animationCounter = 0;
        return false;
    }

    // Mendapatkan potret default untuk dialog
    public Image getDefaultPortrait() { 
        if (this.dialoguePortraitImage == null) {
            loadDialoguePortrait();
            
            if (this.dialoguePortraitImage == null) {
                System.err.println("Potret dialog untuk NPC " + name + " adalah null bahkan setelah upaya pemuatan ulang/fallback.");
                return null; 
            }
        }
        return this.dialoguePortraitImage;
    }

    public String getName() {
        return name;
    }

    public int getHeartPoints() {
        return heartPoints;
    }

    public void setHeartPoints(int heartPoints) {
        this.heartPoints = Math.max(0, Math.min(heartPoints, this.maxHeartPoints));
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

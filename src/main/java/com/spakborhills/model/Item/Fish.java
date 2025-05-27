/*
 *     - rarity: FishRarity
    - requiredSeason: Season
    - startTime: int ' Hour 0-23
    - endTime: int ' Hour 0-23
    - requiredWeather: Weather
    - requiredLocation: LocationType 
    - {static} final int BASE_ENERGY_RESTORE = 1
    + calculateSellPrice(prices: PriceList): int 
    + getEnergyRestore(): int
    + use(player: Player, target: Object): boolean
    + getRarity(): FishRarity
    + canBeCaught(season: Season, time: GameTime, weather: Weather, location: LocationType): boolean
 */
package com.spakborhills.model.Item;

import java.util.List;
import java.util.Set; // Using Set for locations might be slightly better semantically
import java.util.ArrayList;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Enum.FishRarity;
import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.Weather;
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Player;
import com.spakborhills.model.Util.GameTime;


// Assuming these enums and classes exist in the correct packages
// import com.spakborhills.model.Item;
// import com.spakborhills.model.ItemCategory;
// import com.spakborhills.model.Player;
// import com.spakborhills.model.Season;
// import com.spakborhills.model.Weather;
// import com.spakborhills.model.LocationType;
// import com.spakborhills.model.GameTime; // Needed for canBeCaught

public class Fish extends Item implements EdibleItem {

    // Helper class untuk menangani kasus waktu disjoin
    public static class TimeRange {
        public final int startHour;
        public final int endHour;

        public TimeRange(int startHour, int endHour) {
            this.startHour = startHour;
            this.endHour = endHour;
        }

        public int getDurationHours() {
            if (endHour >= startHour) {
                return endHour - startHour + 1;
            } else {
                return 24 - startHour + endHour + 1;
            }
        }

        public boolean contains(int hour) {
            if (endHour >= startHour) {
                return hour >= startHour && hour <= endHour;
            } else {
                return hour >= startHour || hour <= endHour;
            }
        }
    }

    private static final int BASE_ENERGY_RESTORE = 1;

    private final FishRarity rarity;
    private final Set<Season> requiredSeasons; // (e.g., {SPRING, SUMMER}
    private final List<TimeRange> catchableTimeRanges;
    private final Set<Weather> requiredWeather; // (e.g., {RAINY, SUNNY})
    private final Set<LocationType> requiredLocations; 
    
    /**
     * Constructor for Fish items.
     * Sell price is calculated dynamically based on attributes.
     *
     * @param name             The name of the fish (e.g., "Bullhead").
     * @param rarity           The rarity category (COMMON, REGULAR, LEGENDARY).
     * @param requiredSeasons  The set of seasons the fish can be caught in (use Season.ANY if applicable).
     * @param catchableTimeRanges The list of time ranges the fish can be caught in.
     * @param requiredWeathers The set of weather conditions for catching (use Weather.ANY if applicable).
     * @param requiredLocations The set of locations where the fish can be caught.
     */
    public Fish(String name, FishRarity rarity, Set<Season> requiredSeasons, List<TimeRange> catchableTimeRanges, Set<Weather> requiredWeather, Set<LocationType> requiredLocations) {
        // fish gabisa dibeli, jadi buy price = 0
        // sell price nanti dikalkulasi, 0 untuk sementara
        super(name, ItemCategory.FISH, 0, 0);

        if (requiredSeasons == null || requiredSeasons.isEmpty()) {
            throw new IllegalArgumentException("Fish harus memiliki setidaknya satu musim yang dapat ditangkap");
        }

        if (requiredWeather == null || requiredWeather.isEmpty()) {
            throw new IllegalArgumentException("Fish harus memiliki setidaknya satu cuaca yang dapat ditangkap");
        }

        if (requiredLocations == null || requiredLocations.isEmpty()) {
            throw new IllegalArgumentException("Fish harus memiliki setidaknya satu lokasi yang dapat ditangkap");
        }

        if (catchableTimeRanges == null || catchableTimeRanges.isEmpty()) {
            throw new IllegalArgumentException("Fish harus memiliki setidaknya satu waktu yang dapat ditangkap");
        }

        this.rarity = rarity;
        this.requiredSeasons = requiredSeasons;
        this.catchableTimeRanges = catchableTimeRanges;
        this.requiredWeather = requiredWeather;
        this.requiredLocations = requiredLocations;
    }

    /**
     * Gets the energy restored when this fish is eaten.
     * @return Energy restored (always 1 for fish).
     */
    @Override
    public int getEnergyRestore() {
        return BASE_ENERGY_RESTORE;
    }

    /**
     * Implements the 'use' action for a Fish, which is eating it.
     * Assumes the Controller has verified the player has the fish.
     * The Controller is responsible for removing the fish from inventory if use succeeds.
     *
     * @param player The player using (eating) the fish.
     * @param target The target of the action (not relevant for eating fish).
     * @return true if the fish was successfully eaten, false otherwise.
     */
    @Override
    public boolean use(Player player, Object target) {
        // eat fish
        player.changeEnergy(getEnergyRestore());
        System.out.println("Kamu memakan " + getName() + " dan mendapatkan sedikit energi.");
        // Controller should remove 1 fish from inventory after this returns true
        return true;
    }

    /**
     * Calculates the sell price of the fish based on the formula from Specification.
     * sellPrice = (4/banyak season) * (24/jumlah jam) * (2/jumlah variasi weather) * (4/banyak lokasi) * C
     * @return The calculated sell price.
     */
    @Override
    public int getSellPrice() {
        double seasonCount = requiredSeasons.contains(Season.ANY) ? 4.0 : requiredSeasons.size();
        double weatherCount = requiredWeather.contains(Weather.ANY) ? 2.0 : requiredWeather.size();
        double locationCount = requiredLocations.size();
        
        // Hitung durasi (jumlah jam) - handles wrap-around cases
        double duration = 0;
        for (TimeRange range : catchableTimeRanges) {
            duration += range.getDurationHours();
        }

        if (duration <= 0) duration = 1; // untuk menghindari pembagian dengan 0

        double cMultiplier = rarity.getPriceMultiplier();

        double price = (4.0 / seasonCount) * (24.0 / duration) * (2.0 / weatherCount) * (4.0 / locationCount) * cMultiplier;
        return (int) Math.round(price);
    }

    /**
     * Checks if this fish can be caught under the given game conditions.
     *
     * @param currentSeason   The current season in the game.
     * @param currentTime     The current time object (contains hour).
     * @param currentWeather  The current weather in the game.
     * @param currentLocation The location where the player is fishing.
     * @return true if the fish can be caught, false otherwise.
     */
    public boolean canBeCaught(Season currentSeason, GameTime currentTime, Weather currentWeather, LocationType currentLocation) {
        // Check season
        if (!requiredSeasons.contains(Season.ANY) && !requiredSeasons.contains(currentSeason)) {
            return false;
        }

        // Check weather
        if (!requiredWeather.contains(Weather.ANY) && !requiredWeather.contains(currentWeather)) {
            return false;
        }

        // Check location
        if (!requiredLocations.contains(currentLocation)) {
            return false;
        }

        // Check time
        int currentHour = currentTime.getHour(); // GameTime akan punya getHour()
        boolean timeMatch = false;
        for (TimeRange range : this.catchableTimeRanges) {
            if (range.contains(currentHour)) {
                timeMatch = true;
                break;
            }
        }

        if (!timeMatch) {
            return false;
        }
        // Jika semua kondisi terpenuhi, return true
        return true;
    }

    public FishRarity getRarity() {
        return rarity;
    }

    public Set<Season> getRequiredSeasons() {
        return requiredSeasons;
    }

    public List<TimeRange> getCatchableTimeRanges() {
        return catchableTimeRanges;
    }

    public Set<Weather> getRequiredWeather() {
        return requiredWeather;
    }

    public Set<LocationType> getRequiredLocations() {
        return requiredLocations;
    }

    @Override
    public Item cloneItem() {
        // Create new sets and lists to avoid sharing mutable objects
        Set<Season> clonedSeasons = Set.copyOf(this.requiredSeasons);
        List<TimeRange> clonedTimeRanges = new ArrayList<>(this.catchableTimeRanges.size());
        for (TimeRange range : this.catchableTimeRanges) {
            // Assuming TimeRange is immutable or we make a deep copy if it's mutable
            // For now, assuming TimeRange(int, int) constructor is sufficient for a new instance
            clonedTimeRanges.add(new TimeRange(range.startHour, range.endHour)); 
        }
        Set<Weather> clonedWeather = Set.copyOf(this.requiredWeather);
        Set<LocationType> clonedLocations = Set.copyOf(this.requiredLocations);

        return new Fish(this.getName(), this.rarity, clonedSeasons, clonedTimeRanges, clonedWeather, clonedLocations);
    }

}

/*
 * Contoh Instance Halibut
 * List<TimeRange> halibutTimes = Arrays.asList(
    new TimeRange(6, 11),
    new TimeRange(19, 2) // Rentang kedua (19:00 - 02:00)
);
Fish halibut = new Fish("Halibut", Fish.FishRarity.REGULAR, Set.of(Season.ANY),
                       halibutTimes, // <-- Berikan list
                       Set.of(Weather.ANY), Set.of(LocationType.OCEAN));
 */

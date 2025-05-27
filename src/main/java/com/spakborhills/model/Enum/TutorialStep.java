package com.spakborhills.model.Enum;

public enum TutorialStep {
    NONE, // Tutorial complete or not started
    INTRO_MAYOR_WELCOME, // Initial welcome by Mayor, perhaps triggered on first game load/map entry
    EXPLAIN_MOVEMENT, // Mayor explains movement controls
    GO_TO_MAYOR_HOUSE, // Task: Player needs to find Mayor Tadi (e.g., in his house or town square)
    MAYOR_HOUSE_WELCOME, // Dialogue once player reaches Mayor
    EXPLAIN_TOOLS_AND_ENERGY, // Mayor explains basic tools (hoe, watering can) and energy
    EXPLAIN_SEEDS_BUYING, // Mayor explains where and how to buy seeds
    GO_TO_SHOP, // Task: Player needs to go to the General Store
    SHOP_WELCOME_EMILY, // Emily dialogue when player enters shop first time during tutorial
    BOUGHT_FIRST_SEEDS, // Trigger: Player successfully buys any seeds
    EXPLAIN_TILLING_AND_PLANTING, // Mayor (or a thought bubble) explains how to till and plant
    PLANTED_FIRST_CROP, // Trigger: Player successfully plants a seed
    EXPLAIN_WATERING, // Mayor (or thought bubble) explains watering
    WATERED_FIRST_CROP, // Trigger: Player successfully waters a crop
    EXPLAIN_HARVESTING, // Mayor explains that crops take time to grow and how to harvest
    HARVESTED_FIRST_CROP, // Trigger: Player successfully harvests a mature crop
    EXPLAIN_SELLING_SHIPPING_BIN, // Mayor explains using the shipping bin
    SOLD_FIRST_CROP_BIN, // Trigger: Player successfully sells something via shipping bin
    EXPLAIN_FISHING, // Mayor suggests trying fishing
    GO_TO_RIVER, // Task: Player needs to go to a fishing spot (e.g., Forest River)
    CAUGHT_FIRST_FISH, // Trigger: Player successfully catches a fish
    EXPLAIN_SELLING_GENERAL_STORE, // Mayor explains items can also be sold directly to some shops
    SOLD_FIRST_FISH_STORE, // Trigger: Player sells a fish to the General Store
    MAYOR_CONGRATS_BASICS, // Mayor congratulates player on learning the basics
    TUTORIAL_COMPLETE // Tutorial formally ends
}
package com.spakborhills.model.Util;

import java.util.Collections;
import java.util.Map;

public class Recipe {
    private String name;
    private String resultItemName;
    // Tambahkan atribut lain jika perlu

    // Konstruktor stub sederhana
    public Recipe(String name, String resultItemName) {
        this.name = name;
        this.resultItemName = resultItemName;
    }

    public String getName() { return name; }
    public String getResultItemName() { return resultItemName; }
    // Stub: Kembalikan map kosong untuk bahan
    public Map<String, Integer> getIngredients() { return Collections.emptyMap(); }
}
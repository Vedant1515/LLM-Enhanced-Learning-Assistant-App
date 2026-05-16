package com.learningassistant.app.models;

public class UpgradeTier {
    private String name;
    private String description;
    private String price;
    private boolean isBestSeller;
    private boolean isPurchased;

    public UpgradeTier(String name, String description, String price, boolean isBestSeller) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.isBestSeller = isBestSeller;
        this.isPurchased = false;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public boolean isBestSeller() { return isBestSeller; }
    public void setBestSeller(boolean bestSeller) { isBestSeller = bestSeller; }

    public boolean isPurchased() { return isPurchased; }
    public void setPurchased(boolean purchased) { isPurchased = purchased; }
}

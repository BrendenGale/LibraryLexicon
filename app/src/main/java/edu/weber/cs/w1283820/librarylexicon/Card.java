package edu.weber.cs.w1283820.librarylexicon;

public class Card {

    private String cardName;
    private String cmc;
    private String colors;
    private String deckID;
    private String formats;
    private String imageUrl;
    private String multiverseID;
    private String type;
    private String setName;
    private String copies;

    private Card() {

    }

    private Card(String cardName, String cmc, String colors, String deckID, String formats,
                 String imageUrl, String multiverseID, String type) {
        this.cardName = cardName;
        this.cmc = cmc;
        this.colors = colors;
        this.deckID = deckID;
        this.formats = formats;
        this.imageUrl = imageUrl;
        this.multiverseID = multiverseID;
        this.type = type;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCmc() {
        return cmc;
    }

    public void setCmc(String cmc) {
        this.cmc = cmc;
    }

    public String getColors() {
        return colors;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public String getDeckID() {
        return deckID;
    }

    public void setDeckID(String deckID) {
        this.deckID = deckID;
    }

    public String getFormats() {
        return formats;
    }

    public void setFormats(String formats) {
        this.formats = formats;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMultiverseID() {
        return multiverseID;
    }

    public void setMultiverseID(String multiverseID) {
        this.multiverseID = multiverseID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCopies() {
        return copies;
    }

    public void setCopies(String copies) {
        this.copies = copies;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }
}

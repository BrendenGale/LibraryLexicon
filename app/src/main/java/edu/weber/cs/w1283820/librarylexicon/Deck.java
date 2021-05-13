package edu.weber.cs.w1283820.librarylexicon;

public class Deck {

    private String format;
    private String legal;
    private String name;
    private String owner;

    private Deck() {

    }

    private Deck(String format, String legal, String name, String owner){
        this.format = format;
        this.legal = legal;
        this.name = name;
        this.owner = owner;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLegal() {
        return legal;
    }

    public void setLegal(String legal) {
        this.legal = legal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}

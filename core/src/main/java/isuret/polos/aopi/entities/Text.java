package isuret.polos.aopi.entities;

public class Text extends Position {

    public String text;
    public int size;
    public int color;

    public Text(String text, float x, float y, int size, int color) {
        super(x, y);
        this.text = text;
        this.size = size;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public int getSize() {
        return size;
    }

    public int getColor() {
        return color;
    }
}

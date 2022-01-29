package fzmm.zailer.me.utils.position;

public class PosF extends Pos<Float> {
    public PosF(Float x, Float y) {
        super(x, y);
    }

    public void invertX() {
        this.x *= -1f;
    }

    public void invertY() {
        this.y *= -1f;
    }

    public PosF swapValues() {
        float aux = this.x;
        this.x = this.y;
        this.y = aux;
        return this;
    }

    public void add(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public void add(PosF pos) {
        this.add(pos.getX(), pos.getY());
    }
}

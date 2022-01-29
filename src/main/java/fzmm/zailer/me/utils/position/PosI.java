package fzmm.zailer.me.utils.position;

public class PosI extends Pos<Integer> {
    public PosI(int x, int y) {
        super(x, y);
    }

    public void add(int x, int y) {
        this.x += x;
        this.y += y;
    }
}

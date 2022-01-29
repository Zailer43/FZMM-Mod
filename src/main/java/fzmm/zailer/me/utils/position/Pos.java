package fzmm.zailer.me.utils.position;


public class Pos<T extends Number> {
    protected T x;
    protected T y;

    protected Pos(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return this.x;
    }

    public T getY() {
        return this.y;
    }

}

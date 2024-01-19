package types;

public class CartItem {
    private final int movieID;
    private int quantity;

    public CartItem(int mID, int q ) {
        movieID = mID;
        quantity = q;
    }

    public void changeQuantity(int newQ){
        quantity = newQ;
    }

    public int getMovieID()
    {
        return movieID;
    }

    public int getQuantity()
    {
        return quantity;
    }

}

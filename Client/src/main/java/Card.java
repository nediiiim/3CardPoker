import java.io.Serializable;

// Class represents a single playing card (suit and value)
public class Card implements Serializable{
	private static final long serialVersionUID = 1L; // Required variable for Serializable
	
	private String suit; // Suit of the card ("Hearts", "Diamonds", "Spades", "Clubs")
	private int value; // Value of card (2-10 for numbered cards, 11 = Jack, 12 = Queen, 13 = King, 14 = Ace)
	
	// Constructor with suit and value parameters
	public Card(String suit, int value) {
		this.suit = suit;
		this.value = value;
	}
	
	// Getters/Setters
	public String getSuit() { return suit; }
	public int getValue() { return value; }
}

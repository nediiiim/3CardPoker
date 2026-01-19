import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Represents a standard 52-card deck
public class CardDeck {
	private List<Card> cards; // List that holds all cards in the deck
	
	// Constructor with no parameters
	// Creates all cards and stores in list
	public CardDeck() {
		cards = new ArrayList<>();
		
		int[] values = {14, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}; // Stores all possible card values
		String[] suits = {"Diamonds", "Hearts", "Spades", "Clubs"}; // Stores all four suits
		
		// Creates all 52 combinations of suits and values
		for (String suit : suits) {
			for (int value : values) {
				cards.add(new Card(suit, value));
			}
		}
	}
	
	// Randomly shuffles the deck list using Collections
	public void shuffle() {
		Collections.shuffle(cards);
	}
	
	// Deals (removes and returns) the top of the card deck
	// Returns the card removed or null if deck is empty
	public Card dealCard() {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.remove(0);
	}
}

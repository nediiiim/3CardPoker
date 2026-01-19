import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// Represents a 3-card poker hand (player or dealer)
public class Hand implements Serializable, Comparable<Hand>{
	private static final long serialVersionUID = 1L; // Required variable for Serializable
	
	private ArrayList<Card> cards; // Stores exactly 3 cards
	
	public static final int QUEEN_VALUE = 12; // Dealer qualification value
	
	// Constructor, no parameters, initializes array of cards (empty hand)
	public Hand() {
		cards = new ArrayList<>();
	}
	
	// Adds card to hand if hand isn't full
	public void addCard(Card card) { 
		if (cards.size() < 3) {
			cards.add(card); 
		}
	}
	
	// Empties hand
	public void clear() { 
		cards.clear(); 
	} 
	
	// Helper function to sort cards by rank value
	private void sort() {
		Collections.sort(cards, Comparator.comparingInt(Card::getValue));
	}
	
	// Helper method to calculate if hand is a flush
	// Flush being all 3 cards have same suit
	private boolean isFlush() {
		return cards.get(0).getSuit().equals(cards.get(1).getSuit()) &&
			   cards.get(0).getSuit().equals(cards.get(2).getSuit());
	}
	
	// Helper method to calculate if hand is a straight
	// Straight being 3 cards values in order (ex. 2, 3, 4)
	// Handles exception of A, 2, 3 where A is worth 14 instead of 1
	private boolean isStraight() {
		sort(); // Sorts card from lowest to highest
		
		int value0 = cards.get(0).getValue();
		int value1 = cards.get(1).getValue();
		int value2 = cards.get(2).getValue();
		
		if (value0 == 2 && value1 == 3 && value2 == 14) {
			return true;
		}
		
		return value1 == value0 + 1 && value2 == value1 + 1;
	}
	
	// Helper method to calculate if hand is a three of a kind
	// Three of a kind being 3 cards of same value
	private boolean isThreeOfKind() {
		return cards.get(1).getValue() == cards.get(0).getValue() &&
			   cards.get(2).getValue() == cards.get(1).getValue();
	}
	
	// Helper method to calculate if hand is a pair
	// Pair being any 2 of the 3 cards have the same value
	private boolean isPair() {
		return cards.get(0).getValue() == cards.get(1).getValue() ||
			   cards.get(1).getValue() == cards.get(2).getValue() ||
			   cards.get(0).getValue() == cards.get(2).getValue();
	}
	
	// Returns the value of the highest card in the hand
	public int highestCardValue() {
		sort();
		return cards.get(2).getValue();
	}
	
	// Helper method to rank the hands
	private int getRankValue(String rank) {
		switch (rank) {
			case "Straight Flush":
				return 6;
			case "Three of a Kind":
				return 5;
			case "Straight":
				return 4;
			case "Flush":
				return 3;
			case "Pair":
				return 2;
			default:
				return 1;
		}
	}

	// Compares player hand to dealer hand
	// Returns > 0 if this hand wins, < 0 if this hand loses, 0 if tied
	public int compareTo(Hand dealerHand) {
		int playerHandRank = getRankValue(this.getRank());
		int dealerHandRank = getRankValue(dealerHand.getRank());
		
		// If ranks are different, then compare based on strength of ranks
		if (playerHandRank != dealerHandRank) {
			return Integer.compare(playerHandRank, dealerHandRank);
		}
		
		// Else if same rank, compare highest cards
		this.sort();
		dealerHand.sort();
		
		// Compare each card one by one, starting from highest card
		for (int i = 2; i >= 0; i--) {
			int difference = Integer.compare(this.cards.get(i).getValue(), dealerHand.cards.get(i).getValue());
			if (difference != 0) {
				return difference;
			}
		}
		
		return 0;
	}

	// Returns String of hand's rank (ex: "Straight")
	public String getRank() {
		sort();
		
		if (isStraight() && isFlush()) {
			return "Straight Flush";
		} else if (isThreeOfKind()) {
			return "Three of a Kind";
		} else if (isStraight()) {
			return "Straight";
		} else if (isFlush()) {
			return "Flush";
		} else if (isPair()) {
			return "Pair";
		} else {
			return "High Card";
		}
	}
	
	// Getters/Setters
	public ArrayList<Card> getCards() { return cards; }
}

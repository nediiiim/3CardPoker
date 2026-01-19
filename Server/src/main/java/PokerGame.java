// Class manages one player's deck, bets, and outcomes, and all rules/calculations
public class PokerGame {
	private CardDeck deck;
	private Hand playerHand, dealerHand;
	private int anteBet, pairPlusBet, playBet, totalWinnings;
	
	// Constructor with no parameters, initializes all objects
	public PokerGame() {
		deck = new CardDeck();
		playerHand = new Hand();
		dealerHand = new Hand();
		totalWinnings = 0;
	}
	
	// Starts new game by clearing hands, shuffling deck, and resetting bets to 0
	public void startNewGame() {
		deck.shuffle();
		playerHand.clear();
		dealerHand.clear();
	}
	
	// Deals three cards to dealer and player
	public void dealHands() {
		for (int i = 0; i < 3; i++) {
			dealerHand.addCard(deck.dealCard());
			playerHand.addCard(deck.dealCard());
		}
	}
	
	// Starts new round, meaning new game + dealing cards
	public PokerInfo startNewRound() {
		deck = new CardDeck();
		deck.shuffle();
		
		playerHand = new Hand();
		dealerHand = new Hand();
		
		dealHands();
		
		PokerInfo info = new PokerInfo("ROUND_STARTED");
		
		info.setDealerHand(dealerHand);
		info.setPlayerHand(playerHand);
		info.setResultMessage("New round started. Good luck!");
		
		return info;
	}
	
	// Checks if the dealers hand qualifies (Queen-high or better)
	// Returns true if dealer qualifies, false if not
	public boolean evaluateDealer() {
		String rank = dealerHand.getRank();
		
		if (!rank.equals("High Card")) {
			return true;
		}
		
		return dealerHand.highestCardValue() >= Hand.QUEEN_VALUE;
	}
	
	// Calculates Pair Plus bet payout and returns amount as int
	public int calculatePairPlusNet() {
		if (pairPlusBet <= 0) {
			return 0; // No pair plus bet placed
		}
		
		int payoutMultiplier = 0;
		String handRank = playerHand.getRank(); // Gets player's hand rank, ex: Straight
		
		// Payout structure for Pair Plus
		switch(handRank) {
			case "Straight Flush":
				payoutMultiplier = 40;
				break;
			case "Three of a Kind":
				payoutMultiplier = 30;
				break;
			case "Straight":
				payoutMultiplier = 6;
				break;
			case "Flush":
				payoutMultiplier = 3;
				break;
			case "Pair":
				payoutMultiplier = 1;
				break;
			default:
				payoutMultiplier = 0;
		}
		
		int netChange;
		
		if (payoutMultiplier > 0) {
			netChange = pairPlusBet * payoutMultiplier;
		} else {
			netChange = -pairPlusBet;
		}
		
		totalWinnings += netChange;
		
		return netChange;
	}
	
	// Plays round, meaning evaluate hands and calculate winnings
	public PokerInfo playRound() {
		int before = totalWinnings;
		
		boolean dealerQualified = evaluateDealer();
		int comparison = playerHand.compareTo(dealerHand);
		
		String playerRank = playerHand.getRank();
		String dealerRank = dealerHand.getRank();
		
		int antePlayDelta = 0;
		
		// Resolve main game bets (ante + play)
		if (!dealerQualified) {
			antePlayDelta = 0;
		} else if (comparison > 0) {
			antePlayDelta = anteBet + playBet;
			totalWinnings += antePlayDelta;
		} else if (comparison < 0) {
			antePlayDelta = -(anteBet + playBet);
			totalWinnings += antePlayDelta;
		} else {
			antePlayDelta = 0;
		}
		
		int pairPlusNet = calculatePairPlusNet();
		
		int after = totalWinnings;
		int roundDelta = after - before;
		
		String outcomeLine;
		if (roundDelta > 0) {
			outcomeLine = "You Won $" + roundDelta;
		} else if (roundDelta < 0) {
			outcomeLine = "You Lost $" + Math.abs(roundDelta);
 		} else {
 			outcomeLine = "Push";
 		}
		
		String reasonLine;
		if (!dealerQualified) {
			reasonLine = "Dealer did not qualify";
		} else if (comparison > 0) {
			reasonLine = "You won with " + playerRank;
		} else if (comparison < 0) {
			reasonLine = "Dealer won with " + dealerRank;
		} else {
			reasonLine = "Tie - both hands are equal";
		}
		
		String pairPlusLine;
		if (pairPlusBet <= 0) {
			pairPlusLine = "Pair Plus: $0";
		} else if (pairPlusNet > 0) {
			pairPlusLine = "Pair Plus: Won $" + pairPlusNet;
		} else if (pairPlusNet < 0) {
			pairPlusLine = "Pair Plus: Lost $" + Math.abs(pairPlusNet);
		} else {
			pairPlusLine = "Pair Plus: $0";
		}
		
		String fullMessage = outcomeLine + "\n" + reasonLine + "\n" + pairPlusLine;
		
		PokerInfo result = new PokerInfo("ROUND_RESULT");
		
		result.setResultMessage(fullMessage);
		result.setDealerHand(dealerHand);
		result.setPlayerHand(playerHand);
		result.setTotalWinnings(totalWinnings);
		result.setRoundDelta(roundDelta);
		
		return result;
	}
	
	// Plays out fold action
	public PokerInfo foldRound() {
		int anteLoss = anteBet;
		int pairPlusLoss = pairPlusBet;
		int roundDelta = -(anteLoss + pairPlusLoss);
		
		totalWinnings += roundDelta;
		
		String outcomeLine = "You Lost $" + Math.abs(roundDelta);
		
		String reasonLine = "You folded this round";
		
		String pairPlusLine;
		if (pairPlusBet > 0) {
			pairPlusLine = "Pair Plus: Lost $" + pairPlusLoss;
		} else {
			pairPlusLine = "Pair Plus: $0";
		}

		String fullMessage = outcomeLine + "\n" + reasonLine + "\n" + pairPlusLine;
		
		PokerInfo result = new PokerInfo("ROUND_RESULT");
		
		result.setResultMessage(fullMessage);
		result.setDealerHand(dealerHand);
		result.setPlayerHand(playerHand);
		result.setTotalWinnings(totalWinnings);
		result.setRoundDelta(roundDelta);
		
		return result;
	}
	
	// Resets winnings, bets, and hands
	// Returns PokerInfo to confirm reset
	public PokerInfo resetGame() {
		totalWinnings = 0;
		anteBet = 0;
		pairPlusBet = 0;
		playBet = 0;
		
		deck = new CardDeck();
		playerHand = new Hand();
		dealerHand = new Hand();
		
		deck.shuffle();
		
		PokerInfo info = new PokerInfo("RESET_CONFIRM");
		info.setTotalWinnings(totalWinnings);
		info.setResultMessage("Game reset. Ready for a fresh start!");
		
		return info;
	}
	
	// Getters/Setters
	public Hand getPlayerHand() { return playerHand; }
	public Hand getDealerHand() { return dealerHand; }
	public int getAnteBet() { return anteBet; }
	public void setAnteBet(int bet) { anteBet = bet; }
	public int getPlayBet() { return playBet; }
	public void setPlayBet(int bet) { playBet = bet; }
	public int getPairPlusBet() { return pairPlusBet; }
	public void setPairPlusBet(int bet) { pairPlusBet = bet; }
	public int getTotalWinnings() { return totalWinnings; }
	
}

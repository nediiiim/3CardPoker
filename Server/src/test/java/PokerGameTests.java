import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PokerGameTests {

	private PokerGame game;
	
	@BeforeEach
	void setup() {
		game = new PokerGame();
	}
	
	@Test
	void dealerDoesNotQualifyTest() {
		Hand dealer = game.getDealerHand();
		
		dealer.clear();
		dealer.addCard(new Card("Clubs", 11));
		dealer.addCard(new Card("Hearts", 5));
		dealer.addCard(new Card("Diamonds", 3));

		assertFalse(game.evaluateDealer(), "Jack High shouldn't qualify dealer");
	}

	@Test
	void dealerQualifiesQueenHighTest() {
		Hand dealer = game.getDealerHand();
		
		dealer.clear();
		dealer.addCard(new Card("Clubs", 12));
		dealer.addCard(new Card("Hearts", 5));
		dealer.addCard(new Card("Diamonds", 3));

		assertTrue(game.evaluateDealer(), "Queen High should qualify dealer");
	}
	
	@Test
	void dealerQualifiesPairTest() {
		Hand dealer = game.getDealerHand();
		
		dealer.clear();
		dealer.addCard(new Card("Clubs", 10));
		dealer.addCard(new Card("Hearts", 10));
		dealer.addCard(new Card("Diamonds", 3));

		assertTrue(game.evaluateDealer(), "Pair should qualify dealer");
	}
	
	@Test
	void pairPlusNoBetTest() {
		game.setPairPlusBet(0);
		assertEquals(0, game.calculatePairPlusNet(), "No Pair Plus bet should equal to $0 won from it");
	}
	
	@Test
	void pairPlusLosesTest() {
		Hand player = game.getPlayerHand();
		game.setPairPlusBet(10);
		
		player.clear();
		player.addCard(new Card("Clubs", 11));
		player.addCard(new Card("Hearts", 5));
		player.addCard(new Card("Diamonds", 3));
		
		assertEquals(-10, game.calculatePairPlusNet(), "Betting $10 on Pair Plus and not getting a rank should result in -10");
	}
	
	@Test
	void pairPlusPairWinsTest() {
		Hand player = game.getPlayerHand();
		game.setPairPlusBet(10);
		
		player.clear();
		player.addCard(new Card("Clubs", 5));
		player.addCard(new Card("Hearts", 5));
		player.addCard(new Card("Diamonds", 3));
		
		assertEquals(10, game.calculatePairPlusNet(), "Betting $10 on Pair Plus and getting a pair should result in 10");
	}
	
	@Test
	void pairPlusStraightWinsTest() {
		Hand player = game.getPlayerHand();
		game.setPairPlusBet(10);
		
		player.clear();
		player.addCard(new Card("Clubs", 5));
		player.addCard(new Card("Hearts", 6));
		player.addCard(new Card("Diamonds", 4));
		
		assertEquals(60, game.calculatePairPlusNet(), "Betting $10 on Pair Plus and getting a straight should result in 60");
	}
	
	@Test
	void playRoundNotQualifiedTest() {
		Hand player = game.getPlayerHand();
		Hand dealer = game.getDealerHand();
		
		player.clear();
		dealer.clear();
		
		player.addCard(new Card("Clubs", 10));
		player.addCard(new Card("Hearts", 9));
		player.addCard(new Card("Diamonds", 4));
		
		dealer.addCard(new Card("Clubs", 5));
		dealer.addCard(new Card("Hearts", 11));
		dealer.addCard(new Card("Diamonds", 3));
		
		game.setAnteBet(10);
		game.setPlayBet(10);
		game.setPairPlusBet(0);
		
		PokerInfo info = game.playRound();
		
		assertEquals(0, info.getRoundDelta(), "Dealer with Jack High should push");
	}
	
	@Test
	void playRoundPlayerWinsTest() {
		Hand player = game.getPlayerHand();
		Hand dealer = game.getDealerHand();
		
		player.clear();
		dealer.clear();
		
		player.addCard(new Card("Clubs", 9));
		player.addCard(new Card("Hearts", 9));
		player.addCard(new Card("Diamonds", 4));
		
		dealer.addCard(new Card("Clubs", 8));
		dealer.addCard(new Card("Hearts", 8));
		dealer.addCard(new Card("Diamonds", 3));
		
		game.setAnteBet(10);
		game.setPlayBet(10);
		game.setPairPlusBet(0);
		
		PokerInfo info = game.playRound();
		
		assertEquals(20, info.getRoundDelta(), "Players pair is higher than dealers pair");
	}
	
	@Test
	void playRoundPlayerLosesTest() {
		Hand player = game.getPlayerHand();
		Hand dealer = game.getDealerHand();
		
		player.clear();
		dealer.clear();
		
		player.addCard(new Card("Clubs", 9));
		player.addCard(new Card("Hearts", 9));
		player.addCard(new Card("Diamonds", 4));
		
		dealer.addCard(new Card("Clubs", 10));
		dealer.addCard(new Card("Hearts", 10));
		dealer.addCard(new Card("Diamonds", 3));
		
		game.setAnteBet(10);
		game.setPlayBet(10);
		game.setPairPlusBet(0);
		
		PokerInfo info = game.playRound();
		
		assertEquals(-20, info.getRoundDelta(), "Dealers pair is higher than players pair");
	}
	
	@Test
	void playRoundPlayerTiesTest() {
		Hand player = game.getPlayerHand();
		Hand dealer = game.getDealerHand();
		
		player.clear();
		dealer.clear();
		
		player.addCard(new Card("Diamonds", 10));
		player.addCard(new Card("Spades", 10));
		player.addCard(new Card("Diamonds", 4));
		
		dealer.addCard(new Card("Clubs", 10));
		dealer.addCard(new Card("Hearts", 10));
		dealer.addCard(new Card("Hearts", 4));
		
		game.setAnteBet(10);
		game.setPlayBet(10);
		game.setPairPlusBet(0);
		
		PokerInfo info = game.playRound();
		
		assertEquals(0, info.getRoundDelta(), "Dealer and player have equal hands");
	}
	
	@Test
	void foldRoundNoPairPlusTest() {
		game.setAnteBet(10);
		game.setPairPlusBet(0);
		
		PokerInfo info = game.foldRound();
		
		assertEquals(-10, info.getRoundDelta(), "Folding results in loss of ante bet");
	}
	
	@Test
	void foldRoundPairPlusTest() {
		game.setAnteBet(10);
		game.setPairPlusBet(15);
		
		PokerInfo info = game.foldRound();
		assertEquals(-25, info.getRoundDelta(), "Folding results in lose of ante bet and pair plus bet");
	}
	
	@Test
	void resetGameTest() {
		game.setAnteBet(10);
		game.setPlayBet(10);
		game.setPairPlusBet(5);
		game.foldRound();
		
		PokerInfo info = game.resetGame();
		
		assertEquals(0, game.getTotalWinnings(), "Resetting game should set total winnings to 0");
		assertEquals(0, game.getAnteBet(), "Resetting game should set ante bet to 0");
		assertEquals(0, game.getPlayBet(), "Resetting game should set play bet to 0");
		assertEquals(0, game.getPairPlusBet(), "Resetting game should set pair plus bet to 0");
		
		assertEquals("Game reset. Ready for a fresh start!", info.getResultMessage(), "Resetting should send correct message");
	}
}
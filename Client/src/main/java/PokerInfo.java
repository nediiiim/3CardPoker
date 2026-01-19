import java.io.Serializable;

// Serializable container for data exchange between server and client
public class PokerInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String messageType, resultMessage, clientName;
	private int anteBet, pairPlusBet, playBet, totalWinnings, roundDelta;
	private Hand dealerHand, playerHand;
	
	public PokerInfo(String messageType) {
		this.messageType = messageType;
	}
	
	// Getters/Setters

	public String getMessageType() { return messageType; }
	public void setMessageType(String type) { messageType = type; }
	public String getResultMessage() { return resultMessage; }
	public void setResultMessage(String message) { resultMessage = message; }
	public String getClientName() { return clientName; }
	public void setClientName(String name) { clientName = name; }
	public int getAnteBet() { return anteBet; }
	public void setAnteBet(int bet) { anteBet = bet; }
	public int getPairPlusBet() { return pairPlusBet; }
	public void setPairPlusBet(int bet) { pairPlusBet = bet; }
	public int getPlayBet() { return playBet; }
	public void setPlayBet(int bet) { playBet = bet; }
	public int getTotalWinnings() { return totalWinnings; }
	public void setTotalWinnings(int total) { totalWinnings = total; }
	public int getRoundDelta() { return roundDelta; }
	public void setRoundDelta(int delta) { roundDelta = delta; }
	public Hand getDealerHand() { return dealerHand; }
	public void setDealerHand(Hand hand) { dealerHand = hand; }
	public Hand getPlayerHand() { return playerHand; }
	public void setPlayerHand(Hand hand) { playerHand = hand; }
	
}

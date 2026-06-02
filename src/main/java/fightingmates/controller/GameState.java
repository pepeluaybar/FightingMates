package fightingmates.controller;

import java.util.List;

/**
 * Estado inmutable que la vista puede leer sin acceder directamente al modelo.
 */
public final class GameState {
    private final PlayerState currentPlayer;
    private final PlayerState rivalPlayer;
    private final List<CardState> hand;
    private final List<UnitState> currentBoard;
    private final List<UnitState> rivalBoard;
    private final List<String> logMessages;
    private final String winnerName;
    private final int turnsPlayed;

    public GameState(
            PlayerState currentPlayer,
            PlayerState rivalPlayer,
            List<CardState> hand,
            List<UnitState> currentBoard,
            List<UnitState> rivalBoard,
            List<String> logMessages,
            String winnerName,
            int turnsPlayed
    ) {
        this.currentPlayer = currentPlayer;
        this.rivalPlayer = rivalPlayer;
        this.hand = List.copyOf(hand);
        this.currentBoard = List.copyOf(currentBoard);
        this.rivalBoard = List.copyOf(rivalBoard);
        this.logMessages = List.copyOf(logMessages);
        this.winnerName = winnerName;
        this.turnsPlayed = turnsPlayed;
    }

    public PlayerState getCurrentPlayer() { return currentPlayer; }
    public PlayerState getRivalPlayer() { return rivalPlayer; }
    public List<CardState> getHand() { return hand; }
    public List<UnitState> getCurrentBoard() { return currentBoard; }
    public List<UnitState> getRivalBoard() { return rivalBoard; }
    public List<String> getLogMessages() { return logMessages; }
    public String getWinnerName() { return winnerName; }
    public int getTurnsPlayed() { return turnsPlayed; }
    public boolean hasWinner() { return winnerName != null && !winnerName.isBlank(); }

    public static final class PlayerState {
        private final String name;
        private final int life;
        private final int deckSize;
        private final int discardSize;

        public PlayerState(String name, int life, int deckSize, int discardSize) {
            this.name = name;
            this.life = life;
            this.deckSize = deckSize;
            this.discardSize = discardSize;
        }

        public String getName() { return name; }
        public int getLife() { return life; }
        public int getDeckSize() { return deckSize; }
        public int getDiscardSize() { return discardSize; }
    }

    public static final class CardState {
        private final int handIndex;
        private final String name;
        private final String description;
        private final String type;

        public CardState(int handIndex, String name, String description, String type) {
            this.handIndex = handIndex;
            this.name = name;
            this.description = description;
            this.type = type;
        }

        public int getHandIndex() { return handIndex; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getType() { return type; }

        @Override
        public String toString() {
            return handIndex + " - " + name + " (" + type + ")";
        }
    }

    public static final class UnitState {
        private final int position;
        private final String name;
        private final int attack;
        private final int effectiveAttack;
        private final int life;
        private final int maxLife;
        private final boolean active;
        private final String status;
        private final boolean empty;

        private UnitState(
                int position,
                String name,
                int attack,
                int effectiveAttack,
                int life,
                int maxLife,
                boolean active,
                String status,
                boolean empty
        ) {
            this.position = position;
            this.name = name;
            this.attack = attack;
            this.effectiveAttack = effectiveAttack;
            this.life = life;
            this.maxLife = maxLife;
            this.active = active;
            this.status = status;
            this.empty = empty;
        }

        public static UnitState empty(int position) {
            return new UnitState(position, "Vacío", 0, 0, 0, 0, false, "", true);
        }

        public static UnitState occupied(
                int position,
                String name,
                int attack,
                int effectiveAttack,
                int life,
                int maxLife,
                boolean active,
                String status
        ) {
            return new UnitState(position, name, attack, effectiveAttack, life, maxLife, active, status, false);
        }

        public int getPosition() { return position; }
        public String getName() { return name; }
        public int getAttack() { return attack; }
        public int getEffectiveAttack() { return effectiveAttack; }
        public int getLife() { return life; }
        public int getMaxLife() { return maxLife; }
        public boolean isActive() { return active; }
        public String getStatus() { return status; }
        public boolean isEmpty() { return empty; }

        @Override
        public String toString() {
            if (empty) {
                return position + " - [vacío]";
            }

            String activeText = active ? "activa" : "agotada";
            String statusText = status == null || status.isBlank() ? "" : " | " + status;
            return position + " - " + name + " | ATK " + effectiveAttack + " | VIDA "
                    + life + "/" + maxLife + " | " + activeText + statusText;
        }
    }
}

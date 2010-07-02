package org.springframework.beans.factory.support;

/**
 * Created by IntelliJ IDEA.
 * User: stevend
 * Date: 23-feb-2006
 * Time: 18:20:46
 * To change this template use File | Settings | File Templates.
 */
public class Match {
    private Player player1;
    private Player player2;
    private long id;

    public Match(Player player1, Player player2, long id) {
        this.player1 = player1;
        this.player2 = player2;
        this.id = id;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public long getId() {
        return id;
    }
}

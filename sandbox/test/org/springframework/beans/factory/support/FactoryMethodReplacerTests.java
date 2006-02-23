package org.springframework.beans.factory.support;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Created by IntelliJ IDEA.
 * User: stevend
 * Date: 23-feb-2006
 * Time: 18:28:29
 * To change this template use File | Settings | File Templates.
 */
public class FactoryMethodReplacerTests extends AbstractDependencyInjectionSpringContextTests {
    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:org/springframework/beans/factory/support/factory-methods.xml"
        };
    }

    private MatchManager matchManager;

    public void setMatchManager(MatchManager matchManager) {
        this.matchManager = matchManager;
    }

    public void testCreateMatch() {
        Player player1 = new Player();
        Player player2 = new Player();
        long id = 100000;

        Match match = matchManager.createMatch(player1, player2, id);

        assertSame(player1, match.getPlayer1());
        assertSame(player2, match.getPlayer2());
        assertEquals(id, match.getId());
    }
}


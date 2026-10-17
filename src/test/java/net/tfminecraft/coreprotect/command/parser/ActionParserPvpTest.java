package net.tfminecraft.coreprotect.command.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import net.tfminecraft.coreprotect.model.action.LookupActions;

class ActionParserPvpTest {
    @ParameterizedTest
    @CsvSource({ "pvp,14;15;16", "combat,14;15;16", "hit,14", "damage,14", "knockout,15", "ko,15", "pvp-kill,16", "player-kills,16", "kill,3" })
    void pvpActionsParseToTheirOwnIds(String argument, String expected) {
        List<Integer> actions = ActionParser.parseAction(new String[] { "lookup", "a:" + argument }, true);
        assertEquals(java.util.Arrays.stream(expected.split(";")).map(Integer::valueOf).toList(), actions);
    }

    @Test
    void pvpActionsCombine() {
        assertEquals(List.of(LookupActions.PVP_HIT, LookupActions.PVP_KNOCKOUT), ActionParser.parseAction(new String[] { "lookup", "a:hit,knockout" }, true));
    }
}

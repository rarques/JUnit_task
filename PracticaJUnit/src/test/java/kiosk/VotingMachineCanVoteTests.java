package kiosk;

import mocks.ValidationServiceOkay;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author rav3
 */
public class VotingMachineCanVoteTests {

    private VotingMachine votingMachine;

    public VotingMachineCanVoteTests() {
    }

    @Before
    public void setUpVotingMachine() {
        votingMachine = new VotingMachine();
        votingMachine.setValidationService(new ValidationServiceOkay());
    }

    @Test
    public void voterCanVote() {
        votingMachine.activateEmission(new ActivationCard("valid_code"));
        assertTrue(votingMachine.canVote());
    }

    @Test
    public void voterCannotVoteEmissionNotActivated() {
        assertFalse(votingMachine.canVote());
    }

}

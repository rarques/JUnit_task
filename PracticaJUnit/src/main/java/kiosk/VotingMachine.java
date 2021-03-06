package kiosk;

import data.IrisScan;
import data.MailAddress;
import data.Signature;
import data.Vote;
import java.util.Arrays;
import services.IrisScanner;
import services.MailerService;
import services.SignatureService;
import services.ValidationService;
import services.VotePrinter;
import services.VotesDB;

/**
 *
 * @author rav3
 */
/**
 * Implements a simplification of Use Case: Emit Vote
 */
public class VotingMachine {

    private ValidationService validationService;
    private VotePrinter votePrinter;
    private VotesDB votesDB;
    private SignatureService signatureService;
    private MailerService mailerService;
    private IrisScanner irisScanner;

    private boolean activated;
    private boolean hasVoted;
    private ActivationCard cardForVote;
    private Vote vote;

    public VotingMachine() {
        this.activated = false;
        this.hasVoted = false;
    }

    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    public void setVotePrinter(VotePrinter votePrinter) {
        this.votePrinter = votePrinter;
    }

    public void setVotesDB(VotesDB votesDB) {
        this.votesDB = votesDB;
    }

    public void setSignatureService(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    public void setMailerService(MailerService mailerService) {
        this.mailerService = mailerService;
    }

    public void setIrisScanner(IrisScanner irisScanner) {
        this.irisScanner = irisScanner;
    }

    public void activateEmission(ActivationCard card)
            throws IllegalStateException {

        if (this.activated) {
            throw new IllegalStateException("VotingMachine already activated.");
        }
        if (card.getIrisScan().isPresent()) {
            // Biometric code detected
            if (biometricValidation(card)) {
                activateMachine(card);
            }
        } else if (validationService.validate(card)) {
            // Card is valid
            activateMachine(card);
        }
    }

    public boolean canVote() {
        return this.activated && !this.hasVoted;
    }

    public void vote(Vote vote)
            throws IllegalStateException {
        if (!canVote()) {
            throw new IllegalStateException("Can't vote, machine not activated");
        }

        this.vote = vote;
        this.votePrinter.print(this.vote);
        this.votesDB.registerVote(this.vote);
        this.validationService.deactivate(cardForVote);
        this.activated = false;
        this.hasVoted = true;

    }

    public void sendReceipt(MailAddress mailAddress)
            throws IllegalStateException {
        if (!this.activated && !this.hasVoted) {
            throw new IllegalStateException("Can't send receipt, machine not activated");
        } else if (!this.hasVoted) {
            throw new IllegalStateException("Can't send receipt, not voted yet");
        }

        Signature signature = this.signatureService.sign(this.vote);
        this.mailerService.send(mailAddress, signature);
    }

    private boolean biometricValidation(ActivationCard card) {
        IrisScan cardIris = card.getIrisScan().get();
        IrisScan scannedIris = this.irisScanner.scan();
        byte[] cardCode = cardIris.getIrisCode();
        byte[] scannedCode = scannedIris.getIrisCode();
        boolean equalCodes = Arrays.equals(cardCode, scannedCode);
        return equalCodes;
    }

    private void activateMachine(ActivationCard card) {
        this.cardForVote = card;
        this.activated = true;
        this.hasVoted = false;
    }

}

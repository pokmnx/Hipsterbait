package com.hipsterbait.android.other;

import java.util.ArrayList;
import java.util.Random;

public class RotatingTexts {

    public static final String LURE_HIPSTERS = "lureHipsters";
    public static final String HINT_PREVIEW = "hintPreview";
    public static final String HINT_EARNINGS = "hintEarnings";
    public static final String HINT_SHARE = "hintShare";
    public static final String ONE_STAR_RATINGS = "oneStarRatings";
    public static final String TWO_STAR_RATINGS = "twoStarRatings";
    public static final String THREE_STAR_RATINGS = "threeStarRatings";
    public static final String FOUR_STAR_RATINGS = "fourStarRatings";
    public static final String FIVE_STAR_RATINGS = "fiveStarRatings";
    public static final String BAND_FLAGGED = "bandFlagged";

    private static final ArrayList<String> mLureHipstersStrings = new ArrayList<String>() {
        {
            add("Because not all hipsters are as quick as you.");
            add("Some people need it to be kinda obvious. Like drummers.");
            add("Because you’re just that good at hiding tapes. Maybe too good.");
            add("Go on Johnny Cash, write a song about it.");
            add("Because you want people to find your sh#t, right?");
        }
    };
    private static final ArrayList<String> mHintPreviewStrings = new ArrayList<String>() {
        {
            add("Look how rocktacular your hint is!");
            add("Now there’s a good looking hint.");
            add("Lookin’ sharp.");
        }
    };
    private static final ArrayList<String> mHintEarningsStrings = new ArrayList<String>() {
        {
            add("You’ll bag a hundred hipsters with that sick hint (well… maybe)");
            add("Boom. You’re done.");
        }
    };
    private static final ArrayList<String> mHintShareStrings = new ArrayList<String>() {
        {
            add("For those about to rock (we salute you)!");
            add("Go forth and share bountifully.");
            add("C’mon, spread the word.");
        }
    };
    private static final ArrayList<String> mOneStarRatingStrings = new ArrayList<String>() {
        {
            add("Absolute bollocks.");
            add("My ears are bleeding. And not in a good way.");
            add("I wouldn’t listen to this song again if you paid me a hundred bucks.");
            add("This song sounds like your mom looks in the morning.");
            add("It sounds like someone ate a good song last night, and this is what came out.");
            add("Worse than howler monkeys doing it.");
            add("Open Mic Night called and said ‘don’t come back’.");
            add("I’ll need sex and drugs to get over this ‘rock n roll’...");
            add("This track bombs worse than Hiroshima.");
            add("Rubbish mate.");
            add("A hot mess.");
            add("I threw up after I heard this song, and even that sounded better.");
            add("I’ve farted better songs than this.");
            add("If Kurt Cobain heard this song, he’d come back to life and kill himself again.");
            add("I need a shower to wash the pain away.");
        }
    };
    private static final ArrayList<String> mTwoStarRatingStrings = new ArrayList<String>() {
        {
            add("Better than fingernails on a chalkboard, I guess.");
            add("Kinda amateur.");
            add("Not my thing.");
            add("About on par with Mötley Crüe’s followup flop, Theatre of Pain.");
            add("Somebody should Yoko Ono these guys.");
            add("I’d take Van Haggar over this.");
            add("Somebody told them practice was optional?");
            add("Going for the Harley, came out like a Segway.");
            add("Swing and a miss.");
            add("Lukewarm at best.");
            add("Good enough to be on Coldplay’s last album.");
        }
    };
    private static final ArrayList<String> mThreeStarRatingStrings = new ArrayList<String>() {
        {
            add("A decent effort.");
            add("I’m warming up to it. Kinda.");
            add("The beige station wagon of rock, I guess.");
            add("Don’t love it. Don’t hate it.");
            add("Meh.");
            add("If mediocrity’s their thing, they nailed it.");
            add("This sounds like milk. Ya know, fine.");
            add("I could take or leave it.");
            add("Didn’t rock my world but it’s ok.");
            add("‘S cool, ish.");
            add("Generic, not completely blown away.");
        }
    };
    private static final ArrayList<String> mFourStarRatingStrings = new ArrayList<String>() {
        {
            add("Super solid track.");
            add("Decent for sure!");
            add("Nice sound. Nice song. Nice work.");
            add("Yeah, I’d share this with my friends.");
            add("Yup, I approve.");
            add("Glad I found it!");
            add("Pretty cool indeed");
            add("The rock gods are diggin it.");
            add("Nice work team!");
        }
    };
    private static final ArrayList<String> mFiveStarRatingStrings = new ArrayList<String>() {
        {
            add("This baby’s a keeper!");
            add("I wish that was my song!");
            add("Mic drop.");
            add("Why aren't they already famous?!");
            add("I'm never turning this track off.");
            add("Damn talented. Hats off!");
            add("BIG UPS!!!!");
            add("Send this to Bieber; show him what real music sounds like!");
            add("Es el fuego!!!");
            add("The next big thing!");
            add("These guys are too good to miss!");
            add("Absolutely KILLER!");
            add("Effin' rad brah!");
        }
    };
    private static final ArrayList<String> mBandFlaggedStrings = new ArrayList<String>() {
        {
            add("Damn it Janet!");
            add("Ouch!");
            add("Bad review alert...");
            add("It's not going well...");
            add("Trouble brewin'");
            add("We've got a problem.");
            add("Hmmm... this won't work.");
            add("Heads up, sh#t's gone down.");
        }
    };

    public static String getString(String identifier) {
        switch (identifier) {
            case LURE_HIPSTERS:
            default:
                return mLureHipstersStrings.get(new Random().nextInt(mLureHipstersStrings.size()));
            case HINT_PREVIEW:
                return mHintPreviewStrings.get(new Random().nextInt(mHintPreviewStrings.size()));
            case HINT_EARNINGS:
                return mHintEarningsStrings.get(new Random().nextInt(mHintEarningsStrings.size()));
            case HINT_SHARE:
                return mHintShareStrings.get(new Random().nextInt(mHintShareStrings.size()));
            case ONE_STAR_RATINGS:
                return mOneStarRatingStrings.get(new Random().nextInt(mOneStarRatingStrings.size()));
            case TWO_STAR_RATINGS:
                return mTwoStarRatingStrings.get(new Random().nextInt(mTwoStarRatingStrings.size()));
            case THREE_STAR_RATINGS:
                return mThreeStarRatingStrings.get(new Random().nextInt(mThreeStarRatingStrings.size()));
            case FOUR_STAR_RATINGS:
                return mFourStarRatingStrings.get(new Random().nextInt(mFourStarRatingStrings.size()));
            case FIVE_STAR_RATINGS:
                return mFiveStarRatingStrings.get(new Random().nextInt(mFiveStarRatingStrings.size()));
            case BAND_FLAGGED:
                return mBandFlaggedStrings.get(new Random().nextInt(mBandFlaggedStrings.size()));
        }
    }
}

package com.moubassher.accountsfx.accessControl;

public enum PasswordStrength {

    WEAK, MEDIUM, STRONG;

    public double getScore() {
        double score = switch (this)  {
            case WEAK -> 0.33;
            case MEDIUM -> 0.66;
            case STRONG -> 1.0;
        };
        return score;
    }

}

package it.iet.util;

import org.passay.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Component
public class PasswordValidation {

    @Value("${password.lowerCase}")
    private boolean lowerCheck;
    @Value("${password.upperCase}")
    private boolean upperCheck;
    @Value("${password.number}")
    private boolean digitCheck;
    @Value("${password.special}")
    private boolean specialCheck;
    @Value("${password.minLength}")
    private int minLengthCheck;
    @Value("${password.maxLength}")
    private int maxLengthCheck;

    public boolean validPassword(String psw) {
        List<Rule> rules = new ArrayList<>();
        if (lowerCheck)
            rules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
        if (upperCheck)
            rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        if (digitCheck)
            rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));
        if (specialCheck)
            rules.add(new CharacterRule(EnglishCharacterData.Special, 1));
        rules.add(new LengthRule(minLengthCheck, maxLengthCheck));
        var validator = new PasswordValidator(rules);
        var password = new PasswordData(psw);
        RuleResult result = validator.validate(password);
        return result.isValid();

    }

    public String generatePassword() {
        var alphabets = new CharacterRule(EnglishCharacterData.Alphabetical);
        var digits = new CharacterRule(EnglishCharacterData.Digit);
        var special = new CharacterRule(EnglishCharacterData.Special);
        var random = new SecureRandom();
        int pswLength = random.nextInt((maxLengthCheck - minLengthCheck) + 1) + minLengthCheck;
        var passwordGenerator = new PasswordGenerator();
        return passwordGenerator.generatePassword(pswLength, alphabets, digits, special);
    }

}

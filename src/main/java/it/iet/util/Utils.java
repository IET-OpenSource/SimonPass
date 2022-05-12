package it.iet.util;

import it.iet.config.exceptions.impl.BadRequestFormatHttpException;
import it.iet.infrastructure.mongo.entity.user.Authority;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class Utils {

    private Utils() {
    }

    public static String generateCryptId() {
        var objectId = ObjectId.get();
        return generateCryptString(objectId.toString());
    }

    public static String generateCryptString(String str) {
        char[] stringAsArray = str.toCharArray();
        char[] result = new char[stringAsArray.length * 2];
        for (var i = 0; i < stringAsArray.length; i++) {
            result[i * 2] = stringAsArray[i];
            result[i * 2 + 1] = randomCharacter();
        }
        return String.valueOf(result);
    }

    public static Authority containsInAuthorityEnum(String authority) {
        for (Authority a : Authority.values())
            if (a.name().equals(authority))
                return a;
        return null;
    }

    public static String getServerName(String url) {
        var pattern = Pattern.compile("^/(\\w+)");
        var matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    private static char randomCharacter() {
        var r = new SecureRandom();
        double x = r.nextDouble();
        return x < 0.5 ? Character.forDigit(r.nextInt(10), 10) : (char) (r.nextInt(26) + 'a');
    }

    public static Pageable makePageableObject(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        if (!(sortDirection.equals("asc") || sortDirection.equals("desc"))) {
            throw new BadRequestFormatHttpException("Bad order", HttpStatus.BAD_REQUEST);
        }
        Pageable paging;
        if (sortDirection.equals("asc")) {
            paging = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, sortBy));
        } else {
            paging = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        }
        return paging;

    }


}

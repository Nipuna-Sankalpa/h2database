package org.h2.DBInternals;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * Created by yellowflash on 2/23/17.
 */
public class CustomIndex {
    private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int RANDOM_STRING_LENGTH = 9;

    public static void main(String[] args) {
        PrintWriter writer = null;


        try {
            writer = new PrintWriter("sqlQueries.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 9989; i++) {
            byte[] array = new byte[7]; // length is bounded by 7
            new Random().nextBytes(array);
            String saltStr = new String(array, Charset.forName("UTF-8"));
            writer.println("INSERT INTO PERFORMANCE_TEST VALUES(" + i + generateRandomString() + ",'" + generateRandomString() + "','" + generateRandomString() +
                    "'," + new Random()
                    .nextInt(1000) +
                    ",'1981-04-02'," + new Random().nextInt(1000) + ",null,10);");
            writer.println("INSERT INTO PERFORMANCE_TEST VALUES (" +
                    "'" + i + generateRandomString() + "'" +
                    ", '" + new Random().nextInt(1000) + "', " +
                    "'"+generateRandomString()+"', " +
                    "'431'," +
                    " '"+generateRandomString()+"', " +
                    "'2017-02-21'," +
                    " '9876'," +
                    " '7654'," +
                    " '2017-02-21'," +
                    " '876543'" +
                    ");");

        }
        writer.close();
    }

    public static String generateRandomString() {

        StringBuffer randStr = new StringBuffer();
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            int number = getRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    private static int getRandomNumber() {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }
}

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ChatFilter {
    ArrayList<String> badWords = new ArrayList<>();

    public String ChatFilter(String badWordsFileName, String msg) {
        try {
            File f = new File(badWordsFileName);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                badWords.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String passage = msg;
        String[] words = passage.split(" ");
        char[] characters = new char[words.length];
        for (int b = 0; b < words.length; b++) {
            characters[b] = ' ';
        }
        for (int b = 0; b < badWords.size(); b++) {
            for (int a = 0; a < words.length; a++) {
                if (!Character.isLetter(words[a].charAt(words[a].length() - 1)) &&
                        !Character.isWhitespace(words[a].charAt(words[a].length() - 1)) && !(words[a].charAt(0) == '*')) {
                    characters[a] = words[a].charAt(words[a].length() - 1);
                    words[a] = words[a].substring(0, words[a].length() - 1);
                }
            }
            for (int i = 0; i < words.length; i++) {
                if (words[i].equals(badWords.get(b))) {
                    int loop = words[i].length();
                    words[i] = "";
                    for (int j = 0; j < loop; j++) {
                        words[i] = words[i].concat("*");
                    }
                }
            }
            passage = words[0];
            passage += characters[0];
            if (characters[0] != ' ') {
                passage += " ";
            }
            for (int i = 0; i < words.length - 2; i++) {

                passage = passage.concat(words[i + 1]);
                passage += characters[i + 1];
                if (characters[i + 1] != ' ') {
                    passage += " ";
                }
            }
            passage = passage.concat(words[words.length - 1]);
            passage += characters[characters.length - 1];
        }
        return passage;
    }
}





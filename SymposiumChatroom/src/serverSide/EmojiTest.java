package serverSide;

import com.vdurmont.emoji.EmojiParser;
import com.vdurmont.emoji.EmojiParser.FitzpatrickAction;

public class EmojiTest {

	public static void main(String[] args) {
		String str = "An 😀awesome 😃string with a few 😉emojis!";
		String result = EmojiParser.parseToAliases(str);
		System.out.println(result);
		
	}
}

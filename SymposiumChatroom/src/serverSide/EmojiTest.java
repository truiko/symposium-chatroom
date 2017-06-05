package serverSide;

import java.util.ArrayList;

import com.vdurmont.emoji.EmojiParser;
import com.vdurmont.emoji.EmojiParser.FitzpatrickAction;

public class EmojiTest {

	public static void main(String[] args) {
		//String str = "hello my name is gabriel :)";
		//str = checkEmojiFromSymbol(str);
		//System.out.println(str);
		String str = "Hello :) :dollar:";
		str = convertSymbolToEmoji(str);
		System.out.println(str);
	}
	
	private static String changeText(String message){
		String newString = message.replace(message.substring(0,1),"a");
		return newString;
	}
	
	private static String convertStringToEmoji(String message){
		String convertedString = EmojiParser.parseToUnicode(message);
		return convertedString;
		//split message into arrayList
		//check arraylist for any arrays that start with :
		//parseToUnicode that word
		//add everything back together
		//return 
		
	}
	
	private static String convertSymbolToEmoji(String message){
		// this method only used for the type-able Emojis
		String newString ="";
		String[] emojis = {":smiley:", ":wink:", ":slightly_frowning:",
						":upside_down, flipped_face:", ":tired_face:",
						":hushed:", ":blush:", ":expressionless:", ":heart:",
						":broken_heart:"};
		String[] emojiSymbols = {":)", ";)", ":(", "(:", ">.<",
								"o.o",":))", ":|", "<3", "</3"};
		for(int i = 1; i < message.length(); i++){
			for(int j = 0; j < emojis.length; j++){
				if(message.substring(i-1, i+1).equals(emojiSymbols[j])){
					newString = message.replace(message.substring(i-1,i+1), EmojiParser.parseToUnicode(emojis[j]));
				}
			}
		}
		return newString;
	}
}

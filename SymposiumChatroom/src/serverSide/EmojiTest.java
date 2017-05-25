package serverSide;

import com.vdurmont.emoji.EmojiParser;
import com.vdurmont.emoji.EmojiParser.FitzpatrickAction;

public class EmojiTest {

	public static void main(String[] args) {
		String str = "hello my name is gabriel :)";
		str = checkEmojiFromSymbol(str);
		System.out.println(str);
	}
	
	private static String changeText(String message){
		String newString = message.replace(message.substring(0,1),"a");
		return newString;
	}
	
	private static String checkEmojiFromSymbol(String message){
		/* List of Emojis Used for this (so far):
		 * 	frowning face[:(], wink[;)], upside down flipped face[(:"], tired face[>.<],
		 *  hushed[o.o], blushed[:))], expressionless[:|], heart[<3], broken heart[</3] 
		 */
		String newString ="";
		String str = ":smiley:";
		for(int i = 1; i < message.length(); i++){
			if(message.substring(i-1, i+1).equals(":)")){
				newString = message.replace(message.substring(i-1,i+1), EmojiParser.parseToUnicode(str));
			}
		}
		return newString;
	}
}

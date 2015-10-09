import java.util.regex.Pattern;

//Current State: creates a HashMap of words and their associated phonemes
/**
 * The class that organizes the data behind the Speaker GUI and parses all input
 * for the Speaker GUI.
 * 
 * @author Sean Mullan
 * @see SpeakerGUI.java
 * @see Voice.java
 * @see PhonemeDict.java
 */
public class Runner {
	static PhonemeDict phonemes;
	static Voice voice;

	public Runner() {
		phonemes = new PhonemeDict(true);
		voice = new Voice();
	}

	/**
	 * Takes in a list of words then speaks them and converts them into phonemes
	 * 
	 * @param input
	 *            String of words to be parsed into phonemes
	 * @return String of phonemes that represents the input
	 */
	public static String parse(String input) {
		// breaks the input up into words without punctuation or whitespace
		input = input.replaceAll("([.,!?;:]+)", " $1 ");
		String[] splitInput = input.split("[\\s]+");
		// output is a list of the lists of the phonemes
		String[][] output = new String[splitInput.length][];
		int i = 0;
		Pattern stuff = Pattern.compile("[.,!?;:]");
		for (String item : splitInput) {
			String[] newItem;
			if (!stuff.matcher(item).find()) {
				newItem = phonemes.get(item);
			} else {
				newItem = new String[] { item };
			}
			output[i] = newItem;
			i++;
		}
		String finalOutput = speak(output);
		voice.speak(output);
		return finalOutput;
	}

	/**
	 * a test method that prints the phonemes to the terminal
	 * 
	 * @param spoken
	 *            a list of phonemes
	 */
	public static void speak(String[] spoken) {
		for (String item : spoken) {
			System.out.print(item + " ");
		}
		System.out.print("\n");
	}

	/**
	 * Converts an array of phoneme arrays into a single string
	 * 
	 * @param spoken
	 *            the array of phoneme arrays, split up by word
	 * @return a single string of phonemes with sentences split up by white
	 *         space and phonemes connected by hyphens
	 */
	public static String speak(String[][] spoken) {
		String output = "";
		for (String[] item : spoken) {
			for (int i = 0; i < item.length - 1; i++) {
				String phone = item[i];
				output = output + phone + "-";
			}
			output = output + item[item.length - 1] + " ";
		}
		return output;
	}
}

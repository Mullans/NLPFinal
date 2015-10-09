import java.io.File;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.JList;
import javax.swing.JOptionPane;

/**
 * The class that holds a hashmap form of the CMU phoneme dictionary and handles
 * unknown words using a best guess rule system.
 * 
 * @author Sean Mullan
 * @see SpeakerGUI.java
 * @see Voice.java
 * @see Runner.java
 */
public class PhonemeDict {
	HashMap<String, String[]> phonemes;
	HashMap<Character, ArrayList<String>> dictionary;

	public PhonemeDict(boolean a) {
		Character[] alphabet = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
				'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
				'V', 'W', 'X', 'Y', 'Z', '!' };
		phonemes = new HashMap<String, String[]>();
		dictionary = new HashMap<Character, ArrayList<String>>();
		for (Character letter : alphabet) {
			dictionary.put(letter, new ArrayList<String>());
			dictionary.get(letter).add(letter.toString());
		}
		File file = new File("cmudict.txt");
		try {
			Scanner scan = new Scanner(file);
			while (scan.hasNextLine()) {
				String i = scan.nextLine();
				if (i.startsWith(";;;")) {
					continue;
				}
				i = i.replaceAll("\\d*", "");
				String[] split = i.split("\\s+");
				// removes multiple words. Not necessary, but it makes the
				// dictionary smaller overall. Use false if on a slow computer.
				if (a) {
					Pattern pattern = Pattern.compile("[(][)]");
					if (pattern.matcher(split[0]).find()) {
						continue;
					}
				}
				String phoneme = split[0];
				String[] phones = Arrays.copyOfRange(split, 1, split.length);
				// System.out.println(phoneme+' '+phones[phones.length-1]);
				phonemes.put(phoneme, phones);
				// this line was used to see examples of phonemes during rule
				// building
				// if(Arrays.asList(phones).contains("AE")){System.out.println(phoneme);}
				try {
					dictionary.get(phoneme.charAt(0)).add(phoneme);
				} catch (Exception e) {
					dictionary.get('!').add(phoneme);
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a list of phonemes for the input word
	 * 
	 * @param input
	 *            the string that represents the phonemes
	 * @return a list of phonemes that represent the word
	 */
	public String[] get(String input) {
		if (phonemes.containsKey(input.toUpperCase())) {
			return phonemes.get(input.toUpperCase());
		}
		input = speller(input.toUpperCase());
		if (phonemes.containsKey(input.toUpperCase())) {
			return phonemes.get(input.toUpperCase());
		}
		return possibleParse(input);
	}

	/**
	 * Checks the spelling of a word based on possible matches from the CMU
	 * phoneme dictionary
	 * 
	 * @param input
	 *            word to be spell checked
	 * @return the correct spelling of the word as defined by the user
	 */
	public String speller(String input) {
		// creates a list of best matches, then shows a dialog box that gives
		// the user a choice for input
		JList<String> list = new JList<String>(getBestMatches(input));
		JOptionPane.showMessageDialog(null, list, "Did you mean...",
				JOptionPane.PLAIN_MESSAGE);

		String returner = (String) list.getSelectedValue();
		if (returner == null) {
			returner = input + " <No Change>";
		}

		returner = returner.replace("<No Change>", "");
		returner = returner.replace("<No Alternatives>", "");
		return returner;
	}

	/**
	 * Looks at how likely it is that one word is the misspelled version of
	 * another
	 * 
	 * @param string1
	 *            the current spelling of the word
	 * @param string2
	 *            the possible alternative spelling of the word
	 * @return the integer value of the difference between the words
	 */
	public int stringDistance(String string1, String string2) {
		// same string distance formula as from class
		int subCost = 5, insCost = 3, delCost = 3, length1 = string1.length(), length2 = string2
				.length();
		int[][] arr = new int[length1 + 1][length2 + 1];
		for (int i = 1; i < length1 + 1; i++) {
			arr[i][0] = i * insCost;
		}
		for (int j = 1; j < length2 + 1; j++) {
			arr[0][j] = j * delCost;
		}
		for (int i = 1; i < length1 + 1; i++) {
			for (int j = 1; j < length2 + 1; j++) {
				int change = subCost;
				if (string1.charAt(i - 1) == string2.charAt(j - 1)) {
					change = 0;
				}
				arr[i][j] = Math.min(
						arr[i][j - 1] + delCost,
						Math.min(arr[i - 1][j] + insCost, arr[i - 1][j - 1]
								+ change));
			}
		}
		return arr[length1][length2];
	}

	/**
	 * Finds possible alternatives for the input word
	 * 
	 * @param word
	 *            the possible misspelled word
	 * @return an array of up to 6 alternatives for the input word
	 */
	public String[] getBestMatches(String word) {
		int returnNumber = 6, maxDistance = 10;
		final HashMap<String, Integer> possibleWords = new HashMap<String, Integer>();
		// finds a list of all possible alternative words that start with the
		// same letter
		for (String item : dictionary.get(word.charAt(0))) {
			if (Math.abs(item.length() - word.length()) > 3) {
				continue;
			}
			int distance = stringDistance(word, item);
			if (distance <= maxDistance) {
				possibleWords.put(item, distance);
			}
		}
		List<String> wordsByValue = new ArrayList<String>(
				possibleWords.keySet());
		// Sorts the list with the associated value in the hashmap
		Collections.sort(wordsByValue, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return possibleWords.get(o1) - possibleWords.get(o2);
			}
		});
		// sets the number to return to be 6, or the number of possibilities
		if (returnNumber > wordsByValue.size()) {
			returnNumber = wordsByValue.size();
		}
		// adds a tag so that the dialog box shows <No Alternatives>
		if (wordsByValue.size() == 0) {
			return new String[] { word + "<No Alternatives>" };
		}
		String[] toReturn = new String[returnNumber + 1];
		// sets a list for the dialog box that includes the original input with
		// a <No Change> tag
		toReturn[0] = word + " <No Change>";
		for (int i = 1; i < returnNumber + 1; i++) {
			toReturn[i] = wordsByValue.get(i - 1);
		}
		return toReturn;
	}

	/**
	 * Uses a best guess method to parse a word into phonemes.
	 * 
	 * @param input
	 *            word to be parsed
	 * @return an array of the phonemes that make up the word
	 */
	public String[] possibleParse(String input) {
		ArrayList<String> parse = new ArrayList<String>();
		// add list for first letters here
		Pattern vowels = Pattern.compile("[AEIOU]");
		switch (input.charAt(0)) {
		case 'A':
			parse.add("AH");
			break;
		case 'B':
			parse.add("B");
			break;
		case 'C':
			parse.add("K");
			break;
		case 'D':
			parse.add("D");
			break;
		case 'E':
			parse.add("EH");
			break;
		case 'F':
			parse.add("F");
			break;
		case 'G':
			parse.add("G");
			break;
		case 'H':
			parse.add("HH");
			break;
		case 'I':
			parse.add("IH");
			break;
		case 'J':
			parse.add("JH");
			break;
		case 'K':
			parse.add("K");
			break;
		case 'L':
			parse.add("L");
			break;
		case 'M':
			parse.add("M");
			break;
		case 'N':
			parse.add("N");
			break;
		case 'O':
			parse.add("OW");
			break;
		case 'P':
			parse.add("P");
			break;
		case 'Q':
			parse.add("K");
			break;
		case 'R':
			parse.add("R");
			break;
		case 'S':
			parse.add("S");
			break;
		case 'T':
			parse.add("T");
			break;
		case 'U':
			parse.add("AH");
			break;
		case 'V':
			parse.add("V");
			break;
		case 'W':
			parse.add("W");
			break;
		case 'X':
			parse.add("Z");
			break;
		case 'Y':
			parse.add("Y");
			break;
		case 'Z':
			parse.add("Z");
			break;
		default:
			break;
		}
		for (int i = 1; i < input.length(); i++) {
			char a = input.charAt(i);
			char b = input.charAt(i - 1);
			if (a == b) {
				if (a == 'O') {
					parse.remove(parse.size() - 1);
					parse.add("UW");
					continue;
				}
				continue;
			} else if (a == 'H') {
				if (b == 'C') {
					parse.remove(parse.size() - 1);
					parse.add("CH");
					continue;
				} else if (b == 'P') {
					parse.remove(parse.size() - 1);
					parse.add("F");
					continue;
				} else if (b == 'S') {
					parse.remove(parse.size() - 1);
					parse.add("SH");
					continue;
				} else if (b == 'G') {
					if (i > 1) {
						parse.remove(parse.size() - 1);
						parse.add("F");
					}
					continue;
				}
			} else if (a == 'G' && b == 'N') {
				parse.remove(parse.size() - 1);
				parse.add("NG");
				continue;
			} else if (a == 'U' && b == 'Q') {
				parse.add("W");
				continue;
			} else if (a == 'W' && b == 'U') {
				parse.remove(parse.size() - 1);
				parse.add("UW");
				continue;
			} else if (a == 'X' && vowels.matcher("" + b).find()) {
				parse.add("K S");
			} else if (a == 'C') {
				if (vowels.matcher("" + b).find()) {
					if (i + 1 < input.length()) {
						if (vowels.matcher("" + input.charAt(i + 1)).find()) {
							parse.add("S");
						}
					}
				}
			} else {
				// takes care of all other cases
				switch (a) {
				case 'A':
					parse.add("AH");
					break;
				case 'B':
					parse.add("B");
					break;
				case 'C':
					parse.add("K");
					break;
				case 'D':
					parse.add("D");
					break;
				case 'E':
					parse.add("EH");
					break;
				case 'F':
					parse.add("F");
					break;
				case 'G':
					parse.add("G");
					break;
				case 'H':
					parse.add("HH");
					break;
				case 'I':
					parse.add("IH");
					break;
				case 'J':
					parse.add("JH");
					break;
				case 'K':
					parse.add("K");
					break;
				case 'L':
					parse.add("L");
					break;
				case 'M':
					parse.add("M");
					break;
				case 'N':
					parse.add("N");
					break;
				case 'O':
					parse.add("OW");
					break;
				case 'P':
					parse.add("P");
					break;
				case 'Q':
					parse.add("K");
					break;
				case 'R':
					parse.add("R");
					break;
				case 'S':
					parse.add("S");
					break;
				case 'T':
					parse.add("T");
					break;
				case 'U':
					parse.add("AH");
					break;
				case 'V':
					parse.add("V");
					break;
				case 'W':
					parse.add("W");
					break;
				case 'X':
					parse.add("Z");
					break;
				case 'Y':
					parse.add("Y");
					break;
				case 'Z':
					parse.add("Z");
					break;
				default:
					break;
				}
			}
		}
		String[] output = new String[parse.size()];
		for (int i = 0; i < parse.size(); i++) {
			output[i] = parse.get(i);
		}
		return output;
	}

}
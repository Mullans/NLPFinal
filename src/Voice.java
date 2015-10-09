import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.sound.sampled.*;

/**
 * The class that holds the phoneme audio files and outputs sound based on input
 * phoneme lists.
 * 
 * @author Sean Mullan
 * @see SpeakerGUI.java
 * @see Runner.java
 * @see PhonemeDict.java
 */
public class Voice {
	HashMap<String, File> phonemes;
	int checker = 0;
	public Voice() {
		phonemes = new HashMap<String, File>();
		// creates a list of all files in directory 'phones'
		File folder = new File("Phones2");
		File[] listOfFiles = folder.listFiles();
		try {
			for (File phone : listOfFiles) {
				String path = phone.getPath();
				// takes care of hidden files if they are enabled
				if (path.startsWith(".")) {
					continue;
				}
				path = path.replace(".wav", "").replace("Phones2/", "");
				// stores each phone with its label
				phonemes.put(path, phone);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void speak(String[][] input) {
		for (String[] word : input) {
			if (word[0].endsWith(">")) {
				continue;
			}
			Pattern pattern = Pattern.compile("[.,!?;:]");
			if (pattern.matcher(word[0]).find()) {
				continue;
			}
			AudioInputStream phrase = null;
			try {
				phrase = AudioSystem.getAudioInputStream(phonemes.get(word[0]));
				AudioInputStream phone;
				for (int i = 1; i < word.length; i++) {
					if (pattern.matcher(word[i]).find()) {
						continue;
					}
					phone = AudioSystem.getAudioInputStream(phonemes
							.get(word[i]));

					phrase = new AudioInputStream(new SequenceInputStream(
							phrase, phone), phrase.getFormat(),
							phrase.getFrameLength() + phone.getFrameLength());

				}
			} catch (UnsupportedAudioFileException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Clip clip = AudioSystem.getClip();
				clip.open(phrase);
				clip.start();
				while (clip.getMicrosecondLength() != clip
						.getMicrosecondPosition()) {
				}
				// I'm not 100% sure what this does, but it takes a while to do
				// it, so I commented it out
				// clip.close();
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			checkReset();
		}
	}

	// two methods for bug checking
	public void check() {
		checker++;
		System.out.println("check " + checker);
	}

	public void checkReset() {
		checker = 0;
	}
}
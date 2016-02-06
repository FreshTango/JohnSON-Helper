import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class GoogleConvert {

	/**
	This creates the output mp3 file based off the text contents and the language.
	@param destiantion - the requestesd name for the output mp3 file
	       language - the language code of the text (en, es, zh, etc) 
		   snippets - a list of parameter strings that ar eeach less than or
					  equal to 100 characters
	**/
	private static void makeAudio(String destination, String language,
			List<String> snippets) {
		try {
			byte[] buffer = new byte[1 << 20];
			OutputStream os = new FileOutputStream(new File(destination+ ".mp3"));
			boolean shouldAppend = true;
			InputStream in = null;
			for (String snippet : snippets) {
				URLConnection connection = new URL("http://www.translate.google.com/translate_tts?tl="+ language + "&q=" + snippet).openConnection();
				connection.setRequestProperty("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
				connection.connect();
				in = connection.getInputStream();
				int count;
				while ((count = in.read(buffer)) != -1) {
					//write to the output file
					os.write(buffer, 0, count);
					os.flush();
				}
				in.close();
				if (shouldAppend) {
					os = new FileOutputStream(new File(destination + ".mp3"),true);
					shouldAppend = false;
				}
			}
			in.close();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static ArrayList<String> getParams(String text) {
		String paramText = text.replaceAll(" ", "+");
		int startIndex = 0;
		ArrayList<String> result = new ArrayList<String>();
		while (startIndex < text.length() - 1) {
			int endSnippetIndex = getSnippetEnd(paramText, startIndex);
			String snippet = paramText.substring(startIndex, endSnippetIndex);
			if (snippet.length() >= 2) {
				if (snippet.substring(0, 1).equals("+")) {
					snippet = snippet.substring(1);
				}
			}
			result.add(snippet);
			startIndex = endSnippetIndex;
		}
		return result;
	}

	private static int getSnippetEnd(String text, int startIndex) {
		String subtext = (text.substring(startIndex).length() > 100) ? 
		                   text.substring(startIndex, startIndex + 100) : 
		                   text.substring(startIndex);
		int end = subtext.length();
		if (text.substring(startIndex).length() > 100) {
				//while the last character is not a + sign, remove the last character of the substring
				while (!subtext.substring(subtext.length() - 1, subtext.length()).equals("+")) {
					subtext = subtext.substring(0, subtext.length() - 1);
					end--;
				}
				// get rid of the last plus sign
				subtext = subtext.substring(0, subtext.length() - 1);
				end--;
		}
		return end + startIndex;
	}
	public static void textToSpeech(String text, String language, String outputName){
		ArrayList<String> paramSnippets = getParams(text);
		makeAudio(outputName, language, paramSnippets);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String text = "This is a test for google translate text to speech functionality. As you can see, this is over the 100 character limit, but it is no problem if this text is separated into 100 character or less snippets. Also, running this program again with the same destination file name but different text will overwrite the data inside of this mp3 file. I hope you enjoyed this tutorial!";
		textToSpeech(text, "en", "translate");
	}

}
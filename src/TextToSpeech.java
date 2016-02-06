import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class TextToSpeech {

	private static String WEATHER = "http://api.openweathermap.org/data/2.5/weather?q=Austin,TX&units=imperial&APPID=452f600cab86d8a9bc0d071014688aa3";

	public static void main(String[] args) throws IOException {
		String input = (getTime() + getWeather() + getNews());
		System.out.println(input);
		GoogleConvert.textToSpeech(input, "en", "translate");
		say();
	}

	public static String getTime() {
		StringBuilder time = new StringBuilder();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat day = new SimpleDateFormat("EEEE MMMMMMMMMM dd yyyy");
		SimpleDateFormat times = new SimpleDateFormat("h:mm a");
		SimpleDateFormat compare = new SimpleDateFormat("HH");
		int compareTime = Integer.parseInt(compare.format(cal.getTime()));
		if (compareTime >= 0 && compareTime < 12) {
			time.append("Good morning Khiem. ");
		} else if (compareTime >= 12 && compareTime < 17) {
			time.append("Good afternoon Khiem. ");
		} else if (compareTime >= 17 && compareTime <= 24) {
			time.append("Good evening Khiem. ");
		}
		time.append("Today is " + day.format(cal.getTime()) + " at " + times.format(cal.getTime())
				+ ". ");
		return time.toString();
	}

	public static String getWeather() {
		StringBuilder message = new StringBuilder();

		try {
			URL url = new URL(WEATHER);
			InputStream is = url.openStream();
			JsonReader reader = Json.createReader(is);

			message.append("The current weather condition outside is ");
			JsonObject object = reader.readObject();
			JsonArray weather = object.getJsonArray("weather");
			JsonObject indexObject = weather.getJsonObject(0);
			message.append(indexObject.getString("description"));
			JsonObject main = object.getJsonObject("main");
			message.append(" with a temperature of "
					+ (int) main.getJsonNumber("temp").doubleValue() + " degrees, ");
			message.append("and a humidity of " + main.getJsonNumber("humidity").intValue() + "%. ");
			main = object.getJsonObject("wind");
			int windSpeed = (int) main.getJsonNumber("speed").doubleValue();
			if (windSpeed > 10.0) {
				message.append("It's also fairly breezy with wind speeds at " + windSpeed
						+ " miles per hour. ");
			} else {
				message.append("It's also fairly calm with wind speeds at " + windSpeed
						+ " miles per hour. ");
			}
			if (main.getJsonObject("rain") != null) {
				main = object.getJsonObject("rain");
				JsonNumber number = main.getJsonNumber("3h");
				int num = (int) (number.doubleValue() * 100);
				message.append("It also seems like it will rain soon, with a " + num
						+ "% chance of precipitation.");
			}
			reader.close();
			is.close();
		} catch (Exception e) {
			message = new StringBuilder();
			message.append("Sorry, the weather forecast is unavailable.");
		}
		return message.toString();
	}

	// May need to revert to try/catch exception for malformedURL
	public static String getNews() throws IOException {
		URL resUrl = new URL("http://feeds.feedburner.com/Kobcom-AlbuquerqueMetro?format=xml");
		BufferedReader in = new BufferedReader(new InputStreamReader(resUrl.openStream()));
		StringBuilder sourceCode = new StringBuilder();
		String line;
		int count = 0;
		sourceCode.append("Now for local news. ");
		while ((line = in.readLine()) != null && count < 5) {
			if (line.contains("<description>")) {
				int firstPos = line.indexOf("<description>");
				String temp = line.substring(firstPos);
				temp = temp.replace("<description>", "");
				temp = temp.replace("&amp;rsquo;","'");
				int lastPos = temp.indexOf("&lt;");
				temp = temp.substring(0, lastPos);
				sourceCode.append(temp + " ");
				count++;
			}
		}
		sourceCode.append("That is all for today.");
		in.close();
		return sourceCode.toString();
	}

	public static void say() {
		try {
			File f = new File("translate.mp3");
			FileInputStream in = new FileInputStream(f);
			Player p = new Player(in);
			p.play();
			p.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JavaLayerException e) {
			e.printStackTrace();
		}

	}
}
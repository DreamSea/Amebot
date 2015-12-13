package addonOsu;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Scanner;

import org.junit.Test;

import base.ApiConnection;
import base.MessageSender;

public class aoOsuTest {

	private static MockConnections mock = new MockConnections();
	
	@Test
	public void test() {
		long now = System.currentTimeMillis();
		
		aoOsu osu = new aoOsu("testChannel", mock, mock, "jUnit");
		System.out.println(System.currentTimeMillis()-now);
		String[] message = {"","","","_osucount 0 10"};
		osu.checkMessage(message);
		message[3] = "_mania4kfind 3.5";
		osu.checkMessage(message);
		message[3] = "_maNia7kfInd 3.5";
		osu.checkMessage(message);
		message[3] = "_osudiff DreamSea";
		osu.checkMessage(message);
		
		System.out.println(System.currentTimeMillis()-now);
	}

	static class MockConnections implements MessageSender, ApiConnection {

		@Override
		public void sendMessage(String recipient, String message) {
			System.out.println("@"+recipient+": "+message);
		}

		@Override
		public String doGet(String url) throws MalformedURLException {
			System.out.println(url);
			Scanner s;
			String toReturn = null;
			try {
				s = new Scanner(new File("test/addonOsu/fakeApiCall"));
				toReturn = s.nextLine();
				s.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return toReturn;
		}
		
	}

}

package base;

import java.net.MalformedURLException;

public interface ApiConnection {
	String doGet(String url) throws MalformedURLException ;
}

/**
 *
 */
package com.github.publiclibs.httpget2hashdigest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.publicLibs.freedom1b2830.awesomeio.IoUtils;
import com.github.publiclibs.awesome.hash.Hashing;

/**
 * @author freedom1b2830
 * @date 2023-марта-30 19:05:35
 */
public class HttpGetToHashDigest {

	private static final String USER_AGENT_VALUE = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";

	private static final String USER_AGENT = "User-Agent";

	public static ConcurrentMap<String, CopyOnWriteArrayList<URL>> apiGetByStrings(final String[] inputArray)
			throws IOException, NoSuchAlgorithmException {
		final CopyOnWriteArrayList<URL> urls = new CopyOnWriteArrayList<>();
		for (final String urlString : inputArray) {
			urls.addIfAbsent(URI.create(urlString).toURL());
		}
		return apiGetByURLs(urls.toArray(new URL[urls.size()]));
	}

	public static ConcurrentMap<String, CopyOnWriteArrayList<URL>> apiGetByURLs(final URL[] inputArray)
			throws IOException, NoSuchAlgorithmException {
		final ConcurrentHashMap<String, CopyOnWriteArrayList<URL>> pairs = new ConcurrentHashMap<>();
		for (final URL url : inputArray) {
			final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestProperty(USER_AGENT, USER_AGENT_VALUE);
			try (InputStream is = httpURLConnection.getInputStream()) {
				final byte[] bytes = IoUtils.isToBytes(is);
				appendMD5(bytes, url, pairs);
				appendSHA256(bytes, url, pairs);
			}
		}
		return pairs;
	}

	private static void appendMD5(final byte[] bytes, final URL url,
			final ConcurrentMap<String, CopyOnWriteArrayList<URL>> pairs) throws NoSuchAlgorithmException {
		final String hashMD5String = Hashing.convertToStringUpperCase(Hashing.MD5Utils.calcMD5(bytes));
		pairs.computeIfAbsent(hashMD5String, list -> new CopyOnWriteArrayList<>()).addIfAbsent(url);
	}

	private static void appendSHA256(final byte[] bytes, final URL url,
			final ConcurrentMap<String, CopyOnWriteArrayList<URL>> pairs) throws NoSuchAlgorithmException {
		final String hashSHA256String = Hashing.convertToStringUpperCase(Hashing.SHA256Utils.calcSHA256(bytes));
		final CopyOnWriteArrayList<URL> urlMapList2 = pairs.computeIfAbsent(hashSHA256String,
				list -> new CopyOnWriteArrayList<>());
		urlMapList2.addIfAbsent(url);
	}

	public static void main(final String[] args) throws IOException, NoSuchAlgorithmException {
		if (args.length == 0) {
			throw new IllegalArgumentException("need:each arg is URL");
		}
		final ConcurrentMap<String, CopyOnWriteArrayList<URL>> pairs = apiGetByStrings(args);
		print(pairs);
	}

	private static void print(final ConcurrentMap<String, CopyOnWriteArrayList<URL>> pairs) {
		for (final Entry<String, CopyOnWriteArrayList<URL>> entry : pairs.entrySet()) {
			final String hash = entry.getKey();
			final CopyOnWriteArrayList<URL> urlsList = entry.getValue();
			for (final URL url : urlsList) {
				System.err.println(String.format("%s\t%s", hash, url));
			}
		}
	}

}

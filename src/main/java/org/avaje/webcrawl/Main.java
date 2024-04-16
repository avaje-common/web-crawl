package org.avaje.webcrawl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {

  public static void main(String[] args) throws IOException {

    new Main().start();
  }

//  private static final int MAX_DEPTH = 5;

  private final Set<String> exludeUris = new HashSet<>();

  private HashSet<String> urlRegistry = new HashSet<>();

  private Output output;

  Main() throws IOException {

    exludeUris.add("/docs");
    exludeUris.add("/docs/");
    exludeUris.add("/docs/#");
    exludeUris.add("/docs/tooling");
    exludeUris.add("/docs/tooling/");
    exludeUris.add("/docs/tooling#");
    exludeUris.add("/docs/features");
    exludeUris.add("/docs/features/");
    exludeUris.add("/docs/features#");
    exludeUris.add("/docs/topics");
    exludeUris.add("/docs/topics/");
    exludeUris.add("/docs/topics#");
    exludeUris.add("/docs/query/nplus1");
    exludeUris.add("/docs/query/partialobjects");
    exludeUris.add("/videos");

    output = new Output();

    urlRegistry.add("http://localhost:8080/");
    urlRegistry.add("http://localhost:8080");
    output.start();
  }

  private void start() throws IOException {

    crawl("http://localhost:8080/docs/", 1);
    output.end();
  }


  void crawl(String url, int depth) {

    if (url.contains("/docs/query")) {
      System.out.println("here");
    }

    url = trimRelativeAnchor(url);
    String normUrl = normalise(url);
    if (urlRegistry.contains(normUrl)) {
      return;
    }
    if (!isIndexPage(normUrl)) {
      return;
    }

    url = toLocalUrl(url);
    if (!isIndexPage(url)) {
      return;
    }

    urlRegistry.add(normUrl);

    try {

      Document document = Jsoup.connect(url).get();

      String uri = toRelativeUri(url);
      if (includeUri(uri)) {
        System.out.println(">> Depth: " + depth + " [" + url + "] normUrl[" + normUrl+"]");
      }

      if (excludePage(uri)) {
        System.out.println(" skip on: " + uri+" "+url);

      } else {
        System.out.println(" index: " + url);
        Page page = new Page(uri, document);
        page.parse(output);

        Elements linksOnPage = document.select("a[href]");

        int newDepth = depth + 1;
        for (Element link : linksOnPage) {
          String linkUrl = link.attr("abs:href");
          if (!urlRegistry.contains(linkUrl)) {
            crawl(linkUrl, newDepth);
          }
        }
      }

    } catch (IOException e) {
      System.err.println("For '" + url + "': " + e.getMessage());
    }

  }

  private boolean excludePage(String uri) {
    return uri.startsWith("/docs/tooling")
      || uri.startsWith("/docs/intro/logging")
      || uri.startsWith("/docs/codestyle")
      || uri.startsWith("/docs/intro/db-migrations")
      || uri.startsWith("/docs/intro/queries");
//    exludeLinks.add("/docs/tooling");
////    exludeLinks.add("/docs/topics");
////    exludeLinks.add("/docs/query/nplus1");
////    exludeLinks.add("/docs/query/partialobjects");
//    return false;
  }

  private String trimRelativeAnchor(String url) {
    int pos = url.indexOf('#');
    if (pos > -1) {
      url = url.substring(0, pos);
    }
    return url;
  }


  private boolean includeUri(String uri) {
    return !exludeUris.contains(uri);
  }

  private final int trimLen = "http://localhost:8080".length();

  private String toRelativeUri(String url) {
    return url.substring(trimLen);
  }

  private boolean isIndexPage(String url) {
    return (url.startsWith("http://localhost:8080/docs")
//      || url.startsWith("http://localhost/videos"))
      && !url.startsWith("http://localhost:8080/docs/setup"));
  }

  private String toLocalUrl(String url) {
    return url;
  }

//  private boolean isProcessUrl(String url, int depth) {
//
//    if (depth >= MAX_DEPTH && !urlRegistry.contains(normUrl)) {
//      System.out.println("exclude on depth: " + url);
//    }
//    boolean process = (depth < MAX_DEPTH) && !urlRegistry.contains(normUrl);
//
//    return process;
//  }

  private String normalise(String url) {
    if (url.endsWith("/")) {
      url = url.substring(0, url.length()-1);
    }
    return url;
  }


}

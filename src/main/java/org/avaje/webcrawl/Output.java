package org.avaje.webcrawl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class Output {

  private final LinkedHashMap<String, Search> entries = new LinkedHashMap<>();

  private FileWriter writer;

  private int count;


  public Output() throws IOException {

    File file = new File("search.json");
    writer = new FileWriter(file);
  }

  public void start() {
  }

  public void end() throws IOException {

    List<Search> list = new ArrayList<>(entries.values());

    list.sort(Comparator
      .comparingInt(Search::priority)
      .thenComparing(Search::category)
      .thenComparing(Search::title)
    );

    writer.write("[\n");
    for (Search value : list) {
      write(value);
    }
    writer.write("]\n");
    writer.close();
  }

  private void write(Search entry) throws IOException {

    if (count++ > 0) {
      writer.write(",");
    }
    writer.write("{\n");
    write("title", entry.title(), true);
    write("caption", entry.caption(), true);
    write("category", entry.category(), true);
    write("priority", String.valueOf(entry.priority()), true);
    write("keywords", entry.keywords(), true);
    write("url", entry.url(), false);
    writer.write("}\n");
  }

  private void write(String title, String text, boolean comma) throws IOException {
    writer.write("  \"");
    writer.write(title);
    writer.write("\":");
    writer.write("\"");
    writer.write(text);
    writer.write("\"");
    if (comma) {
      writer.write(",");
    }
    writer.write("\n");
  }


  public void add(Search search) {

    Search existing = entries.get(search.key());
    if (existing != null) {
      if (search.priority() < existing.priority()) {
        System.out.println("override ... duplicate: " + search.url());
        entries.put(search.key(), search);
      } else {
        if (sameUrl(existing.url(), search.url())) {
          System.out.println("entry clash ... duplicate: " + search.url());
        } else {
          System.out.println("ENTRY CLASH: " + existing + " with url:" + search.url());
        }
      }
    } else {
      entries.put(search.key(), search);
    }
  }

  private boolean sameUrl(String url, String url2) {
    if (url.equals(url2)) {
      return true;
    }
    if (url.endsWith("/")) {
      return url.substring(0, url.length() - 1).equals(url2);
    }
    if (url2.endsWith("/")) {
      return url2.substring(0, url2.length() - 1).equals(url);
    }
    return false;
  }
}

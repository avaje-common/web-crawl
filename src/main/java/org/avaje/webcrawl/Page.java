package org.avaje.webcrawl;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

class Page {

  private final String uri;

  private final Document document;

  private Header header;

  Page(String uri, Document document) {
    this.uri = uri;
    this.document = document;
  }

  void parse(Output output) {

    Elements h1 = document.select("h1");
    if (h1.size() == 0) {
      System.out.println("Error: no H1 tag for page " + uri);
      h1 = document.select("h2");
    }
    if (h1.size() > 1) {
      System.out.println("Error: multiple H1 tags for page " + uri);
      return;
    }

    Element h1Element = h1.get(0);
    String raw = h1Element.text();
    String h1Text = filterHeader(safeText(raw));
    String[] sections = splitH1(h1Text);
    if (sections.length == 1) {
      // top level
      int priority = uri.contains("/features/") ? 1 : 0;
      header = new Header(uri, h1Text, priority);
    } else {
      int priority = 3;
      String category = sections[0].trim();
      if (category.contains("Introduction")) {
        priority = 2;
      } else if ( uri.contains("/background/")) {
        priority = 4;
      }
      header = new Header(uri, category, title(sections), priority);
    }

    Elements h2s = document.select("h2");
    for (Element h2 : h2s) {

      String id = h2.id();
      String text = safeText(h2.text());
      header.addChild(text, id);
    }

    dump(output);
  }

  private String title(String[] sections) {
    Set<String> dedup = new LinkedHashSet<>();
    for (int i = 1; i < sections.length; i++) {
      dedup.add(sections[i].trim());
    }
    StringJoiner joiner = new StringJoiner(" / ");
    for (String section : dedup) {
      joiner.add(section.trim());
    }

    return joiner.toString().trim();
  }

  private String[] splitH1(String h1Text) {
    return h1Text.trim().split("/");
  }

  private String safeText(String text) {
    text = text.replace('"', ' ');
    text = text.replace('\n', ' ');
    text = text.replaceAll("  ", " ");
    return text.trim();
  }

  private String filterHeader(String h1Text) {
    h1Text = h1Text.replaceFirst("Documentation /", "");
    h1Text = h1Text.replaceFirst("Features /", "");
    return h1Text.trim();
  }

  private void dump(Output output) {

    output.add(header);
    for (Child child : header.children) {
      output.add(child);
    }
  }

  static class Header implements Search {

    private final String uri;

    private final String title;

    private int priority = 1;

    private String category;

    private String caption = "";

    private List<Child> children = new ArrayList<>();

    Header(String uri, String category, int priority) {
      this.uri = uri;
      this.category = category;
      this.priority = priority;//String.valueOf(priority);
      this.title = "";
    }

    void addChild(String text, String id) {

      int priority = 3;
      if (category.contains("Introduction")) {
        priority = 2;
      } else if (uri.contains("/background/")) {
        priority = 5;
      } else if (uri.contains("/docs/features/")) {
        priority = 4;
      }
      Child child = new Child(this, text, id, priority);
      if (!child.duplicate()) {
        children.add(child);
      }
    }

    Header(String uri, String category, String title, int priority) {
      this.uri = uri;
      this.category = category;
      this.title = title;
      this.priority = priority;
    }

    @Override
    public String key() {
      return category() + "-" + title();
    }

    @Override
    public String category() {
      return category;
    }

    @Override
    public String title() {
      return title;
    }

    @Override
    public String caption() {
      return caption;
    }

    @Override
    public int priority() {
      return priority;
    }

    @Override
    public String keywords() {
      return "";
    }

    @Override
    public String url() {
      return uri;
    }

    String pageUrl() {
      return uri;
    }

    @Override
    public String toString() {
      return "cat:" + category() + " tit:" + title() + " url:" + url();
    }

    boolean duplicate(String childTitle) {
      return title.endsWith("/ "+childTitle);
    }

    public String appendTitle(String childTitle) {
      if (title.length() == 0 || title.equals(childTitle)) {
        return childTitle;
      }
      if (title.contains("/ "+childTitle)) {
        return title;
      }
      return title + " - " + childTitle;
    }
  }

  static class Child implements Search {

    private final Header header;
    private final String title;
    private final String id;
    private final String category;
    private final int priority;
    private boolean duplicate;

    Child(Header header, String title, String id, int priority) {
      this.header = header;
      if (header.duplicate(title)) {
        this.duplicate = true;
      }
      this.title = header.appendTitle(title);
      this.category = header.category();
      this.id = id;
      this.priority = priority;
    }
    boolean duplicate() {
      return duplicate;
    }
    @Override
    public String key() {
      return category() + "-" + title();
    }

    @Override
    public String category() {
      return category;
    }

    @Override
    public String title() {
      return title;
    }

    @Override
    public String caption() {
      return "";
    }

    @Override
    public int priority() {
      return priority;
    }

    @Override
    public String keywords() {
      return "";
    }

    @Override
    public String url() {
      String url = header.pageUrl();
      if (id.length() > 0 && !url.contains("#")) {
        if (url.endsWith("/")) {
          url = url.substring(0, url.length() - 1);
        }
        url += "#" + id;
      }
      return url;
    }

    @Override
    public String toString() {
      return "cat:" + category() + " tit:" + title() + " url:" + url();
    }


  }
}

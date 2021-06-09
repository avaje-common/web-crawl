package org.avaje.webcrawl;

public interface Search {

  String key();

  String category();

  String title();

  String caption();

  int priority();

  String keywords();

  String url();
}

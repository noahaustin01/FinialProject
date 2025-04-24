package org.group.common;

import java.io.Serializable;

public class Message implements Serializable {
    final User author;
    private User[] to;
    final String content;

    public Message(User author, String content) {
        this.author = author;
        this.content = content;
        parse();
    }

    public void parse() {
        to = null;
    }
}

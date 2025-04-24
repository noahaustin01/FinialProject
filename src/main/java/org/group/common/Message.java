package org.group.common;

public class Message {
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

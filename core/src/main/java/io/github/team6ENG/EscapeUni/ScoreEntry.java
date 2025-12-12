package io.github.team6ENG.EscapeUni;

public class ScoreEntry {
    public String name;
    public int score;
    public long timestamp;

    // required for libGDX Json
    public ScoreEntry() {}

    public ScoreEntry(String name, int score) {
        this.name = name;
        this.score = score;
        this.timestamp = System.currentTimeMillis();
    }
}

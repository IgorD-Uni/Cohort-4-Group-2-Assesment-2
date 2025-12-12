package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import java.util.Comparator;

public class LeaderboardManager {
    private static LeaderboardManager instance;
    private final String FILE_NAME = "leaderboard.json";
    private final Json json = new Json();
    private Array<ScoreEntry> entries = new Array<>();
    private int maxEntries = 5;

    private LeaderboardManager() {}

    public static LeaderboardManager getInstance() {
        if (instance == null) instance = new LeaderboardManager();
        return instance;
    }

    public void load() {
        try {
            FileHandle fh = Gdx.files.local(FILE_NAME);
            if (fh.exists()) {
                entries = json.fromJson(Array.class, ScoreEntry.class, fh);
                if (entries == null) entries = new Array<>();
            }
        } catch (Exception e) {
            Gdx.app.error("Leaderboard", "load failed: " + e.getMessage());
            entries = new Array<>();
        }
        sortAndTrim();
    }

    public void save() {
        try {
            FileHandle fh = Gdx.files.local(FILE_NAME);
            fh.writeString(json.toJson(entries), false);
        } catch (Exception e) {
            Gdx.app.error("Leaderboard", "save failed: " + e.getMessage());
        }
    }

    public void addScore(String name, int score) {
        if (name == null || name.trim().isEmpty()) name = "Player";
        entries.add(new ScoreEntry(name.trim(), score));
        sortAndTrim();
        save();
    }

    public Array<ScoreEntry> getTop(int n) {
        int limit = Math.min(n, entries.size);
        Array<ScoreEntry> out = new Array<>(limit);
        for (int i = 0; i < limit; i++) out.add(entries.get(i));
        return out;
    }

    public Array<ScoreEntry> getAll() {
        return entries;
    }

    public void clear() {
        entries.clear();
        save();
    }

    private void sortAndTrim() {
        entries.sort(new Comparator<ScoreEntry>() {
            @Override
            public int compare(ScoreEntry a, ScoreEntry b) {
                return Integer.compare(b.score, a.score); // descending
            }
        });
        while (entries.size > maxEntries) entries.pop();
    }

    public void setMaxEntries(int max) {
        this.maxEntries = Math.max(1, max);
        sortAndTrim();
        save();
    }
}

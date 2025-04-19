package com.huashanlunjian.amara.music_game_core.chart;

import com.huashanlunjian.amara.music_game_core.AbstractChart;
import com.huashanlunjian.amara.utils.chartparser.OsuChartParser;

import java.util.List;

public class OsuChart extends AbstractChart<Integer> {
    public OsuChart(OsuChartParser parser) {
        super();
        parser.parse();
        this.title = parser.getTitle();
        this.artist = parser.getArtist();
        this.charter = parser.getCreator();
        this.maxTime = parser.getMaxTime();
        this.notes = parser.getNotes();
    }
    public String getDifficulty() {
        return "";
    }

    public String getArtist() {
        return this.artist;
    }
    public String getTitle() {
        return this.title;
    }
    public String getCharter() {
        return this.charter;
    }
    public float getBpm() {
        return this.bpm;
    }
    @Override
    public int getMaxTime() {
        return this.maxTime;
    }

    public List<Integer> getNotes() {
        return this.notes;
    }

    @Override
    public float getNoteTime(Integer index, float bpm) {
        return notes.get(index);
    }


}

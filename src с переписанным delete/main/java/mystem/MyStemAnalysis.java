package mystem;

import lombok.Getter;
import lombok.Setter;



public class MyStemAnalysis {
    @Getter @Setter
    private String lex;
    @Getter @Setter
    private String gr;
    @Getter @Setter
    private String qual;

    private boolean stopWord;

    public boolean isStopWord() {
        return stopWord;
    }

    public void setStopWord(boolean stopWord) {
        this.stopWord = stopWord;
    }
}

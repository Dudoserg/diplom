package Ngrams;

import mystem.MyStemItem;
import mystem.MyStemResult;
import utils.Bigram;
import utils.Unigram;

import java.util.List;
import java.util.Map;

public interface NgramsInt {
    Map<Bigram, Integer> getBigramFrequensy(List<MyStemItem> itemList);
    Map<Unigram, Integer> getUnigramFrequensy(List<MyStemItem> itemList);
}

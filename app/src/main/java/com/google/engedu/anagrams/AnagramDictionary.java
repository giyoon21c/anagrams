/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.anagrams;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static android.R.attr.key;
import static android.media.CamcorderProfile.get;
import static android.provider.CallLog.Calls.NEW;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class AnagramDictionary {

    private static final int DEFAULT_WORD_LENGTH = 3;
    private static final int MIN_NUM_ANAGRAMS = DEFAULT_WORD_LENGTH;
    private static final int MAX_WORD_LENGTH = 7;
    private Random random = new Random();
    private static ArrayList<String> wordList = new ArrayList<String>();
    private static HashSet<String> wordSet = new HashSet<>();
    private static HashMap<String, ArrayList<String>> lettersToWord = new HashMap<>();
    private static HashMap<Integer, ArrayList<String>> sizeToWords = new HashMap<>();
    private static int wordLength = DEFAULT_WORD_LENGTH;

    private void logKeyValues(String key) {
        ArrayList<String> tmp;
        tmp = lettersToWord.get(key);
        for (String value : tmp) {
            Log.v("ANAGRAM-ARRAYLIST", value);
        }
    }

    public AnagramDictionary(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String line;
        while((line = in.readLine()) != null) {
            String word = line.trim();
            wordList.add(word);
            wordSet.add(word);
            // get the sortedWord via sortLetters, and use it as key to see if HashMap has the key
            // if key is present, add the sortedWord to the ArrayList of the said key
            // if key it not present, add the sortedWord to the new ArrayList and insert the K,V
            // to HashMap
            String sortedWord = sortLetters(word);
            if (lettersToWord.containsKey(sortedWord)) {
                Log.v("ANAGRAM", "key present: just add V " + "(" + sortedWord + "," + word + ")");
                ArrayList<String> tmp = lettersToWord.get(sortedWord);
                tmp.add(word);
                //logKeyValues(sortedWord);
            } else {
                // create list one and store values
                Log.v("ANAGRAM", "key not present: put new K, V " + "(" + sortedWord + "," + word + ")");
                ArrayList<String> newValue = new ArrayList<>();
                newValue.add(word);
                lettersToWord.put(sortedWord, newValue);
            }

            // also add the word to sizeToWord hashmap with word length as the key
            int wordLength = word.length();
            if (sizeToWords.containsKey(wordLength)) {
                //Log.v("ANAGRAM", "key present: just add V " + "(" + wordLength + "," + word + ")");
                ArrayList<String> tmp = sizeToWords.get(wordLength);
                tmp.add(word);
            } else {
                //Log.v("ANAGRAM", "key not present: put new K, V " + "(" + wordLength + "," + word + ")");
                ArrayList<String> newValue = new ArrayList<>();
                newValue.add(word);
                sizeToWords.put(wordLength, newValue);
            }
        }
    }

    /**
     *
     * @param word
     * @param base
     * @return
     *
     *   Check for 1) the provided word is a valid dictionary word (i.e., in wordSet), and
     *             2) the word does not contain the base word as a substring.
     */
    public boolean isGoodWord(String word, String base) {
        Log.v("ANAGRAM-isGoodWord", word + ":" + base);
        if (wordSet.contains(word) && !word.contains(base)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param word
     * @return sorted word
     *
     *   String doesn't have a sort method, so turn it into a char array and use Arrays.sort().
     *   Then, convert it back to String type.
     */
    private String sortLetters(String word) {
        char[] sortedArray;
        sortedArray = word.toCharArray();
        Arrays.sort(sortedArray);
        String sorted = new String(sortedArray);
        return sorted;
    }
    /**
     *
     * @param targetWord
     * @return
     *  compare each string in wordList to the input word to determine if they are anagrams
     *
     */
    public List<String> getAnagrams(String targetWord) {
        ArrayList<String> result = new ArrayList<String>();
        Log.v("ANAGRAM", targetWord);
        Log.v("ANAGRAM", sortLetters(targetWord));
        String sortedTargetWord = sortLetters(targetWord);
        for (String word : wordList) {
            String sortedWord = sortLetters(word);
            if (sortedWord.equals(sortedTargetWord)) {
                Log.v("ANAGRAM", "Anagram found: " + word + ", " + sortedWord);
                result.add(word);
            }
        }
        return result;
    }

    /**
     *
     * @param word
     * @return
     *
     *   implement getAnagramsWithOneMoreLetter which takes a string and finds all anagrams
     *   that can be formed by adding one letter to that word.
     */
    public List<String> getAnagramsWithOneMoreLetter(String targetWord) {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> candidateWordList = new ArrayList<>();
        Log.v("ANAGRAM-ONEMORE", targetWord);
        char ch;
        for(ch = 'a' ; ch <= 'z' ; ch++) {
            String newTargetWord = targetWord + ch;
            Log.v("ANAGRAM-ONEMORE", "base: " + targetWord + " target: " + newTargetWord +
                    " sorted: " + sortLetters(newTargetWord));

            String hashNewTargetWord = sortLetters(newTargetWord);
            candidateWordList = lettersToWord.get(hashNewTargetWord);

            if (candidateWordList != null) {
                //Log.v("ANAGRAM-ONEMORE", "--found out words for hashNewTargetWord: " + hashNewTargetWord);
                //logKeyValues(hashNewTargetWord);
                //Log.v("ANAGRAM-ONEMORE", "-------");

                for (String candidateWord : candidateWordList) {
                    Log.v("ANAGRAM-ONEMORE", "    result added: " + candidateWord);
                    result.add(candidateWord);
                }

            } else {
                Log.v("ANAGRAM-ONEMORE", "  no anagram for " + newTargetWord);
            }
        }
        return result;
    }

    /**
     *
     * @return
     *
     *   Pick a random starting point in the wordList array and check each word in the array
     *   until you find one that has at least MIN_NUM_ANAGRAMS anagrams.
     *
     *   Be sure to handle wrapping around to the start of the array if needed
     *
     */
    public String pickGoodStarterWord() {

        //Log.v("ANAGRAM-RANDOM", ""+randomIndex);
        ArrayList<String> newWordList = sizeToWords.get(wordLength);
        Log.v("PICKGOODSTARTERWORD", "wordlength: " + wordLength + " has " + newWordList.size()
             + " entries");

        int randomIndex = random.nextInt(newWordList.size());
        Log.v("PICKGOODSTARTERWORD", "starting randomIndex: " + randomIndex);

        for (int size = 0; size < newWordList.size(); size++) {
            //Log.v("GOODWORD", "randomIndex: " + randomIndex + " " + wordList.get(randomIndex));
            //String candidateWord = wordList.get(randomIndex);
            String candidateWord = newWordList.get(randomIndex);
            Log.v("PICKGOODWORD", "word: " + candidateWord + " from: index " + randomIndex);

            String hashCandidateWord = sortLetters(candidateWord);
            ArrayList<String> tmp = lettersToWord.get(hashCandidateWord);
            Log.v("PICKGOODWORD", "(word,hash)=(" + candidateWord + "," + hashCandidateWord +
                    ") with freq. " + tmp.size());

            if (tmp.size() > MIN_NUM_ANAGRAMS) {
                Log.v("GOODWORD_FOUND", "  enough anagrams!  randomIndex: " + randomIndex + " " +
                        newWordList.get(randomIndex));
                logKeyValues(hashCandidateWord);
                break;
            } else {
                Log.v("GOODWORD_FOUND", "  no enough anagrams!");
            }

            randomIndex++;
            if (randomIndex > (newWordList.size()-1)) {
                randomIndex = randomIndex - newWordList.size();
            }
        }
        // increment wordLength by one until the MAX_WORD_LENGTH is reached
        if (wordLength < MAX_WORD_LENGTH) {
            wordLength++;
        }
        Log.v("GOODWORD_FOUND", "Use " + newWordList.get(randomIndex));
        return newWordList.get(randomIndex);
    }
}

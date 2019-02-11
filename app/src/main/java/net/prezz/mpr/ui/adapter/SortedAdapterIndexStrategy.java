package net.prezz.mpr.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;

public class SortedAdapterIndexStrategy implements AdapterIndexStrategy {

    public static final SortedAdapterIndexStrategy INSTANCE = new SortedAdapterIndexStrategy();


    private SortedAdapterIndexStrategy() {
        //prevent instantiation
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void createSectionIndexes(AdapterEntity[] inEntities, ArrayList<String> outSectionsList, ArrayList<Integer> outPositionForSection, ArrayList<Integer> outSectionForPosition) {
        HashMap<String, Integer> sectionsMap = new HashMap<String, Integer>();

        for (int i = 0; i < inEntities.length; i++) {
            String label = inEntities[i].getSectionIndexText();
            String letter = (label.isEmpty()) ? "":  label.substring(0, 1).toUpperCase();
            if (!sectionsMap.containsKey(letter)) {
                sectionsMap.put(letter, sectionsMap.size());
                outSectionsList.add(letter);
            }
        }

        // Calculate the section for each position in the list.
        for (int i = 0; i < inEntities.length; i++) {
            String label = inEntities[i].getSectionIndexText();
            String letter = (label.isEmpty()) ? "":  label.substring(0, 1).toUpperCase();
            outSectionForPosition.add(sectionsMap.get(letter));
        }

        // Calculate the first position where each section begins.
        for (int i = 0; i < sectionsMap.size(); i++) {
            outPositionForSection.add(0);
        }
        for (int i = 0; i < sectionsMap.size(); i++) {
            for (int j = 0; j < inEntities.length; j++) {
                if (i == outSectionForPosition.get(j).intValue()) {
                    outPositionForSection.set(i, j);
                    break;
                }
            }
        }
        
        //finally add position just past the last element such scrolling for the last section can be correctly calculated
        outPositionForSection.add(inEntities.length);
    }
}

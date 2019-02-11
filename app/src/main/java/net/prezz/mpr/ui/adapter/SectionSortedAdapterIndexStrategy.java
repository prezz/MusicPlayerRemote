package net.prezz.mpr.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;

public class SectionSortedAdapterIndexStrategy implements AdapterIndexStrategy {

    public static final SectionSortedAdapterIndexStrategy INSTANCE = new SectionSortedAdapterIndexStrategy();


    private SectionSortedAdapterIndexStrategy() {
        //prevent instantiation
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void createSectionIndexes(AdapterEntity[] inEntities, ArrayList<String> outSectionsList, ArrayList<Integer> outPositionForSection, ArrayList<Integer> outSectionForPosition) {
        HashMap<Key, Integer> sectionsMap = new HashMap<Key, Integer>();

        int section = 0;
        for (int i = 0; i < inEntities.length; i++) {
            AdapterEntity entity = inEntities[i];
            if (entity instanceof SectionAdapterEntity) {
                section++;
            }
            String label = (entity instanceof SectionAdapterEntity) ? "" : entity.getSectionIndexText();
            String letter = (label.isEmpty()) ? "":  label.substring(0, 1).toUpperCase();
            Key key = new Key(section, letter);
            if (!sectionsMap.containsKey(key)) {
                sectionsMap.put(key, sectionsMap.size());
                outSectionsList.add(letter);
            }
            if (entity instanceof SectionAdapterEntity) {
                section++;
            }
        }

        // Calculate the section for each position in the list.
        section = 0;
        for (int i = 0; i < inEntities.length; i++) {
            AdapterEntity entity = inEntities[i];
            if (entity instanceof SectionAdapterEntity) {
                section++;
            }
            String label = (entity instanceof SectionAdapterEntity) ? "" : entity.getSectionIndexText();
            String letter = (label.isEmpty()) ? "":  label.substring(0, 1).toUpperCase();
            outSectionForPosition.add(sectionsMap.get(new Key(section, letter)));
            if (entity instanceof SectionAdapterEntity) {
                section++;
            }
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

    private static final class Key {

        private int section;
        private String letter;

        public Key(int section, String letter) {
            this.section = section;
            this.letter = letter;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof Key) {
                Key other = (Key)obj;
                return this.section == other.section && this.letter.equals(other.letter);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return section ^ letter.hashCode() ;
        }
    }
}
